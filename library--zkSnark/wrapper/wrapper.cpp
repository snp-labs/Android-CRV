#define NO_PROCPS
#define CURVE_ALT_BN128
//#define MIE_ATE_USE_GMP
#define NDEBUG
//#define _FILE_OFFSET_BITS 64
//#define MIE_ZM_VUINT_BIT_LEN (64 * 9)
//#define NDEBUG 1


#if SYSTEM_NAME == iOS
    #include <logging.hpp>
#elif SYSTEM_NAME == ANDRIOD
    #include <android/log.h>
    #define  LOG_TAG    "NDK_TEST"
    #define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#endif


#include <initializer_list>

//#include <jni.h>
#include <string>
#include <iostream>
#include <gmp.h>
//#include <gmpxx.h>
#include <openssl/bn.h>
#include <cassert>
#include <cstdio>
#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <sstream>
#include <type_traits>
#include <libff/common/profiling.hpp>
#include <libff/common/utils.hpp>
#include <libff/algebra/curves/public_params.hpp>
#include <libsnark/common/default_types/r1cs_gg_ppzksnark_pp.hpp>
#include <libsnark/relations/constraint_satisfaction_problems/r1cs/examples/r1cs_examples.hpp>
#include <libsnark/jsnark_interface/CircuitReader.hpp>
//#include "CircuitReader.hpp"
#include <libsnark/gadgetlib2/integration.hpp>
#include <libsnark/gadgetlib2/adapters.hpp>
#include <libsnark/zk_proof_systems/ppzksnark/voting/r1cs_gg_ppzksnark.hpp>
#include <libsnark/zk_proof_systems/ppzksnark/voting/run_r1cs_gg_ppzksnark.hpp>

#include <libsnark/common/default_types/r1cs_gg_ppzksnark_pp.hpp>
#include <unistd.h>

#include "wrapper.h"


using namespace libsnark;
using namespace std;




#define STRINGIZE_2(x) #x
#define STRINGIZE_1(x) STRINGIZE_2(x)

const char* CgetBuildVersion() {
    return STRINGIZE_1(BUILD_VERSION) ;
}

int CsubActivity_FromApp(
    sub_activity_task task , 
    sub_activity_mode mode ,
    char* arith_text_content ,
    char* inputs_text_content,
    char* DocumentFolder  ) 
{

    libff::start_profiling();
    gadgetlib2::initPublicParamsFromDefaultPp();
    gadgetlib2::GadgetLibAdapter::resetVariableIndex();
    ProtoboardPtr pb = gadgetlib2::Protoboard::create(gadgetlib2::R1P);

    std::string task_name = "" ;

    if(task == task_register) {
        task_name = "register" ;
        LOGD("task : %s", task_name.c_str());
    }
    else if(task == task_vote ) {
        task_name = "vote" ;
        LOGD("task : %s", task_name.c_str());
    }
    else if(task == task_tally) {
        task_name = "tally" ;
        LOGD("task : %s", task_name.c_str());
    }
    

    printf("\n%s\n", arith_text_content  );
    printf("\n%s\n", inputs_text_content  );

    // Read the circuit, evaluate, and translate constraints

    //CircuitReader reader(path1, path2, pb);
    istringstream arithIsstream (arith_text_content) ;
    istringstream inputsIsstream (inputs_text_content) ;
    CircuitReader reader(arithIsstream, inputsIsstream, pb);
    LOGD("circuit read done");
    
    

    r1cs_constraint_system<FieldT> cs = get_constraint_system_from_gadgetlib2(*pb);
    const r1cs_variable_assignment<FieldT> full_assignment =
            get_variable_assignment_from_gadgetlib2(*pb);
    cs.primary_input_size = reader.getNumInputs() + reader.getNumOutputs();
    cs.auxiliary_input_size = full_assignment.size() - cs.num_inputs();
    LOGD("%d %d\n", cs.primary_input_size, cs.auxiliary_input_size);
    
    // extract primary and auxiliary input
    const r1cs_primary_input<FieldT> primary_input(full_assignment.begin(),
                                                   full_assignment.begin() + cs.num_inputs());
    const r1cs_auxiliary_input<FieldT> auxiliary_input(
            full_assignment.begin() + cs.num_inputs(), full_assignment.end());


    // A follow-up will be added.
    if(!cs.is_satisfied(primary_input, auxiliary_input)){
        LOGD("The constraint system is  not satisifed by the value assignment - Terminating.");
        LOGD("1194");
    }

    r1cs_example<FieldT> example(cs, primary_input, auxiliary_input);
    const bool test_serialization = false;
    bool successBit = false;





    if( mode == mode_setup )
    {
        LOGD("mode : setup");

        libsnark::run_r1cs_gg_ppzksnark_setup<libsnark::default_r1cs_gg_ppzksnark_pp>(example, test_serialization, task_name, DocumentFolder);

    }
    else if( mode == mode_verify )
    {
        LOGD("mode : verify");
            // The following code makes use of the observation that
            // libsnark::default_r1cs_gg_ppzksnark_pp is the same as libff::default_ec_pp (see r1cs_gg_ppzksnark_pp.hpp)
            // otherwise, the following code won't work properly, as GadgetLib2 is hardcoded to use libff::default_ec_pp.
            successBit = libsnark::run_r1cs_gg_ppzksnark_verify<libsnark::default_r1cs_gg_ppzksnark_pp>(
                    example, test_serialization, task_name, DocumentFolder);

        if(!successBit){
            LOGD("Problem occurred while running the ppzksnark algorithms .. ");

        }

    }
    else if ( mode == mode_run )
    {
        LOGD("mode : run");

        // The following code makes use of the observation that
            // libsnark::default_r1cs_gg_ppzksnark_pp is the same as libff::default_ec_pp (see r1cs_gg_ppzksnark_pp.hpp)
            // otherwise, the following code won't work properly, as GadgetLib2 is hardcoded to use libff::default_ec_pp.
            libsnark::run_r1cs_gg_ppzksnark<libsnark::default_r1cs_gg_ppzksnark_pp>(
                    example, test_serialization, task_name , DocumentFolder);

    }
    else if( mode == mode_all ) 
    {
        LOGD("all");

        successBit = libsnark::run_r1cs_gg_ppzksnark_all<libsnark::default_r1cs_gg_ppzksnark_pp>(
                example, test_serialization, task_name , DocumentFolder);


        if (!successBit) {
            LOGD("Problem occurred while running the ppzksnark algorithms .. " );

        }

    }

    return 0 ;
}





