

include ../native/Makefile





#
# 	ReBuild Wrapper
#
WRAPPER_OBJS	=  build_env/${target}/wrapper.cpp.o  
WRAPPER_AR 		:= build_env/${target}/wrapper.a 

${PWD}/build_env/${target} :
	mkdir -p build_env/${target}

make_wrapper_obj : check_target_arg  ;
	clang++ -c  \
	-DSYSTEM_NAME=$(SYSTEM_NAME) \
	-DBUILD_VERSION="$(shell date)" \
	${MAKE_CXXFLAGS} \
	-I${PWD} \
	-I${PWD}/../native \
	-I${PWD}/../native/lib/$(target)/include \
	-I${PWD}/../native/depends/libff \
	-I${PWD}/../native/depends/libfqfft \
	${SRC} \
	-o ${OBJ}

build_env/${target}/wrapper.cpp.o : wrapper.cpp ;
	make make_wrapper_obj target=${target} SRC=$< OBJ=$@ 

${WRAPPER_AR} : ${WRAPPER_OBJS} ;
	libtool -static \
		${WRAPPER_OBJS} \
		-o $@

install_wrapper_link_lib :  
	rm -f ${iOS_APP_DIR}libsnark-wrapper.a
	libtool -static \
		build_env/*/wrapper.a \
		-o ${iOS_APP_DIR}libsnark-wrapper.a

batch_build_wrapper :
	mkdir -p build_env/ios ; mkdir -p build_env/ios_sim ; 
	@echo "\n\n\n" ; make target=ios build_env/ios/wrapper.a 
	@echo "\n\n\n" ; make target=ios_sim build_env/ios_sim/wrapper.a 
	@echo "\n\n\n" ; make install_wrapper_link_lib

clean_wrapper :
	rm -rf build_env/ios 
	rm -rf build_env/ios_sim 
	

