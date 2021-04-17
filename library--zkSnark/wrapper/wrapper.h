

#ifndef NATIVE_LIB_WRAPPER_HDR
#define NATIVE_LIB_WRAPPER_HDR


#ifdef __cplusplus
extern "C" {
#endif

    enum sub_activity_task {
        task_register   = 1 ,
        task_vote       = 2 ,
        task_tally      = 3
    };

    enum sub_activity_mode {
        mode_setup      = 1 ,
        mode_verify     = 2 ,
        mode_run        = 3 ,
        mode_all        = 4 
    };

    int CsubActivity_FromApp(
        enum sub_activity_task task, 
        enum sub_activity_mode mode,
        const char* arith_text_content ,
        const char* inputs_text_content ,
        const char* DocumentFolder 
    ) ;

    const char* CgetBuildVersion();
 

#ifdef __cplusplus
}
#endif


#endif 