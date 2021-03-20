// swift-tools-version:5.3
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    
    name: "zkSnark",
    
    platforms: [
             .iOS(.v13)
        ],
    
    products: [
        
        .library(
            name: "zkSnark",
            targets: ["zkSnark"]),
    ],
    
    dependencies: [],
    
    targets: [
        
        .systemLibrary(
            name: "native_libsnark"
        ),

        .target(
            name: "zkSnark" ,
            dependencies: ["native_libsnark"] ,
            resources: [
                        .copy("Resources/registerarith.dat") ,
                        .copy("Resources/registerin.dat") ,
                        .copy("Resources/tallyarith.dat") ,
                        .copy("Resources/tallyin.dat") ,
                        .copy("Resources/votearith.dat") ,
                        .copy("Resources/votein.dat")
                        ]
        )
        
    ]
)
