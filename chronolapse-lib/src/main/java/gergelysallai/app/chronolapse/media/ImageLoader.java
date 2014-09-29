package gergelysallai.app.chronolapse.media;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import gergelysallai.app.chronolapse.media.scale.Graphics2dScaler;
import gergelysallai.app.chronolapse.media.scale.ImageScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ImageLoader.class.getName());

    private final LinkedBlockingQueue<BufferedImage> images;
    private final Collection<File> imageFiles;
    private final AtomicBoolean abort = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger itemsProcessed = new AtomicInteger(0);
    private final Optional<ImageSize> imageSize;
//    private final ImageScaler imageScaler = new ThumbnailatorScaler();
    private final ImageScaler imageScaler = new Graphics2dScaler();
    private final ProgressUpdateHandler progressHandler;

    public ImageLoader(Collection<File> files, int readForward, ProgressUpdateHandler progressHandler) {
        this(files, null, readForward, progressHandler);
    }

    public ImageLoader(Collection<File> files, ImageSize imageSize, int readForward, ProgressUpdateHandler progressHandler) {
        super("ImageLoader");
        checkNotNull(progressHandler);
        this.progressHandler = progressHandler;
        this.imageFiles = files;
        this.imageSize = Optional.fromNullable(imageSize);
        images = new LinkedBlockingQueue<BufferedImage>(++readForward);
    }

    @Override
    public void run() {
        running.set(true);
        int itemsLoaded = 0;
        for(File file: imageFiles) {
            if (abort.get()) {
                break;
            }
            BufferedImage image;
            try {
                image = ImageIO.read(file);
                if (imageSize.isPresent() && !imageSize.get().equals(new ImageSize(image.getWidth(), image.getHeight()))) {
                    images.put(imageScaler.scale(image, imageSize.get()));
                } else {
                    images.put(image);
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Could not load picture: {}", file.getPath());
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.info("Interrupt");
            } finally {
                ++itemsLoaded;
                logger.debug("loaded item: {}/{}", itemsLoaded, imageFiles.size());
                progressHandler.postLoadedImage(itemsLoaded);
            }
        }
        running.set(false);
        progressHandler.postImageLoadingCompleted();
    }

    public BufferedImage getNextImage() {
        try {
            BufferedImage result = images.take();
            itemsProcessed.incrementAndGet();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("Interrupted exception! Should not have happened!");
            throw new RuntimeException("Should not happen!");
        }
    }

    public void abort() {
        logger.warn("aborted");
        abort.set(true);
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean hasNext() {
        return !abort.get() && itemsProcessed.get() < imageFiles.size();
    }

}
