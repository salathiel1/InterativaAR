package salathiel.interativaarlib.models;

//receber x,y,z nos set
//receber e retornar r matriz para transformacao
//retirar abstract
//atualizar equals (occlusionchecker utiliza esse equals)

//Interface que um marcador precisa implementar para ser interavel

import salathiel.interativaarlib.api.ApproximationListener;
import salathiel.interativaarlib.api.MovementListener;
import salathiel.interativaarlib.api.OcclusionListener;
import salathiel.interativaarlib.lib.MatrixUtil;

public class InteractiveObject {
    
	//numero de identificacao unico do objeto
    private int id;
	//marcador visivel
    private boolean visible;
	//marcador ja esteve visivel?
    private boolean alreadyVisible;

    //atributos do marcador, que representao a matriz de transfomacao
    private float[][] transformationMatrix;
    
	//contola disparo de acao de occlusao
    private boolean triggerOcclusion;
    
    //listener do metodo aproximacao
    private ApproximationListener approximationListener;
    //listener do metodo de oclusao
    private OcclusionListener occlusionListener;
    //listener do metodo de movimentacao
    private MovementListener movementListener;

    public InteractiveObject(int id){
        this.id = id;
        this.visible = false;
        this.alreadyVisible = false;
        this.triggerOcclusion = true;
        this.transformationMatrix = MatrixUtil.identity();
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
    	if(this.visible == false && visible == true && alreadyVisible == true && this.triggerOcclusion == false)
    		this.triggerOcclusion = true;
    	if(visible == true && this.alreadyVisible == false){
    		this.triggerOcclusion = false;
    		this.alreadyVisible = true;
    	}
    	this.visible = visible;
    }


    public float[][] getTransformationMatrix() {
        return transformationMatrix;
    }

    public float[] getTransformationMatrixArray(){
        return MatrixUtil.matrix2vector(transformationMatrix);
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
    
    //retorna se o marcador ja esteve visivel
    public boolean isAlreadyVisible(){
    	return alreadyVisible;
    }
    
    //set e gets dos atributos do marcador
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

	public Vector3D getMarkScale() {
		return new Vector3D(transformationMatrix[0][0], transformationMatrix[1][1], transformationMatrix[2][2]);
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
	
	//disparar acao de occlusao?
	public boolean triggerOcclusion(){
		return triggerOcclusion;
	}
	
	public void setTriggerOcclusion(boolean triggerOcclusion){
		this.triggerOcclusion = triggerOcclusion;
	}
	

    //gets e sets dos listeners
    public ApproximationListener getApproximationListener(){
        return approximationListener;
    }
	
	public void setApproximationListener(ApproximationListener listener){
        this.approximationListener = listener;
    }
    
    public void clearApproximationListener(){
    	this.approximationListener = null; 
    }
   
    public OcclusionListener getOcclusionListener() {
		return occlusionListener;
	}

    public void setOcclusionListener(OcclusionListener occlusionListener) {
		this.occlusionListener = occlusionListener;
	}

    public MovementListener getMovementListener() {
        return movementListener;
    }

    public void setMovementListener(MovementListener movementListener) {
        this.movementListener = movementListener;
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