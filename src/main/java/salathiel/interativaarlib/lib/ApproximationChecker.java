package salathiel.interativaarlib.lib;

import java.util.List;

import salathiel.interativaarlib.models.InteractiveObject;

public class ApproximationChecker {

    public static double minDistance = 200;

    public static void checkApproximation(List<InteractiveObject> markers){
        for(InteractiveObject i : markers){
            for(InteractiveObject j : markers){
                if( i.isVisible() && j.isVisible() && (!i.equals(j)) && i.getApproximationListener() != null){
                    if(i.getObjectPosition().distance(j.getObjectPosition()) < minDistance)
                        i.getApproximationListener().markerNearby(j);
                }
            }
        }
    }
}
