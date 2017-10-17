package salathiel.interativaarlib.util;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import salathiel.interativaarlib.models.InteractiveObject;

/**
 * Created by salathiel on 15/10/17.
 */
public class ScreenCalcUtil {

    //converte os pontos 3d do objeto em pontos 2d
    public static int[][] calcScreenRect(InteractiveObject iobject, float[][] projectionMatrix, int width, int height){
        float[] pos1 = new float[]{-1, 1, 0, 1};
        float[] pos2 = new float[]{1, -1, 0, 1};
        pos1 = MatrixUtil.convert2d(pos1, iobject.getTransformationMatrix(), projectionMatrix, width, height);
        pos2 = MatrixUtil.convert2d(pos2, iobject.getTransformationMatrix(), projectionMatrix, width, height);

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
}
