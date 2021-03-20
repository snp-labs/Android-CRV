/** @file
 *****************************************************************************

 Implementation of functionality that runs the R1CS GG-ppzkSNARK for
 a given R1CS example.

 See run_r1cs_gg_ppzksnark.hpp .

 *****************************************************************************
 * @author     This file is part of libsnark, developed by SCIPR Lab
 *             and contributors (see AUTHORS).
 * @copyright  MIT license (see LICENSE file)
 *****************************************************************************/

#ifndef RUN_R1CS_GG_PPZKSNARK_TCC_
#define RUN_R1CS_GG_PPZKSNARK_TCC_
 


#if SYSTEM_NAME == iOS
    #include <logging.hpp>
#elif SYSTEM_NAME == ANDRIOD
    #include <android/log.h>
    #define  LOG_TAG    "NDK_TEST"
    #define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#endif



#include <libff/common/profiling.hpp>
#include <libsnark/zk_proof_systems/ppzksnark/voting/r1cs_gg_ppzksnark.hpp>
 

using namespace std;
using namespace libsnark ;


namespace fs=std::__fs::filesystem;

namespace libsnark {

template<typename ppT>
typename std::enable_if<ppT::has_affine_pairing, void>::type
test_affine_verifier(const r1cs_gg_ppzksnark_verification_key<ppT> &vk,
                     const r1cs_gg_ppzksnark_primary_input<ppT> &primary_input,
                     const r1cs_gg_ppzksnark_proof<ppT> &proof,
                     const bool expected_answer)
{
    LOGD("R1CS GG-ppzkSNARK Affine Verifier");
    const bool answer = r1cs_gg_ppzksnark_affine_verifier_weak_IC<ppT>(vk, primary_input, proof);
    assert(answer == expected_answer);
}

template<typename ppT>
typename std::enable_if<!ppT::has_affine_pairing, void>::type
test_affine_verifier(const r1cs_gg_ppzksnark_verification_key<ppT> &vk,
                     const r1cs_gg_ppzksnark_primary_input<ppT> &primary_input,
                     const r1cs_gg_ppzksnark_proof<ppT> &proof,
                     const bool expected_answer)
{
    LOGD("R1CS GG-ppzkSNARK Affine Verifier");
    libff::UNUSED(vk, primary_input, proof, expected_answer);
    LOGD("Affine verifier is not supported; not testing anything.\n");
}

/**
 * The code below provides an example of all stages of running a R1CS GG-ppzkSNARK.
 *
 * Of course, in a real-life scenario, we would have three distinct entities,
 * mangled into one in the demonstration below. The three entities are as follows.
 * (1) The "generator", which runs the ppzkSNARK generator on input a given
 *     constraint system CS to create a proving and a verification key for CS.
 * (2) The "prover", which runs the ppzkSNARK prover on input the proving key,
 *     a primary input for CS, and an auxiliary input for CS.
 * (3) The "verifier", which runs the ppzkSNARK verifier on input the verification key,
 *     a primary input for CS, and a proof.
 */
template<typename ppT>
void run_r1cs_gg_ppzksnark_setup(const r1cs_example<libff::Fr<ppT> > &example,
                        const bool test_serialization, string name , 
                        const char* DocumentFolder )
{
    LOGD("Call to run_r1cs_gg_ppzksnark setup");

    LOGD("R1CS GG-ppzkSNARK Generator");
    r1cs_gg_ppzksnark_keypair<ppT> keypair = r1cs_gg_ppzksnark_generator<ppT>(example.constraint_system);

    LOGD("after generator");

    LOGD("GG-ppzkSNARK CRS Out file");
    string name1, name2;

    name1 = string(DocumentFolder) + name + "_CRS_pk.dat";
    name2 = string(DocumentFolder) + name + "_CRS_vk.dat";

    LOGD("\n CRS_pk File : [%s]" , name1.c_str() );
    LOGD("\n CRS_vk File : [%s]" , name2.c_str() );

 
    std::ofstream crs_pk_outfile(name1, ios::trunc | ios::out | ios::binary);
    
    std::ofstream crs_vk_outfile(name2, ios::trunc | ios::out | ios::binary);

    crs_pk_outfile << keypair.pk;
    crs_vk_outfile << keypair.vk; 
    crs_pk_outfile.close();
    crs_vk_outfile.close();
    LOGD("End Call to run_r1cs_gg_ppzksnark setup");
   // return keypair;

}

template<typename ppT>
void run_r1cs_gg_ppzksnark(const r1cs_example<libff::Fr<ppT> > &example,
                        const bool test_serialization, string name,
                        const char* DocumentFolder )
{
    LOGD("Call to run_r1cs_gg_ppzksnark");
    LOGD("GG-ppzkSNARK CRS In file");
    r1cs_gg_ppzksnark_keypair<ppT> keypair;
    string name1, name2, name3;
    
    name1 = string(DocumentFolder) + name + "_CRS_pk.dat";
    name2 = string(DocumentFolder) + name + "_CRS_vk.dat";
    name3 = string(DocumentFolder) + name + "_Proof.dat";

    LOGD("\n CRS_pk File : [%s]" , name1.c_str() );
    LOGD("\n CRS_vk File : [%s]" , name2.c_str() );
    LOGD("\n Proof File : [%s]" , name3.c_str() );



//    std::ifstream infile;
    std::ifstream crs_pk_infile(name1, ios::in | ios::binary);
//    std::ifstream crs_vk_infile(name2, ios::in);

    //LOGD("path : %s", name1.c_str());
//    char buf[1000];
//    if(crs_vk_infile.is_open()) {
//        LOGD("vk is open");
//        crs_vk_infile >> keypair.vk;
//        LOGD("vk in");
//        crs_vk_infile.close();
//    }
//    else
//        LOGD("not open vk");

    if(crs_pk_infile.is_open() ) {
        LOGD("pk is open!");
        crs_pk_infile >> keypair.pk;
        LOGD("pk in!");
        crs_pk_infile.close();
    }
    else
        LOGD ("not open pk");



    LOGD("R1CS GG-ppzkSNARK Prover");
    r1cs_gg_ppzksnark_proof<ppT> proof = r1cs_gg_ppzksnark_prover<ppT>(keypair.pk, example.primary_input, example.auxiliary_input);
    LOGD("after prover");

    LOGD("proof out");

    std::ofstream proof_outfile(name3.c_str(), ios::trunc | ios::out);
   
    proof_outfile << proof;
    proof_outfile.close();
    LOGD("End Call to run_r1cs_gg_ppzksnark");
}

template<typename ppT>
bool run_r1cs_gg_ppzksnark_verify(const r1cs_example<libff::Fr<ppT> > &example,
                        const bool test_serialization, string name,
                        const char* DocumentFolder )
{
    LOGD("Call to run_r1cs_gg_ppzksnark verify");
    LOGD("GG-ppzkSNARK CRS In file");
    r1cs_gg_ppzksnark_keypair<ppT> keypair;
    string name1, name2, name3;
    
    name1 = string(DocumentFolder) + name + "_CRS_pk.dat";
    name2 = string(DocumentFolder) + name + "_CRS_vk.dat";
    name3 = string(DocumentFolder) + name + "_Proof.dat";

    LOGD("\n CRS_pk File : [%s]" , name1.c_str() );
    LOGD("\n CRS_vk File : [%s]" , name2.c_str() );
    LOGD("\n Proof File : [%s]" , name3.c_str() );


    std::ifstream crs_pk_infile(name1.c_str(), ios::in);
    std::ifstream crs_vk_infile(name2.c_str(), ios::in);
    std::ifstream proof_infile(name3.c_str(), ios::in);

    crs_pk_infile >> keypair.pk; crs_pk_infile.close();
    crs_vk_infile >> keypair.vk; crs_vk_infile.close();

    r1cs_gg_ppzksnark_proof<ppT> proof;
    proof_infile >> proof;
    proof_infile.close();
    LOGD("Preprocess verification key");
    r1cs_gg_ppzksnark_processed_verification_key<ppT> pvk = r1cs_gg_ppzksnark_verifier_process_vk<ppT>(keypair.vk);

    pvk = libff::reserialize<r1cs_gg_ppzksnark_processed_verification_key<ppT> >(pvk);

    LOGD("R1CS GG-ppzkSNARK Verifier");
    const bool ans = r1cs_gg_ppzksnark_verifier_strong_IC<ppT>(keypair.vk, example.primary_input, proof);
    LOGD("after verifier");
    LOGD("* The verification result is: %s\n", (ans ? "PASS" : "FAIL"));

    LOGD("R1CS GG-ppzkSNARK Online Verifier");
    const bool ans2 = r1cs_gg_ppzksnark_online_verifier_strong_IC<ppT>(pvk, example.primary_input, proof);
    assert(ans == ans2);

    test_affine_verifier<ppT>(keypair.vk, example.primary_input, proof, ans);

    LOGD("End Call to run_r1cs_gg_ppzksnark verify");

    return ans;
}
 
