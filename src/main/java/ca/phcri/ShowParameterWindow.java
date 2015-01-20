package ca.phcri;

import ij.plugin.PlugIn;

public class ShowParameterWindow implements PlugIn {
	public void run(String arg) {
		CombinedGridsPlugin.showHistory(null);
	}
}