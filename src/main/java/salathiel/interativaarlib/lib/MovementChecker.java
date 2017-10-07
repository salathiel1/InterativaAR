package salathiel.interativaarlib.lib;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.models.Movement;

/**
 * Created by salathiel on 20/09/17.
 */
public class MovementChecker{

    //minimo de intensidade nescessaria para que o vetor de movimentacao seja considerado
    public static  int MIN_INTEN_V = 8;
    public static int MIN_SIZE_ROI = 300;

    public static float pyr_scale = 0.5f;
    public static int levels = 3;
    public static int winsize=16;
    public static int iterations = 3;
    public static int step = 3;

    private static int framesave = 0;
    private static int idframe = 0;
    public static int maxFrameSave = 10;

    //calcula a direcao do movimento
    private static float[] calcMovement(Mat flow, Mat gray, int step, Point roiRectp1, Point roiRectp2)
    {
        float vx = 0;
        float vy = 0;
        float ivpx = 0;
        float ivnx = 0;
        float ivpy = 0;
        float ivny = 0;
        float[] v_result = new float[2];
        v_result[0] = 0;
        v_result[1] = 0;
        boolean intersectRoi = false;

        for(int y = 0; y < flow.rows(); y += step){
            for(int x = 0; x < flow.cols(); x += step)
            {
                double[] fxy = flow.get(y, x);
                double dx = fxy[0];
                double dy = fxy[1];
                if(Math.abs(dx) >= MIN_INTEN_V || Math.abs(dy) >= MIN_INTEN_V){
                    Point p2 = new Point((int) x + fxy[0], (int) y + fxy[1]);
                    if(lineIntersectsRect(new Point(x,y), p2, new Rect(roiRectp1, roiRectp2))) {
                        Imgproc.arrowedLine(gray, new Point(x, y), p2, new Scalar(0, 0, 0));
                        intersectRoi = true;
                    }else{
                        Imgproc.arrowedLine(gray, new Point(x, y), p2, new Scalar(200, 200, 200));
                    }

                    vx += dx > 0 ? 1 : -1;
                    vy += dy > 0 ? 1 : -1;

                    if(dx > 0) ivpx += dx;
                    else ivnx += dx;
                    if(dy > 0) ivpy += dy;
                    else ivny += dy;
                }
            }
        }

        if(intersectRoi) {
            if (vx > 0)
                v_result[0] = ivpx;
            else if (vx < 0)
                v_result[0] = ivnx;

            if (vy > 0)
                v_result[1] = -ivpy;
            else if (vy < -0)
                v_result[1] = -ivny;
        }

        Log.v("movf", v_result[0]+", "+v_result[1]);

        return v_result;
    }

    private static boolean lineIntersectsRect(Point p1, Point p2, Rect r)
    {
        return lineIntersectsLine(p1, p2, new Point(r.x, r.y), new Point(r.x + r.width, r.y)) ||
                lineIntersectsLine(p1, p2, new Point(r.x + r.width, r.y), new Point(r.x + r.width, r.y + r.height)) ||
                lineIntersectsLine(p1, p2, new Point(r.x + r.width, r.y + r.height), new Point(r.x, r.y + r.height)) ||
                lineIntersectsLine(p1, p2, new Point(r.x, r.y + r.height),new Point(r.x, r.y)) ||
                (r.contains(p1) && r.contains(p2));
    }

    private static boolean lineIntersectsLine(Point l1p1, Point l1p2, Point l2p1, Point l2p2)
    {
        double q = (l1p1.y - l2p1.y) * (l2p2.x - l2p1.x) - (l1p1.x - l2p1.x) * (l2p2.y - l2p1.y);
        double d = (l1p2.x - l1p1.x) * (l2p2.y - l2p1.y) - (l1p2.y - l1p1.y) * (l2p2.x - l2p1.x);

        if( d == 0 )
        {
            return false;
        }

        double r = q / d;

        q = (l1p1.y - l2p1.y) * (l1p2.x - l1p1.x) - (l1p1.x - l2p1.x) * (l1p2.y - l1p1.y);
        double s = q / d;

        if( r < 0 || r > 1 || s < 0 || s > 1 )
        {
            return false;
        }

        return true;
    }

    //converte os pontos 3d do objeto em pontos 2d
    private static int[][] calc2dPoints(InteractiveObject iobject, float[][] projectionMatrix, int width, int height){
        float[] pos1 = new float[]{-1, 1, 0, 1};
        float[] pos2 = new float[]{1, -1, 0, 1};
        pos1 = MatrixUtil.convert2d(pos1, iobject.getTransformationMatrix(), projectionMatrix, width, height);
        pos2 = MatrixUtil.convert2d(pos2, iobject.getTransformationMatrix(), projectionMatrix, width, height);
        return new int[][]{
                {(int)pos1[0], (int)pos1[1]},
                {(int)pos2[0], (int)pos2[1]}
        };
    }

