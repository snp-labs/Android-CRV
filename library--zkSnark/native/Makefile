

C_COMPILER          :=clang
CXX_COMPILER        :=clang++
COMPILERS           :=CC=${C_COMPILER} CXX=${CXX_COMPILER}
MAKE_JOBS           := -j 4

BUILD_WORKSPACE     :=build_env/
BUILD_DIR           :=${BUILD_WORKSPACE}${target}/
INSTALL_DIR         :=${PWD}/lib/${target}/
LIBCXX_DIR          :=$(shell xcrun --show-sdk-platform-path)/../../Toolchains/XcodeDefault.xctoolchain/usr/include/c++/v1


ifeq ($(strip $(target)),ios)

TRIPLE              :=arm64-apple-ios
CONF_TRIPLE         :=arm64-apple-ios
SDK_DIR             :=$(shell xcrun --show-sdk-path --sdk iphoneos)
SYSTEM_NAME         :=iOS
SYSTEM_PROCESSOR    :=arm64

else ifeq ($(strip $(target)),ios_sim)

TRIPLE              :=x86_64-apple-ios-simulator
CONF_TRIPLE         :=x86_64-apple-ios
SDK_DIR             :=$(shell xcrun --show-sdk-path --sdk iphonesimulator)
SYSTEM_NAME         :=iOS
SYSTEM_PROCESSOR    :=x86_64

endif



C_ARGUMENTS         = -target ${TRIPLE} -DSYSTEM_NAME_${SYSTEM_NAME} -DSCHEME_${scheme} 
C_ARGUMENTS         += -Wall -Wextra -Wfatal-errors -ggdb3 -O2
CXX_ARGUMENTS       := ${C_ARGUMENTS} -stdlib=libc++ -std=c++11 
SYSROOT_OPT         := -isystem=${SDK_DIR} -isysroot ${SDK_DIR} -I${LIBCXX_DIR} 

MAKE_CFLAGS         = ${C_ARGUMENTS} ${SYSROOT_OPT} 
MAKE_CXXFLAGS       = ${CXX_ARGUMENTS} ${SYSROOT_OPT} 



ifeq ($(strip $(target)),ios)

MAKE_CFLAGS         += -fembed-bitcode 
MAKE_CXXFLAGS       += -fembed-bitcode 

GMP_CONF_EXTRA_OPT  := MPN_PATH="generic" --enable-cxx  --host=${CONF_TRIPLE} 

SSL_EXTRA_CONF_OPT  := ios64-cross enable-ec_nistp_64_gcc_128
SSL_EXTRA_MAKE_OPT  := -miphoneos-version-min=7.0 

FF_SNARK_CFLAGS     := -fembed-bitcode ${C_ARGUMENTS} 
FF_SNARK_CXXFLAGS   := -fembed-bitcode ${CXX_ARGUMENTS} -I${LIBCXX_DIR} 

else ifeq ($(strip $(target)),ios_sim)

GMP_CONF_EXTRA_OPT  :=MPN_PATH="x86_64 generic" --enable-cxx

SSL_EXTRA_CONF_OPT  :=darwin64-x86_64-cc enable-ec_nistp_64_gcc_128
SSL_EXTRA_MAKE_OPT  :=-miphoneos-version-min=7.0 

FF_SNARK_CFLAGS     := ${C_ARGUMENTS} 
FF_SNARK_CXXFLAGS   := ${CXX_ARGUMENTS} -I${LIBCXX_DIR} 
 
endif




${BUILD_DIR} : ${BUILD_WORKSPACE} ;
	mkdir -p ${BUILD_DIR}	

check_target_arg : ;
	@if [ "${target}" = "" ] ; then \
		echo "\nTarget option not set " ; \
		echo "\tEg: make target=[ios,ios_sim] make_gmp \n" ; \
		exit 1 ; \
	fi ;



#
#   Rebuild GMP 
#
GMP_VERSION         :=6.2.1
GMP_SRC             :=gmp-${GMP_VERSION}
GMP_SRC_DIR         :=$(PWD)/depends/gmp-${GMP_VERSION}
GMP_BUILD_DIR       :=${BUILD_DIR}gmp

