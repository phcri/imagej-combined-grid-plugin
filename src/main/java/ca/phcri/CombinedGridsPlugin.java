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
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class CombinedGridsPlugin implements PlugIn, DialogListener, ComponentListener {
	final static String[] colors = 
		{ "Red", "Green", "Blue", "Magenta", "Cyan", "Yellow", "Orange", 
		"Black", "White" };
	static String color = "Blue";
	static String acceptanceLineColor = "Green";
	static String prohibitedLineColor = "Red";
	
	final static int COMBINED = 0, DOUBLE_LATTICE = 1, LINES = 2,
			HLINES = 3, CROSSES = 4, POINTS = 5;
	final static String[] types = 
		{ "Combined Point", "Double Lattice", "Lines", "Horizontal Lines", 
		"Crosses", "Points" };
	
	static String type = types[COMBINED];
	static double areaPerPoint;

	final static int ONE_TO_FOUR = 0, ONE_TO_NINE = 1, ONE_TO_SIXTEEN = 2, 
			ONE_TO_TWENTYFIVE = 3, ONE_TO_THIRTYSIX = 4, ONE_TO_FORTY_NINE = 5,
			ONE_TO_HUNDRED = 6, ONE_TO_ONENINETYSIX = 7;
	final static String[] ratioChoices = 
		{ "1:4", "1:9", "1:16", "1:25", "1:36", "1:49", "1:100", "1:196" };
	static String gridRatio = ratioChoices[ONE_TO_FOUR];
	final String[] radiobuttons = 
		{ "Random Offset", "Fixed Position", "Manual Input" };
	final int RANDOM = 0, FIXED = 1, MANUAL = 2;
	String locationChoice = radiobuttons[RANDOM];
	final static String[] applyChoices = 
		{ "One Grid for the Current Slice", "One Grid for All Slices", 
		"Different Grids for Each Slice", "Systematically Randomly", };
	final static int CURRENT = 0, ONEforALL = 1, DIFFERENTforEACH = 2, SYSTEMATIC = 3;
	static String applyTo = applyChoices[DIFFERENTforEACH];
	
	Component[] components; 
	// this is to select components in the dialog box
	private final static int[] ratioField = { 4, 5 };
	private final static int[] combinedGridFields = { 14, 15, 16, 17 };
	private final static int[] parameterFieldsOff = { 10, 11, 12, 13, 14, 15, 16, 17 };
	private final static int[] xstartField = { 10, 11 };
	private final static int[] ystartField = { 12, 13 };
	private final static int[] intervalField = { 21 };
	static boolean showGridSwitch = true;
	static String gridHistoryHeadings = 
			"Date \t Image \t Slice \t Grid Type \t Area per Point \t Units "
					+ "\t Ratio \t Color \t Location Setting "
					+ "\t xstart \t ystart \t xstartCoarse \t ystartCoarse "
					+ "\t Left Margin \t Right Margin \t Top Margin \t Bottom Margin "
					+ "\t prohibitedLineColor \t acceptanceLineColor"
					+ " \t acceptanceLineType";

	Random random = new Random(System.currentTimeMillis());
	ImagePlus imp;
	double tileWidth, tileHeight;
	int width, height;
	int xstart, ystart;
	int xstartCoarse, ystartCoarse, coarseGridX, coarseGridY;
	int linesV, linesH;
	double pixelWidth = 1.0, pixelHeight = 1.0;
	String units;
	String err = "";
	Roi[] gridRoiArray;
	String[] gridParameterArray;
	int totalSlices;
	static boolean saveXml = true;
	int interval = 2;
	boolean samplingFrameOn = false;
	static int marginLeft, marginRight, marginTop, marginBottom;
	String[] lineTypes = {"Solid", "Dashed"};
	int SOLID = 0, DASHED = 1;
	String acceptanceLineType = lineTypes[SOLID];
	SamplingFrame sfd;
	Checkbox samplingFrameCheckbox;
	GenericDialog gd;
	
	final static String historyWindowTitle = "Grid History";
	final static String textfileName = "CombinedGridsHistory.txt";
	DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date;

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
			imp.setOverlay(ol);
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
						path.lineTo(centerX + rad * circleX[i], 
								centerY - rad * circleY[i]);
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
		
		sfd = new SamplingFrame();
		sfd.setAsIndividual(false);
		sfd.makeUnvisibleDialog();
		
		
		
		
		
		
		// get values in a dialog box
		gd = new GenericDialog("Grid...");
		gd.addChoice("Grid Type:", types, type);
		gd.addNumericField("Area per Point:", areaPerPoint, places, 6, units + "^2");
		gd.addChoice("Ratio:", ratioChoices, gridRatio);
		gd.addChoice("Color:", colors, color);
		gd.addRadioButtonGroup("Grid Location", radiobuttons, 3, 1, locationChoice);
		gd.addNumericField("xstart:", 0, 0);
		gd.addNumericField("ystart:", 0, 0);
		gd.addNumericField("xstartCoarse:", 0, 0);
		gd.addNumericField("ystartCoarse:", 0, 0);
		
		if(totalSlices > 1){
			gd.addRadioButtonGroup("The way to apply grid(s) to a Stack",
					applyChoices, 4, 1, applyTo);
			gd.addNumericField("on every", interval, 0, 6, " slices");
		}
		
		gd.addCheckbox("Put sampling frame on the image", samplingFrameOn);
		gd.addCheckbox("Save parameters as an xml file", saveXml);
		gd.addCheckbox("Show a Grid Switch if none exists", showGridSwitch);
		
		// to switch enable/disable for parameter input boxes
		components = gd.getComponents();
		enableFields();
		
		samplingFrameCheckbox = (Checkbox) gd.getCheckboxes().firstElement();	
		
		gd.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
		gd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		gd.addDialogListener(this);
		gd.addComponentListener(this);
		gd.setResizable(false);
		
		
		sfd.sfgd.removeKeyListener(sfd.sfgd);
		sfd.sfgd.addKeyListener(gd);
		
		for(Component c : sfd.sfgd.getComponents()){
			c.removeKeyListener(sfd.sfgd);
			c.addKeyListener(gd);
		}
		
		gd.showDialog();
		
		if (gd.wasCanceled()){
			sfd.dispose();
			SamplingFrame.removeSamplingFrame();
			
			showGrid(null);
		}
			
		
		
		if (gd.wasOKed()) {
			sfd.dispose();
			if (!"".equals(err)) { 	
				IJ.error("Grid", err);
				
				SamplingFrame.removeSamplingFrame();
				showGrid(null);
			} else {
				
				
				if(saveXml) {
					GridOutputXml gox = 
							new GridOutputXml(gridParameterArray);
					boolean saved = gox.save();
					
					if(!saved){
						GenericDialog gdWantGrid = 
								new GenericDialog("Do you want a grid?");
						gdWantGrid.addMessage("Do you want to overlay a grid?");
						gdWantGrid.enableYesNoCancel();
						gdWantGrid.hideCancelButton();
						gdWantGrid.showDialog();
					
						if(!gdWantGrid.wasOKed()){
							showGrid(null);
							SamplingFrame.removeSamplingFrame();
							return;
						}
					}
				}
				
				showHistory(gridParameterArray);
				
				if (showGridSwitch && !gridSwitchExist()){
						Grid_Switch gs = new Grid_Switch();
						gs.gridSwitch();
				}
				
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
		locationChoice = gd.getNextRadioButton();
		xstart = (int) gd.getNextNumber();
		ystart = (int) gd.getNextNumber();
		xstartCoarse = (int) gd.getNextNumber();
		ystartCoarse = (int) gd.getNextNumber();

		if(totalSlices > 1){
			applyTo = gd.getNextRadioButton();
			interval = (int) gd.getNextNumber();
		}
		
		samplingFrameOn = gd.getNextBoolean();
		saveXml = gd.getNextBoolean();
		showGridSwitch = gd.getNextBoolean();
		
		err = "";
		IJ.showStatus(err);
		
		
		if(e != null && e.getSource().equals(samplingFrameCheckbox)){
			sfd.showInputWindow(samplingFrameOn);
			return true;
		//when samplingFrame is disposed, actionPerformed is triggered 
		//by samplingFrameCheckbox
		}
		
		
		
		gridParameterArray = new String[totalSlices];
		gridRoiArray = new Roi[totalSlices];
				
		minAreaCheck();
		enableFields();
		setCoarseGrids();
		calculateTile();
		
		err += sfd.err;
		
		
		if(applyChoices[DIFFERENTforEACH].equals(applyTo)){
			for (int i = 1; i <= totalSlices; i++){
				calculateFirstGrid();
				calculateNLines();
				
				if (!"".equals(err) || gd.invalidNumber()) {
					IJ.showStatus(err);
					return true;
				}
				
				ShapeRoi gridRoi = getGridRoi();
				addGridOnArray(gridRoi, i);
				saveGridParameters(i);
			}
			
		} else if(applyChoices[SYSTEMATIC].equals(applyTo)){
			if(interval > totalSlices){
				err += "Interval for systematic random sampling \n"
						+ "should not be greater than the stack size";
				IJ.showStatus(err);
				return true;
			}
			
			if(interval < 1){
				err += "Interval for systematic random sampling \n"
						+ "should be a positive integer";
				IJ.showStatus(err);
				return true;
			}
			
			int startSlice = random.nextInt(interval) + 1;
			
			for(int i = startSlice; i <= totalSlices; i += interval){
				calculateFirstGrid();
				calculateNLines();
				
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
			calculateNLines();
		
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
		
		
		if (locationChoice.equals(radiobuttons[MANUAL])) {
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
		
		if(totalSlices > 1){
			if (applyChoices[SYSTEMATIC].equals(applyTo))
				fieldEnabler(intervalField, true);
			else
				fieldEnabler(intervalField, false);
		}
		
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
		} else if (gridRatio.equals(ratioChoices[ONE_TO_FORTY_NINE])) {
			coarseGridX = 7;
			coarseGridY = 7;
		} else if (gridRatio.equals(ratioChoices[ONE_TO_HUNDRED])) {
			coarseGridX = 10;
			coarseGridY = 10;
		} else if (gridRatio.equals(ratioChoices[ONE_TO_ONENINETYSIX])) {
			coarseGridX = 14;
			coarseGridY = 14;
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
		if (locationChoice.equals(radiobuttons[RANDOM])) {
			xstart = (int) (random.nextDouble() * tileWidth);
			ystart = (int) (random.nextDouble() * tileHeight);
					// 0 <= random.nextDouble() < 1
			xstartCoarse = random.nextInt(coarseGridX);
			ystartCoarse = random.nextInt(coarseGridY);
		} else if (locationChoice.equals(radiobuttons[FIXED])) {
			xstart = (int) (tileWidth / 2.0 + 0.5);
			ystart = (int) (tileHeight / 2.0 + 0.5);
			xstartCoarse = 0;
			ystartCoarse = 0;
		} else if (locationChoice.equals(radiobuttons[MANUAL])) {

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
	}
	
	void calculateNLines() {	
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
		roi.setStrokeColor(getColor(color));
		return roi;
	}
	
	
	
	Color getColor(String requestedColor) {
		Color c = Color.black;
		if (requestedColor.equals(colors[0]))
			c = Color.red;
		else if (requestedColor.equals(colors[1]))
			c = Color.green;
		else if (requestedColor.equals(colors[2]))
			c = Color.blue;
		else if (requestedColor.equals(colors[3]))
			c = Color.magenta;
		else if (requestedColor.equals(colors[4]))
			c = Color.cyan;
		else if (requestedColor.equals(colors[5]))
			c = Color.yellow;
		else if (requestedColor.equals(colors[6]))
			c = Color.orange;
		else if (requestedColor.equals(colors[7]))
			c = Color.black;
		else if (requestedColor.equals(colors[8]))
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
		
		String sliceStr;
		int index;
		
		if(sliceNumber == 0){
			sliceStr = "All";
			index = 0; // to input parameters into gridParameterArray
		} else{
			sliceStr = "" + sliceNumber;
			index = sliceNumber - 1;
		}
		
		String frameParameters;
		
		if(samplingFrameOn)
			frameParameters = sfd.getParameters();
		else
		frameParameters = "" + "\t" + "" + "\t"  + "" + "\t" + "" + "\t" + "" + "\t" + "" + "\t" + "";
		
		date = new Date();
		
		String gridParameters = df.format(date) + "\t" + imp.getTitle() + "\t" + 
				sliceStr + "\t" + type + "\t" + areaPerPoint + "\t" + units + "^2" +
				"\t" + singleQuart + gridRatio + "\t" + color + "\t" + locationChoice
				+ "\t" + xStartOutput + "\t" + ystart + "\t"
				+ xStartCoarseOutput + "\t" + yStartCoarseOutput + "\t"
				+ frameParameters;
		// singleQuart before gridRatio is to prevent conversion to date in
		// Excel.
		
		gridParameterArray[index] = gridParameters;
	}
	
	
	
	static void showHistory(String[] parameterArray) {
		
		TextWindow gridHistoryWindow = 
				(TextWindow) WindowManager.getWindow(historyWindowTitle);
		
		if (gridHistoryWindow == null) {
			//make a new empty TextWindow with String historyWindowTitle with headings
			gridHistoryWindow = new TextWindow(
					historyWindowTitle,
					gridHistoryHeadings ,
					"", 1028, 250);
			
			//If a file whose name is String textfileName exists in the plugin folder, 
			//read it into the list.
			try {
				BufferedReader br = new BufferedReader(
						new FileReader(IJ.getDirectory("plugins") + textfileName)
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
		
		if(parameterArray != null){
			 for(String str : parameterArray)
				if(str != null)
					gridHistoryWindow.append(str);
			
			//auto save the parameters into a file whose name is String textfileName
			TextPanel tp = gridHistoryWindow.getTextPanel();
			tp.saveAs(IJ.getDirectory("plugins") + textfileName);	
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
	
	
	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		int parentX = gd.getLocation().x;
		int parentY = gd.getLocation().y;
		int parentWidth = gd.getWidth();
		int parentHeight = gd.getHeight();
		int childHeight = sfd.sfgd.getHeight();
		
		sfd.setLocation(parentX + parentWidth, 
				parentY + parentHeight - childHeight - 36);
	}
	
	
	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
}
