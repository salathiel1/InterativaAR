package salathiel.interativaarlib.lib;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.util.ScreenCalcUtil;

/**
 * Created by salathiel on 17/10/17.
 */
public class OcclusionChecker {

    private static final double MIN_SIMILARITY = 0.8;
    private static Map<InteractiveObject, Mat> baseBackgrounds = new HashMap<>();
    private static Map<InteractiveObject, Boolean> objectPressed = new HashMap<>();

    public static void checkOcclusion(List<InteractiveObject> interactiveObjects, Mat cameraImage, float[][] projectionMatrix, int width, int height){

        for(InteractiveObject iObj : interactiveObjects) {
            if (iObj.getOcclusionListener() != null && iObj.isVisible()) {
                if(baseBackgrounds.containsKey(iObj) && objectPressed.containsKey(iObj)) {
                    boolean hasPressed = objectPressed.get(iObj);
                    boolean oclusion = calOcclusion(iObj, cameraImage, projectionMatrix, width, height);
                    if (oclusion == true) {
                        iObj.getOcclusionListener().occlusionPressed();
                        if (hasPressed == false)
                            iObj.getOcclusionListener().occlusionEnter();
                        objectPressed.put(iObj, true);
                    } else {
                        if (hasPressed == true)
                            iObj.getOcclusionListener().occlusionReleased();
                        else
                            baseBackgrounds.put(iObj, ScreenCalcUtil.getROIObject(iObj, cameraImage, projectionMatrix, width, height));
                        objectPressed.put(iObj, false);
                    }
                }else{
                    try {
                        baseBackgrounds.put(iObj, ScreenCalcUtil.getROIObject(iObj, cameraImage, projectionMatrix, width, height));
                        objectPressed.put(iObj, false);
                    }catch (CvException e){}
                }
            }
        }

    }

    private static boolean calOcclusion(InteractiveObject iObject, Mat cameraImage, float[][] projectionMatrix, int width, int height){
        Mat ROIobj;
        try {
            ROIobj = ScreenCalcUtil.getROIObject(iObject, cameraImage, projectionMatrix, width, height);
        }catch (CvException e){
            return false;
        }
        Mat ROIbase = baseBackgrounds.get(iObject);
        if(ROIobj.rows() != ROIbase.rows() || ROIobj.cols() != ROIbase.cols()) return false;

        /*int countDiff = 0;
        for(int y = 0; y < ROIobj.rows(); y++) {
            for (int x = 0; x < ROIobj.cols(); x++) {
                double[] valObj = ROIobj.get(y, x);
                double[] valBase = ROIbase.get(y, x);
                double diff = Math.abs(valObj[0] - valBase[0]);
                if(diff > 30) countDiff++;
            }
        }*/

        Mat histBase = new Mat();
        List<Mat> listROIBase = new ArrayList<>();
        listROIBase.add(ROIbase);
        Imgproc.calcHist(listROIBase, new MatOfInt(0), new Mat(), histBase, new MatOfInt(1), new MatOfFloat(0, 255));

        Mat histObj = new Mat();
        List<Mat> listROIObj = new ArrayList<>();
        listROIObj.add(ROIobj);
        Imgproc.calcHist(listROIObj, new MatOfInt(0), new Mat(), histObj, new MatOfInt(1), new MatOfFloat(0, 255));

        Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(histObj, histObj, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        double similarity = Imgproc.compareHist(histBase, histObj, 0);

        //double minValueDiff = (ROIobj.rows() * ROIobj.cols()) * 0.2;
        //return countDiff > minValueDiff;

        return similarity > MIN_SIMILARITY;
    }
}
