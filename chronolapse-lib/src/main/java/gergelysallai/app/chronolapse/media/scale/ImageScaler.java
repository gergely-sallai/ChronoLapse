package gergelysallai.app.chronolapse.media.scale;

import gergelysallai.app.chronolapse.media.ImageSize;

import java.awt.image.BufferedImage;

public interface ImageScaler {

    BufferedImage scale(BufferedImage input, ImageSize imageSize);
}
