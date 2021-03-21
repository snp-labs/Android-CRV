

include ../native/Makefile


#
# 	ReBuild Wrapper
#
WRAPPER_OBJS	=  build_env/${target}/${scheme}/wrapper.cpp.o  
WRAPPER_AR 		:= build_env/${target}/${scheme}/libsnark-wrapper.a 

make_wrapper_obj : check_target_arg  ;
	clang++ -c  \
	-DBUILD_VERSION="$(shell date)" \
	${MAKE_CXXFLAGS} \
	-I${PWD} \
	-I${PWD}/../native \
	-I${PWD}/../native/lib/$(target)/include \
	-I${PWD}/../native/depends/libff \
	-I${PWD}/../native/depends/libfqfft \
	${SRC} \
	-o ${OBJ} 

build_env/${target}/${scheme}/wrapper.cpp.o : wrapper.cpp ;
	make make_wrapper_obj target=${target} scheme=${scheme} SRC=$< OBJ=$@ 

${WRAPPER_AR} : ${WRAPPER_OBJS} ;
	libtool -static ${WRAPPER_OBJS} -o $@

install_wrapper_link_lib :  
	libtool -static \
		build_env/ios/release/libsnark-wrapper.a \
		build_env/ios_sim/release/libsnark-wrapper.a \
		-o ../archive-release/libsnark-wrapper.a
	libtool -static \
		build_env/ios/debug/libsnark-wrapper.a \
		build_env/ios_sim/debug/libsnark-wrapper.a \
		-o ../archive-debug/libsnark-wrapper.a


clean_wrapper :
	rm -rf build_env/ios 
	rm -rf build_env/ios_sim 
	make make_dir

make_dir :
	mkdir -p build_env/ios
	mkdir -p build_env/ios/release
	mkdir -p build_env/ios/debug
	mkdir -p build_env/ios_sim
	mkdir -p build_env/ios_sim/release
	mkdir -p build_env/ios_sim/debug

batch_build_wrapper :
	@scheme=release target=ios     ; make scheme=$$scheme target=$$target build_env/$$target/$$scheme/libsnark-wrapper.a 
	@scheme=release target=ios_sim ; make scheme=$$scheme target=$$target build_env/$$target/$$scheme/libsnark-wrapper.a 
	@scheme=debug   target=ios     ; make scheme=$$scheme target=$$target build_env/$$target/$$scheme/libsnark-wrapper.a 
	@scheme=debug   target=ios_sim ; make scheme=$$scheme target=$$target build_env/$$target/$$scheme/libsnark-wrapper.a 
	@make install_wrapper_link_lib

