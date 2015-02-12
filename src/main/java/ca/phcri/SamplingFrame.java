package ca.phcri;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.measure.Calibration;

import java.awt.AWTEvent;
import java.awt.Dialog;


public class SamplingFrame extends CombinedGridsPlugin {
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
		
		sfd.setInsets(0, 5, 0);
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
		sfd.pack();
		sfd.setResizable(false);
		sfd.removeWindowListener(sfd);
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

		drawSamplingFrame(true);
		
		return true;
	}
	
	
	void showInputWindow(boolean frameOn){
		drawSamplingFrame(frameOn);
		sfd.setVisible(frameOn);
	}
	
	
	
	void dispose(){
		sfd.dispose();
	}
	
	boolean isVisible(){
		return sfd.isVisible();
	}

	String getParameters() {
		String frameParameters = marginLeft + "\t" + marginRight + "\t"  + marginTop +
				"\t"  + marginBottom + "\t" + prohibitedLineColor  + "\t" +
				acceptanceLineColor  + "\t" + acceptanceLineType;
		return frameParameters;
	}
	
	
}
