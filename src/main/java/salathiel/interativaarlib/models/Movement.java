package salathiel.interativaarlib.models;

/**
 * Created by salathiel on 30/09/17.
 */
public class Movement {
    private InteractiveObject io;
    private Float[] result;

    public Movement(InteractiveObject io, Float[] result) {
        this.io = io;
        this.result = result;
    }

    public InteractiveObject getIo() {
        return io;
    }

    public void setIo(InteractiveObject io) {
        this.io = io;
    }

    public Float[] getResult() {
        return result;
    }

    public void setResult(Float[] result) {
        this.result = result;
    }
}
