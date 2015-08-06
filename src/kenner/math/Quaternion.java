package kenner.math;

/**
 * Class to basically be extended by Versor (Rotation Quaternion).
 * W = Angle
 * @author kenner
 */
public class Quaternion {
	//must be protected (package-access) for use with Versors.
	protected float w, x, y, z;
	
	/**
	 * Constructs a Quaternion with all values set at 0.
	 */
	public Quaternion(){
		w = x = y = z = 0;
	}
	
	/**
	 * Constructs a Quaternion with given values.
	 * @param w
	 * @param x
	 * @param y
	 * @param z
	 */
	public Quaternion(float w, float x, float y, float z){
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Normalises the Quaterion.  Length is calculated with all 4 components.
	 */
	public void normalise(){
		float length = (float) Math.sqrt((w*w+x*x+y*y+z*z));
		w /= length;
		x /= length;
		y /= length;
		z /= length;
	}
	
	/**
	 * Conjugate of a quaternion (Q' = [w, -v])
	 */
	public void conjugate(){
		x = -x;
		y = -y;
		z = -z;
	}
	
	/**
	 * Static function for Quaternion multiplication
	 * Non-commutative (ORDER MATTERS)
	 * Format:  T = Q * R
	 * @param Q
	 * @Param R
	 * @return T
	 */
	public static Quaternion multiply(Quaternion Q, Quaternion R){
		return new Quaternion(
				(R.w*Q.w - R.x*Q.x - R.y*Q.y - R.z*Q.z),
				(R.w*Q.x + R.x*Q.w - R.y*Q.z + R.z*Q.y),
				(R.w*Q.y + R.x*Q.z + R.y*Q.w - R.z*Q.x),
				(R.w*Q.z - R.x*Q.y + R.y*Q.x + R.z*Q.w));
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}
	
	public float[] toFloatArray(){
		float[] a = {w, x, y, z};
		return a;
	}
	
	public Versor toVersor(){
		Versor v = new Versor(0, 0, 0, 0);
		v.w = w;
		v.x = x;
		v.y = y;
		v.z = z;
		return v;
	}
}
