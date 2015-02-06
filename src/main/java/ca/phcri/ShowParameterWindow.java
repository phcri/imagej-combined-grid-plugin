package ca.phcri;

import java.awt.Frame;

import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.text.TextWindow;

public class ShowParameterWindow implements PlugIn {
	@Override
	public void run(String arg) {
		CombinedGridsPlugin.showHistory(null);
		
		TextWindow gridHistoryWindow = 
				(TextWindow) WindowManager.getWindow(
						CombinedGridsPlugin.historyWindowTitle);
		gridHistoryWindow.setState(Frame.NORMAL);
		gridHistoryWindow.toFront();
	}
}