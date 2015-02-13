package ca.phcri;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.io.OpenDialog;
import ij.measure.Calibration;

public class GridFromXml extends SamplingFrame {
	int[] xstartArray, ystartArray, xstartCoarseArray, ystartCoarseArray, sliceNoArray;
	String imageName;
	
	String unitsXml;
	int totalGridNo;
	String filePath;
	boolean goBack = false;
	
	
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.47")) return;
		imageInformation();
		err = "";
		
		xmlFileOpen();
		if(filePath == null) return;
		if(!"".equals(err)){
			IJ.error(err);
			return;
		}
		
		xmlReader();
		
		if(!"".equals(err)){
			IJ.error(err);
			return;
		}
		
		imageCheck();
		if(goBack) return;
		
		drawSamplingFrame(samplingFrameOn);
		gridLayer();
		
		Grid_Switch gs = new Grid_Switch();
		gs.gridSwitch();

		showHistory(gridParameterArray);
	}
	
	void xmlReader(){
		//read in parameters as an Array
			
		try {
			//load common parameters
			DocumentBuilder builder = 
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			Document doc = builder.parse(new File(filePath));
			NodeList combinedgridNL = doc.getElementsByTagName("CombinedGrids");
			Element combinedgridEl = (Element) combinedgridNL.item(0);
			
			if(combinedgridEl == null){
				err += "This file does not have information for grids";
			}else{
				NodeList gridNL = combinedgridEl.getElementsByTagName("grid");
				Element gridEl = (Element) gridNL.item(0);
				
				if(gridEl != null){
					gridEl.getAttribute("date");
					imageName = getElementValueAsStr(gridEl, "image", 0);
					type = getElementValueAsStr(gridEl, "type", 0);
					areaPerPoint = getElementValueAsDouble(gridEl,  "app", 0);
					gridRatio = getElementValueAsStr(gridEl, "ratio", 0);
					unitsXml = getElementValueAsStr(gridEl, "unit", 0);
					color = getElementValueAsStr(gridEl, "color", 0);
					
					
					NodeList sliceNL = gridEl.getElementsByTagName("slice");
					totalGridNo = sliceNL.getLength();
					
					sliceNoArray = new int[totalGridNo];
					xstartArray = new int[totalGridNo];
					ystartArray = new int[totalGridNo];
					xstartCoarseArray = new int[totalGridNo];
					ystartCoarseArray = new int[totalGridNo];
					
					for(int i = 0; i < sliceNL.getLength(); i++){
						Element sliceNode = (Element) sliceNL.item(i);
						String sliceNo = sliceNode.getAttribute("z");
						if(!"All".equals(sliceNo))
							sliceNoArray[i] = Integer.parseInt(sliceNo);
						xstartArray[i] = 
								getElementValueAsInteger(sliceNode, "xstart", 0);
						ystartArray[i] = 
								getElementValueAsInteger(sliceNode, "ystart", 0);
						xstartCoarseArray[i] = 
								getElementValueAsInteger(sliceNode, "xstartCoarse", 0);
						ystartCoarseArray[i] = 
								getElementValueAsInteger(sliceNode, "ystartCoarse", 0);
					}
				}
				
				
				NodeList samplingFrameNL = 
						combinedgridEl.getElementsByTagName("samplingFrame");
				Element samplingFrameEl = 
						(Element)  samplingFrameNL.item(0);
				
				if(samplingFrameEl == null){
					samplingFrameOn = false;
					
				} else{
					samplingFrameOn = true;
					marginLeft = 
							getElementValueAsDouble(samplingFrameEl, "left", 0);
					marginRight = 
							getElementValueAsDouble(samplingFrameEl, "right", 0);
					marginTop = 
							getElementValueAsDouble(samplingFrameEl, "top", 0);
					marginBottom = 
							getElementValueAsDouble(samplingFrameEl, "bottom", 0);
					prohibitedLineColor = 
							getElementValueAsStr(samplingFrameEl, "prohibitedColor", 0);
					acceptanceLineColor = 
							getElementValueAsStr(samplingFrameEl, "acceptanceColor", 0);
					acceptanceLineType = 
							getElementValueAsStr(samplingFrameEl, "acceptanceType", 0);
				}
				
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	String getElementValueAsStr(Element e, String tag, int index){
		return e.getElementsByTagName(tag).item(index).getFirstChild().getNodeValue();
	}
	
	
	int getElementValueAsInteger(Element e, String tag, int index){
		String str = getElementValueAsStr(e, tag, index);
		if("null".equals(str) || "".equals(str))
			return 0;
		return Integer.parseInt(str);
	}
	
	double getElementValueAsDouble(Element e, String tag, int index){
		return Double.parseDouble(getElementValueAsStr(e, tag, index));
	}
	
	
	void gridLayer(){		
		gridRoiArray = new Roi[imp.getStackSize()];
		gridParameterArray = new String[imp.getStackSize()];
		
		setCoarseGrids();
		calculateTile();
		
		locationChoice = "Manual Input";
		
		for(int i = 0; i < totalGridNo; i++){
			xstart = xstartArray[i];
			ystart = ystartArray[i];
			xstartCoarse = xstartCoarseArray[i];
			ystartCoarse = ystartCoarseArray[i];
			int sliceNo = sliceNoArray[i];
			
			calculateNLines();
			
			ShapeRoi gridRoi = getGridRoi();
			addGridOnArray(gridRoi, sliceNo);
			saveGridParameters(sliceNo);
		}
		
		showGrid(gridRoiArray);
	}
	
	void xmlFileOpen(){
		OpenDialog.setDefaultDirectory(IJ.getDirectory("plugins"));
		OpenDialog od = new OpenDialog("Select XML file containing grid data");
		filePath = od.getPath();
		
		if(filePath == null)
			return;
		
		if(!filePath.endsWith("xml"))
			err += "This is not an XML file";
	}
	
	
	void imageInformation(){
		imp = IJ.getImage();
		width = imp.getWidth();
		height = imp.getHeight();
		Calibration cal = imp.getCalibration();
		units = cal.getUnits();
		pixelWidth = cal.pixelWidth;
		pixelHeight = cal.pixelHeight;
	}
	
	void imageCheck(){
		if(!imp.getTitle().equals(imageName) &&
				!imp.getTitle().equals("Counter Window - " + imageName))
			err += "The image name does not match with the current image\n";
				
		if(!units.equals(unitsXml))
			err += "units of the image does not match with "
					+ "the units of the current image\n";
		
		if(!"".equals(err)){
			GenericDialog wd = new GenericDialog("Warning");
			wd.addMessage(err);
			wd.addMessage("Do you want to put grids anyway?");
			wd.showDialog();
			if(wd.wasOKed())
				goBack = false;
			else
				goBack = true;
		} else
			goBack = false;
	}
	
}