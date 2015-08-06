package kenner.opengl.camera;

import kenner.ko.util.Logger;
import kenner.math.Versor;
import silvertiger.tutorial.lwjgl.math.Matrix4f;
import silvertiger.tutorial.lwjgl.math.Vector3f;
import silvertiger.tutorial.lwjgl.math.Vector4f;

public class BasicCamera {
	private float speed = 20f;  //move 1 unit per sec
	private float rotateSpeed = 50f;  //rotate 10 degrees per sec
	private Vector3f position = new Vector3f(0, 0, 2f);
	private Vector3f move = new Vector3f(0, 0, 0);
	private float yaw = 0f;  //y rotation (degrees)
	private float pitch = 0f;  //x rotation
	
	/*
	 * Matricies
	 */
	private Matrix4f translationMatrix = Matrix4f.translate(-position.x, -position.y, -position.z);
	private Matrix4f rotation;
	private Matrix4f viewMatrix;// = yRotationMatrix.multiply(translationMatrix);
	private Matrix4f projectionMatrix;
	
	/*
	 * Quaternions / Versors 
	 */
	private Versor orientation;
	
	/*
	 * directional vectors 
	 */
	private Vector4f forward, up, right;
	
	//clipping
	private float near = 0.1f;
	private float far = 1000f;
	//field of view
	private float fov = (float) Math.toRadians(67f);
	private float width, height;
	//aspect ratio, width / height
	private float aspectRatio;
	private float range;
	private float sx, sy, sz, pz;
	
	private int viewMatrixLocation;
	private int projectionMatrixLocation;
	
	public BasicCamera(float width, float height){
		this.width = width;
		this.height = height;
		aspectRatio = width / height;
		range = (float) (Math.tan(fov * 0.5f) * near);
		sx = (2f * near) / (range * aspectRatio + range * aspectRatio);
		sy = near / range;
		sz = -(far + near) / (far - near);
		pz = -(2f * far * near) / (far - near);
		projectionMatrix = new Matrix4f(
				new Vector4f(sx, 0f, 0f, 0f), //column 1
				new Vector4f(0f, sy, 0f, 0f), //column 2
				new Vector4f(0f, 0f, sz, -1f), //column 3
				new Vector4f(0f, 0f, pz, 0f)); //column 4
		
		//set up orientation versor
		orientation = new Versor(-yaw, 0, 1f, 0);
		rotation = orientation.toRotationMatrix();
		
		//create view matrix
		viewMatrix = rotation.multiply(translationMatrix);
		
		//set up directional vectors
		resetDirectionalVectors();
		
		Logger.info("View Matrix: \n" + viewMatrix.toString());
		Logger.info("Projection Matrix: \n" + projectionMatrix.toString());
	}
	
	public void calculateView(){
		rotation = orientation.toRotationMatrix();
		//recalculate posotion
		position = new Vector3f(
					position.x + (forward.x * -move.z),
					position.y + (forward.y * -move.z),
					position.z + (forward.z * -move.z));
		position = new Vector3f(
					position.x + (up.x * move.y), 
					position.y + (up.y * move.y),
					position.z + (up.z * move.y)
				);
		position = new Vector3f(
					position.x + (right.x * move.x),
					position.y + (right.y * move.x),
					position.z + (right.z * move.x)
				);
		
		
		//translation matrix recalculation
		translationMatrix = Matrix4f.translate(position.x, position.y, position.z);
		viewMatrix = rotation.inverse().multiply(translationMatrix.inverse());
		
		//reset move 
		move = new Vector3f(0, 0, 0);
		
		//reset directional vectors
		resetDirectionalVectors();
	}
	
	/*
	 * Movement functionality
	 */
	public void moveLeft(float distance){
		move.z += distance * (float)Math.sin(Math.toRadians(yaw));
		move.x -= distance * (float)Math.cos(Math.toRadians(yaw));
	}
	
	public void moveRight(float distance){
		move.z -= distance * (float)Math.sin(Math.toRadians(yaw));
		move.x += distance * (float)Math.cos(Math.toRadians(yaw));
	}
	
