package gergelysallai.app.chronolapse.media;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoCreator extends Thread {

    public enum FrameRate {
        FPS23_976(IRational.make(24000/1001)),
        FPS24(IRational.make(24/1)),
        FPS25(IRational.make(25/1)),
        FPS30(IRational.make(30/1)),
        FPS60(IRational.make(60/1));

        public final IRational frameRate;

        private FrameRate(IRational frameRate) {
            this.frameRate = frameRate;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(VideoCreator.class.getName());

    private final AtomicBoolean abort = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ImageSize imageSize;
    private final FrameRate frameRate;
    private final ImageLoader imageLoader;
    private final IMediaWriter mediaWriter;
    private final ProgressUpdateHandler progressHandler;

    public VideoCreator(String name, ImageSize imageSize, FrameRate frameRate, ImageLoader imageLoader, ProgressUpdateHandler progressHandler) {
        super("VideoCreator");
        this.imageSize = imageSize;
        this.frameRate = frameRate;
        this.imageLoader = imageLoader;
        this.progressHandler = progressHandler;
        this.mediaWriter = ToolFactory.makeWriter(name);
    }

    @Override
    public void run() {
        running.set(true);
        mediaWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, frameRate.frameRate, imageSize.width, imageSize.height);
        long nextFrameTime = 0;
        int iter = 0;
        while (imageLoader.hasNext() && !abort.get()) {
            mediaWriter.encodeVideo(0, imageLoader.getNextImage(), nextFrameTime, TimeUnit.MILLISECONDS);
            ++iter;
            logger.debug("Encoded frames: {}", iter);
            progressHandler.postEncoderStatus(iter);
            nextFrameTime += frameRate.frameRate.getValue();
        }
        mediaWriter.flush();
        mediaWriter.close();
        running.set(false);
        progressHandler.postEncodeCompleted();
    }

    public void abort() {
        logger.warn("aborted");
        abort.set(true);
    }

    public boolean isRunning() {
        return running.get();
    }
}
