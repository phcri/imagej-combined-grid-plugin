package ca.phcri;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.measure.Calibration;

import java.awt.AWTEvent;
import java.awt.Dialog;


public class SamplingFrame extends CombinedGridsPlugin {
	GenericDialog sfgd;
	String frameName = "Sampling Frame";
	
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.47"))
			return;
		new SamplingFrame();
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
			
			String[] gridParameterArray = {df.format(date) + "\t" + "" + "\t" + 
					"" + "\t" + "" + "\t" + "" + "\t" + units + "^2" +
					"\t" + "" + "" + "\t" + "" + "\t" + ""
					+ "\t" + "" + "\t" + "" + "\t"
					+ "" + "\t" + "" + "\t"
					+ getParameters()};
			showHistory(gridParameterArray);
		}
		
	}
	
	SamplingFrame(){
		this(true);
	}
	
	SamplingFrame(boolean decorated){
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
		if(!decorated){
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
		
		err = "";
		
		drawSamplingFrame(true);
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
