package kenner.ko.tools;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GLContext;

import silvertiger.tutorial.lwjgl.math.Vector3f;
import kenner.glfw.keybind.BasicKeyboardMovement;
import kenner.ko.map.ServerMap;
import kenner.ko.renderable.Terrain;
import kenner.ko.util.Logger;
import kenner.ko.util.SmdXmlFileFilter;
import kenner.opengl.camera.BasicCamera;
import kenner.opengl.shader.ProgramManager;

/**
 * Requires OpenGL 3.3+.  Compiled on Java 1.8
 * 
 * Allows you to select an xml or smd file to load, and then renders the terrain.
 * You will be placed at (maxX/2, avgHeight, maxZ+15) of the map.
 * 
 * See configuration.properties for various settings.
 * 
 * Controls:
 * WASD:  Movement on the X/Z plane
 * Space:  Fly Up
 * Left-Shift:  Fly Down
 * Left-Arrow:  Rotate view left.
 * Right-Arrow: Rotate view right.
 * **Pitch modification is a bit buggy at the moment, use up and down at your own risk.
 * Up-Arrow:  Rotate view up.
 * Down-Arrow:  Rotate view down.
 * X:  Toggle X-Ray (Mesh-View)
 * Esc:  Exit SmdViewer
 * 
 * Future Releases
 * 1) Render Collision Data, Warp Points, etc.
 * 2) Render coordinate system
 * 3) Add Ray-casting for mouse point-selection
 * 4) Add manipulation of points via raycasting
 * 5) Rename SmdEditor
 * 
 * @author kenner
 */
public class SmdViewer {

	//window pointer used by various glfw functions.
	private long windowPtr;
	//Resolution
	private int width = 1600, height = 900;
	//Window Title
	private String windowTitle;
	//callbacks (ALL MUST BE CLEANED UP)
	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyboardCallback;
	private BasicKeyboardMovement keyboard;
	private GLFWScrollCallback scrollCallback;
	private GLFWCursorPosCallback cursorPosCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;
	
	//configuration
	private Properties config;
	
	//for game loop
	private boolean running = true;
	
	//terrain loaded from ServerMap's height data
	private Terrain terrain;
	
	/*
	 * Timer related
	 */
	private double previous, current, elapsed;
	
	/*
	 * Camera Setup
	 */
	private BasicCamera camera;
	private float rotateSpeed;
	private float moveSpeed;
	private boolean cameraMoved = false;
	private AtomicBoolean xray = new AtomicBoolean(false);
	
	/**
	 * Default constructor.  Sets up window/terrain/camera/keybindings.
	 */
	public SmdViewer(){
		//load the config file
		try {
			loadConfig();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		//Start GL context 
		if(glfwInit() != GL_TRUE){
			Logger.error("Error starting context/GLFW");
		}
		
		//set OpenGL window hints
		setWindowHints();
		
		//create the window - store the pointer
		windowPtr = glfwCreateWindow(width, height, windowTitle, NULL, NULL);
		
		//if window pointer equals 0, basically
		if(windowPtr == NULL){
			Logger.error("Error creating glfw window.");
			glfwTerminate();
		}
		//make the context current
		glfwMakeContextCurrent(windowPtr);
		GLContext.createFromCurrent();
		
		//Log OpenGL info.
		String renderer = glGetString(GL_RENDERER);
		String version = glGetString(GL_VERSION);
		Logger.info("Render: " + renderer);
		Logger.info("OpenGL Version: " + version);
		
		//tells opengl to draw onto a pixel only if the shape is closer to the viewer;
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LESS);
		
		//Launch Shader Program Manager
		new ProgramManager("shader/");
		
		//camera stuff
		camera = new BasicCamera(width, height);
		camera.setSpeed(moveSpeed);
		camera.setRotateSpeed(rotateSpeed);
		//set view matrix
		camera.setViewMatrixLocation(glGetUniformLocation(ProgramManager.getShaderId("ColorSpectrum"), "view"));
		//set projection matrix
		camera.setProjectionMatrixLocation(glGetUniformLocation(ProgramManager.getShaderId("ColorSpectrum"), "proj"));
		
		//set error callback
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback(this::glfwErrorCallback));
		keyboard = new BasicKeyboardMovement(this);
		//set keyboard callback
		glfwSetKeyCallback(windowPtr, keyboardCallback = GLFWKeyCallback(keyboard::glfwKeyCallback));
		//set mouse position callback
		glfwSetCursorPosCallback(windowPtr, cursorPosCallback = GLFWCursorPosCallback(this::glfwCursorPosCallback));
		//set mouse button callback
		glfwSetMouseButtonCallback(windowPtr, mouseButtonCallback = GLFWMouseButtonCallback(this::glfwMouseButtonCallback));
		//set mouse scroll callback
		glfwSetScrollCallback(windowPtr, scrollCallback = GLFWScrollCallback(this::glfwScrollCallback));
		
