package salathiel.interativaarlib.models;

/**
 * Created by salathiel on 30/09/17.
 */
public class Movement {
    private InteractiveObject io;
    private float[] result;

    public Movement(InteractiveObject io, float[] result) {
        this.io = io;
        this.result = result;
    }

    public InteractiveObject getIo() {
        return io;
    }

    public void setIo(InteractiveObject io) {
        this.io = io;
    }

    public float[] getResult() {
        return result;
    }

    public void setResult(float[] result) {
        this.result = result;
    }
}
