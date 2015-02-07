package ca.phcri;


import java.io.File;

import ij.IJ;
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
	static String date;
	String type;
	String units;
	String gridRatio;
	String color;
	String areaPerPoint;
	String location;
	static String directory = IJ.getDirectory("plugins");
	DOMSource source;
	
	GridOutputXml(String date, String[] parameterArray){
		GridOutputXml.date = date;
		
		int totalSlice = parameterArray.length;
		sliceNoArray = new String[totalSlice];
		xstartArray = new String[totalSlice];
		ystartArray = new String[totalSlice];
		xstartCoarseArray = new String[totalSlice];
		ystartCoarseArray = new String[totalSlice];
		
		for(int i = 0; i < parameterArray.length; i++){
			String[] parameters = parameterArray[i].split("\t");
			imageName = parameters[0];
			sliceNoArray[i] = parameters[1];
			type = parameters[2];
			areaPerPoint = parameters[3];
			units = parameters[4];
			gridRatio = parameters[5];
			color = parameters[6];
			location = parameters[7]; 
			xstartArray[i] = parameters[8];
			ystartArray[i] = parameters[9];
			xstartCoarseArray[i] = parameters[10];
			ystartCoarseArray[i] = parameters[11];
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
			gridEl.setAttribute("date", date);
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