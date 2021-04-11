/** @file
 *****************************************************************************
 * @author     This file is part of libsnark, developed by SCIPR Lab
 *             and contributors (see AUTHORS).
 * @copyright  MIT license (see LICENSE file)
 *****************************************************************************/

#include <thread>
#include <mutex>
#include <libsnark/gadgetlib2/adapters.hpp>
#include <libsnark/gadgetlib2/integration.hpp>

#include <android/log.h>
#define  LOG_TAG    "NDK_TEST"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)


namespace libsnark {

std::mutex log_print_mtx ;

typedef struct {
    size_t min_size, max_size , avg_size , call_cc ;
}debug_lc_convert_sizes_t ;

typedef struct {
    linear_combination<libff::Fr<libff::default_ec_pp>> a_result ;
    linear_combination<libff::Fr<libff::default_ec_pp>> b_result ;
    linear_combination<libff::Fr<libff::default_ec_pp>> c_result ;
    volatile bool lc_processed ;
} intermediate_result_t ;


linear_combination<libff::Fr<libff::default_ec_pp> > convert_gadgetlib2_linear_combination(const gadgetlib2::GadgetLibAdapter::linear_combination_t &lc)
{
    typedef libff::Fr<libff::default_ec_pp> FieldT;
    typedef gadgetlib2::GadgetLibAdapter GLA;

    linear_combination<FieldT> result = lc.second * variable<FieldT>(0);
    for (const GLA::linear_term_t &lt : lc.first)
    {
        result = result + lt.second * variable<FieldT>(lt.first+1);
    }

    return result;
}

linear_combination<libff::Fr<libff::default_ec_pp> > 
    convert_gadgetlib2_linear_combination(
        const gadgetlib2::GadgetLibAdapter::linear_combination_t &lc ,
        debug_lc_convert_sizes_t*  counts )
{
    typedef libff::Fr<libff::default_ec_pp> FieldT;
    typedef gadgetlib2::GadgetLibAdapter GLA;

    linear_combination<FieldT> result = lc.second * variable<FieldT>(0);
    for (const GLA::linear_term_t &lt : lc.first)
    {
        result = result + lt.second * variable<FieldT>(lt.first+1);
    }

    if (lc.first.size() < counts->min_size ) { counts->min_size = lc.first.size() ;} ;
    if (lc.first.size() > counts->max_size ) { counts->max_size = lc.first.size() ;} ;
    counts->avg_size += lc.first.size() ;
    counts->call_cc ++ ;
    
    return result;
}


r1cs_constraint_system<libff::Fr<libff::default_ec_pp> > get_constraint_system_from_gadgetlib2(const gadgetlib2::Protoboard &pb)
{
    typedef libff::Fr<libff::default_ec_pp> FieldT;
    typedef gadgetlib2::GadgetLibAdapter GLA;

    r1cs_constraint_system<FieldT> result;
    const GLA adapter;
    debug_lc_convert_sizes_t counts = {1000000000, 0 , 0, 0} ;

    libff::enter_block("Call to adapter.convert(pb)");
    GLA::protoboard_t converted_pb = adapter.convert(pb);
    libff::leave_block("Call to adapter.convert(pb)");

    libff::enter_block("Push the constraint");
    for (const GLA::constraint_t &constr : converted_pb.first)
    {
        result.constraints.emplace_back(r1cs_constraint<FieldT>(convert_gadgetlib2_linear_combination(std::get<0>(constr), &counts),
                                                                convert_gadgetlib2_linear_combination(std::get<1>(constr), &counts),
                                                                convert_gadgetlib2_linear_combination(std::get<2>(constr), &counts)));
    }
    libff::leave_block("Push the constraint");
    LOGD("converted_pb.first.size() = %lu " , converted_pb.first.size() ) ;
    LOGD("lc convert calls=%lu , lc.first.size() [ min=%lu, max=%lu, avg=%lu ] " , 
            counts.call_cc, counts.min_size, counts.max_size , 
            counts.avg_size / counts.call_cc ) ;

    //The number of variables is the highest index created.
    //TODO: If there are multiple protoboards, or variables not assigned to a protoboard, then getNextFreeIndex() is *not* the number of variables! See also in get_variable_assignment_from_gadgetlib2.
    const size_t num_variables = GLA::getNextFreeIndex();
    result.primary_input_size = pb.numInputs();
    result.auxiliary_input_size = num_variables - pb.numInputs();
    return result;
}

// 
// Multi-threading calls to convert_gadgetlib2_linear_combination function
//

void bg_routine (gadgetlib2::GadgetLibAdapter::protoboard_t* converted_pb_ptr ,
                intermediate_result_t* itmd_result_ptr,
                unsigned int vector_offset ,
                unsigned int vector_step ,
                unsigned int thrd_id )
{
    typedef gadgetlib2::GadgetLibAdapter GLA;

    debug_lc_convert_sizes_t counts = {1000000000, 0 , 0, 0} ;

    for ( unsigned int vector_index = vector_offset ; 
          vector_index < converted_pb_ptr->first.size() ; 
          vector_index += vector_step  )
    {    
        itmd_result_ptr[vector_index].lc_processed = false ;
    }
    
    for ( unsigned int vector_index = vector_offset ; 
          vector_index < converted_pb_ptr->first.size() ; 
          vector_index += vector_step  )
    {    
        const GLA::constraint_t &constr = converted_pb_ptr->first.at(vector_index) ;
        
        itmd_result_ptr[vector_index].a_result = convert_gadgetlib2_linear_combination(std::get<0>(constr), &counts) ;
        itmd_result_ptr[vector_index].b_result = convert_gadgetlib2_linear_combination(std::get<1>(constr), &counts ) ;
        itmd_result_ptr[vector_index].c_result = convert_gadgetlib2_linear_combination(std::get<2>(constr), &counts) ;
        itmd_result_ptr[vector_index].lc_processed = true ;
    }

    log_print_mtx.lock();
    LOGD ( "Thread %u Done :: lc convert calls=%lu , lc.first.size() [ min=%lu, max=%lu, avg=%lu ]" , 
            thrd_id , counts.call_cc , counts.min_size, counts.max_size , counts.avg_size / counts.call_cc ) ;
    log_print_mtx.unlock();
}


r1cs_constraint_system<libff::Fr<libff::default_ec_pp> > 
    get_constraint_system_from_gadgetlib2_multi_threaded(const gadgetlib2::Protoboard &pb)
{
    typedef libff::Fr<libff::default_ec_pp> FieldT;
    typedef gadgetlib2::GadgetLibAdapter GLA;

    r1cs_constraint_system<FieldT> result;
    const GLA adapter;
    
    libff::enter_block("Call to adapter.convert(pb)");
    GLA::protoboard_t converted_pb = adapter.convert(pb);
    libff::leave_block("Call to adapter.convert(pb)");

    // allocate mem to hold linear_combination results
    int itmd_r_mem_size = sizeof (intermediate_result_t) * converted_pb.first.size() ; 
    intermediate_result_t* itmd_r = (intermediate_result_t*) malloc ( itmd_r_mem_size ) ;
    LOGD ("itmd_r_mem_size = %d KB " ,itmd_r_mem_size >> 10);

    
    libff::enter_block("Push the constraint  Multithread Block"); 

    // number of threads to create
    #define thread_count  8 

    // set only first lc_processed to false for each thread 
    for (unsigned int i = 0 ; i < thread_count ; i++){ itmd_r[i].lc_processed = false ; }

    // start all thread_count threads
    std::thread* worker_threads[thread_count] ;
    for (unsigned int i = 0 ; i < thread_count ; i++){
        worker_threads[i] = new std::thread ( bg_routine, &converted_pb , itmd_r , i , thread_count , i+1 ) ;
    }

    // pre-allocate std::vector internal array to avoid any automatic reallocation
    result.constraints.get_allocator().allocate(converted_pb.first.size()) ;
    
    for ( unsigned int i = 0 ; i < converted_pb.first.size() ; i++)
    {
        while( ! itmd_r[i].lc_processed ) ; // wait for lc_processed == true
        
        result.constraints.emplace_back(r1cs_constraint<FieldT>(
                                            itmd_r[i].a_result, 
                                            itmd_r[i].b_result, 
                                            itmd_r[i].c_result));
    }
    
    libff::leave_block("Push the constraint  Multithread Block");
    
    LOGD("result.constraints.size() = %lu " , result.constraints.size() ) ;  
    LOGD("converted_pb.first.size() = %lu " , converted_pb.first.size() ) ;
    
    free(itmd_r) ;
    
    //The number of variables is the highest index created.
    //TODO: If there are multiple protoboards, or variables not assigned to a protoboard, then getNextFreeIndex() is *not* the number of variables! See also in get_variable_assignment_from_gadgetlib2.
    const size_t num_variables = GLA::getNextFreeIndex();
    result.primary_input_size = pb.numInputs();
    result.auxiliary_input_size = num_variables - pb.numInputs();
    return result;
}

// 
// End : Multi-threading 
//


r1cs_variable_assignment<libff::Fr<libff::default_ec_pp> > get_variable_assignment_from_gadgetlib2(const gadgetlib2::Protoboard &pb)
{
    typedef libff::Fr<libff::default_ec_pp> FieldT;
    typedef gadgetlib2::GadgetLibAdapter GLA;

    //The number of variables is the highest index created. This is also the required size for the assignment vector.
    //TODO: If there are multiple protoboards, or variables not assigned to a protoboard, then getNextFreeIndex() is *not* the number of variables! See also in get_constraint_system_from_gadgetlib2.
    const size_t num_vars = GLA::getNextFreeIndex();
    const GLA adapter;
    r1cs_variable_assignment<FieldT> result(num_vars, FieldT::zero());
    VariableAssignment assignment = pb.assignment();

    //Go over all assigned values of the protoboard, from every variable-value pair, put the value in the variable.index place of the new assignment.
    for(VariableAssignment::iterator iter = assignment.begin(); iter != assignment.end(); ++iter){
    	result[GLA::getVariableIndex(iter->first)] = adapter.convert(iter->second);
    }

    return result;
}

}
