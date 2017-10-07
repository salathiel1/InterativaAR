package salathiel.interativaarlib.models;

import org.opencv.core.Mat;

import java.util.List;

/**
 * Created by salathiel on 29/09/17.
 */
public class MovementCheckerItem {

    private List<InteractiveObject> iobjects;
    private Mat cameraImage;
    private float[][] projectionMatrix;
    private int screenWidth;
    private int screenHeight;

    public MovementCheckerItem(List<InteractiveObject> iobjects, Mat cameraImage, float[][] projectionMatrix, int screenWidth, int screenHeight) {
        this.iobjects = iobjects;
        this.cameraImage = cameraImage;
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

    public Mat getCameraImage() {
        return cameraImage;
    }

    public void setCameraImage(Mat cameraImage) {
        this.cameraImage = cameraImage;
    }

    public float[][] getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(float[][] projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
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
}
