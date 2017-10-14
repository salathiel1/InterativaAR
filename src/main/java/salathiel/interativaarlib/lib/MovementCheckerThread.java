package salathiel.interativaarlib.lib;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import salathiel.interativaarlib.models.Movement;
import salathiel.interativaarlib.models.MovementCheckerItem;
import salathiel.interativaarlib.util.MovementCheckerUtil;

/**
 * Created by salathiel on 29/09/17.
 */

public class MovementCheckerThread implements Runnable{

    private static final int MAX_SIZE_QUEUE = 10;
    private List<MovementCheckerItem> frames2Check;
    private int skipFrame;
    private int framei;

    public MovementCheckerThread() {
        frames2Check = new ArrayList<>();
        skipFrame = 0;
        framei = 0;
    }

    public void addFrame2Check(MovementCheckerItem mci){
        if(frames2Check.size() < (MAX_SIZE_QUEUE*2))
            frames2Check.add(mci);
    }

    public int getFrames2CheckSize(){
        return frames2Check.size();
    }

    //tavez seja preciso copiar parametros porque objs podem ser mudados dinamicamente
    @Override
    public void run() {
        while(true) {
            framei++;
            if(framei == MAX_SIZE_QUEUE-skipFrame){
                framei = 0;
                if(frames2Check.size() > MAX_SIZE_QUEUE && skipFrame < (MAX_SIZE_QUEUE/2)) skipFrame++;

                int sizeFrames2Remove = skipFrame;
                for(int i = 1; i < frames2Check.size() && sizeFrames2Remove > 0; i+=2){
                    sizeFrames2Remove--;
                    frames2Check.remove(i);
                }
            }

            if(frames2Check.size() > 0) {
                MovementCheckerItem current = frames2Check.remove(0);
                Movement mv = MovementCheckerUtil.checkMovement(current.getIobjects(), current.getCameraImage(), current.getPrevgray(),
                        current.getProjectionMatrix(), current.getScreenWidth(), current.getScreenHeight());
                if(mv != null) MovementChecker.sumMovement(mv);
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