    //checa se algum movimento occorreu
    public static Movement checkMovement(List<InteractiveObject> iobjects, Mat camera, Mat prevgray, float[][] projectionMatrix, int width, int height){
        if( prevgray != null && !prevgray.empty() )
        {
            for(InteractiveObject io : iobjects) {
                if(io.getMovementListener() != null && io.isVisible()) {
                    Mat gray = new Mat();
                    Mat flow = new Mat();
                    if(camera.channels() == 3)
                        Imgproc.cvtColor(camera, gray, Imgproc.COLOR_BGR2GRAY);
                    else
                        gray = camera.clone();

                    int[][] points2d = calc2dPoints(io, projectionMatrix, width, height);

                    if(points2d[0][0] < 0 || points2d[0][1] < 0 || points2d[1][0] < 0 || points2d[1][1] < 0) continue;
                    if(points2d[0][0] > gray.cols() || points2d[1][0] > gray.cols()) continue;
                    if(points2d[0][1] > gray.rows() || points2d[1][1] > gray.rows()) continue;

                    int x1,x2,y1,y2;
                    if(points2d[0][0] < points2d[1][0]) {
                        x1 = points2d[0][0];
                        x2 = points2d[1][0];
                    }else{
                        x1 = points2d[1][0];
                        x2 = points2d[0][0];
                    }

                    if(points2d[0][1] < points2d[1][1]){
                        y1 = points2d[0][1];
                        y2 = points2d[1][1];
                    }else{
                        y1 = points2d[1][1];
                        y2 = points2d[0][1];
                    }

                    int roix1, roix2, compx2, compx1, roiy1, roiy2, compy1, compy2;
                    roix1 = x1;
                    roix2 = x2;
                    roiy1 = y1;
                    roiy2 = y2;
                    compx1 = compx2 = compy1 = compy2 = 0;
                    if( (x2 - x1) < MIN_SIZE_ROI){
                        int need2Min = (MIN_SIZE_ROI - (x2 - x1))/2;
                        compx1 = -(x1 - need2Min);
                        compx2 = (x2 + need2Min) - width;
                        if(compx2 > 0){
                            if( (x1 - need2Min - compx2) < 0) roix1 = 0;
                            else roix1 = x1 - need2Min - compx2;
                        }else{
                            if(x1 - need2Min >= 0) roix1 = x1 - need2Min;
                            else roix1 = 0;
                        }
                        if(compx1 > 0){
                            if( (x2 + need2Min + compx1) > width) roix2 = width;
                            else roix2 = x2 + need2Min + compx1;
                        }else{
                            if(x2 + need2Min <= width) roix2 = x2 + need2Min;
                            else roix2 = width;
                        }
                    }

                    if( (y2 - y1) < MIN_SIZE_ROI){
                        int need2Min = (MIN_SIZE_ROI - (y2 - y1))/2;
                        compy1 = -(y1 - need2Min);
                        compy2 = (y2 + need2Min) - height;
                        if(compy2 > 0){
                            if( (y1 - need2Min - compy2) < 0) roiy1 = 0;
                            else roiy1 = y1 - need2Min - compy2;
                        }else{
                            if(y1 - need2Min >= 0) roiy1 = y1 - need2Min;
                            else roiy1 = 0;
                        }
                        if(compy1 > 0){
                            if( (y2 + need2Min + compy1) > height) roiy2 = height;
                            else roiy2 = y2 + need2Min + compy1;
                        }else{
                            if(y2 + need2Min <= height) roiy2 = y2 + need2Min;
                            else roix2 = height;
                        }
                    }

                    Mat ROIprevgray = prevgray.submat(roiy1, roiy2, roix1, roix2);
                    Mat ROIgray = gray.submat(roiy1, roiy2, roix1, roix2);

                    Video.calcOpticalFlowFarneback(ROIprevgray, ROIgray, flow, pyr_scale, levels, winsize, iterations, 5, 1.1, Video.OPTFLOW_USE_INITIAL_FLOW);

                    Point p1Roi = new Point(x1-roix1, y1-roiy1);
                    Point p2Roi = new Point(x2-roix1, y2-roiy1);
                    float[] result = calcMovement(flow, ROIgray, step, p1Roi, p2Roi);

                    if(framesave > maxFrameSave) {
                        saveMat(ROIgray, result);
                        framesave = 0;
                    }
                    framesave++;

                    return new Movement(io, result);
                }
            }
        }

        return null;
    }

    static void saveMat(Mat subimg, float[] result){
        Imgproc.putText(subimg, String.valueOf(result[0])+","+String.valueOf(result[1]), new Point(0,0), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(0,255,0));
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(subimg.cols(), subimg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(subimg, bmp);
        } catch (CvException e) {
            Log.d("Erro", e.getMessage());
        }

        subimg.release();
        FileOutputStream out = null;
        String filename = "frame"+idframe+".png";
        idframe++;

        File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
        boolean success = true;
        if (!sd.exists()) {
            success = sd.mkdir();
        }
        if (success) {
            File dest = new File(sd, filename);

            try {
                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("mov", e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        Log.d("mov", "Frame save!!");
                    }
                } catch (IOException e) {
                    Log.d("mov", e.getMessage() + "Error");
                    e.printStackTrace();
                }
            }
        }
    }
}
