



#ifndef NATIVE_LIB_LOGGING_HDR
#define NATIVE_LIB_LOGGING_HDR

 
#include <cstddef>
#include <string.h>
#include <string>

#if defined(SCHEME_release)
#define LOGD(...) 
#elif defined(SCHEME_debug)
#define LOGD(...)  fprintf(stdout,__VA_ARGS__) ; fprintf(stdout,"\n") ;
#endif

#endif 
