package de.lessvoid.simpleimageloader.type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import de.lessvoid.simpleimageloader.ImageData;
import de.lessvoid.simpleimageloader.SimpleImageLoaderConfig;

/**
 * An image data provider that uses ImageIO to retrieve image data in a format
 * suitable for creating OpenGL textures. This implementation is used when
 * formats not natively supported by the library are required.
 *
 * Refitted into the simple image loader framework and cleaning up by void.
 *
 * @author kevin
 * @author void
 */
public class ImageTypeImageIO implements ImageType {
  private static final int COLOR_COMPONENT_COUNT = 3;
  private static final int COLOR_PALETTE_SIZE = 256;
  private static final int COMPONENTS_PER_PIXEL_3 = 3;
  private static final int COMPONENTS_PER_PIXEL_4 = 4;
  private static final int BIT_DEPTH_24 = 24;
  private static final int BIT_DEPTH_32 = 32;

  private static final ColorModel GL_ALPHA_COLOR_MODEL =
      new ComponentColorModel(
          ColorSpace.getInstance(ColorSpace.CS_sRGB),
          new int[] { 8, 8, 8, 8 },
          true,
          false,
          ComponentColorModel.TRANSLUCENT,
          DataBuffer.TYPE_BYTE);

  private static final ColorModel GL_COLOR_MODEL =
      new ComponentColorModel(
          ColorSpace.getInstance(ColorSpace.CS_sRGB),
          new int[] { 8, 8, 8, 0 },
          false,
          false,
          ComponentColorModel.OPAQUE,
          DataBuffer.TYPE_BYTE);

  public ImageData load(final SimpleImageLoaderConfig config, final InputStream inputStream) throws IOException {
    return loadInternal(config, ImageIO.read(inputStream));
  }

  private ImageData loadInternal(final SimpleImageLoaderConfig config, final BufferedImage image) {
    int imageWidth = powerOfTwoSupport(image.getWidth(), config.isPowerOfTwoSupport());
    int imageHeight = powerOfTwoSupport(image.getHeight(), config.isPowerOfTwoSupport());

    // create a raster that can be used by OpenGL as a source for a texture
    int depth;
    WritableRaster raster;
    BufferedImage texImage;

    boolean useAlpha = image.getColorModel().hasAlpha() || config.isForceAlpha();
    if (useAlpha) {
      depth = BIT_DEPTH_32;
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, imageWidth, imageHeight, COMPONENTS_PER_PIXEL_4, null);
      texImage = new BufferedImage(GL_ALPHA_COLOR_MODEL, raster, false, new Hashtable<String, Object>());
    } else {
      depth = BIT_DEPTH_24;
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, imageWidth, imageHeight, COMPONENTS_PER_PIXEL_3, null);
      texImage = new BufferedImage(GL_COLOR_MODEL, raster, false, new Hashtable<String, Object>());
    }

    // copy the source image into the produced image
    Graphics2D g = (Graphics2D) texImage.getGraphics();

    // only need to blank the image for mac compatibility if we're using alpha
    processUseAlpha(imageWidth, imageHeight, useAlpha, g);
    processFlipped(image, config.isFlipped(), g, image.getHeight());

    // build a byte buffer from the temporary image
    // that be used by OpenGL to produce a texture.
    byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();
    processTransparent(config.getTransparent(), data);
    processModeARGB(config.isModeARGB(), data);

    ByteBuffer imageBuffer = ByteBuffer.allocateDirect(data.length);
    imageBuffer.order(ByteOrder.nativeOrder());
    imageBuffer.put(data, 0, data.length);
    imageBuffer.flip();
    g.dispose();

    return new ImageData(imageWidth, imageHeight, image.getWidth(), image.getHeight(), depth, imageBuffer);
  }

  private void processModeARGB(final boolean modeARGB, final byte[] data) {
    if (!modeARGB) {
      return;
    }

    for (int i = 0; i < data.length; i += 4) {
      byte rr = data[i + 0];
      byte gg = data[i + 1];
      byte bb = data[i + 2];
      byte aa = data[i + 3];
      data[i + 0] = bb;
      data[i + 1] = gg;
      data[i + 2] = rr;
      data[i + 3] = aa;
    }
  }

  private void processTransparent(final int[] transparent, final byte[] data) {
    if (transparent == null) {
      return;
    }

    for (int i = 0; i < data.length; i += 4) {
      boolean match = true;
      for (int c = 0; c < COLOR_COMPONENT_COUNT; c++) {
        int value = data[i + c] < 0 ? COLOR_PALETTE_SIZE + data[i + c] : data[i + c];
        if (value != transparent[c]) {
          match = false;
        }
      }

      if (match) {
        data[i + 3] = 0;
      }
    }
  }

  private void processFlipped(final BufferedImage image, final boolean flipped, final Graphics2D g, final int height) {
    if (flipped) {
      g.scale(1, -1);
      g.drawImage(image, 0, -height, null);
    } else {
      g.drawImage(image, 0, 0, null);
    }
  }

  private void processUseAlpha(final int imageWidth, final int imageHeight, final boolean useAlpha, final Graphics2D g) {
    if (!useAlpha) {
      return;
    }

    g.setColor(new Color(0f, 0f, 0f, 0f));
    g.fillRect(0, 0, imageWidth, imageHeight);
  }

  private int powerOfTwoSupport(final int originalValue, final boolean powerOfTwoSupport) {
    if (!powerOfTwoSupport) {
      return originalValue;
    }
    int value = 2;
    while (value < originalValue) {
      value *= 2;
    }
    return value;
  }
}
