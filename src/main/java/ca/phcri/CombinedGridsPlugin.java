package ca.phcri;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.text.TextPanel;
import ij.text.TextWindow;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.geom.GeneralPath;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class CombinedGridsPlugin implements PlugIn, DialogListener {
	private final static String[] colors = 
		{ "Red", "Green", "Blue", "Magenta", "Cyan", "Yellow", "Orange", 
		"Black", "White" };
	private static String color = "Blue";
	
	private final static int COMBINED = 0, DOUBLE_LATTICE = 1, LINES = 2,
			HLINES = 3, CROSSES = 4, POINTS = 5;
	private final static String[] types = 
		{ "Combined Point", "Double Lattice", "Lines", "Horizontal Lines", 
		"Crosses", "Points" };
	
	private static String type = types[COMBINED];
	private static double areaPerPoint;

	private final static int ONE_TO_FOUR = 0, ONE_TO_NINE = 1, ONE_TO_SIXTEEN = 2, 
			ONE_TO_TWENTYFIVE = 3, ONE_TO_THIRTYSIX = 4;
	private final static String[] ratioChoices = { "1:4", "1:9", "1:16", "1:25", "1:36" };
	private static String gridRatio = ratioChoices[ONE_TO_FOUR];
	private final static String[] radiobuttons = 
		{ "Random Offset", "Fixed Position", "Manual Input" };
	private final static int RANDOM = 0, FIXED = 1, MANUAL = 2;
	private String radiochoice = radiobuttons[RANDOM];
	private final static String[] applyChoices = 
		{ "One Grid for All Slices", "Different Grids for Each Slice", 
		"One Grid for the Current Slice"};
	private final static int ONEforALL = 0, DIFFERENTforEACH = 1, CURRENT = 2;
	private static String applyTo = applyChoices[DIFFERENTforEACH];
	
	private static Component[] components; 
	// this is to select components in the dialog box
	private final static int[] ratioField = { 4, 5 };
	private final static int[] combinedGridFields = { 14, 15, 16, 17 };
	private final static int[] parameterFieldsOff = { 10, 11, 12, 13, 14, 15, 16, 17 };
	private final static int[] xstartField = { 10, 11 };
	private final static int[] ystartField = { 12, 13 };
	private static boolean showGridSwitch = true;

	private Random random = new Random(System.currentTimeMillis());
	private ImagePlus imp;
	private double tileWidth, tileHeight;
	private int width, height;
	private int xstart, ystart;
	private int xstartCoarse, ystartCoarse, coarseGridX, coarseGridY;
	private int linesV, linesH;
	private double pixelWidth = 1.0, pixelHeight = 1.0;
	private String units;
	private String err = "";
	private Roi[] gridRoiArray;
	private String[] gridParameterArray;
	private int totalSlices;

	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.47"))
			return;
		imp = IJ.getImage();
		showDialog();
	}
	
	
	void removeGrid(){
		Overlay ol = imp.getOverlay();
		
		if(ol != null){
			for(Roi element : ol.toArray()){
				if(element != null && 
						element.getName() != null &&
						element.getName().startsWith("grid"))
							ol.remove(element);
			}
		}
	}
	
	void showGrid(Roi[] rois) {
		removeGrid();

		if(rois != null) {
			Overlay ol = imp.getOverlay();
			
			if(ol == null)
				ol = new Overlay();
			
			for(Roi roi : rois)
				if(roi != null)
					ol.add(roi);
			
			imp.setOverlay(ol);

		}
	}

	// methods to form grids
	GeneralPath drawPoints() {
		int one = 1;
		int two = 2;
		GeneralPath path = new GeneralPath();
		for (int h = 0; h < linesV; h++) {
			for (int v = 0; v < linesH; v++) {
				float x = (float) (xstart + h * tileWidth);
				float y = (float) (ystart + v * tileHeight);
				path.moveTo(x - two, y - one); path.lineTo(x - two, y + one);
				path.moveTo(x + two, y - one); path.lineTo(x + two, y + one);
				path.moveTo(x - one, y - two); path.lineTo(x + one, y - two);
				path.moveTo(x - one, y + two); path.lineTo(x + one, y + two);
			}
		}
		return path;
	}

	GeneralPath drawCrosses() {
		GeneralPath path = new GeneralPath();
		float arm = 5;
		for (int h = 0; h < linesV; h++) {
			for (int v = 0; v < linesH; v++) {
				float x = (float) (xstart + h * tileWidth);
				float y = (float) (ystart + v * tileHeight);
				path.moveTo(x - arm, y); path.lineTo(x + arm, y);
				path.moveTo(x, y - arm); path.lineTo(x, y + arm);
			}
		}
		return path;
	}

	GeneralPath drawCombined() {
		GeneralPath path = new GeneralPath();
		float arm = 5;
		float pointSizeCoarse = 10;
		float armCoarse = pointSizeCoarse / 2;

		for (int h = 0; h < linesV; h++) {
			for (int v = 0; v < linesH; v++) {
				float x = (float) (xstart + h * tileWidth);
				float y = (float) (ystart + v * tileHeight);
				path.moveTo(x - arm, y); path.lineTo(x + arm, y);
				path.moveTo(x, y - arm); path.lineTo(x, y + arm);

				if ((h % coarseGridX == 0) && (v % coarseGridY == 0)) {
					float centerX = 
							(float) (xstart + xstartCoarse * tileWidth  + h * tileWidth);
					float centerY = 
							(float) (ystart + ystartCoarse * tileHeight + v * tileHeight);

					// drawing a coarse point by lines
					path.moveTo(centerX - pointSizeCoarse, centerY - armCoarse);
					path.lineTo(centerX - pointSizeCoarse, centerY + armCoarse);
					path.moveTo(centerX + pointSizeCoarse, centerY - 0);
					path.lineTo(centerX + pointSizeCoarse, centerY + armCoarse);
					path.moveTo(centerX - armCoarse, centerY - pointSizeCoarse);
					path.lineTo(centerX + 0,		 centerY - pointSizeCoarse);
					path.moveTo(centerX - armCoarse, centerY + pointSizeCoarse);
					path.lineTo(centerX + armCoarse, centerY + pointSizeCoarse);
				}
			}
		}
		return path;
	}
	
	
	//Drawing curve in this method is the potential problem.	
	GeneralPath drawDoubleLattice() {
		GeneralPath path = new GeneralPath();
				
		for (int i = 0; i < linesV; i++) {
			float xoff = (float) (xstart + i * tileWidth);
			path.moveTo(xoff, 0f); path.lineTo(xoff, height);
		}
		for (int i = 0; i < linesH; i++) {
			float yoff = (float) (ystart + i * tileHeight);
			path.moveTo(0f, yoff); path.lineTo(width, yoff);
		}
		
		
		float rad = 14;
		int paiDivision = 12;  //to divide seme-circle into segments
		int nPoints = paiDivision/2 * 3 + 1;
		
		Double radSeg = Math.PI /paiDivision;
		Double[] circleX = new Double[nPoints];
		Double[] circleY = new Double[nPoints];
		
		for(int i = 0; i < 19; i++){
			circleX[i] = Math.cos(radSeg * (i + paiDivision/2));
			circleY[i] = Math.sin(radSeg * (i + paiDivision/2));
		}
		
		for (int h = 0; h < linesV; h++) {  //linesV for vertical lines
			for (int v = 0; v < linesH; v++) { //linesH for horizontal lines
				
				if ((h % coarseGridX == 0) && (v % coarseGridY == 0)) {
					float centerX = 
							(float) (xstart + xstartCoarse * tileWidth + h * tileWidth);
					float centerY = 
							(float) (ystart + ystartCoarse * tileHeight + v * tileHeight);
					
					// drawing curve for coarse grid
					path.moveTo(centerX + rad * circleX[0], centerY - rad * circleY[0]);
					for(int i = 0; i < nPoints; i++)
						path.lineTo(centerX + rad * circleX[i], centerY - rad * circleY[i]);
				}
			}
		}
		
		return path;
	}

	GeneralPath drawLines() {
		GeneralPath path = new GeneralPath();

		
		for (int i = 0; i < linesV; i++) {
			float xoff = (float) (xstart + i * tileWidth);
			path.moveTo(xoff, 0f);
			path.lineTo(xoff, height);
		}
		for (int i = 0; i < linesH; i++) {
			float yoff = (float) (ystart + i * tileHeight);
			path.moveTo(0f, yoff);
			path.lineTo(width, yoff);
		}
		return path;
	}

	GeneralPath drawHorizontalLines() {
		GeneralPath path = new GeneralPath();
		
		for (int i = 0; i < linesH; i++) {
			float yoff = (float) (ystart + i * tileHeight);
			path.moveTo(0f, yoff);
			path.lineTo(width, yoff);
		}
		return path;
	}

	// end of methods for drawing grids

	void showDialog() {
		width = imp.getWidth();
		height = imp.getHeight();
		Calibration cal = imp.getCalibration();
		int places;
		if (cal.scaled()) {
			pixelWidth = cal.pixelWidth;
			pixelHeight = cal.pixelHeight;
			units = cal.getUnits();
			places = 2;
		} else {
			pixelWidth = 1.0;
			pixelHeight = 1.0;
			units = "pixels";
			places = 0;
		}
		if (areaPerPoint == 0.0) // default to 9x9 grid
			areaPerPoint = (width * cal.pixelWidth * height * cal.pixelHeight) / 81.0;
		
		totalSlices = imp.getStackSize();
		
		// get values in a dialog box
		GenericDialog gd = new GenericDialog("Grid...");
		gd.addChoice("Grid Type:", types, type);
		gd.addNumericField("Area per Point:", areaPerPoint, places, 6, units + "^2");
		gd.addChoice("Ratio:", ratioChoices, gridRatio);
		gd.addChoice("Color:", colors, color);
		gd.addRadioButtonGroup("Grid Location", radiobuttons, 3, 1, radiochoice);
		gd.addNumericField("xstart:", 0, 0);
		gd.addNumericField("ystart:", 0, 0);
		gd.addNumericField("xstartCoarse:", 0, 0);
		gd.addNumericField("ystartCoarse:", 0, 0);
		gd.addCheckbox("Show a Grid Switch if none exists", showGridSwitch);
		if(imp.getStackSize() > 1)
			gd.addRadioButtonGroup("The way to apply grid(s) to a Stack",
					applyChoices, 3, 1, applyTo);

		// to switch enable/disable for parameter input boxes
		components = gd.getComponents();
		enableFields();
		
		gd.addDialogListener(this);
		gd.showDialog();

		if (gd.wasCanceled())
			showGrid(null);
		if (gd.wasOKed()) {
			if ("".equals(err)) {
				showHistory(gridParameterArray);
				if (showGridSwitch && !gridSwitchExist()){
					Grid_Switch gs = new Grid_Switch();
					gs.gridSwitch();
				}
			} else {
				IJ.error("Grid", err);
				showGrid(null);
			}
		}
	}

	// event control for the dialog box
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		type = gd.getNextChoice();
		areaPerPoint = gd.getNextNumber();
		gridRatio = gd.getNextChoice();
		color = gd.getNextChoice();
		radiochoice = gd.getNextRadioButton();
		xstart = (int) gd.getNextNumber();
		ystart = (int) gd.getNextNumber();
		xstartCoarse = (int) gd.getNextNumber();
		ystartCoarse = (int) gd.getNextNumber();
		showGridSwitch = gd.getNextBoolean();
		if(imp.getStackSize() > 1)
			applyTo = gd.getNextRadioButton();
		err = "";
		IJ.showStatus(err);
		
		gridParameterArray = new String[totalSlices];
		gridRoiArray = new Roi[totalSlices];
				
		minAreaCheck();
		enableFields();
		setCoarseGrids();
		calculateTile();
		

		if(applyChoices[DIFFERENTforEACH].equals(applyTo)){
			for (int i = 1; i <= totalSlices; i++){
				calculateFirstGrid();
				
				if(gd.invalidNumber()) return true;
				
				if (!"".equals(err) || gd.invalidNumber()) {
					IJ.showStatus(err);
					return true;
				}
				
				ShapeRoi gridRoi = getGridRoi();
				addGridOnArray(gridRoi, i);
				saveGridParameters(i);
			}
		} else {
			calculateFirstGrid();
		
			if (!"".equals(err) || gd.invalidNumber()) {
				IJ.showStatus(err);
				return true;
			}
			
			ShapeRoi gridRoi = getGridRoi();
			
			if(applyChoices[ONEforALL].equals(applyTo)){
				addGridOnArray(gridRoi, 0);
				saveGridParameters(0);
			}
			
			if(applyChoices[CURRENT].equals(applyTo)){
				int currentSlice = imp.getCurrentSlice();
				addGridOnArray(gridRoi, currentSlice);
				saveGridParameters(currentSlice);
			}
		}

		showGrid(gridRoiArray);
		
		return true;
	}
	
	
	void addGridOnArray(ShapeRoi gridRoi, int sliceIndex){
		ShapeRoi sliceGridRoi = (ShapeRoi) gridRoi.clone();
		
		if(sliceIndex == 0){
			sliceGridRoi.setName("grid");
			gridRoiArray[0] = sliceGridRoi;
		}else {
			sliceGridRoi.setName("grid" + sliceIndex);
			sliceGridRoi.setPosition(sliceIndex);
			gridRoiArray[sliceIndex - 1] = sliceGridRoi;
		}
	}
	
	
	// if areaPerPoint is not too small, show an error
	void minAreaCheck(){
		double minArea = (width * height) / 50000.0;
		if (type.equals(types[CROSSES]) && minArea < 144.0)
			minArea = 144.0;
			// to avoid overlap of grid points.
			// ((5 + 1) * 2) ^2 = 12^2 = 144
		else if (type.equals(types[COMBINED]) && minArea < 484.0)
			minArea = 484.0;
			// As pointSizeCoarse = 10,
			//(10 + 1) * 2)^2 = 22^2 = 484
		
		else if (type.equals(types[DOUBLE_LATTICE]) && minArea < 900.0)
			minArea = 900.0;
		 	// As rad = 14, ((14 + 1) * 2) ^2 = 900
		
		else if (minArea < 16)
			minArea = 16.0;

		if (Double.isNaN(areaPerPoint) || 
				areaPerPoint / (pixelWidth * pixelHeight) < minArea) {
			err = "\"Area per Point\" too small. \n";
			areaPerPoint = 0;
		}
	}
	
	
	void enableFields(){
		if (type.equals(types[COMBINED]) || type.equals(types[DOUBLE_LATTICE]))
			fieldEnabler(ratioField, true);
		else
			fieldEnabler(ratioField, false);
		
		
		if (radiochoice.equals(radiobuttons[MANUAL])) {
			fieldEnabler(ystartField, true);

			if (type.equals(types[HLINES]))
				fieldEnabler(xstartField, false);
				//disable xstartField because
				//Horizontal lines needs just ystart and does not need xstart
			else
				fieldEnabler(xstartField, true);
			
			if (type.equals(types[COMBINED]) || type.equals(types[DOUBLE_LATTICE]))
				fieldEnabler(combinedGridFields, true);
			else
				fieldEnabler(combinedGridFields, false);
			
		} else 
			fieldEnabler(parameterFieldsOff, false);
	}
	
	void fieldEnabler(int[] fields, boolean show){
		for(int i : fields)
			components[i].setEnabled(show);
	}
	
	// enables gridRatio choice for Combined Points and Double Lattice
	void setCoarseGrids(){
		if (gridRatio.equals(ratioChoices[ONE_TO_FOUR])) {
			coarseGridX = 2;
			coarseGridY = 2;
		} else if (gridRatio.equals(ratioChoices[ONE_TO_NINE])) {
			coarseGridX = 3;
			coarseGridY = 3;
		} else if (gridRatio.equals(ratioChoices[ONE_TO_SIXTEEN])) {
			coarseGridX = 4;
			coarseGridY = 4;
		} else if (gridRatio.equals(ratioChoices[ONE_TO_TWENTYFIVE])) {
			coarseGridX = 5;
			coarseGridY = 5;
		} else if (gridRatio.equals(ratioChoices[ONE_TO_THIRTYSIX])) {
			coarseGridX = 6;
			coarseGridY = 6;
		}
	}
	
	
	// calculation for tileWidth and tileLength
	void calculateTile() {
		double tileSize = Math.sqrt(areaPerPoint);
		tileWidth  = tileSize / pixelWidth;
		tileHeight = tileSize / pixelHeight;
	}
	
	
	// decide the first point(s) depending on the way to place a grid
	void calculateFirstGrid(){
		if (radiochoice.equals(radiobuttons[RANDOM])) {
			xstart = (int) (random.nextDouble() * tileWidth);
			ystart = (int) (random.nextDouble() * tileHeight);
					// 0 <= random.nextDouble() < 1
			xstartCoarse = random.nextInt(coarseGridX);
			ystartCoarse = random.nextInt(coarseGridY);
		} else if (radiochoice.equals(radiobuttons[FIXED])) {
			xstart = (int) (tileWidth / 2.0 + 0.5);
			ystart = (int) (tileHeight / 2.0 + 0.5);
			xstartCoarse = 0;
			ystartCoarse = 0;
		} else if (radiochoice.equals(radiobuttons[MANUAL])) {

			if (type.equals(types[HLINES])) {
				xstart = 0; // just to prevent an error
			}

			// check if both xstart and ystart are within proper ranges
			if (areaPerPoint != 0 && (xstart >= tileWidth || ystart >= tileHeight)) {
				if (xstart >= tileWidth) err +=  "\"xstart\" ";
				if (ystart >= tileHeight) err +=  "\"ystart\" ";
				err +=  "too large. \n";
			}

			// input for the Combined grids
			if (type.equals(types[COMBINED]) || type.equals(types[DOUBLE_LATTICE])) {

				// check if both xstartCoarse and ystartCoarse are within proper ranges
				if (xstartCoarse >= coarseGridX || ystartCoarse >= coarseGridY) {
					if (xstartCoarse >= coarseGridX) err +=  "\"xstartCoarse\" ";
					if (ystartCoarse >= coarseGridY) err +=  "\"ystartCoarse\" ";
					err +=  "too large.";
				}
			} 
		}
		
		// calculating number of vertical and horizontal lines in a selected image
		linesV = (int) ((width  - xstart) / tileWidth) + 1;
		linesH = (int) ((height - ystart) / tileHeight) + 1;
	}
	
	
	ShapeRoi getGridRoi() {
		GeneralPath path; 
		
		if (type.equals(types[LINES]))
			 path = drawLines();
		else if (type.equals(types[HLINES]))
			path = drawHorizontalLines();
		else if (type.equals(types[CROSSES]))
			path =  drawCrosses();
		else if (type.equals(types[POINTS]))
			path =  drawPoints();
		else if (type.equals(types[COMBINED]))
			path =  drawCombined();
		
		else if (type.equals(types[DOUBLE_LATTICE]))
			path =  drawDoubleLattice();
		
		else
			path =  null;
		
		ShapeRoi roi = new ShapeRoi(path);
		roi.setStrokeColor(getColor());
		return roi;
	}
	
	
	
	Color getColor() {
		Color c = Color.black;
		if (color.equals(colors[0]))
			c = Color.red;
		else if (color.equals(colors[1]))
			c = Color.green;
		else if (color.equals(colors[2]))
			c = Color.blue;
		else if (color.equals(colors[3]))
			c = Color.magenta;
		else if (color.equals(colors[4]))
			c = Color.cyan;
		else if (color.equals(colors[5]))
			c = Color.yellow;
		else if (color.equals(colors[6]))
			c = Color.orange;
		else if (color.equals(colors[7]))
			c = Color.black;
		else if (color.equals(colors[8]))
			c = Color.white;
		return c;
	}
	
	
	
	// output grid parameters
	void saveGridParameters(int sliceNumber){
		Integer xStartOutput = new Integer(xstart);
		Integer xStartCoarseOutput = new Integer(xstartCoarse);
		Integer yStartCoarseOutput = new Integer(ystartCoarse);
		String singleQuart = "'";

		if (type.equals(types[HLINES]))
			xStartOutput = null;

		if (!(type.equals(types[COMBINED]) || type.equals(types[DOUBLE_LATTICE]))) {
			xStartCoarseOutput = null;
			yStartCoarseOutput = null;
			singleQuart = "";
			gridRatio = null;
		}

		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		String sliceStr;
		
		if(sliceNumber == 0){
			sliceStr = "All";
			sliceNumber = 1; // to input parameters into gridParameterArray
		} else
			sliceStr = "" + sliceNumber;
		
		String gridParameters = df.format(date) + "\t" + imp.getTitle() + "\t" + 
				sliceStr + "\t" + type + "\t" + areaPerPoint + "\t" + units + "^2" +
				"\t" + singleQuart + gridRatio + "\t" + color + "\t" + radiochoice
				+ "\t" + xStartOutput + "\t" + ystart + "\t"
				+ xStartCoarseOutput + "\t" + yStartCoarseOutput;
		// singleQuart before gridRatio is to prevent conversion to date in
		// Excel.
		
		gridParameterArray[sliceNumber - 1] = gridParameters;
	}
	
	
	static void showHistory(String[] parameters) {
		String windowTitle = "Grid History";
			//ShowParameterWindow.java uses String "Grid History" without 
			//referring to this windowTitle variable,
			//so be careful to change the title this window. 
		String fileName = "CombinedGridsHistory.txt";
		
		TextWindow gridHistoryWindow = (TextWindow) WindowManager.getWindow(windowTitle);
		
		if (gridHistoryWindow == null) {
			//make a new empty TextWindow with String windowTitle with headings
			gridHistoryWindow = new TextWindow(
					windowTitle,
					"Date \t Image \t Slice \t Grid Type \t Area per Point \t Unit "
					+ "\t Ratio \t Color \t Location Setting "
					+ "\t xstart \t ystart \t xstartCoarse \t ystartCoarse",
					"", 1028, 250);
			
			//If a file whose name is String fileName exists in the plugin folder, 
			//read it into the list.
			try {
				BufferedReader br = new BufferedReader(
						new FileReader(IJ.getDirectory("plugins") + fileName)
						);
				boolean isHeadings = true;
				while (true) {
		            String s = br.readLine();
		            if (s == null) break;
		            if(isHeadings) {
		            	isHeadings = false;
		            	continue;
		            }
		            gridHistoryWindow.append(s);
				}
				br.close();
			} catch (IOException e) {}
		}
		
		if(parameters != null){
			for(String str : parameters)
				if(str != null)
					gridHistoryWindow.append(str);
			
			//auto save the parameters into a file whose name is String fileName
			TextPanel tp = gridHistoryWindow.getTextPanel();
			tp.saveAs(IJ.getDirectory("plugins") + fileName);	
		}
	}
	
	
	boolean gridSwitchExist(){
		Frame[] frames = Frame.getFrames();
		for (Frame frame : frames){
			if("Grid Switch".equals(frame.getTitle()) && frame.isVisible())
					return true;
		}
		return false;
	}
}
