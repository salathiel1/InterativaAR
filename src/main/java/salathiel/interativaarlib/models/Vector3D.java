package salathiel.interativaarlib.models;

public class Vector3D {

	private float x;
	private float y;
	private float z;
	
	public Vector3D(){
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public Vector3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
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
	
	public double distance(Vector3D j){
        return Math.sqrt( Math.pow(j.getX() - getX(), 2) + Math.pow(j.getY() - getY(), 2) + 
        		Math.pow(j.getZ() - getZ(), 2)  );
    }
	
}
