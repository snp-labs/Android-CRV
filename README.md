# libsnark-porting on iOS


- Open app--iOS-CRV/iOS-CRV.xcodeproj in Xcode

- Supports both iOS device and simulator

- Libraries built for release and debug build schemes.

- To change build scheme, goto "Product" menu -> "Scheme" -> "iOS-CRV__Release" or "iOS-CRV__Debug"

- libsnark, wrapper and dependencies (libgmp, libssl and libcrypto ) have been prebuilt and placed in 
	library--zkSnark/archive-release 
	library--zkSnark/archive-debug 

- To rebuild see [library--zkSnark/native/Makefile](https://github.com/snp-labs/Android-CRV/blob/8e719737483961c71a0637b5ac85cb6e9d05cd69/library--zkSnark/native/Makefile#L227) and [library--zkSnark/wrapper/Makefile](https://github.com/snp-labs/Android-CRV/blob/8e719737483961c71a0637b5ac85cb6e9d05cd69/library--zkSnark/wrapper/Makefile#L54)



 