download_gmp : 
	cd depends ; wget https://gmplib.org/download/${GMP_SRC}/${GMP_SRC}.tar.xz
	cd depends ; tar -xf ${GMP_SRC}.tar.xz
	cd depends ; rm ${GMP_SRC}.tar.xz

configure_gmp : check_target_arg  ${BUILD_DIR}
	@echo "\n\n--> Clean build folder "
	rm -fr ${GMP_BUILD_DIR} ; mkdir ${GMP_BUILD_DIR}
	@echo "\n\n--> Run Configure \n"
	cd ${GMP_BUILD_DIR} ; \
		${GMP_SRC_DIR}/configure \
			${COMPILERS} \
			${GMP_CONF_EXTRA_OPT} \
			--prefix=${INSTALL_DIR}
	@echo "\n\n--> Modify generated Makefiles \n"
	rpl --  'CFLAGS = '   'CFLAGS = ${MAKE_CFLAGS} ' ${GMP_BUILD_DIR}/*/Makefile
	rpl --  'CFLAGS = '   'CFLAGS = ${MAKE_CFLAGS} ' ${GMP_BUILD_DIR}/Makefile
	rpl --  'CXXFLAGS = ' 'CXXFLAGS = ${MAKE_CXXFLAGS} ' ${GMP_BUILD_DIR}/*/Makefile
	rpl --  'CXXFLAGS = ' 'CXXFLAGS = ${MAKE_CXXFLAGS} ' ${GMP_BUILD_DIR}/Makefile

make_gmp : check_target_arg ; 
	cd ${GMP_BUILD_DIR} ; make ${MAKE_JOBS}
	cd ${GMP_BUILD_DIR} ; make install 
	




#
#   Rebuild OpenSSL 
#
SSL_VERSION         :=1.1.1j
SSL_SRC             :=openssl-${SSL_VERSION}
SSL_SRC_DIR         :=$(PWD)/depends/${SSL_SRC}
SSL_BUILD_DIR       :=${BUILD_DIR}openssl

download_ssl : 
	cd depends ; wget https://www.openssl.org/source/${SSL_SRC}.tar.gz
	cd depends ; tar -xf ${SSL_SRC}.tar.gz
	cd depends ; rm ${SSL_SRC}.tar.gz

configure_ssl : check_target_arg ${BUILD_DIR} ;
	@echo "\n\n--> Clean build folder "
	rm -fr ${SSL_BUILD_DIR} ; mkdir ${SSL_BUILD_DIR}
	@echo "\n\n--> Run Configure \n"
	cd ${SSL_BUILD_DIR} ; \
		${SSL_SRC_DIR}/Configure \
			${COMPILERS} \
			${SSL_EXTRA_CONF_OPT} \
			--openssldir=${INSTALL_DIR} \
			--prefix=${INSTALL_DIR}
	@echo "\n\n--> Modify generated Makefiles \n"
	echo "\n\n" 							>> ${SSL_BUILD_DIR}/Makefile 
	echo "CFLAGS += ${MAKE_CFLAGS} "  		>> ${SSL_BUILD_DIR}/Makefile 
	echo "CFLAGS += ${SSL_EXTRA_MAKE_OPT} " >> ${SSL_BUILD_DIR}/Makefile 

make_ssl : check_target_arg ; 
	cd ${SSL_BUILD_DIR} ; make ${MAKE_JOBS}
	cd ${SSL_BUILD_DIR} ; make install 
	



#
# 	Rebuild libff and libsnark
#
FF_SNARK_BUILD_DIR      :=${BUILD_DIR}${scheme}_ff_snark
FF_SNARK_INCLUDE_DIR    = ${PWD} ${PWD}/depends ${INSTALL_DIR}include
FF_SNARK_INCLUDE_DIR    += ${PWD}/../wrapper ${LIBCXX_DIR}  

