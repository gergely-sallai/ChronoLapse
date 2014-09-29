package gergelysallai.app.chronolapse;

public interface ProgressUpdateListener {

    void onProgressUpdate(float percentCompleted);

    void onCompleted();
}
