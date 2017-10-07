package salathiel.interativaarlib.lib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import salathiel.interativaarlib.models.InteractiveObject;

public class OcclusionChecker {

	private static Map<InteractiveObject, Integer> intensitys = new HashMap<>();
	
	public static void checkOcclusion(List<InteractiveObject> markers){
		for(InteractiveObject im : markers){
			if(im.getOcclusionListener() != null && im.isAlreadyVisible()){ 
				if (im.isVisible() && im.triggerOcclusion() ){
					im.setTriggerOcclusion(false);
					if(intensitys.containsKey(im))
						im.getOcclusionListener().occlusion(intensitys.get(im));
					intensitys.put(im, 0);
				}
				if (!im.isVisible()){
					if(intensitys.containsKey(im)){
						Integer intensityIm = intensitys.get(im);
						if(intensityIm < Integer.MAX_VALUE) intensityIm += 1;
						intensitys.put(im, intensityIm);
					}else{
						intensitys.put(im, 0);
					}
				}
			}
		}
	}
}
