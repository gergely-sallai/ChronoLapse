package gergelysallai.app.chronolapse.media;

import gergelysallai.app.chronolapse.ProgressUpdateListener;

import java.util.concurrent.Executor;

public class ProgressUpdateHandler {

    private final ProgressUpdateListener imageLoaderListener;
    private final ProgressUpdateListener encodeListener;
    private final int pictureCount;
    private final Executor executor;

    public ProgressUpdateHandler(int pictureCount, ProgressUpdateListener imageLoaderListener, ProgressUpdateListener encodeListener, Executor executor) {
        this.pictureCount = pictureCount;
        this.imageLoaderListener = imageLoaderListener;
        this.encodeListener = encodeListener;
        this.executor = executor;
    }

    private float getPercent(int completed, int all) {
        return (completed * 100.0f) / all;
    }

    void postEncoderStatus(final int encodedFrameNum) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                encodeListener.onProgressUpdate(getPercent(encodedFrameNum, pictureCount));
            }
        });
    }

    void postEncodeCompleted() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                encodeListener.onCompleted();
            }
        });
    }

    void postLoadedImage(final int loadedImageNum) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                imageLoaderListener.onProgressUpdate(getPercent(loadedImageNum, pictureCount));
            }
        });
    }

    void postImageLoadingCompleted() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                imageLoaderListener.onCompleted();
            }
        });
    }
}
