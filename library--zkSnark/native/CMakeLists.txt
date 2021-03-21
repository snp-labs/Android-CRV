cmake_minimum_required(VERSION 3.19)



set(include_dir_list ${I_DIR_LIST})

if(${dump_args})
  message(STATUS "${C_COMPILER}")
  message(STATUS "${CXX_COMPILER}")
  message(STATUS "${C_COMPILER_TARGET}")
  message(STATUS "${CXX_COMPILER_TARGET}")
  message(STATUS "${SYSTEM_NAME}")
  message(STATUS "${SYSTEM_PROCESSOR}")
  message(STATUS "${OSX_SYSROOT}")
  message(STATUS "${SYSROOT}")
  message(STATUS "${C_FLAGS}")
  message(STATUS "${CXX_FLAGS}")
  message(STATUS "${INSTALL_DIR}")
  
  foreach(include_dir ${include_dir_list})
    message(STATUS "include_dir : ${include_dir}")
  endforeach()

endif()


project (AppLibraries)
 
include ( snark.options.cmake )

set( CMAKE_C_COMPILER           ${C_COMPILER} )
set( CMAKE_CXX_COMPILER         ${CXX_COMPILER} )
set( CMAKE_C_COMPILER_TARGET    ${C_COMPILER_TARGET} )
set( CMAKE_CXX_COMPILER_TARGET  ${CXX_COMPILER_TARGET} )
set( CMAKE_SYSTEM_NAME          ${SYSTEM_NAME} )
set( CMAKE_SYSTEM_PROCESSOR     ${SYSTEM_PROCESSOR} )
set( CMAKE_OSX_SYSROOT          ${OSX_SYSROOT} )
set( CMAKE_SYSROOT              ${SYSROOT} )
set( CMAKE_C_FLAGS              "${C_FLAGS}" )
set( CMAKE_CXX_FLAGS            "${CXX_FLAGS}" )

foreach(include_dir ${include_dir_list})
  include_directories("${include_dir}")
endforeach()




add_library(
  ff
  STATIC

  depends/libff/libff/algebra/curves/alt_bn128/alt_bn128_g1.cpp
  depends/libff/libff/algebra/curves/alt_bn128/alt_bn128_g2.cpp
  depends/libff/libff/algebra/curves/alt_bn128/alt_bn128_init.cpp
  depends/libff/libff/algebra/curves/alt_bn128/alt_bn128_pairing.cpp
  depends/libff/libff/algebra/curves/alt_bn128/alt_bn128_pp.cpp
  depends/libff/libff/algebra/curves/edwards/edwards_g1.cpp
  depends/libff/libff/algebra/curves/edwards/edwards_g2.cpp
  depends/libff/libff/algebra/curves/edwards/edwards_init.cpp
  depends/libff/libff/algebra/curves/edwards/edwards_pairing.cpp
  depends/libff/libff/algebra/curves/edwards/edwards_pp.cpp
  depends/libff/libff/algebra/curves/mnt/mnt4/mnt4_g1.cpp
  depends/libff/libff/algebra/curves/mnt/mnt4/mnt4_g2.cpp
  depends/libff/libff/algebra/curves/mnt/mnt4/mnt4_init.cpp
  depends/libff/libff/algebra/curves/mnt/mnt4/mnt4_pairing.cpp
  depends/libff/libff/algebra/curves/mnt/mnt4/mnt4_pp.cpp
  depends/libff/libff/algebra/curves/mnt/mnt46_common.cpp
  depends/libff/libff/algebra/curves/mnt/mnt6/mnt6_g1.cpp
  depends/libff/libff/algebra/curves/mnt/mnt6/mnt6_g2.cpp
  depends/libff/libff/algebra/curves/mnt/mnt6/mnt6_init.cpp
  depends/libff/libff/algebra/curves/mnt/mnt6/mnt6_pairing.cpp
  depends/libff/libff/algebra/curves/mnt/mnt6/mnt6_pp.cpp
  depends/libff/libff/common/double.cpp
  depends/libff/libff/common/profiling.cpp
  depends/libff/libff/common/utils.cpp
)

target_include_directories(
  ff PUBLIC 
  
  ..
  ${CMAKE_CURRENT_LIST_DIR}/depends/libff
  ${INSTALL_DIR}/include 
)

install(
  TARGETS ff 
  DESTINATION ${INSTALL_DIR}
)





add_library(
  snark
  STATIC  

  libsnark/common/data_structures/integer_permutation.cpp
  libsnark/common/data_structures/set_commitment.cpp
  libsnark/common/default_types/r1cs_ppzkpcd_pp.cpp
  libsnark/common/default_types/tinyram_ppzksnark_pp.cpp
  libsnark/common/default_types/tinyram_zksnark_pp.cpp
  libsnark/common/routing_algorithms/as_waksman_routing_algorithm.cpp
  libsnark/common/routing_algorithms/benes_routing_algorithm.cpp
  libsnark/gadgetlib1/constraint_profiling.cpp
  libsnark/gadgetlib2/adapters.cpp
  libsnark/gadgetlib2/constraint.cpp
  libsnark/gadgetlib2/gadget.cpp
  libsnark/gadgetlib2/infrastructure.cpp
  libsnark/gadgetlib2/integration.cpp
  libsnark/gadgetlib2/pp.cpp
  libsnark/gadgetlib2/protoboard.cpp
  libsnark/gadgetlib2/variable.cpp
  libsnark/relations/circuit_satisfaction_problems/tbcs/tbcs.cpp
  libsnark/relations/ram_computations/memory/memory_store_trace.cpp
  libsnark/relations/ram_computations/memory/ra_memory.cpp
  libsnark/relations/ram_computations/rams/fooram/fooram_aux.cpp
  libsnark/relations/ram_computations/rams/tinyram/tinyram_aux.cpp
  libsnark/jsnark_interface/CircuitReader.cpp
  libsnark/jsnark_interface/Util.cpp
)

target_include_directories(
  snark PUBLIC 

  ${CMAKE_CURRENT_LIST_DIR}/depends/libff
  ${INSTALL_DIR}/include
)

install(
  TARGETS snark 
  DESTINATION ${INSTALL_DIR}
)


set(CMAKE_VERBOSE_MAKEFILE off)


