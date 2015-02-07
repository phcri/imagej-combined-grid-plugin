package ca.phcri;


import java.io.File;
import java.util.ArrayList;

import ij.IJ;
import ij.gui.Roi;
import ij.io.SaveDialog;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class GridOutputXml {
	String[] xstartArray, ystartArray, xstartCoarseArray, ystartCoarseArray;
	String[] sliceNoArray;
	static String imageName;
	String savedDate;
	String type;
	String units;
	String gridRatio;
	String color;
	String areaPerPoint;
	String location;
	static String directory = IJ.getDirectory("plugins");
	DOMSource source;
	
	GridOutputXml(String[] parameterArray){
		ArrayList<String> parameterList = new ArrayList<String>();
		
		for(String str : parameterArray){
			if(str != null)
				parameterList.add(str);
		}
				
		int totalSlice = parameterList.size();
		sliceNoArray = new String[totalSlice];
		xstartArray = new String[totalSlice];
		ystartArray = new String[totalSlice];
		xstartCoarseArray = new String[totalSlice];
		ystartCoarseArray = new String[totalSlice];
		
		for(int i = 0; i < totalSlice; i++){
			String[] parameters = parameterArray[i].split("\t");
			savedDate = parameters[0];
			imageName = parameters[1];
			sliceNoArray[i] = parameters[2];
			type = parameters[3];
			areaPerPoint = parameters[4];
			units = parameters[5];
			gridRatio = parameters[6];
			color = parameters[7];
			location = parameters[8]; 
			xstartArray[i] = parameters[9];
			ystartArray[i] = parameters[10];
			xstartCoarseArray[i] = parameters[11];
			ystartCoarseArray[i] = parameters[12];
		}
				
		String prefixCellCounter = "Counter Window - ";
		if(imageName.startsWith(prefixCellCounter))
			imageName = imageName.substring(17);
		
		units = units.substring(0, units.indexOf("^"));
		
		if(!"null".equals(gridRatio))
			gridRatio = gridRatio.substring(1);
		
		
		try {
			Document doc = DocumentBuilderFactory
											.newInstance()
											.newDocumentBuilder()
											.newDocument();
			doc.setXmlStandalone(true);
			Element combinedgridEl = doc.createElement("CombinedGrids");
			doc.appendChild(combinedgridEl);
			
			Element gridEl = doc.createElement("grid");
			gridEl.setAttribute("date", savedDate);
			combinedgridEl.appendChild(gridEl);
			
			String[] elementName =
				{"image", "type", "app", "ratio", "unit", "color", "location"};
			String[] input =
				{imageName, type, areaPerPoint, gridRatio, units, color, location};
			
			for(int i = 0; i < elementName.length; i++){
				Element el = doc.createElement(elementName[i]);
				el.appendChild(doc.createTextNode(input[i]));
				gridEl.appendChild(el);
			}
			
			String[] startName = 
				{"xstart", "ystart", "xstartCoarse", "ystartCoarse"};
			String[][] startInput = 
				{xstartArray, ystartArray, xstartCoarseArray, ystartCoarseArray};
			
			for(int i = 0; i < totalSlice; i++){
				Element sliceEl = doc.createElement("slice");
				sliceEl.setAttribute("z", sliceNoArray[i]);
				gridEl.appendChild(sliceEl);
				
				for(int j = 0; j < 4; j++){
					Element el = doc.createElement(startName[j]);
					el.appendChild(doc.createTextNode(startInput[j][i]));
					sliceEl.appendChild(el);
				}
			}
			
			
			source = new DOMSource(doc);
			
		} catch (ParserConfigurationException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		} catch (TransformerFactoryConfigurationError exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
	}
	
	
	boolean save(){
		Transformer tf;
		try {
			tf = TransformerFactory.newInstance().newTransformer();
			
			SaveDialog sd = 
					new SaveDialog("Save parameters as XML file", 
							directory, 
							"grid_" + imageName + ".xml");
			
			directory = sd.getDirectory();
			String outputFileName = sd.getFileName();
			
			if(outputFileName == null)
				return false;
			
			StreamResult result = new StreamResult(new File(directory + outputFileName));
			tf.transform(source, result);
			
		} catch (TransformerConfigurationException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		} catch (TransformerFactoryConfigurationError exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		} catch (TransformerException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
		
		return true;
	}
	
}