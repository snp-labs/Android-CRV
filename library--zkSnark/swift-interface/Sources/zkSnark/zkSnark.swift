
import Foundation
import CSnark
 
public func getBuildVersion ( ) -> String {
    let version = String(cString: CgetBuildVersion())
	return version
}

public enum testSnarkMode {
    case setup , verify , run , all
}

public enum testSnarkTask {
    case register , vote , tally
}

public func testSnark(  test_task : Int , test_mode : Int ) -> Void {
    
    var ArithName, InputsName : String
    let mode : sub_activity_mode
    let task : sub_activity_task
    
    switch test_mode {
    case 0 :
        mode = mode_setup
    case 1 :
        mode = mode_verify
    case 2 :
        mode = mode_run
    case 3 :
        mode = mode_all
    default :
        mode = mode_all
    }
    
    switch test_task {
    case 0 :
        task = task_register
        ArithName = "register" + "arith"
        InputsName = "register" + "in"
    case 1 :
        task = task_vote
        ArithName = "vote" + "arith"
        InputsName = "vote" + "in"
    case 2 :
        task = task_tally
        ArithName = "tally" + "arith"
        InputsName = "tally" + "in"
    default :
        task = task_register
        ArithName = "register" + "arith"
        InputsName = "register" + "in"
    }
    
    print (" selected task : \(test_task) ")
    print (" selected mode : \(test_mode) ")
    
    // Call native function
    let rtn = CsubActivity_FromApp(task, mode,
                                   FilePath(ArithName,"dat").CCharPtr() ,
                                   FilePath(InputsName,"dat").CCharPtr() ,
                                   DocDir().CCharPtr() )
    
    print ("\n\n CsubActivity_FromApp:\(rtn)")
    
}



class FilePath {
    
    var NSfileContent : NSString = ""
    
    init( _ forResource : String , _ ofType : String  ) {
        
        if let path = Bundle.module.path (forResource: forResource , ofType: ofType ) {
            print ("[\(forResource).\(ofType)] @ [\(path)]" )
            NSfileContent = path as NSString
        }else{
            print (" File [\(forResource).\(ofType)] not found" )
        }
    }
    
    public func CCharPtr() -> UnsafeMutablePointer<CChar>? {
        return UnsafeMutablePointer<CChar>(mutating: NSfileContent.utf8String )
    }
}

class DocDir {
    
    var NSfileContent : NSString = ""
    
    init() {
        var DocDir = NSSearchPathForDirectoriesInDomains( .documentDirectory , .userDomainMask , true ).first ?? ""
        DocDir += "/"
        NSfileContent = DocDir as NSString
    }
    
    public func CCharPtr() -> UnsafeMutablePointer<CChar>? {
        return UnsafeMutablePointer<CChar>(mutating: NSfileContent.utf8String )
    }
}

 
