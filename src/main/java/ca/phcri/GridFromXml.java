package ca.phcri;

import ij.IJ;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;

public class GridFromXml extends CombinedGridsPlugin {
	String gridParameterXmlArray[];
	
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.47")) 
			return;
		imp = IJ.getImage();
		
		xmlReader();
		
		//check if file names and number of slices match
		
		gridLayer();
		
		//if ("".equals(err)) {
			showHistory(gridParameterArray);
			
			if (showGridSwitch && !gridSwitchExist()){
				Grid_Switch gs = new Grid_Switch();
				gs.gridSwitch();
			}
		//}
	}
	
	void xmlReader(){
		//read in parameters as an Array
		//load common parameters
	}
	
	void gridLayer(){
		width = imp.getWidth();
		height = imp.getHeight();
		Calibration cal = imp.getCalibration();
		units = cal.getUnits();
		totalSlices = imp.getStackSize();
		
		
		
		calculateTile();
		
		for (String str : gridParameterXmlArray){
			//load parameters for individual grid
			calculateNLines();
			ShapeRoi gridRoi = getGridRoi();
			int i = 0;
			addGridOnArray(gridRoi, i);
		}
		
		showGrid(gridRoiArray);
	}
	
	
}