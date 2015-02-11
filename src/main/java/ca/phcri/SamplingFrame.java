package ca.phcri;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.geom.GeneralPath;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SamplingFrame implements PlugIn, DialogListener {
	final static String[] colors = 
		{ "Red", "Green", "Blue", "Magenta", "Cyan", "Yellow", "Orange", 
		"Black", "White" };
	static String acceptanceLineColor = "Green";
	static String prohibitedLineColor = "Red";
	
	ImagePlus imp;
	double pixelWidth = 1.0, pixelHeight = 1.0;
	String units;
	String err = "";
	double marginLeft;
	double marginRight;
	double marginTop;
	double marginBottom;
	String acceptanceLineType;
	String[] lineTypes = {"Solid", "Dashed"};
	int SOLID = 0, DASHED = 1;
	int width;
	int height;
	
	static String historyWindowTitle = "Grid History";
	static String textfileName = "CombinedGridsHistory.txt";
	static DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	static Date date;
	GenericDialog sfd;
	
	
	@Override
	public void run(String arg) {
		
		if (IJ.versionLessThan("1.47"))
			return;
		SamplingFrame sf = new SamplingFrame();
		
	}
	
	
	
	SamplingFrame(){
		imp = IJ.getImage();
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
		
		sfd = new GenericDialog("Sampling Frame");
		sfd.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		sfd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		sfd.setInsets(5, 5, 0);
		sfd.addMessage("Margins");
		sfd.addNumericField("Left", marginLeft, places, 6, units);
		sfd.addNumericField("Right", marginRight, places, 6, units);
		sfd.addNumericField("Top", marginTop, places, 6, units);
		sfd.addNumericField("Bottom", marginBottom, places, 6, units);
		
		sfd.setInsets(10, 5, 0);
		sfd.addMessage("Prohibited Line");
		sfd.addChoice("Color:", colors, prohibitedLineColor);
		
		sfd.setInsets(10, 5, 0);
		sfd.addMessage("Acceptance Line");
		sfd.addChoice("Color:", colors, acceptanceLineColor);
		sfd.addChoice("Type:", lineTypes, acceptanceLineType);
		sfd.addDialogListener(this);
		sfd.setSize(200, 350);
	}
	
	
	// event control for the dialog box
	@Override
	public boolean dialogItemChanged(GenericDialog sfd, AWTEvent e) {
		marginLeft = sfd.getNextNumber();
		marginRight = sfd.getNextNumber();
		marginTop = sfd. getNextNumber();
		marginBottom = sfd.getNextNumber();
		prohibitedLineColor = sfd.getNextChoice();
		acceptanceLineColor = sfd.getNextChoice();
		acceptanceLineType = sfd.getNextChoice();
		
		err = "";
		IJ.showStatus(err);

		drawFrame(true);
		
		return true;
	}
	
	
	void removeSamplingFrame(){
		Overlay ol = imp.getOverlay();
		
		if(ol == null) return;
		
		Roi[] rois = ol.toArray();
		for(Roi roi : rois){
			if(roi != null && roi.getName() != null && 
					roi.getName().startsWith("samplingFrame"))
				ol.remove(roi);
		}
		
		imp.setOverlay(ol);
	}
	
	
	void showInputWindow(boolean frameOn){
		sfd.setVisible(frameOn);
		drawFrame(frameOn);
	}
	
	void dispose(){
		sfd.dispose();
	}
	
	void drawFrame(boolean frameOn) {
		removeSamplingFrame();
		
		if(frameOn){
			Overlay ol = imp.getOverlay();
			
			if(ol == null)
				ol = new Overlay();
			
			if(marginLeft < 0 || marginRight < 0 || marginTop < 0 || marginBottom < 0){
				err += "Parameter for Margins should be bositive";
				return;
			}
			
			GeneralPath forbiddenPath = new GeneralPath();
			forbiddenPath.moveTo(marginLeft, 0);
			forbiddenPath.lineTo(marginLeft, height - marginBottom);
			forbiddenPath.lineTo(width - marginRight, height - marginBottom);
			forbiddenPath.lineTo(width - marginRight, height);
			ShapeRoi prohibitedLine = new ShapeRoi(forbiddenPath);
			prohibitedLine.setStrokeColor(getColor(prohibitedLineColor));
			prohibitedLine.setName("samplingFrameProhibited");
			
			
			GeneralPath acceptancePath = new GeneralPath();
			
			if(lineTypes[DASHED].equals(acceptanceLineType)){
				int i = 0;
				int lineSegment = 8;
				int lineInterval = 14;
				while (lineInterval * i < (width - marginLeft - marginRight)){
					acceptancePath
						.moveTo(marginLeft + lineInterval * i, 
								marginTop);
					acceptancePath
						.lineTo(marginLeft + lineInterval * i + lineSegment, 
								marginTop);
					i++;
				}
				
				i = 0;
				while (lineInterval * i < (height - marginTop - marginBottom)){
					acceptancePath
						.moveTo(width - marginRight, 
								marginTop + lineInterval * i);
					acceptancePath
						.lineTo(width - marginRight,
								marginTop + lineInterval * i + lineSegment);
					i++;
				}
			} else {
				acceptancePath.moveTo(marginLeft, marginTop);
				acceptancePath.lineTo(width - marginRight, marginTop);
				acceptancePath.lineTo(width - marginRight, height - marginBottom);
			}
			
			ShapeRoi acceptanceLine = new ShapeRoi(acceptancePath);
			acceptanceLine.setStrokeColor(getColor(acceptanceLineColor));
			acceptanceLine.setName("samplingFrameAcceptance");
			
			
			
			ol.add(prohibitedLine);
			ol.add(acceptanceLine);
			
			imp.setOverlay(ol);
		}
		
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
	
}
