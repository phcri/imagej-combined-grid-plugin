package ca.phcri;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.measure.Calibration;

import java.awt.AWTEvent;
import java.awt.Dialog;
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
				
		
		sfgd = new GenericDialog(frameName);
		if(!asIindividualFrame){
			sfgd.setUndecorated(true);
			sfgd.setInsets(0, 0, 0);
			sfgd.addMessage(frameName);
			sfgd.setInsets(10, 5, 0);
		} else{
			sfgd.setInsets(0, 5, 0);
		}
		
		sfgd.addMessage("Margins");
		sfgd.addNumericField("Top", marginTop, places, 6, units);
		sfgd.addNumericField("Bottom", marginBottom, places, 6, units);
		sfgd.addNumericField("Left", marginLeft, places, 6, units);
		sfgd.addNumericField("Right", marginRight, places, 6, units);
		
		
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
	
	
	// event control for the dialog box
	@Override
	public boolean dialogItemChanged(GenericDialog sfgd, AWTEvent e) {
		marginTop = sfgd. getNextNumber();
		marginBottom = sfgd.getNextNumber();
		marginLeft = sfgd.getNextNumber();
		marginRight = sfgd.getNextNumber();
		
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
