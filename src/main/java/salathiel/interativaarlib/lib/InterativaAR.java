package salathiel.interativaarlib.lib;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.util.MatrixUtil;
import salathiel.interativaarlib.util.MovementCheckerUtil;


public class InterativaAR implements SensorEventListener {
    public static boolean debugMode = false;
    private static final String TAG = "interativa";

    private List<InteractiveObject> iobjects;
    private float[][] projectionMatrix;
    private int screenWidth;
    private int screenHeight;
    private boolean processFrame;
    private Mat prevgray;
    private Sensor gyroscopeSensor;
    private boolean screenRotating;
    private float gyroError;

    public InterativaAR(){
        if(OpenCVLoader.initDebug()) Log.d(TAG, "Ok!");
        else Log.d(TAG, "Error!");
        iobjects = new ArrayList<>();
        screenWidth = 0;
        screenHeight = 0;
        processFrame = false;
        prevgray = null;
        screenRotating = false;
        gyroError = 0.03f;
    }

    public InterativaAR(float[][] projectionMatrix, int screenWidth, int screenHeight){
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
        TouchChecker.getInstance().update(iobjects, projectionMatrix, screenWidth, screenHeight);
    }

    public void update(Mat cameraImage){
        update();
        if(processFrame) OcclusionChecker.checkOcclusion(iobjects, cameraImage, projectionMatrix, screenWidth, screenHeight);
        if(!screenRotating && processFrame) MovementChecker.checkMovement(iobjects, cameraImage, prevgray, projectionMatrix, screenWidth, screenHeight);

        processFrame = !processFrame;
        if(!processFrame) prevgray = cameraImage;
    }

    public void update(byte[] cameraImage){
        if(screenWidth > 0 && screenHeight > 0) {
            Mat mYuv = new Mat(screenHeight + screenHeight / 2, screenWidth, CvType.CV_8UC1);
            mYuv.put(0, 0, cameraImage);
            Mat mRgba = new Mat();

            Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);

            update(mRgba);
        }
    }

    public void setApproximationMinDistance(double minDistance){
        ApproximationChecker.minDistance = minDistance;
    }

    public void setMovementRigor(int rigor){
        MovementCheckerUtil.winsize = rigor;
    }

    public void setDebugMode(boolean d){
        debugMode = d;
    }

    public void useGyroMovement(SensorManager sensorManager){
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void setGyroError(float gyroError) {
        this.gyroError = gyroError;
    }

    public void setOcclusionRigor(double rigor){
        OcclusionChecker.RIGOR = rigor;
    }

    public View.OnTouchListener getViewTouchListener(){
        return TouchChecker.getInstance();
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