What does it do?
Allows you to select an xml or smd file to load, and then renders the terrain.

OpenGL Dependency
Requires OpenGL 3.3 or greater.

Java Dependency
You must have the following done before running this program:
1)  Install Java 1.8+
2)  Make sure your PATH variables are set up to run "java" from cmd.  Google it if you don't know how.
Note:  I used an application to compile the java into an exe.  I'm not sure if this exe is dependent on Java, I didn't feel like uninstalling or messing with my path variables to find out.  If someone without java is able to run the exe without issues, let me know and I'll take this part out.

Usage
1) Place map files (smd or xml) into the maps folder
2)  See configuration.properties for various settings.
3) Run either SmdViewer.jar or SmdViewer.exe
4) Pick a map file to load.
5) Map will be rendered.  You'll be placed at x = MaxX / 2, y = avgMapHeight, z = MaxZ + 15
 
Controls
WASD:  Movement on the X/Z plane
Space:  Fly Up
Left-Shift:  Fly Down
Left-Arrow:  Rotate view left.
Right-Arrow: Rotate view right.
**Pitch modification is a bit buggy at the moment, use up and down at your own risk.
Up-Arrow:  Rotate view up.
Down-Arrow:  Rotate view down.
X:  Toggle X-Ray (Mesh-View)  (I'd suggest using this at the moment.  I'm not in love with the shader on GL_FILL)
Esc:  Exit SmdViewer

Future Releases
1) Render Collision Data, Warp Points, etc.
2) Render coordinate system
3) Add Ray-casting for mouse point-selection
4) Add manipulation of points via raycasting
5) Rename SmdEditor

Issue Reporting
Any issues, post on the snoxd.net thread.
Github for project sources/updates: https://github.com/kenner2/SmdViewer
