

include ../native/Makefile


#
# 	ReBuild Wrapper
#
WRAPPER_BUILD_DIR	:=build_env/${scheme}_${target}
WRAPPER_OBJS        :=${WRAPPER_BUILD_DIR}/wrapper.cpp.o  
WRAPPER_AR          :=${WRAPPER_BUILD_DIR}/libsnark-wrapper.a 

make_wrapper_obj : check_target_arg  ;
	clang++ -c  \
    -DBUILD_VERSION="$(shell date)" \
    ${MAKE_CXXFLAGS} \
    -I${PWD} \
    -I${PWD}/../native \
    -I${PWD}/../native/lib/${scheme}_${target}/include \
    -I${PWD}/../native/depends/libff \
    -I${PWD}/../native/depends/libfqfft \
    ${SRC} \
    -o ${OBJ} 

${WRAPPER_BUILD_DIR}/wrapper.cpp.o : wrapper.cpp ${WRAPPER_BUILD_DIR} ;
	make make_wrapper_obj target=${target} scheme=${scheme} SRC=$< OBJ=$@ 

${WRAPPER_AR} : ${WRAPPER_OBJS} ;
	libtool -static ${WRAPPER_OBJS} -o $@

install_wrapper_link_lib :  
	libtool -static \
        build_env/release_ios/libsnark-wrapper.a \
        build_env/release_ios_sim/libsnark-wrapper.a \
        -o ../archive-release/libsnark-wrapper.a
	libtool -static \
        build_env/debug_ios/libsnark-wrapper.a \
        build_env/debug_ios_sim/libsnark-wrapper.a \
        -o ../archive-debug/libsnark-wrapper.a

batch_build_wrapper :
	make scheme=release target=ios     build_env/release_ios/libsnark-wrapper.a 
	make scheme=release target=ios_sim build_env/release_ios_sim/libsnark-wrapper.a 
	make scheme=debug   target=ios     build_env/debug_ios/libsnark-wrapper.a 
	make scheme=debug   target=ios_sim build_env/debug_ios_sim/libsnark-wrapper.a 
	make install_wrapper_link_lib




clean_wrapper :
    rm -rf build_env/ios 
    rm -rf build_env/ios_sim 


