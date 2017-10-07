package salathiel.interativaarlib.lib;

import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.models.MovementCheckerItem;

//padrao getinstance

//classe principal, responsavel pela logica da lib
public class InteracaoLib {
    private static final int MOVEMENT_THREADS = 4;
    private static int movMinFrames = 30;

    //marcadores que a biblioteca ira 'monitorar'
    private List<InteractiveObject> iobjects;
    private float[][] projectionMatrix;
    private int screenWidth;
    private int screenHeight;
    private MovementCheckerThread[] movimentThreads;
    private int ithread;

    public InteracaoLib(){
        if(OpenCVLoader.initDebug()) Log.d("ocv", "Iniciado");
        else Log.d("ocv", "Erro");
        iobjects = new ArrayList<>();
        screenWidth = 0;
        screenHeight = 0;
        movimentThreads = new MovementCheckerThread[MOVEMENT_THREADS];
        ithread = 0;
    }

    public InteracaoLib(float[][] projectionMatrix, int screenWidth, int screenHeight){
        this();
        this.projectionMatrix = projectionMatrix;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public List<InteractiveObject> getIobjects() {
        return iobjects;
    }

    public void setIobjects(List<InteractiveObject> iobjects) {
        this.iobjects = iobjects;
    }

    public void addIobject(InteractiveObject Iobject){
        iobjects.add(Iobject);
    }

    public float[][] getProjectionMatrix() {
        return projectionMatrix;
    }

    public float[] getProjectionMatrixArray() {
        return MatrixUtil.matrix2vector(projectionMatrix);
    }

    public float[][] getTransposeProjectionMatrix() {
        return MatrixUtil.transposeMatrix(getProjectionMatrix());
    }

    public float[] getTransposeProjectionMatrixArray() {
        return MatrixUtil.transposeVector(getProjectionMatrixArray());
    }

    public void setProjectionMatrix(float[][] projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        this.projectionMatrix = MatrixUtil.vector2matrix(projectionMatrix);
    }

    public void setTransposeProjectionMatrix(float[][] transposeProjectionMatrix) {
        this.projectionMatrix = MatrixUtil.transposeMatrix(transposeProjectionMatrix);
    }

    public void setTransposeProjectionMatrix(float[] transposeProjectionMatrix) {
        this.projectionMatrix = MatrixUtil.vector2matrix(MatrixUtil.transposeVector(transposeProjectionMatrix));
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }


    public void update(){
        ApproximationChecker.checkApproximation(iobjects);
        OcclusionChecker.checkOcclusion(iobjects);
    }

    public void update(Mat cameraImage){
        update();
        if(cameraImage != null && (!cameraImage.empty()) && projectionMatrix != null
                && screenWidth > 0 && screenHeight > 0) {
            MovementCheckerItem mci = new MovementCheckerItem(iobjects, cameraImage, projectionMatrix, screenWidth, screenHeight);
            if(movimentThreads[ithread] == null){
                movimentThreads[ithread] = new MovementCheckerThread(movMinFrames/MOVEMENT_THREADS);
                movimentThreads[ithread].addFrame2Check(mci);
                new Thread(movimentThreads[ithread]).start();
            }else{
                movimentThreads[ithread].addFrame2Check(mci);
                Log.v("fila", "acumulado de "+ithread+": "+movimentThreads[ithread].getFrames2CheckSize());
            }
            ithread = (ithread+1) % MOVEMENT_THREADS;
        }
    }

    public void update(byte[] cameraImage){
        if(screenWidth > 0 && screenHeight > 0) {
            Mat mat = new Mat(screenHeight, screenWidth, CvType.CV_8UC1);
            mat.put(0, 0, cameraImage);
            update(mat);
        }
    }

    public void setApproximationMinDistance(double minDistance){
        ApproximationChecker.minDistance = minDistance;
    }

    public void setMovementPyr_scale(float pyr_scale){
        MovementChecker.pyr_scale = pyr_scale;
    }

    public void setMovementLevels(int levels){
        MovementChecker.levels = levels;
    }

    public void setMovementWinsize(int winsize){
        MovementChecker.winsize = winsize;
    }

    public void setMovementIterations(int iterations){
        MovementChecker.iterations = iterations;
    }

    public void setMovementStep(int step){
        MovementChecker.step = step;
    }

    public void setMovementMinFrame(int frames) {movMinFrames = frames;}

    public void setMovementMaxFrameSave(int frames){
        MovementChecker.maxFrameSave = frames;
    }
}