package salathiel.interativaarlib.models;

import salathiel.interativaarlib.api.ApproximationListener;
import salathiel.interativaarlib.api.MovementListener;
import salathiel.interativaarlib.api.TouchListener;
import salathiel.interativaarlib.util.MatrixUtil;

public class InteractiveObject {

    private static int UNIQUE_ID = 0;

	private int id;
	private boolean visible;
    private float[][] transformationMatrix;
    private ApproximationListener approximationListener;
    private MovementListener movementListener;
    private TouchListener touchListener;

    public InteractiveObject(int id){
        this.id = id;
        this.visible = false;
        this.transformationMatrix = MatrixUtil.identity();
        UNIQUE_ID++;
    }

    public InteractiveObject(){
        this(UNIQUE_ID);
    }

    //retorna id unico
    public int getId() {
        return id;
    }
    //seta id unico
    public void setId(int id) {
        this.id = id;
    }
    
    //retorna se marcador esta visivel
    public boolean isVisible(){
    	return visible;
    }
    
    //seta marcador visivel
    public void setVisible(boolean visible){
    	this.visible = visible;
    }


    public float[][] getTransformationMatrix() {
        return transformationMatrix;
    }

    public float[] getTransformationMatrixArray(){
        return MatrixUtil.matrix2vector(getTransformationMatrix());
    }

    public float[][] getTransposeTransformationMatrix() {
        return MatrixUtil.transposeMatrix(getTransformationMatrix());
    }

    public float[] getTransposeTransformationMatrixArray(){
        return MatrixUtil.transposeVector(getTransformationMatrixArray());
    }

    public void setTransformationMatrix(float[][] transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
    }

    public void setTransformationMatrix(float[] transformationVector) {
        this.transformationMatrix = MatrixUtil.vector2matrix(transformationVector);
    }

    public void setTransposeTransformationMatrix(float[][] transposeTransformationMatrix) {
        this.transformationMatrix = MatrixUtil.transposeMatrix(transposeTransformationMatrix);
    }

    public void setTransposeTransformationMatrix(float[] transposeTransformationMatrix) {
        this.transformationMatrix = MatrixUtil.vector2matrix(MatrixUtil.transposeVector(transposeTransformationMatrix));
    }

    public Vector3D getObjectPosition() {
        return new Vector3D(transformationMatrix[0][3], transformationMatrix[1][3], transformationMatrix[2][3]);
    }
    
    public void translate(float x, float y, float z) {
        float[][] mtranslate = new float[][]{
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1}
        };
        transformationMatrix = MatrixUtil.multiply(transformationMatrix, mtranslate);
    }

	public void translate(Vector3D translation) {
        translate(translation.getX(), translation.getY(), translation.getZ());
	}

    public void scale(float x, float y, float z) {
        float[][] mscale = new float[][]{
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, z, 0},
                {0, 0, 0, 1}
        };
        transformationMatrix = MatrixUtil.multiply(transformationMatrix, mscale);
    }

	public void scale(Vector3D s) {
		scale(s.getX(), s.getY(), s.getZ());
	}
	

    //gets e sets dos listeners
    public ApproximationListener getApproximationListener(){
        return approximationListener;
    }
	
	public void setApproximationListener(ApproximationListener listener){
        this.approximationListener = listener;
    }

    public MovementListener getMovementListener() {
        return movementListener;
    }

    public void setMovementListener(MovementListener movementListener) {
        this.movementListener = movementListener;
    }

    public TouchListener getTouchListener() {
        return touchListener;
    }

    public void setTouchListener(TouchListener touchListener) {
        this.touchListener = touchListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveObject that = (InteractiveObject) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}