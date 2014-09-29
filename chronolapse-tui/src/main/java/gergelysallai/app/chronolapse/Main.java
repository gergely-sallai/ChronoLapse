package gergelysallai.app.chronolapse;

import gergelysallai.app.chronolapse.media.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {
        File file = new File(".");
        ImageSelector.ImageType imageType = ImageSelector.ImageType.JPG;
        ImageSize imageSize = new ImageSize(1280, 720);

        ProgressUpdateListener imageLoaderListener = new ProgressUpdateListener() {
            @Override
            public void onProgressUpdate(float percentCompleted) {
                logger.info(String.format("Images loaded and scaled: %.2f", percentCompleted));
            }

            @Override
            public void onCompleted() {
                logger.info("Image load and scale completed");
            }
        };
        ProgressUpdateListener encodeListener = new ProgressUpdateListener() {
            @Override
            public void onProgressUpdate(float percentCompleted) {
                logger.info(String.format("Encoding progress: %.2f", percentCompleted));
            }

            @Override
            public void onCompleted() {
                logger.info("Encoding completed");
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();

        ImageSelector imageSelector = new ImageSelector(new Comparators.FileDateAscending());
        Collection<File> images = imageSelector.getFiles(file, imageType);
        ProgressUpdateHandler progressUpdateHandler = new ProgressUpdateHandler(images.size(), imageLoaderListener, encodeListener, executor);
        ImageLoader imageLoader = new ImageLoader(images, imageSize, 100, progressUpdateHandler);
        imageLoader.start();

        String path;
        if (file.isDirectory()) {
            path = file.getPath();
        } else {
            path = file.getParent();
        }

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH-mm-ss");
        final Date date = new Date();
        final String videoFile = String.format("%s/Timelapse-%s.mp4", path, dateFormat.format(date));
        
        VideoCreator videoCreator = new VideoCreator(videoFile, imageSize, VideoCreator.FrameRate.FPS25, imageLoader, progressUpdateHandler);
        videoCreator.start();

        try {
            videoCreator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        logger.info("Everything completed, shutting down. Video file: {}", videoFile);
    }
}
