package ca.phcri;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.geom.GeneralPath;
import java.util.Date;


public class SamplingFrame extends CombinedGridsPlugin {
	GenericDialog sfgd;
	String frameName = "Sampling Frame";
	static boolean asIindividualFrame; 
	
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.47"))
			return;
		
		setAsIndividual(true);
		makeUnvisibleDialog();
		sfgd.showDialog();
		
		if(sfgd.wasCanceled()){
			drawSamplingFrame(false);
			return;
		}
		
		if(sfgd.wasOKed()){
			if(!"".equals(err)){
				IJ.error(err);
				drawSamplingFrame(false);
				return;
			}
			
			date = new Date();
			
			String[] gridParameterArray = {df.format(date) + "\t" + imp.getTitle() + "\t" + 
					"" + "\t" + "" + "\t" + "" + "\t" + units +
					"\t" + "" + "" + "\t" + "" + "\t" + ""
					+ "\t" + "" + "\t" + "" + "\t"
					+ "" + "\t" + "" + "\t"
					+ getParameters()};
			
			
			if(asIindividualFrame && saveXml) {
				GridOutputXml gox = 
						new GridOutputXml(gridParameterArray);
				boolean saved = gox.save();
				
				if(!saved){
					GenericDialog gdWantGrid = 
							new GenericDialog("Do you want a Sampling Frame?");
					gdWantGrid.addMessage("Do you want to overlay a Sampling Frame?");
					gdWantGrid.enableYesNoCancel();
					gdWantGrid.hideCancelButton();
					gdWantGrid.showDialog();
				
					if(!gdWantGrid.wasOKed()){
						removeSamplingFrame();
						return;
					}
					
				}
				
			}
			
			showHistory(gridParameterArray);
		}
		
	}
	
	
	
	void setAsIndividual(boolean indivudial){
		asIindividualFrame = indivudial;
	}
	
	void makeUnvisibleDialog(){
		imp = IJ.getImage();
		width = imp.getWidth();
		height = imp.getHeight();
		Calibration cal = imp.getCalibration();
		if (cal.scaled()) {
			units = cal.getUnits();
		} else {
			units = "pixels";
		}
				
		
		sfgd = new GenericDialog(frameName);
		if(!asIindividualFrame){
			sfgd.setUndecorated(true);
			sfgd.setInsets(0, 0, 0);
			sfgd.addMessage(frameName);
			sfgd.setInsets(10, 5, 0);
		} else{
			sfgd.setInsets(0, 5, 0);
		}
		
		sfgd.addMessage("Margins (pixels)");
		sfgd.addNumericField("Top", marginTop, 0);
		sfgd.addNumericField("Bottom", marginBottom, 0);
		sfgd.addNumericField("Left", marginLeft, 0);
		sfgd.addNumericField("Right", marginRight, 0);
		
		
		sfgd.setInsets(10, 5, 0);
		sfgd.addMessage("Prohibited Line");
		sfgd.addChoice("Color:", colors, prohibitedLineColor);
		
		sfgd.setInsets(10, 5, 0);
		sfgd.addMessage("Acceptance Line");
		sfgd.addChoice("Color:", colors, acceptanceLineColor);
		sfgd.addChoice("Type:", lineTypes, acceptanceLineType);
		
		if(asIindividualFrame)
			sfgd.addCheckbox("Save parameters as a xml file", saveXml);
		
		sfgd.addDialogListener(this);
		sfgd.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
		sfgd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		sfgd.pack();
		sfgd.setResizable(false);
		
	}
	
	void setParameters(int marginTop, int marginBottom, 
			int marginLeft, int marginRight,
			String prohibitedLineColor, String acceptanceLineColor, String acceptanceLinetype
			){
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		this.prohibitedLineColor = prohibitedLineColor;
		this.acceptanceLineColor = acceptanceLineColor;
		this.acceptanceLineColor = acceptanceLineColor;
		imp = IJ.getImage();
		width = imp.getWidth();
		height = imp.getHeight();
	}
	
	
	// event control for the dialog box
	@Override
	public boolean dialogItemChanged(GenericDialog sfgd, AWTEvent e) {
		marginTop = (int) sfgd. getNextNumber();
		marginBottom = (int) sfgd.getNextNumber();
		marginLeft = (int) sfgd.getNextNumber();
		marginRight = (int) sfgd.getNextNumber();
		
		prohibitedLineColor = sfgd.getNextChoice();
		acceptanceLineColor = sfgd.getNextChoice();
		acceptanceLineType = sfgd.getNextChoice();
		
		if(asIindividualFrame)
			saveXml = sfgd.getNextBoolean();
		
		err = "";
		
		drawSamplingFrame(true);
		IJ.showStatus(err);
		
		return true;
	}
	
	
	void showInputWindow(boolean frameOn){
		err = "";
		drawSamplingFrame(frameOn);
		sfgd.setVisible(frameOn);
	}
	
	
	
	void dispose(){
		sfgd.dispose();
	}
	static void removeSamplingFrame(){
		ImagePlus imp = IJ.getImage();
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
	
	
	void drawSamplingFrame(boolean frameOn) {
		removeSamplingFrame();
		
		if(frameOn){
			
			Overlay ol = imp.getOverlay();
			
			if(ol == null)
				ol = new Overlay();
			
			if(marginLeft < 0 || marginRight < 0 || marginTop < 0 || marginBottom < 0){
				err += "Parameter for Margins should not be negative\n";
				return;
			}
			
			if(marginLeft + marginRight >= width){
				err += "Left and/or Right margins are too large\n";
				return;
			}
			
			if(marginTop + marginBottom >= height){
				err += "Top and/or Bottom margins are too large\n";
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

	String getParameters() {
		String frameParameters = marginLeft + "\t" + marginRight + "\t"  + marginTop +
				"\t"  + marginBottom + "\t" + prohibitedLineColor  + "\t" +
				acceptanceLineColor  + "\t" + acceptanceLineType;
		return frameParameters;
	}

	@Override
	void setLocation(int x, int y) {
		sfgd.setLocation(x, y);
	}
	
	
}
