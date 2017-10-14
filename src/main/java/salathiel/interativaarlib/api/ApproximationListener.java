package salathiel.interativaarlib.api;

//Listener responsavel por metodo de aproximacao

import salathiel.interativaarlib.models.InteractiveObject;

public interface ApproximationListener {
    //funcao chamada ao aproximar dois marcadores
    public void iObjectNearby(InteractiveObject nearIObject);
}
