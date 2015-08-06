package kenner.opengl.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import kenner.ko.util.FragFileNameFilter;
import kenner.ko.util.Logger;
import kenner.ko.util.VertFileNameFilter;

/**
 * Static class for the management of shader programs.
 * @author kenner
 */
public class ProgramManager {
	/**
	 * Compiled shader programs.  Takes the name as index from shader/*
	 * For example, basic.frag and basic.vert will be compiled to a program named "basic".
	 */
	private static Map<String, Integer> shaders = new HashMap<String, Integer>();
	
	/**
	 * Since this is a singleton class, the constructor logic should only execute once.
	 * This AtomicBoolean will help ensure it only happens once.
	 */
	private static AtomicBoolean isConstructed = new AtomicBoolean(false);
	
	/**
	 * Path to the shader files to compile/link.
	 */
	private static String compilePath = "shader";
	
	/**
	 * Default constructor.  Polls isConstructed to check if the logic should be executed.
	 */
	public ProgramManager(){
		if(ProgramManager.isConstructed.compareAndSet(false, true)){
			try {
				loadAvailableShaders();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Constructor that allows compilePath to be modified before loading available shaders.
	 */
	public ProgramManager(String compilePath){
		if(ProgramManager.isConstructed.compareAndSet(false, true)){
			ProgramManager.compilePath = compilePath;
			try {
				loadAvailableShaders();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Logger.error("Singleton class ProgramManager has already been constructed.");
		}
	}
	
	/**
	 * Loads all parts of a shader and compiles them if valid.
	 * Must have at least a .frag and a .vert part of the same name to compile.
	 * @throws IOException 
	 */
	private static void loadAvailableShaders() throws IOException{
		//grab the compile path directory
		File shaderDir = new File(compilePath);
		//grab the frag/vert files
		File[] verts = shaderDir.listFiles(new VertFileNameFilter());
		File[] frags = shaderDir.listFiles(new FragFileNameFilter());
		//create temporary containers to store the frag names to
		Map<String, File> fragments = new HashMap<String, File>();
		//populate name list
		for(File f : frags){
			fragments.put(f.getName().replace(".frag", ""), f);
		}
		
		
		//Start Linking/Compiling process
		for(File f : verts){
			//check to make sure there is a frag to link to the vert
			String name = f.getName().replace(".vert", "");
			if(fragments.containsKey(name)){
				//load vertex shader
				String vertexShader = new String(Files.readAllBytes(f.toPath()));
				int vShader = glCreateShader(GL_VERTEX_SHADER);
				glShaderSource(vShader, vertexShader);
				glCompileShader(vShader);
				if(glGetShaderi(vShader, GL_COMPILE_STATUS) != GL_TRUE){
					Logger.error("Failed compiling Vertex Shader: " + name);
				}
				
				//load fragment shader
				String fragmentShader = new String(Files.readAllBytes(fragments.get(name).toPath()));
				int fShader = glCreateShader(GL_FRAGMENT_SHADER);
				glShaderSource(fShader, fragmentShader);
				glCompileShader(fShader);
				if(glGetShaderi(fShader, GL_COMPILE_STATUS) != GL_TRUE){
					Logger.error("Failed compiling Fragment Shader: " + name);
				}
				
				//create shader program
				int shaderProgram = glCreateProgram();
				glAttachShader(shaderProgram, vShader);
				glAttachShader(shaderProgram, fShader);
				glLinkProgram(shaderProgram);
				if(glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE){
					String linkError = glGetProgramInfoLog(shaderProgram);
					Logger.error("Failed compiling Shader Program \"" + name + "\"");
					Logger.error("Link error: " + linkError);
					Logger.error("OpenGL Error Code: " + glGetError());
				}
				
				//add shader program id to list
				shaders.put(name, shaderProgram);
				
				//delete shaders  - only need the compiled program
				glDeleteShader(vShader);
				glDeleteShader(fShader);
			} else {
				Logger.error("Fragment shader not found for : " + name);
			}
		}
		
	}
	
	/**
	 * Attempts to pull a shader id given the shader name.
	 * Shader name is the part of the file name before the ext.
	 * For example, basic.frag + basic.vert = basic
	 * @param shaderName
	 * @return
	 */
	public static int getShaderId(String shaderName){
		//make sure the object was initialized before trying to pull from an empty map
		if(ProgramManager.isConstructed.compareAndSet(false, true)){
			try {
				loadAvailableShaders();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(shaders.get(shaderName) != null){
			return shaders.get(shaderName);
		} else {
			Logger.error("No known shader: " + shaderName);
			return -1;
		}
	}
	
	/**
	 * Disposes shader programs.  Should be called at the end of the application
	 */
	public static void dispose(){
		for(int i : shaders.values()){
			glDeleteProgram(i);
		}
	}
}
