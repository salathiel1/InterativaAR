package salathiel.interativaarlib.lib;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.models.MovementCheckerItem;
import salathiel.interativaarlib.util.MatrixUtil;
import salathiel.interativaarlib.util.MovementCheckerUtil;

//padrao getinstance

//classe principal, responsavel pela logica da lib
public class InterativaLib implements SensorEventListener {
    private static final String TAG = "interativa";
    private static final int MOVEMENT_THREADS = 20;
    private static InterativaLib instance;

    private List<InteractiveObject> iobjects;
    private float[][] projectionMatrix;
    private int screenWidth;
    private int screenHeight;
    private MovementCheckerThread[] movimentThreads;
    private int ithread;
    private boolean processFrame;
    private Mat prevgray;
    private Sensor gyroscopeSensor;
    private boolean screenRotating;
    private float gyroError;

    public InterativaLib(){
        if(OpenCVLoader.initDebug()) Log.d(TAG, "Ok!");
        else Log.d(TAG, "Error!");
        iobjects = new ArrayList<>();
        screenWidth = 0;
        screenHeight = 0;
        movimentThreads = new MovementCheckerThread[MOVEMENT_THREADS];
        ithread = 0;
        processFrame = true;
        prevgray = null;
        screenRotating = false;
        gyroError = 0.03f;
    }

    public InterativaLib(float[][] projectionMatrix, int screenWidth, int screenHeight){
        this();
        this.projectionMatrix = projectionMatrix;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public static synchronized InterativaLib getInstance(){
        if(instance == null)
            instance = new InterativaLib();
        return instance;
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
        if(screenRotating) return;

        processFrame = !processFrame;
        if(!processFrame) prevgray = cameraImage;

        if(cameraImage != null && (!cameraImage.empty()) && projectionMatrix != null
                && screenWidth > 0 && screenHeight > 0 && processFrame) {
            MovementCheckerItem mci = new MovementCheckerItem(iobjects, cameraImage, prevgray, projectionMatrix, screenWidth, screenHeight);
            if(movimentThreads[ithread] == null){
                movimentThreads[ithread] = new MovementCheckerThread();
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

    public void setMovementRigor(int rigor){
        MovementCheckerUtil.winsize = rigor;
    }

    public void setDebugMovementMaxFrameSave(int frames){
        MovementCheckerUtil.maxFrameSave = frames;
    }

    public void useGyroMovement(SensorManager sensorManager){
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void setGyroError(float gyroError) {
        this.gyroError = gyroError;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > gyroError || event.values[0] < -gyroError){
            screenRotating = true;
        }else if(event.values[1] > gyroError || event.values[1] < -gyroError){
            screenRotating = true;
        }else if(event.values[2] > gyroError || event.values[2] < -gyroError){
            screenRotating = true;
        }else{
            screenRotating = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}