I_DIR       :=$(foreach Idir,$(FF_SNARK_INCLUDE_DIR),$(strip $(Idir)))
I_DIR_LIST  :=$(shell echo "${I_DIR}" | sed -e 's/ /;/g')

configure_ff_snark : check_target_arg ${BUILD_DIR} ;
	@echo "\n\n--> Clean build folder "
	rm -fr ${FF_SNARK_BUILD_DIR} ; mkdir ${FF_SNARK_BUILD_DIR}
	cmake  \
	-D C_COMPILER="${C_COMPILER}" \
	-D CXX_COMPILER="${CXX_COMPILER}" \
	-D C_COMPILER_TARGET="${TRIPLE}" \
	-D CXX_COMPILER_TARGET="${TRIPLE}" \
	-D SYSTEM_NAME="${SYSTEM_NAME}" \
	-D SYSTEM_PROCESSOR="${SYSTEM_PROCESSOR}" \
	-D OSX_SYSROOT="${SDK_DIR}" \
	-D SYSROOT="${SDK_DIR}" \
	-D C_FLAGS="${FF_SNARK_CFLAGS}" \
	-D CXX_FLAGS="${FF_SNARK_CXXFLAGS}" \
	-D INSTALL_DIR="${PWD}/lib/${target}_${scheme}" \
	-D I_DIR_LIST="${I_DIR_LIST}" \
	-S . -B ${FF_SNARK_BUILD_DIR} 


make_ff_snark : check_target_arg ;
	@echo "\n\n--> Build libff and libsnark "
	cmake --build ${FF_SNARK_BUILD_DIR} ${MAKE_JOBS} 
	cmake --install ${FF_SNARK_BUILD_DIR}





#
# 	Create and copy universal static library
#
install_snark_link_libs :
	libtool -static \
		lib/ios_release/libsnark.a \
		lib/ios_sim_release/libsnark.a \
		-o ../archive-release/libsnark.a
	libtool -static \
		lib/ios_debug/libsnark.a \
		lib/ios_sim_debug/libsnark.a \
		-o ../archive-debug/libsnark.a
	libtool -static \
		lib/ios_release/libff.a \
		lib/ios_sim_release/libff.a \
		-o ../archive-release/libff.a
	libtool -static \
		lib/ios_debug/libff.a \
		lib/ios_sim_debug/libff.a \
		-o ../archive-debug/libff.a	
	libtool -static lib/*/lib/libgmp.a    	 -o ../archive-release/libgmp.a
	libtool -static lib/*/lib/libgmpxx.a     -o ../archive-release/libgmpxx.a
	libtool -static lib/*/lib/libssl.a       -o ../archive-release/libssl.a
	libtool -static lib/*/lib/libcrypto.a    -o ../archive-release/libcrypto.a
	cp ../archive-release/libgmp.a \
		../archive-release/libgmpxx.a \
		../archive-release/libssl.a \
		../archive-release/libcrypto.a \
		../archive-debug/



#
# 	Single make command to build everything
# 	$ make batch_build_all
#

clean_all :
	rm -rf build_env ; mkdir build_env
	rm -rf lib       ; mkdir lib 

batch_build_target : check_target_arg ;
	@echo "\n\n\n" ; make target=${target} configure_gmp
	@echo "\n\n\n" ; make target=${target} make_gmp 
	@echo "\n\n\n" ; make target=${target} configure_ssl 
	@echo "\n\n\n" ; make target=${target} make_ssl 
	@echo "\n\n\n" ; make target=${target} scheme=release configure_ff_snark 
	@echo "\n\n\n" ; make target=${target} scheme=release make_ff_snark 
	@echo "\n\n\n" ; make target=${target} scheme=debug   configure_ff_snark 
	@echo "\n\n\n" ; make target=${target} scheme=debug   make_ff_snark 

batch_build_all :
	@#echo "\n\n\n" ; make download_gmp
	@#echo "\n\n\n" ; make download_ssl
	@make batch_build_target target=ios
	@make batch_build_target target=ios_sim
	@make install_snark_link_libs 






