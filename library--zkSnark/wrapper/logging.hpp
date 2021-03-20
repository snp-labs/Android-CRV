



#ifndef NATIVE_LIB_LOGGING_HDR
#define NATIVE_LIB_LOGGING_HDR

 
#include <cstddef>
#include <string.h>
#include <string>

#define LOGD(...)  fprintf(stdout,__VA_ARGS__) ; fprintf(stdout,"\n") ;

#endif 
