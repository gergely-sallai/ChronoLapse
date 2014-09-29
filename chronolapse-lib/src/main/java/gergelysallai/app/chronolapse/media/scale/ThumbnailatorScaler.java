package gergelysallai.app.chronolapse.media.scale;

import gergelysallai.app.chronolapse.media.ImageSize;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Canvas;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ThumbnailatorScaler implements ImageScaler {

    @Override
    public BufferedImage scale(BufferedImage input, ImageSize imageSize) {
        try {
            return Thumbnails.of(input).height(imageSize.height).addFilter(new Canvas(imageSize.width, imageSize.height, Positions.CENTER, Color.black)).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