    template<typename ppT>
    bool run_r1cs_gg_ppzksnark_all(const r1cs_example<libff::Fr<ppT> > &example,
                                      const bool test_serialization, string name,
                                      const char* DocumentFolder )
    {
        LOGD("Call to run_r1cs_gg_ppzksnark setup");

        LOGD("R1CS GG-ppzkSNARK Generator");
        r1cs_gg_ppzksnark_keypair<ppT> keypair = r1cs_gg_ppzksnark_generator<ppT>(example.constraint_system);

        LOGD("after generator");

        LOGD("GG-ppzkSNARK CRS Out file");
        string name1, name2, name3 ;

        name1 = string(DocumentFolder) + name + "_CRS_pk.dat";
        name2 = string(DocumentFolder) + name + "_CRS_vk.dat";
        name3 = string(DocumentFolder) + name + "_Proof.dat";
        
        LOGD("\n CRS_pk File : [%s]" , name1.c_str() );
        LOGD("\n CRS_vk File : [%s]" , name2.c_str() );
        LOGD("\n Proof File : [%s]" , name3.c_str() );


        std::ofstream crs_pk_outfile(name1, ios::trunc | ios::out | ios::binary);

        std::ofstream crs_vk_outfile(name2, ios::trunc | ios::out);

        crs_pk_outfile << keypair.pk;
        crs_vk_outfile << keypair.vk;
        crs_pk_outfile.close();
        crs_vk_outfile.close();
        LOGD("End Call to run_r1cs_gg_ppzksnark setup");

        LOGD("R1CS GG-ppzkSNARK Prover");
        r1cs_gg_ppzksnark_proof<ppT> proof = r1cs_gg_ppzksnark_prover<ppT>(keypair.pk, example.primary_input, example.auxiliary_input);
        LOGD("after prover");

        LOGD("proof out");

        std::ofstream proof_outfile(name3.c_str(), ios::trunc | ios::out);

        proof_outfile << proof;
        proof_outfile.close();
        LOGD("End Call to run_r1cs_gg_ppzksnark");

        LOGD("Call to run_r1cs_gg_ppzksnark verify");


        LOGD("Preprocess verification key");
        r1cs_gg_ppzksnark_processed_verification_key<ppT> pvk = r1cs_gg_ppzksnark_verifier_process_vk<ppT>(keypair.vk);

//        pvk = libff::reserialize<r1cs_gg_ppzksnark_processed_verification_key<ppT> >(pvk);

        LOGD("R1CS GG-ppzkSNARK Verifier");
        const bool ans = r1cs_gg_ppzksnark_verifier_strong_IC<ppT>(keypair.vk, example.primary_input, proof);
        LOGD("after verifier");
        LOGD("* The verification result is: %s\n", (ans ? "PASS" : "FAIL"));

        LOGD("R1CS GG-ppzkSNARK Online Verifier");
        const bool ans2 = r1cs_gg_ppzksnark_online_verifier_strong_IC<ppT>(pvk, example.primary_input, proof);
        assert(ans == ans2);

        test_affine_verifier<ppT>(keypair.vk, example.primary_input, proof, ans);

        LOGD("End Call to run_r1cs_gg_ppzksnark verify");

        return ans;
    }


} // libsnark

#endif // RUN_R1CS_GG_PPZKSNARK_TCC_
