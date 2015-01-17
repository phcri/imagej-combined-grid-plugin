package ca.phcri;



import ij.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.*;
import ij.plugin.frame.PlugInFrame;


public class Grid_Switch implements PlugIn, ActionListener, WindowListener {
	private ImagePlus imp;
	private boolean gridOn = false;
	private Button b1;
	private static Overlay layer;
	private static Roi gridRoi;
	
	public void run(String arg) {
		if (IJ.versionLessThan("1.47"))	 		return;
		gridSwitch();
	}
	
	void gridSwitch(){
		PlugInFrame gs = new PlugInFrame("Grid Switch");
		gs.setSize(200, 100);
		gs.addWindowListener(this);
		b1 = new Button("Grid On");
		b1.addActionListener(this);
		gs.add(b1);

		gs.setVisible(true);
	}
	
	public void windowActivated(WindowEvent e) {
		imp = WindowManager.getCurrentImage();
		if(imp != null){
			layer = imp.getOverlay();
			if(layer == null || layer.getIndex("grid") == -1) IJ.showStatus("Grid Switch \"" + imp.getTitle() + "\" does not have a grid.");
			else gridRoi = layer.get(layer.getIndex("grid"));
		}
	}
	public void windowClosed(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {
		enableGrid();
		b1.setLabel("Grid On");
		gridOn = false;
	}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
	public void actionPerformed(ActionEvent e) {
		if(gridOn) {
			enableGrid();
			b1.setLabel("Grid On");
		} else {
			layer.remove(layer.getIndex("grid"));
			imp.setOverlay(layer);
			b1.setLabel("Grid Off");
		}
		gridOn = !gridOn;
	}
	
	void enableGrid() {
		if(layer.getIndex("grid") != -1) layer.remove(layer.getIndex("grid"));
		layer.add(gridRoi);
		imp.setOverlay(layer);
	}
}