	public void moveForward(float distance){
		move.z -= distance * (float)Math.sin(Math.toRadians(yaw+90f));
		move.x += distance * (float)Math.cos(Math.toRadians(yaw+90f));
	}
	
	public void moveBackward(float distance){
		move.z -= distance * (float)Math.sin(Math.toRadians(yaw-90f));
		move.x += distance * (float)Math.cos(Math.toRadians(yaw-90f));
	}
	
	public void moveUp(float distance){
		move.y += distance;
	}
	
	public void moveDown(float distance){
		move.y -= distance;
	}
	
	public void rotateRight(float angle){
		yaw -= angle;
		Versor rotate = new Versor(-angle, up.x, up.y, up.z);
		orientation = Versor.multiply(rotate, orientation);
		rotation = orientation.toRotationMatrix();
		updateDirectionalVectors();
	}
	
	public void rotateLeft(float angle){
		yaw += angle;
		Versor rotate = new Versor(angle, up.x, up.y, up.z);
		orientation = Versor.multiply(rotate, orientation);
		rotation = orientation.toRotationMatrix();
		updateDirectionalVectors();
	}
	
	public void rotateUp(float angle){
		pitch += angle;
		Versor rotate = new Versor(angle, right.x, right.y, right.z);
		orientation = Versor.multiply(rotate, orientation);
		rotation = orientation.toRotationMatrix();
		updateDirectionalVectors();
	}
	
	public void rotateDown(float angle){
		pitch -= angle;
		Versor rotate = new Versor(-angle, right.x, right.y, right.z);
		orientation = Versor.multiply(rotate, orientation);
		rotation = orientation.toRotationMatrix();
		updateDirectionalVectors();
	}
	
	public void updateDirectionalVectors(){
		forward = rotation.multiply(new Vector4f(0, 0, -1f, 0));
		right = rotation.multiply(new Vector4f(1f, 0, 0, 0));
		up = rotation.multiply(new Vector4f(0, 1f, 0, 0));
	}
	
	public void resetDirectionalVectors(){
		forward = new Vector4f(0, 0, -1f, 0);
		right = new Vector4f(1f, 0, 0, 0);
		up = new Vector4f(0, 1f, 0, 0);
	}

	/*
	 * Getters / Setters
	 */
	public float getSpeed() {
		return speed;
	}


	public void setSpeed(float speed) {
		this.speed = speed;
	}


	public float getRotateSpeed() {
		return rotateSpeed;
	}


	public void setRotateSpeed(float yaw_speed) {
		this.rotateSpeed = yaw_speed;
	}


	public Vector3f getPosition() {
		return position;
	}


	public void setPosition(Vector3f position) {
		this.position = position;
	}


	public float getYaw() {
		return yaw;
	}


	public void setYaw(float yaw) {
		this.yaw = yaw;
	}


	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}


	public void setViewMatrix(Matrix4f viewMatrix) {
		this.viewMatrix = viewMatrix;
	}


	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}


	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}


	public float getNear() {
		return near;
	}


	public void setNear(float near) {
		this.near = near;
	}


	public float getFar() {
		return far;
	}


	public void setFar(float far) {
		this.far = far;
	}


	public float getFov() {
		return fov;
	}


	public void setFov(float fov) {
		this.fov = fov;
	}


	public float getWidth() {
		return width;
	}


	public void setWidth(float width) {
		this.width = width;
	}


	public float getHeight() {
		return height;
	}


	public void setHeight(float height) {
		this.height = height;
	}


	public float getAspectRatio() {
		return aspectRatio;
	}


	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
	}


	public int getViewMatrixLocation() {
		return viewMatrixLocation;
	}


	public void setViewMatrixLocation(int viewMatrixLocation) {
		this.viewMatrixLocation = viewMatrixLocation;
	}


	public int getProjectionMatrixLocation() {
		return projectionMatrixLocation;
	}


	public void setProjectionMatrixLocation(int projectionMatrixLocation) {
		this.projectionMatrixLocation = projectionMatrixLocation;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
}
