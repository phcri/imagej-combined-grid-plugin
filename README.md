# imagej-combined-grids-plugin
Combined Grids Plugin for ImageJ

<p>
The Combined Grids Plugin is based on the Grid Plugin (<a target="_blank"
href="http://rsb.info.nih.gov/ij/plugins/grid.html"
>http://rsb.info.nih.gov/ij/plugins/grid.html</a>) originally written by Dr.
Wayne Rasband and modified with his permission.
</p>
<p>
Authors; Daisuke Kinose<SUP>[1]</SUP> and Adrian Png<SUP>[2]</SUP><BR>
<SUP>[1]</SUP> Dr. James Hogg's Lab, the Centre for Heart Lung Innovation in St. Paul's Hospital, University of British Columbia, Canada<BR>
<SUP>[2]</SUP> IT department, the Centre for Heart Lung Innovation in St. Paul's Hospital, University of British Columbia, Canada<BR>
</p>
<p>
Released on February 5th, 2015
</p>

<p>
	<H2>Update on February 5th, 2015</H2>
	<ol>
		<li>Each slice in a stacked image can have a grid at different position.</li>
		<li>Output of the slice position in a stack</li>
	</ol>
</p>


<p>
	<H2>Implemented Functions:</H2>
	<ol>
		<li>Combined, or multi-purpose, point grid with several fine to coarse grid
			ratios</li>
		<li>Double lattice square grid with several fine to coarse grid ratios</li>
		<li>Output and autosave of grid parameters</li>
		<li>Manual specification of grid position</li>
		<li>Overlay/remove a grid without destroying the other Overlay elements</li>
		<li>Grid Switch to temporary hide an overlaid grid</li>
	</ol>
</p>

<p>
	<H2>Installation and Usage:</H2>
	<ol>
		<li>Put "CombinedGridPlugin.jar" into the plugins folder of ImageJ (or Fiji) and 
			restart the software. Please note that this does
			<strong>NOT</strong> overwrite or replace the existing Grid plugin installed
			with ImageJ.</li>
		<li>Open an image in ImageJ.</li>
		<li>Choose "Plugins" > "Grids" > "Combined Grids" in ImageJ.</li>
		<li>Specify your grid settings in the dialog box and click OK.</li>
	</ol>
</p>

<p>
	<H2>Notes for the Combined Grids Plugin and Grid History:</H2>
	<ul>
		<li>For the Combined Point Grid and the Double Lattice Square grid,
			"Area per Point" determines density of the fine grid.</li>
		<li>Grid location by "Fixed Position" in this plugin is same as grid location
			with "random-offset" unchecked in the original "Grid" plugin. For
			"Combined Point" and "Double Lattice" grids, the first point of the coarse
			grid is placed on the first point for the fine grid at the left upper corner.</li>
		<li>When you overlay a new grid, the parameters for the grid are automatically added to the history of previousely overlaid grids.
			This list is automatically saved as "CombinedGridsHistory.txt" in the plugin folder.</li>
		<li>If you want to delete grid information from the list of "Grid History",<BR>
			1. select lines you want to delete in the "Grid History" window,<BR>
			2. choose "cut" or "clear" in the right click menue or in the "Edit" tab<BR>
			3. choose "save as..." in the "File" tab and overwrite "CombinedGridsHistory.txt" in the plugins folder of ImageJ.<BR>
			You can completely clean the list by removing "CombinedGridsHistory.txt" from the folder. This plugin will create a new one.
			</li>
	</ul>
</p>
<p>
<H2>Notes for the Grid Switch:</H2>
	<ul>
		<li>The Grid Switch specifically hides/redraws a grid overlaid by 
			the "Grid with Combined Grids" plugin. This switch is not compatible with the original Grid Plugin</li>
		<li>When it redraws a grid, the grid is placed on the top of 
			all the other elements of the overlay.</li>
		<li>You can have multiple grid switches.</li>
		<li>It works for a currently active image. If you have multiple images with grids, 
			you can change its target by activating another image.</li>
		<li>When the Grid Switch looses its window focus, i.e. minimized, closed, or inactivated, the grid is 
			redrawn onto the image.</li>
	</ul>
</p>
<p>
<H2>References:</H2>
	<ol>
		<li>Howard CV, Reed MG. Unbiased Stereology, 2nd ed. Oxon, UK: Garland
			Science/BIOS Scientific Publishers; 2005.</li>
		<li>Hsia CC, Hyde DM, Ochs M, Weibel ER; ATS/ERS Joint Task Force on
			Quantitative Assessment of Lung Structure. An official research policy
			statement of the American Thoracic Society/European Respiratory Society:
			standards for quantitative assessment of lung structure. Am J Respir Crit
			Care Med 2010;181:39-418.</li>
	</ol>
</p>

<p>
<H2>Corresponding Author:</H2>
Please contact Daisuke Kinose (<a href="mailto:Daisuke.Kinose@hli.ubc.ca">Daisuke.Kinose@hli.ubc.ca</a>) for questions about
this plugin.
</p>

<p>
<H2>Source Code:</H2>
The source code of this plugin is avaliable at https://github.com/phcri/imagej-combined-grids-plugin on GitHub.
</p>
