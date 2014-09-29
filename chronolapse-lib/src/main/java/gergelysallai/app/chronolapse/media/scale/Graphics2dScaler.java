package gergelysallai.app.chronolapse.media.scale;

import gergelysallai.app.chronolapse.media.ImageSize;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Graphics2dScaler implements ImageScaler {

    @Override
    public BufferedImage scale(BufferedImage input, ImageSize desiredSize) {
        final ImageSize originalSize = new ImageSize(input.getWidth(), input.getHeight());
        final double aspectRatio = (double)originalSize.width / (double)originalSize.height;
        final double calcWidth = aspectRatio * desiredSize.height;
        final ImageSize scaleSize = new ImageSize((int)calcWidth, desiredSize.height);
        BufferedImage tmp = new BufferedImage(desiredSize.width, desiredSize.height, input.getType());
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.black);
        g2.drawRect(0, 0, desiredSize.width, desiredSize.height);
        g2.drawImage(
                input,
                (desiredSize.width - scaleSize.width)/2,
                (desiredSize.height - scaleSize.height)/2,
                scaleSize.width,
                scaleSize.height,
                null);
        g2.dispose();
        return tmp;
    }
}
