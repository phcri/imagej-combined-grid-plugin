# imagej-combined-grids-plugin
Combined Grids Plugin for ImageJ

The Combined Grids Plugin is based on the Grid Plugin (http://rsb.info.nih.gov/ij/plugins/grid.html) originally written by Dr. Wayne Rasband and modified with his permission. 

Authors; Daisuke Kinose[1] and Adrian Png[2]
[1] Dr. James Hogg's Lab, the Centre for Heart Lung Innovation in St. Paul's Hospital, University of British Columbia, Canada
[2] IT department, the Centre for Heart Lung Innovation in St. Paul's Hospital, University of British Columbia, Canada


Released on January 27th, 2015 



Implemented Functions:
1.Combined, or multi-purpose, point grid with several fine to coarse grid ratios
2.Double lattice square grid with several fine to coarse grid ratios
3.Output and autosave of grid parameters
4.Manual specification of grid position
5.Overlay/remove a grid without destroying the other Overlay elements
6.Grid Switch to temporary hide an overlaid grid




Installation and Usage:
1.Put "CombinedGridPlugin.jar" into the plugins folder of ImageJ (or Fiji) and restart the software. Please note that this does NOT overwrite or replace the existing Grid plugin installed with ImageJ.
2.Open an image in ImageJ.
3.Choose "Plugins" > "Grids" > "Combined Grids" in ImageJ.
4.Specify your grid settings in the dialog box and click OK.




Notes for the Combined Grids Plugin and Grid History:
•For the Combined Point Grid and the Double Lattice Square grid, "Area per Point" determines density of the fine grid.
•Grid location by "Fixed Position" in this plugin is same as grid location with "random-offset" unchecked in the original "Grid" plugin. For "Combined Point" and "Double Lattice" grids, the first point of the coarse grid is placed on the first point for the fine grid at the left upper corner.
•When you overlay a new grid, the parameters for the grid are automatically added to the history of previousely overlaid grids. This list is automatically saved as "CombinedGridsHistory.txt" in the plugin folder.
•If you want to delete grid information from the list of "Grid History",
 1. select lines you want to delete in the "Grid History" window,
 2. choose "cut" or "clear" in the right click menue or in the "Edit" tab
 3. choose "save as..." in the "File" tab and overwrite "CombinedGridsHistory.txt" in the plugins folder of ImageJ.
 You can completely clean the list by removing "CombinedGridsHistory.txt" from the folder. This plugin will create a new one. 


Notes for the Grid Switch:
•The Grid Switch specifically hides/redraws a grid overlaid by the "Grid with Combined Grids" plugin. This switch is not compatible with the original Grid Plugin
•When it redraws a grid, the grid is placed on the top of all the other elements of the overlay.
•You can have multiple grid switches.
•It works for a currently active image. If you have multiple images with grids, you can change its target by activating another image.
•When the Grid Switch looses its window focus, i.e. minimized, closed, or inactivated, the grid is redrawn onto the image.




References:
1.Howard CV, Reed MG. Unbiased Stereology, 2nd ed. Oxon, UK: Garland Science/BIOS Scientific Publishers; 2005.
2.Hsia CC, Hyde DM, Ochs M, Weibel ER; ATS/ERS Joint Task Force on Quantitative Assessment of Lung Structure. An official research policy statement of the American Thoracic Society/European Respiratory Society: standards for quantitative assessment of lung structure. Am J Respir Crit Care Med 2010;181:39-418.




Corresponding Author:
Please contact Daisuke Kinose (Daisuke.Kinose@hli.ubc.ca) for questions about this plugin. 
