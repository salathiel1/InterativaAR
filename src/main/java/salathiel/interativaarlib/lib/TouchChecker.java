package salathiel.interativaarlib.lib;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;

import salathiel.interativaarlib.exception.InterativaException;
import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.util.ScreenCalcUtil;

/**
 * Created by salathiel on 15/10/17.
 */
public class TouchChecker implements View.OnTouchListener{

    private static List<InteractiveObject> interactiveObjects;
    private float[][] projectionMatrix;
    private int width;
    private int height;
    private static TouchChecker instance;

    public TouchChecker(){}

    public static synchronized TouchChecker getInstance(){
        if(instance == null)
            instance = new TouchChecker();
        return instance;
    }

    public void update(List<InteractiveObject> interactiveObjects, float[][] projectionMatrix, int width, int height) {
        this.interactiveObjects = interactiveObjects;
        this.projectionMatrix = projectionMatrix;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(interactiveObjects == null || projectionMatrix == null || width <= 0 || height <= 0)
            return false;

        for(InteractiveObject iObj : interactiveObjects){
            if(iObj.getTouchListener() != null && iObj.isVisible()){
                int[][] screenPosObj;
                try {
                    screenPosObj = ScreenCalcUtil.calcScreenRect(iObj, projectionMatrix, width, height);
                } catch (InterativaException e) {
                    continue;
                }

                int tx = (int) (event.getX() * width) / v.getWidth();
                int ty = (int) (event.getY() * height) / v.getHeight();

                Point touchPoint = new Point(tx, ty);
                Point r1 = new Point(screenPosObj[0][0], screenPosObj[0][1]);
                Point r2 = new Point(screenPosObj[1][0], screenPosObj[1][1]);
                Log.v("touch", touchPoint + "," + r1 + ", " + r2);
                if(ScreenCalcUtil.pointInsideRect(touchPoint, r1, r2))
                    iObj.getTouchListener().touched();

            }
        }

        return true;
    }
}
