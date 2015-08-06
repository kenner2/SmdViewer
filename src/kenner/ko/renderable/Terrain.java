package kenner.ko.renderable;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import kenner.ko.map.ServerMap;
import kenner.ko.util.Logger;
//lwjgl static imports
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Terrain {
	private ServerMap map;
	private int vboId;
	private int vaoId;
	private int colorBufferId;
	//private int indexBufferId;
	private int shapeCount;
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	//private IntBuffer indexBuffer;
	private int pointCount;
	private Float maxHeight = 0f, minHeight = 0f, avgHeight = 0f;
	
	public Terrain(ServerMap map){
		this.map = map;
		analyzeHeights();
		if(map != null){
			vboId = GL15.glGenBuffers();
			colorBufferId = GL15.glGenBuffers();
			
			loadBuffers();
			//vertices
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
			//colors
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorBufferId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
			
			//set up the vertex attribute object (vao)
			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);
			//bind buffers
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			glBindBuffer(GL_ARRAY_BUFFER, colorBufferId);
			glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
			//enable attributes
			glEnableVertexAttribArray(0);
			glEnableVertexAttribArray(1);
			//unbind current vao
			glBindVertexArray(0);
			//glDrawElements(GL_QUADS, quadCount, GL_FLOAT, 0);
		}
	}
	
	/**
	 * Loads vertex and color buffers.
	 */
	private void loadBuffers(){
		//map.setUnitDistance(1f);
		//we need to load to a vertex array to load later
		//((3 floats) * ((mapSize-1)^2)) * 3(points)
		shapeCount = (int) Math.pow(map.getMapSize()-2, 2)*2;
		//pointCount = shapeCount * 3 (points) 
		pointCount = shapeCount * 3 * 3;
		float[] vertices = new float[pointCount];
		//should be same-size arrays, each point has a (x,y,z) and a (r,g,b)
		float[] colors = new float[pointCount];
		
		int vi = 0, ci = 0;
		for(float i = 0; i < map.getMapSize()-2; i++){
			for(float j = 0; j < map.getMapSize()-2; j++){
				//Mesh will be made of triangles merged into quads
				
				float y = map.getHeight()[(int)i][(int)j];
				//(x,z)
				float[] v0 = {(i * map.getUnitDistance()), (y * map.getUnitDistance()),(j * map.getUnitDistance())};
				float[] c0 = getVertexColor(y);
				//(x,z+1)
				y = map.getHeight()[(int)i+1][(int)j];
				float[] v1 = {((i+1) * map.getUnitDistance()), (y * map.getUnitDistance()),(j * map.getUnitDistance())};
				float[] c1 = getVertexColor(y);
				//(x+1,z)
				y = map.getHeight()[(int)i+1][(int)j+1];
				float[] v2 = {((i+1) * map.getUnitDistance()), (y * map.getUnitDistance()),((j+1) * map.getUnitDistance())};
				float[] c2 = getVertexColor(y);
				//(x+1, z+1)
				y = map.getHeight()[(int)i][(int)j+1];
				float[] v3 = {(i * map.getUnitDistance()), (y * map.getUnitDistance()),((j+1) * map.getUnitDistance())};
				float[] c3 = getVertexColor(y);

				//set up vertex buffers
				//Triangle 1:  v0 + v1 + v2
				vertices[vi++] = v0[0];
				vertices[vi++] = v0[1];
				vertices[vi++] = v0[2];
				vertices[vi++] = v1[0];
				vertices[vi++] = v1[1];
				vertices[vi++] = v1[2];
				vertices[vi++] = v2[0];
				vertices[vi++] = v2[1];
				vertices[vi++] = v2[2];
				colors[ci++] = c0[0];
				colors[ci++] = c0[1];
				colors[ci++] = c0[2];
				colors[ci++] = c1[0];
				colors[ci++] = c1[1];
				colors[ci++] = c1[2];
				colors[ci++] = c2[0];
				colors[ci++] = c2[1];
				colors[ci++] = c2[2];
				
				//Triangle 2:  v2 + v3 + v0
				vertices[vi++] = v2[0];
				vertices[vi++] = v2[1];
				vertices[vi++] = v2[2];
				vertices[vi++] = v3[0];
				vertices[vi++] = v3[1];
				vertices[vi++] = v3[2];
				vertices[vi++] = v0[0];
				vertices[vi++] = v0[1];
				vertices[vi++] = v0[2];
				colors[ci++] = c2[0];
				colors[ci++] = c2[1];
				colors[ci++] = c2[2];
				colors[ci++] = c3[0];
				colors[ci++] = c3[1];
				colors[ci++] = c3[2];
				colors[ci++] = c0[0];
				colors[ci++] = c0[1];
				colors[ci++] = c0[2];
				
			}
		}
		vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
		vertexBuffer.put(vertices).flip();
		colorBuffer = BufferUtils.createFloatBuffer(colors.length);
		colorBuffer.put(colors).flip();
	}
	
	/**
	 * Generate stats on terrain heights
	 */
	public void analyzeHeights(){
		float sum = 0;
		float count = 0;
		for(int i = 0; i < map.getMapSize()-1; i++){
			for(int j = 0; j < map.getMapSize()-1; j++){
				float y = map.getHeight()[i][j];
				
				//on first iteration of loop, initialize max/min heights
				if(i+j == 0){
					maxHeight = minHeight = y;
				}
				
				//max height set
				if(y > maxHeight){
					maxHeight = y;
				}
				//min height set
				if(y < minHeight){
					minHeight = y;
				}
				
				sum += y;
				count++;
			}
		}
		
		//calculate average
		avgHeight = sum / count;
		Logger.info("Terrain info:");
		Logger.info("Min. Height: " + minHeight);
		Logger.info("Avg. Height: " + avgHeight);
		Logger.info("Max. Height: " + maxHeight);
	}
	
	/**
	 * Calculates the vertex color
	 * @param y
	 * @return
	 */
	public float[] getVertexColor(float y){
		//we basically need to create a scale of colors between max and min
		//where max = red
		//		half = green
		//		min = blue
		
		//calculate colors
		//x = where y falls percentage wise on range min to max
		float x = (y - minHeight) / (maxHeight - minHeight);
		//quadradic functions to find color percentages
		float red = Math.abs(1.7143f*x*x-0.7143f*x+0.0143f);
		float green = Math.abs(-3.4286f*x*x+3.4286f*x-0.0286f);
		float blue = Math.abs(1.7143f*x*x-2.7143f*x+1.0143f);
		//range check
		if(red > 1f) red = 1f;
		if(green > 1f) green = 1f;
		if(blue > 1f) blue = 1f;
		
		return new float[] {red, green, blue};
	}

	
	
	/**
	 * OpenGL Cleanup
	 */
	public void dispose(){
		glDeleteBuffers(vboId);
		glDeleteBuffers(colorBufferId);
		glDeleteVertexArrays(vaoId);
	}

	/*
	 * Getters / Setters
	 */
	public ServerMap getMap() {
		return map;
	}

	public void setMap(ServerMap map) {
		this.map = map;
	}

	public int getVaoId() {
		return vaoId;
	}

	public void setVaoId(int vaoId) {
		this.vaoId = vaoId;
	}

	public int getShapeCount() {
		return shapeCount;
	}

	public void setShapeCount(int shapeCount) {
		this.shapeCount = shapeCount;
	}

	public int getPointCount() {
		return pointCount;
	}

	public void setPointCount(int pointCount) {
		this.pointCount = pointCount;
	}

	public Float getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(Float maxHeight) {
		this.maxHeight = maxHeight;
	}

	public Float getMinHeight() {
		return minHeight;
	}

	public void setMinHeight(Float minHeight) {
		this.minHeight = minHeight;
	}

	public Float getAvgHeight() {
		return avgHeight;
	}

	public void setAvgHeight(Float avgHeight) {
		this.avgHeight = avgHeight;
	}
}
