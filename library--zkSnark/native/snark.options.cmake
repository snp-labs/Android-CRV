
set(
  CURVE
  "ALT_BN128"
  CACHE
  STRING
  "Default curve: one of ALT_BN128, BN128, EDWARDS, MNT4, MNT6"
)

option(
  DEBUG
  "Enable debugging mode"
  OFF
)
option(
  LOWMEM
  "Limit the size of multi-exponentiation tables, for low-memory platforms"
  OFF
)
option(
  MULTICORE
  "Enable parallelized execution, using OpenMP"
  OFF
)
option(
  BINARY_OUTPUT
  "In serialization, output raw binary data (instead of decimal), which is smaller and faster."
  ON
)
option(
  MONTGOMERY_OUTPUT
  "Serialize Fp elements as their Montgomery representations (faster but not human-readable)"
  ON
)
option(
  USE_PT_COMPRESSION
  "Use point compression"
  ON
)
option(
  PROFILE_OP_COUNTS
  "Collect counts for field and curve operations"
  OFF
)
option(
  USE_MIXED_ADDITION
  "Convert each element of the key pair to affine coordinates"
  OFF
)

option(
  WITH_SUPERCOP
  "Support for Ed25519 signatures required by ADSNARK"
  ON
)

option(
  WITH_PROCPS
  "Use procps for memory profiling"
  ON
)

option(
  CPPDEBUG
  "Enable debugging of C++ STL (does not imply DEBUG)"
  OFF
)

option(
  PERFORMANCE
  "Enable link-time and aggressive optimizations"
  OFF
)

option(
  USE_ASM
  "Use architecture-specific optimized assembly code"
  OFF
)


add_definitions(
  -DCURVE_${CURVE}
)

if(${CURVE} STREQUAL "BN128")
  add_definitions(
    -DBN_SUPPORT_SNARK=1
  )
endif()

if("${DEBUG}")
  add_definitions(-DDEBUG=1)
endif()

if("${LOWMEM}")
  add_definitions(-DLOWMEM=1)
endif()

if("${MULTICORE}")
  add_definitions(-DMULTICORE=1)
endif()

if("${BINARY_OUTPUT}")
  add_definitions(-DBINARY_OUTPUT)
endif()

if("${MONTGOMERY_OUTPUT}")
  add_definitions(-DMONTGOMERY_OUTPUT)
endif()

if(NOT "${USE_PT_COMPRESSION}")
  add_definitions(-DNO_PT_COMPRESSION=1)
endif()

if("${PROFILE_OP_COUNTS}")
  add_definitions(-DPROFILE_OP_COUNTS=1)
endif()

if("${USE_MIXED_ADDITION}")
  add_definitions(-DUSE_MIXED_ADDITION=1)
endif()

if("${CPPDEBUG}")
  add_definitions(-D_GLIBCXX_DEBUG -D_GLIBCXX_DEBUG_PEDANTIC)
endif()

add_definitions(
  -DNO_PROCPS -DWITH_PROCPS=OFF
)

if("${USE_ASM}")
  add_definitions(-DUSE_ASM)
endif()


 

