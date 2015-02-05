package ca.phcri;



import ij.*;
import ij.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import ij.plugin.*;
import ij.plugin.frame.PlugInFrame;


public class Grid_Switch implements PlugIn, ActionListener, WindowListener {
	private ImagePlus imp;
	private boolean gridOn = false;
	private Button b1;
	private static Overlay ol;
	private static Roi[] gridRois;
	
	@Override
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
	
	@Override
	public void windowActivated(WindowEvent e) {
		imp = WindowManager.getCurrentImage();
		if(imp != null){
			ol = imp.getOverlay();
			if(ol != null){
				Roi[] elements = ol.toArray();
				
				ArrayList<Roi> gridRoisList = new ArrayList<Roi>();
				
				for(Roi element : elements){
					if(element.getName() != null &&
							element.getName().startsWith("grid")){
						gridRoisList.add(element);
					}
				}
				
				gridRois = new Roi[gridRoisList.size()];
				gridRois = gridRoisList.toArray(gridRois);
			}
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowClosing(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {
		enableGrid();
		b1.setLabel("Grid On");
		gridOn = false;
	}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(gridOn) {
			enableGrid();
			b1.setLabel("Grid On");
		} else {
			removeGrid();
			b1.setLabel("Grid Off");
		}
		gridOn = !gridOn;
	}
	
	void enableGrid() {
		removeGrid();
		for(Roi gridRoi : gridRois)
			ol.add(gridRoi);
		imp.setOverlay(ol);
	}
	
	void removeGrid(){
		if(ol != null)
			for(Roi roi : gridRois)
				ol.remove(roi);
		imp.setOverlay(ol);
	}
	
}