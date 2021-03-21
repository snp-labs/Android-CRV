//
//  ViewController.swift
//  iOS-CRV
//
//  Created by ThomasMacBooK on 3/20/21.
//

import UIKit
import zkSnark

class ViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource {

    @IBOutlet weak var picker: UIPickerView!
    @IBOutlet weak var runButton: UIButton!
    
    var pickerData : [[String]] = [[String]]()
        
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        self.picker.delegate = self
        self.picker.dataSource = self
        
        pickerData = [["Register","Vote","Tally"],["Setup","Verify","Run","All"]]
        
    }
    
    // Number of columns of data
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 2
    }
    
    // The number of rows of data
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return pickerData[component].count
    }
    
    // The data to return fopr the row and component (column) that's being passed in
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return pickerData[component][row]
    }
    
    // Capture the picker view selection
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        // This method is triggered whenever the user makes a change to the picker selection.
        // The parameter named row and component represents what was selected.
    }

    @IBAction func OnClick(_ sender: UIButton) {
        
        runButton.isEnabled = false

        testSnark(test_task: picker.selectedRow(inComponent: 0 ),
                  test_mode : picker.selectedRow(inComponent: 1 ) )
        
        runButton.isEnabled = true
    
    }
    
}

