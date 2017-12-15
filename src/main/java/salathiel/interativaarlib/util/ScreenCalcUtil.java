package salathiel.interativaarlib.util;

import android.util.Log;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;

import salathiel.interativaarlib.exception.InterativaException;
import salathiel.interativaarlib.models.InteractiveObject;

/**
 * Created by salathiel on 15/10/17.
 */
public class ScreenCalcUtil {

    //converte os pontos 3d do objeto em pontos 2d
    public static int[][] calcScreenRect(InteractiveObject iobject, float[][] projectionMatrix, int width, int height) throws InterativaException{
        int[] pos1;
        int[] pos2;
        try {
            float[] fpos1 = new float[]{-1f, 1f, 0, 1};
            float[] fpos2 = new float[]{1f, -1f, 0, 1};
            pos1 = MatrixUtil.convert2d(fpos1, iobject.getTransformationMatrix(), projectionMatrix, width, height);
            pos2 = MatrixUtil.convert2d(fpos2, iobject.getTransformationMatrix(), projectionMatrix, width, height);
        }catch (InterativaException e){
            float[] fpos1 = new float[]{-0.8f, 0.8f, 0, 1};
            float[] fpos2 = new float[]{0.8f, -0.8f, 0, 1};
            pos1 = MatrixUtil.convert2d(fpos1, iobject.getTransformationMatrix(), projectionMatrix, width, height);
            pos2 = MatrixUtil.convert2d(fpos2, iobject.getTransformationMatrix(), projectionMatrix, width, height);
        }
        float x1,x2,y1,y2;

        if(pos1[0] < pos2[0]) {
            x1 = pos1[0];
            x2 = pos2[0];
        }else{
            x1 = pos2[0];
            x2 = pos1[0];
        }

        if(pos1[1] < pos2[1]){
            y1 = pos1[1];
            y2 = pos2[1];
        }else{
            y1 = pos2[1];
            y2 = pos1[1];
        }

        return new int[][]{
                {(int)x1, (int)y1},
                {(int)x2, (int)y2}
        };
    }

    public static boolean lineIntersectsRect(Point p1, Point p2, Rect r)
    {
        return lineIntersectsLine(p1, p2, new Point(r.x, r.y), new Point(r.x + r.width, r.y)) ||
                lineIntersectsLine(p1, p2, new Point(r.x + r.width, r.y), new Point(r.x + r.width, r.y + r.height)) ||
                lineIntersectsLine(p1, p2, new Point(r.x + r.width, r.y + r.height), new Point(r.x, r.y + r.height)) ||
                lineIntersectsLine(p1, p2, new Point(r.x, r.y + r.height),new Point(r.x, r.y)) ||
                (r.contains(p1) && r.contains(p2));
    }

    public static boolean lineIntersectsLine(Point l1p1, Point l1p2, Point l2p1, Point l2p2)
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

    public static boolean pointInsideRect(Point p, Point r1, Point r2){
        return (p.x >= r1.x) && (p.x <= r2.x) && (p.y >= r1.y) && (p.y <= r2.y);
    }

    public static Mat getROIObject(InteractiveObject iObject, Mat cameraImage, float[][] projectionMatrix, int width, int height) throws InterativaException{
        int[][] screenPos = calcScreenRect(iObject, projectionMatrix, width, height);
        Log.v("screen", screenPos[0][0] + "," + screenPos[0][1] + "," + screenPos[1][0] + "," + screenPos[1][1]);
        return cameraImage.submat(screenPos[0][1], screenPos[1][1], screenPos[0][0], screenPos[1][0]);
    }

    public static Mat getROIObjectMinSize(InteractiveObject io, Mat cameraImage, int minSize, float[][] projectionMatrix, int width, int height) throws InterativaException{
        int[][] points2d = calcScreenRect(io, projectionMatrix, width, height);

        int x1,x2,y1,y2;
        x1 = points2d[0][0];
        x2 = points2d[1][0];
        y1 = points2d[0][1];
        y2 = points2d[1][1];

        int roix1, roix2, compx2, compx1, roiy1, roiy2, compy1, compy2;
        roix1 = x1;
        roix2 = x2;
        roiy1 = y1;
        roiy2 = y2;
        compx1 = compx2 = compy1 = compy2 = 0;
        if( (x2 - x1) < minSize){
            int need2Min = (minSize - (x2 - x1))/2;
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

        if( (y2 - y1) < minSize){
            int need2Min = (minSize - (y2 - y1))/2;
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

        return cameraImage.submat(roiy1, roiy2, roix1, roix2);
    }
}
