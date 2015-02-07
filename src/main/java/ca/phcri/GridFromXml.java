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

public class GridFromXml extends CombinedGridsPlugin {
	int[] xstartArray, ystartArray, xstartCoarseArray, ystartCoarseArray, sliceNoArray;
	private String imageName, gridDate;
	private Element gridNode;
	
	private String unitsXml;
	private int totalGridNo;
	String filePath;
	private boolean goBack;
	
	
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.47")) return;
		imageInformation();
		
		xmlFileOpen();
		if(filePath == null) return;
		xmlReader();
		imageCheck();
		if(goBack) return;
		gridLayer();
		
		Grid_Switch gs = new Grid_Switch();
		gs.gridSwitch();
		showHistory(null);
	}
	
	void xmlReader(){
		//read in parameters as an Array
			
		try {
			//load common parameters
			DocumentBuilder builder = DocumentBuilderFactory
											.newInstance()
											.newDocumentBuilder();
			
			Document doc = builder.parse(new File(filePath));
			NodeList combinedgridNL = doc.getElementsByTagName("CombinedGrid");
			
			if(combinedgridNL != null){
				NodeList gridNL = doc.getElementsByTagName("grid");
				gridNode = (Element) gridNL.item(0);
				
				gridDate = gridNode.getAttribute("date");
				imageName = getElementValueAsStr(gridNode, "image", 0);
				type = getElementValueAsStr(gridNode, "type", 0);
				areaPerPoint = getElementValueAsInteger(gridNode,  "app", 0);
				unitsXml = getElementValueAsStr(gridNode, "units", 0);
				gridRatio = getElementValueAsStr(gridNode, "ratio", 0);
				
				NodeList sliceNL = gridNode.getElementsByTagName("slice");
				totalGridNo = sliceNL.getLength();
				
				sliceNoArray = new int[totalGridNo];
				xstartArray = new int[totalGridNo];
				ystartArray = new int[totalGridNo];
				xstartCoarseArray = new int[totalGridNo];
				ystartCoarseArray = new int[totalGridNo];
				
				for(int i = 0; i < sliceNL.getLength(); i++){
					Element sliceNode = (Element) sliceNL.item(i);
					String sliceNo = sliceNode.getAttribute("name");
					if(!"All".equals(sliceNo))
						sliceNoArray[i] = Integer.parseInt(sliceNo);
					xstartArray[i] = getElementValueAsInteger(sliceNode, "xstart", 0);
					ystartArray[i] = getElementValueAsInteger(sliceNode, "ystart", 0);
					xstartCoarseArray[i] = 
							getElementValueAsInteger(sliceNode, "xstartCoarse", 0);
					ystartCoarseArray[i] = 
							getElementValueAsInteger(sliceNode, "ystartCoarse", 0);
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
		return Integer.parseInt(getElementValueAsStr(e, tag, index));
	}
	
	
	void gridLayer(){		
		gridRoiArray = new Roi[totalGridNo];
		
		setCoarseGrids();
		calculateTile();
		
		for(int i = 0; i < totalGridNo; i++){
			xstart = xstartArray[i];
			ystart = ystartArray[i];
			xstartCoarse = xstartCoarseArray[i];
			ystartCoarse = ystartCoarseArray[i];
			int sliceNo = sliceNoArray[i];
			
			calculateNLines();
			
			ShapeRoi gridRoi = getGridRoi();
			addGridOnArray(gridRoi, sliceNo);
		}
		
		showGrid(gridRoiArray);
	}
	
	void xmlFileOpen(){
		OpenDialog.setDefaultDirectory(IJ.getDirectory("plugins"));
		OpenDialog od = new OpenDialog("Select XML file containing grid data");
		filePath = od.getPath();
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
		if(!imp.getTitle().equals(imageName))
			err += "The image name does not match with the current image\n";
		if(!units.equals(unitsXml))
			err += "units of the image does not match with "
					+ "the units of the current image\n";
		
		if(!"".equals(err)){
			GenericDialog wd = new GenericDialog("Warning");
			wd.addMessage(err);
			wd.addMessage("Do you want to continue?");
			if(wd.wasOKed())
				goBack = false;
			else
				goBack = true;
		}
	}
	
}