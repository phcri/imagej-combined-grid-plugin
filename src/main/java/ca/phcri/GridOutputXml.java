package ca.phcri;


import java.io.File;

import ij.IJ;

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
	String[] xstartArray, ystartArray, xstartCoarseArray, ystartCoarseArray, sliceNoArray;
	String imageName, date, type, units, gridRatio, color, areaPerPoint, location;
	
	GridOutputXml(String date, String[] parameterArray){
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
				el.setNodeValue(input[i]);
				gridEl.appendChild(el);
			}
			
			String[] startName = {"xstart", "ystart", "xstartCoarse", "ystartCoarse"};
			String[][] startInput 
			= {xstartArray, ystartArray, xstartCoarseArray, ystartCoarseArray};
			
			for(int i = 0; i < sliceNoArray.length; i++){
				Element sliceEl = doc.createElement("slice");
				sliceEl.setAttribute("slice", sliceNoArray[i]);
				doc.appendChild(sliceEl);
				
				for(int j = 0; i < 4; i++){
					Element el = doc.createElement(startName[j]);
					el.setNodeValue(startInput[j][i]);
					sliceEl.appendChild(el);
				}
			}
			
			
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(doc);
			String outputFile = "grid_" +imageName + "_" + date + ".xml";
			StreamResult result = 
					new StreamResult(new File(IJ.getDirectory("plugins") + outputFile));
			
			tf.transform(source, result);
			
		} catch (ParserConfigurationException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
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
	}
	
}