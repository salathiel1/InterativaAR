package salathiel.interativaarlib.lib;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import salathiel.interativaarlib.exception.InterativaException;
import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.util.MovementCheckerUtil;
import salathiel.interativaarlib.util.ScreenCalcUtil;

/**
 * Created by salathiel on 17/10/17.
 */
public class OcclusionChecker {

    public static double MIN_SIMILARITY = 0.01;

    private static final int FRAMES_SKIP_INI = 90;
    private static final int FRAMES_CALC_OCCLUSION = 5;
    private static int iFramesSkip = 0;
    private static Map<InteractiveObject, Mat> baseBackgrounds = new HashMap<>();
    private static Map<InteractiveObject, Boolean> objectPressed = new HashMap<>();
    private static Map<InteractiveObject, ArrayList<Boolean>> objectFramesToCalc = new HashMap<>();
    private static int isave = 0;

    public static void checkOcclusion(List<InteractiveObject> interactiveObjects, Mat cameraImage, float[][] projectionMatrix, int width, int height){
        for(InteractiveObject iObj : interactiveObjects) {
            if (iObj.getOcclusionListener() != null && iObj.isVisible()) {
                if(iFramesSkip < FRAMES_SKIP_INI) iFramesSkip++;

                Mat ROIobj;
                try {
                    ROIobj = ScreenCalcUtil.getROIObjectMinSize(iObj, cameraImage, 60, projectionMatrix, width, height);
                    Imgproc.medianBlur(ROIobj, ROIobj, 9);
                }catch (InterativaException e){
                    continue;
                }

                if(baseBackgrounds.containsKey(iObj) && objectPressed.containsKey(iObj)) {
                    boolean hasPressed = objectPressed.get(iObj);
                    boolean ioclusion = calOcclusion(ROIobj, baseBackgrounds.get(iObj), cameraImage, projectionMatrix, width, height);

                    ArrayList<Boolean> framesOclusion;
                    if(objectFramesToCalc.containsKey(iObj)){
                        framesOclusion = objectFramesToCalc.get(iObj);
                        if(framesOclusion.size() < FRAMES_CALC_OCCLUSION){
                            framesOclusion.add(ioclusion);
                            objectFramesToCalc.put(iObj, framesOclusion);
                            continue;
                        }
                    }else{
                        framesOclusion = new ArrayList<>();
                        framesOclusion.add(ioclusion);
                        objectFramesToCalc.put(iObj, framesOclusion);
                        continue;
                    }

                    int countTrue = 0;
                    int countFalse = 0;
                    Log.v("framesOc", Arrays.toString(framesOclusion.toArray()));
                    for(Boolean oc : framesOclusion){
                        if(oc == true) countTrue++;
                        else countFalse++;
                    }

                    boolean oclusion = countTrue > countFalse;

                    objectFramesToCalc.remove(iObj);

                    Log.v("touc oclusion", ""+oclusion);
                    Log.v("touc has", ""+hasPressed);
                    if (oclusion == true) {
                        if (hasPressed == false)
                            iObj.getOcclusionListener().occlusionEnter();
                        iObj.getOcclusionListener().occlusionPressed();
                        objectPressed.put(iObj, true);
                    } else {
                        if (hasPressed == true)
                            iObj.getOcclusionListener().occlusionReleased();
                        objectPressed.put(iObj, false);
                    }
                }else{
                    if(iFramesSkip == FRAMES_SKIP_INI) {
                        baseBackgrounds.put(iObj, ROIobj);
                        objectPressed.put(iObj, false);
                    }
                }
            }
        }

    }

    private static boolean calOcclusion(Mat ROIobj, Mat ROIbase, Mat cameraImage, float[][] projectionMatrix, int width, int height){
        isave++;
        if(isave > 10) {
            MovementCheckerUtil.saveMat(ROIbase, "base");
            MovementCheckerUtil.saveMat(ROIobj, "atual");
        }


        //Mat hsvROIbase = convert2HSV(ROIbase);
        //Mat hsvROIobj = convert2HSV(ROIobj);

        Mat histBase = calcHistogram(/*hsv*/ROIbase);
        Mat histObj = calcHistogram(/*hsv*/ROIobj);

        Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(histObj, histObj, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        double similarity = Imgproc.compareHist(histBase, histObj, 0);

        if(isave > 10) {
            MovementCheckerUtil.saveMat(new Mat(10, 10, CvType.CV_8U, new Scalar(0)), ""+(similarity < MIN_SIMILARITY));
            isave = 0;
        }

        Log.v("touch", "" + similarity);

        return similarity < MIN_SIMILARITY;
    }

    private static Mat convert2HSV(Mat imagem){
        Mat mat = imagem.clone();
        Log.v("type", ""+mat.type());
        if(mat.type() == 16) Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV);
        else if(mat.type() == 24){
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);
        }
        else if(mat.type() == 0) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);
        }
        else throw new CvException("Camera image need to be gray, RGB or RGBA");

        return mat;
    }

    private static Mat calcHistogram(Mat mat){
        Mat hist = new Mat();
        List<Mat> listMat = new ArrayList<>();
        listMat.add(mat);
        Imgproc.calcHist(listMat, new MatOfInt(0, 1), new Mat(), hist, new MatOfInt(50, 60), new MatOfFloat(0f, 180f, 0f, 256), false);
        return hist;
    }
}