		//load terrain
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("maps"));
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		FileFilter smdXmlFileFilter = new SmdXmlFileFilter();
		chooser.setFileFilter(smdXmlFileFilter);
		
		File f = null;
		if (chooser.showOpenDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
		     f = chooser.getSelectedFile();
		}
		ServerMap m = new ServerMap();
		m.loadMap(f);
		terrain = new Terrain(m);
		
		//place camera at a position relative to the terrain.
		float x = (m.getMapSize()*m.getUnitDistance())/2f;
		float y = terrain.getAvgHeight();
		float z = x*2f + 15f;
		
		camera.setPosition(new Vector3f(x, y, z));
		camera.calculateView();
		
		previous = glfwGetTime();
	}
	
	/**
	 * Loads the configuration from configuration.properties
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void loadConfig() throws FileNotFoundException, IOException{
		//load the configuration file
		config = new Properties();
		config.load(new FileInputStream("configuration.properties"));
		width = Integer.parseInt(config.getProperty("resolution.width"));
		height = Integer.parseInt(config.getProperty("resolution.height"));
		windowTitle = config.getProperty("window.title");
		rotateSpeed = Float.parseFloat(config.getProperty("camera.rotateSpeed"));
		moveSpeed = Float.parseFloat(config.getProperty("camera.rotateSpeed"));
	}
	
	/**
	 * Includes game loop and cleanup functionality.
	 */
	public void run(){
		//game loop time
		while(running && glfwWindowShouldClose(windowPtr) != GL_TRUE){
			//pre-render logic
			current = glfwGetTime();
			elapsed = current - previous;
			previous = current;
			
			float distance = (float) (camera.getSpeed() * getElapsed());
			float angle = (float) (camera.getRotateSpeed() * getElapsed());
			keyboard.move(distance, angle);
			
			//update camera view
			if(cameraMoved){
				camera.calculateView();
				cameraMoved = false;
			}
			
			//wipe the drawing surface clear
			glClearColor(0.2f, 0.2f, 0.2f, 1f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			
			//draw points 0-3 from the currently bound VAO with current in-use shader
			glUseProgram(ProgramManager.getShaderId("ColorSpectrum"));
			glUniformMatrix4fv(camera.getViewMatrixLocation(), false, camera.getViewMatrix().getBuffer());
			glUniformMatrix4fv(camera.getProjectionMatrixLocation(), false, camera.getProjectionMatrix().getBuffer());
			//render terrain
			glBindVertexArray(terrain.getVaoId());
			if(xray.get()){
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			} else {
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			}
			glDrawArrays(GL_TRIANGLES, 0, terrain.getPointCount());
			//unbind vao
			glBindVertexArray(0);
			//update events and swap buffers
			glfwPollEvents();
			glfwSwapBuffers(windowPtr);
		}
		
		//de-allocate resources
		terrain.dispose();
		ProgramManager.dispose();
		glfwDestroyWindow(windowPtr);
		//end
		glfwTerminate();
	}
	
	/**
	 * Runs program.
	 * @param args
	 */
	public static void main(String[] args){
		System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());
		new SmdViewer().run();
	}
	
	/**
	 * GLFW window hints - by default.
	 */
	private void setWindowHints(){
		glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
		//AAx4
		glfwWindowHint(GLFW_SAMPLES, 4);
	}
	
	/* Sets the running flag to false */
	public void end(){
		running = false;
	}
	
	/* Callbacks */
	public void glfwErrorCallback(int error, long description){
	    Logger.error("[GLFW ERROR] " + error + ": " + memDecodeUTF8(description));
	}
	
	public void glfwCursorPosCallback(long window, double xpos, double ypos){
	}

	public void glfwMouseButtonCallback(long window, int button, int action, int mods){
	}

	public void glfwScrollCallback(long window, double xoffset, double yoffset){
	}

	public long getWindowPtr() {
		return windowPtr;
	}

	public void setWindowPtr(long windowPtr) {
		this.windowPtr = windowPtr;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public GLFWErrorCallback getErrorCallback() {
		return errorCallback;
	}

	public void setErrorCallback(GLFWErrorCallback errorCallback) {
		this.errorCallback = errorCallback;
	}

	public GLFWKeyCallback getKeyboardCallback() {
		return keyboardCallback;
	}

	public void setKeyboardCallback(GLFWKeyCallback keyboardCallback) {
		this.keyboardCallback = keyboardCallback;
	}

	public GLFWScrollCallback getScrollCallback() {
		return scrollCallback;
	}

	public void setScrollCallback(GLFWScrollCallback scrollCallback) {
		this.scrollCallback = scrollCallback;
	}

	public GLFWCursorPosCallback getCursorPosCallback() {
		return cursorPosCallback;
	}

	public void setCursorPosCallback(GLFWCursorPosCallback cursorPosCallback) {
		this.cursorPosCallback = cursorPosCallback;
	}

	public GLFWMouseButtonCallback getMouseButtonCallback() {
		return mouseButtonCallback;
	}

	public void setMouseButtonCallback(GLFWMouseButtonCallback mouseButtonCallback) {
		this.mouseButtonCallback = mouseButtonCallback;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public void setTerrain(Terrain terrain) {
		this.terrain = terrain;
	}

	public double getPrevious() {
		return previous;
	}

	public void setPrevious(double previous) {
		this.previous = previous;
	}

	public double getCurrent() {
		return current;
	}

	public void setCurrent(double current) {
		this.current = current;
	}

	public double getElapsed() {
		return elapsed;
	}

	public void setElapsed(double elapsed) {
		this.elapsed = elapsed;
	}

	public BasicCamera getCamera() {
		return camera;
	}

	public void setCamera(BasicCamera camera) {
		this.camera = camera;
	}

	public boolean isCameraMoved() {
		return cameraMoved;
	}

	public void setCameraMoved(boolean cameraMoved) {
		this.cameraMoved = cameraMoved;
	}

	public AtomicBoolean getXray() {
		return xray;
	}

	public void setXray(AtomicBoolean xray) {
		this.xray = xray;
	}
}
