
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
    
     
    let content1 = textResourceToCCharPtr(forResource: ArithName , withExtension: "dat" )
    if !content1.good() { return }
    //content1.dumpContent()
    
    let content2 = textResourceToCCharPtr(forResource: InputsName , withExtension: "dat" ) ;
    if !content2.good() { return }
    //content2.dumpContent()
    
    
    // Call native function
    let rtn = CsubActivity_FromApp(task, mode, content1.CCharPtr() , content2.CCharPtr() , DocDir().CCharPtr() )
    
    print ("\n\n CsubActivity_FromApp:\(rtn)")
    
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


class textResourceToCCharPtr {
    
    var forResource : String
    var withExtension : String
    var NSfileContent : NSString = ""
    var fileName : String = ""
    var fail : Bool = true
    
    init(forResource : String , withExtension : String ) {
        self.forResource = forResource
        self.withExtension = withExtension
        self.fileName = "\(forResource).\(withExtension)"
        self.loadContent()
    }
    
    public func good() -> Bool {
        return !fail
    }
    
    func loadContent() {
        
        var fileContent : String = ""
        
        
        if let FileURL = Bundle.module.url(forResource: forResource , withExtension: withExtension ) {
            
            do {
                fileContent = try String(contentsOf: FileURL)
            }catch{
                print (" Could not load File [\(fileName)]" )
            }
            
        }else{
            print (" File [\(fileName)] not found" )
        }
        
        NSfileContent = fileContent as NSString
        fail = false
    }
    
    public func reLoadContent() {
        loadContent()
    }
    
    public func dumpContent() {
        print ("\n\n ---- Content of [\(fileName)] ----- ")
        print ("\(NSfileContent)")
        print (" ---- End of [\(fileName)] ----- \n\n")
    }
    
    public func CCharPtr() -> UnsafeMutablePointer<CChar>? {
        return UnsafeMutablePointer<CChar>(mutating: NSfileContent.utf8String )
    }

}
