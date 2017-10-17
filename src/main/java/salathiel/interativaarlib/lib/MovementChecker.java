package salathiel.interativaarlib.lib;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import salathiel.interativaarlib.models.Direction;
import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.models.Movement;
import salathiel.interativaarlib.models.MovementCheckerItem;

/**
 * Created by salathiel on 10/10/17.
 */
public class MovementChecker {

    private static final int MOV_COUNT_MAX = 5;
    private static final int MOVEMENT_THREADS = 20;
    private static  int ithread = 0;
    private static MovementCheckerThread[] movimentThreads = new MovementCheckerThread[MOVEMENT_THREADS];
    private static Map<InteractiveObject, Float[]> sumIObjectMov = new HashMap<>();
    private static Map<InteractiveObject, Integer> movCount = new HashMap<>();

    public static void checkMovement(List<InteractiveObject> iobjects, Mat cameraImage, Mat prevgray, float[][] projectionMatrix, int screenWidth, int screenHeight){
        if(cameraImage != null && (!cameraImage.empty()) && projectionMatrix != null
                && screenWidth > 0 && screenHeight > 0) {
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

    //chamado por MovementCheckerThread
    public static void sumMovement(Movement m){
        if(sumIObjectMov.containsKey(m.getIo())){
            Float[] r = sumIObjectMov.get(m.getIo());
            r[0] += m.getResult()[0];
            r[1] += m.getResult()[1];
            sumIObjectMov.put(m.getIo(), r);
        }else{
            sumIObjectMov.put(m.getIo(), m.getResult());
        }

        if(movCount.containsKey(m.getIo())){
            Integer mcount = movCount.get(m.getIo());

            if(m.getResult()[0] == 0 && m.getResult()[1] == 0) mcount += 1;
            else mcount = 0;

            if(mcount >= MOV_COUNT_MAX){
                Float[] result = sumIObjectMov.get(m.getIo());
                if (result[0] != 0 || result[1] != 0) {
                    float intensityMovX = Math.abs(result[0]);
                    float intensityMovY = Math.abs(result[1]);

                    if(intensityMovX > intensityMovY){
                        if(result[0] > 0)
                            m.getIo().getMovementListener().movement(Direction.RIGHT);
                        else
                            m.getIo().getMovementListener().movement(Direction.LEFT);
                    }else{
                        if(result[1] > 0)
                            m.getIo().getMovementListener().movement(Direction.UP);
                        else
                            m.getIo().getMovementListener().movement(Direction.DOWN);
                    }

                }

                movCount.remove(m.getIo());
                sumIObjectMov.remove(m.getIo());
            }else {
                movCount.put(m.getIo(), mcount);
            }

        }else{
            movCount.put(m.getIo(), 0);
        }
    }
}
