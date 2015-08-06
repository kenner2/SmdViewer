package kenner.math;

import silvertiger.tutorial.lwjgl.math.Matrix4f;
import silvertiger.tutorial.lwjgl.math.Vector4f;

/**
 * Rotation quaternion impl
 * @author kenner
 */
public class Versor extends Quaternion {

	/**
	 * Constructs a versor for the given float and abstract axis.
	 * @param w
	 * @param x
	 * @param y
	 * @param z
	 */
	public Versor(float w, float x, float y, float z){
		this.w = (float) Math.cos(Math.toRadians(w) / 2);
		this.x = (float) Math.sin(Math.toRadians(w) / 2) * x;
		this.y = (float) Math.sin(Math.toRadians(w) / 2) * y;
		this.z = (float) Math.sin(Math.toRadians(w) / 2) * z;
	}
	
	/**
	 * Static function for Quaternion multiplication
	 * Non-commutative (ORDER MATTERS)
	 * Format:  T = Q * R
	 * @param Q
	 * @Param R
	 * @return T
	 */
	public static Versor multiply(Versor Q, Quaternion R){
		Quaternion t = Q.toQuaternion();
		t = Quaternion.multiply(t, R);
		return t.toVersor();
	}
	
	/**
	 * Static function for Quaternion multiplication
	 * Non-commutative (ORDER MATTERS)
	 * Format:  T = Q * R
	 * @param Q
	 * @Param R
	 * @return T
	 */
	public static Versor multiply(Quaternion Q, Versor R){
		Quaternion t = R.toQuaternion();
		t = Quaternion.multiply(Q, t);
		return t.toVersor();
	}
	
	/**
	 * Static function for Quaternion multiplication
	 * Non-commutative (ORDER MATTERS)
	 * Format:  T = Q * R
	 * @param Q
	 * @Param R
	 * @return T
	 */
	public static Versor multiply(Versor Q, Versor R){
		Quaternion t1 = Q.toQuaternion();
		Quaternion t2 = R.toQuaternion();
		t1 = Quaternion.multiply(t1, t2);
		return t1.toVersor();
	}
	
	/**
	 * Returns a rotation matrix in column-major notation.
	 * Transpose the result for row-major notation.
	 * @return
	 */
	public Matrix4f toRotationMatrix(){
		return new Matrix4f(
				new Vector4f((float)(1-2*Math.pow(y, 2)-2*Math.pow(z, 2)), (2*x*y+2*w*z), (2*x*z-2*w*y),0),
				new Vector4f((2*x*y-2*w*z), (float)(1-2*Math.pow(x, 2)-2*Math.pow(z, 2)), (2*y*z+2*w*x),0),
				new Vector4f((2*x*z+2*w*y), (2*y*z-2*w*x), (float)(1-2*Math.pow(x, 2)-2*Math.pow(y, 2)),0),
				new Vector4f(0,0,0,1f));
	}
	
	public Quaternion toQuaternion(){
		return new Quaternion(w, x, y, z);
	}
}
