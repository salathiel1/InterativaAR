package salathiel.interativaarlib.lib;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import salathiel.interativaarlib.models.InteractiveObject;
import salathiel.interativaarlib.models.Movement;
import salathiel.interativaarlib.models.MovementCheckerItem;

/**
 * Created by salathiel on 29/09/17.
 */

public class MovementCheckerThread implements Runnable{

    private static final int MAX_SIZE_QUEUE = 30;

    private Queue<MovementCheckerItem> frames2Check;
    private Map<InteractiveObject, Float[]> moves;
    private Map<InteractiveObject, Integer> imoves;
    private int skipFrame;
    private Mat prevgray;
    public int movFrames;

    public MovementCheckerThread(int movFrames) {
        this.movFrames = movFrames;
        frames2Check = new LinkedList<>();
        moves = new HashMap<>();
        imoves = new HashMap<>();
        skipFrame = 0;
        prevgray = null;
    }

    public void addFrame2Check(MovementCheckerItem mci){
        frames2Check.add(mci);
    }

    public int getFrames2CheckSize(){
        return frames2Check.size();
    }

    //tavez seja preciso copiar parametros porque objs podem ser mudados dinamicamente
    //implementar skip frame se fila ficar longa
    @Override
    public void run() {
        while(true) {
            if(frames2Check.size() > MAX_SIZE_QUEUE) skipFrame++;
            for(int i = 0; i < skipFrame; i++){
                if(frames2Check.size() > 0) prevgray = frames2Check.remove().getCameraImage();
            }

            if(frames2Check.size() > 0) {
                MovementCheckerItem current = frames2Check.remove();
                Movement mv = MovementChecker.checkMovement(current.getIobjects(), current.getCameraImage(), prevgray,
                        current.getProjectionMatrix(), current.getScreenWidth(), current.getScreenHeight());
                prevgray = current.getCameraImage();

                if(mv != null) {
                    InteractiveObject io = mv.getIo();
                    float result[] = mv.getResult();
                    Float[] rio;

                    if (moves.containsKey(io)) {
                        rio = moves.get(io);
                        rio[0] += result[0];
                        rio[1] += result[1];
                    } else {
                        rio = new Float[2];
                        rio[0] = result[0];
                        rio[1] = result[1];
                    }

                    moves.put(io, rio);
                    Integer movei;
                    if(imoves.containsKey(io)) {
                        movei = imoves.get(io);
                        movei++;
                    } else {
                        movei = 0;
                    }

                    Log.v("movct", toString()+", "+movei+", "+movFrames);

                    if (movei >= movFrames) {
                        movei = 0;
                        if (moves.containsKey(io)) {
                            Float[] r = moves.get(io);
                            if (r[0] != 0 || r[1] != 0) {
                                if(r[0] != 0) r[0] = 1 / (r[0] > 0 ? r[0] + 1 : r[0] - 1 );
                                if(r[1] != 0) r[1] = 1 / (r[1] > 0 ? r[1] + 1 : r[1] - 1 );
                                io.getMovementListener().movement(r[0], r[1]);
                            }
                            moves.remove(io);
                        }
                    }
                    imoves.put(io, movei);
                }

            }else{
                if(skipFrame > 0) skipFrame--;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}