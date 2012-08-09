package de.lessvoid.simpleimageloader.type;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import de.lessvoid.simpleimageloader.ImageData;
import de.lessvoid.simpleimageloader.SimpleImageLoaderConfig;

/**
 * A utility to load TGAs.
 * 
 * Fresh cut of code but largely influeneced by the TGA loading class provided
 * as part of the Java Monkey Engine (JME). Why not check out what they're doing
 * over at http://www.jmonkeyengine.com. kudos to Mark Powell.
 *
 * Refitted into the simple image loader framework and cleaning up by void.
 *
 * @author Kevin Glass
 * @author void
 */
public class ImageTypeTGA implements ImageType {
  private static final int PIXEL_DEPTH_24 = 24;
  private static final int PIXEL_DEPTH_32 = 32;
  private static final int IMAGE_DESCRIPTOR_MASK = 0x0020;

  public ImageData load(final SimpleImageLoaderConfig config, final InputStream inputStream) throws IOException {
    return loadInternal(config, inputStream);
  }

  private short flipEndian(final short signedShort) {
    int input = signedShort & 0xFFFF;
    return (short) (input << 8 | (input & 0xFF00) >>> 8);
  }

  private ImageData loadInternal(final SimpleImageLoaderConfig config, final InputStream inputStream ) throws IOException {
    boolean forceAlpha = config.isForceAlpha();
    if (config.getTransparent() != null) {
      forceAlpha = true;
    }

    byte red = 0;
    byte green = 0;
    byte blue = 0;
    byte alpha = 0;

    BufferedInputStream bis = new BufferedInputStream(inputStream, 100000);
    DataInputStream dis = new DataInputStream(bis);

    // Read in the Header
    short idLength = (short) dis.read();
    /*short colorMapType = (short) */dis.read();
    /*short imageType = (short) */dis.read();
    /*short cMapStart = */flipEndian(dis.readShort());
    /*short cMapLength = */flipEndian(dis.readShort());
    /*short cMapDepth = (short) */dis.read();
    /*short xOffset = */flipEndian(dis.readShort());
    /*short yOffset = */flipEndian(dis.readShort());

    int width = flipEndian(dis.readShort());
    int height = flipEndian(dis.readShort());
    int pixelDepth = (short) dis.read();
    if (pixelDepth == 32) {
      forceAlpha = false;
    }

    int texWidth = width;
    int texHeight = height;
    if (config.isPowerOfTwoSupport()) {
      texWidth = get2Fold(width);
      texHeight = get2Fold(height);
    }

    boolean flipped = config.isFlipped();
    short imageDescriptor = (short) dis.read();
    if ((imageDescriptor & IMAGE_DESCRIPTOR_MASK) == 0) {
      flipped = !flipped;
    }

    // Skip image ID
    if (idLength > 0) {
      bis.skip(idLength);
    }

    byte[] rawData = null;
    if ((pixelDepth == PIXEL_DEPTH_32) || (forceAlpha)) {
      pixelDepth = PIXEL_DEPTH_32;
      rawData = new byte[texWidth * texHeight * 4];
    } else if (pixelDepth == PIXEL_DEPTH_24) {
      rawData = new byte[texWidth * texHeight * 3];
    } else {
      throw new IOException("Only 24 and 32 bit TGAs are supported");
    }

    if (pixelDepth == PIXEL_DEPTH_24) {
      if (flipped) {
        for (int i = height - 1; i >= 0; i--) {
          for (int j = 0; j < width; j++) {
            blue = dis.readByte();
            green = dis.readByte();
            red = dis.readByte();

            int ofs = ((j + (i * texWidth)) * 3);
            rawData[ofs] = red;
            rawData[ofs + 1] = green;
            rawData[ofs + 2] = blue;
          }
        }
      } else {
        for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
            blue = dis.readByte();
            green = dis.readByte();
            red = dis.readByte();

            int ofs = ((j + (i * texWidth)) * 3);
            rawData[ofs] = red;
            rawData[ofs + 1] = green;
            rawData[ofs + 2] = blue;
          }
        }
      }
    } else if (pixelDepth == 32) {
      if (flipped) {
        for (int i = height - 1; i >= 0; i--) {
          for (int j = 0; j < width; j++) {
            blue = dis.readByte();
            green = dis.readByte();
            red = dis.readByte();
            if (forceAlpha) {
              alpha = (byte) 255;
            } else {
              alpha = dis.readByte();
            }

            int ofs = ((j + (i * texWidth)) * 4);

            rawData[ofs] = red;
            rawData[ofs + 1] = green;
            rawData[ofs + 2] = blue;
            rawData[ofs + 3] = alpha;

            if (alpha == 0) {
              rawData[ofs + 2] = (byte) 0;
              rawData[ofs + 1] = (byte) 0;
              rawData[ofs] = (byte) 0;
            }
          }
        }
      } else {
        for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
            blue = dis.readByte();
            green = dis.readByte();
            red = dis.readByte();
            if (forceAlpha) {
              alpha = (byte) 255;
            } else {
              alpha = dis.readByte();
            }

            int ofs = ((j + (i * texWidth)) * 4);

            if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
              rawData[ofs] = red;
              rawData[ofs + 1] = green;
              rawData[ofs + 2] = blue;
              rawData[ofs + 3] = alpha;
            } else {
              rawData[ofs] = red;
              rawData[ofs + 1] = green;
              rawData[ofs + 2] = blue;
              rawData[ofs + 3] = alpha;
            }

            if (alpha == 0) {
              rawData[ofs + 2] = 0;
              rawData[ofs + 1] = 0;
              rawData[ofs] = 0;
            }
          }
        }
      }
    }
    inputStream.close();

    if (config.getTransparent() != null) {
      for (int i = 0; i < rawData.length; i += 4) {
        boolean match = true;
        for (int c = 0; c < 3; c++) {
          if (rawData[i + c] != config.getTransparent()[c]) {
            match = false;
          }
        }

        if (match) {
          rawData[i + 3] = 0;
        }
      }
    }

    // Get a pointer to the image memory
    ByteBuffer scratch = createByteBuffer(rawData.length);
    scratch.put(rawData);

    int perPixel = pixelDepth / 8;
    if (height < texHeight - 1) {
      int topOffset = (texHeight - 1) * (texWidth * perPixel);
      int bottomOffset = (height - 1) * (texWidth * perPixel);
      for (int x = 0; x < texWidth * perPixel; x++) {
        scratch.put(topOffset + x, scratch.get(x));
        scratch.put(bottomOffset + (texWidth * perPixel) + x, scratch.get((texWidth * perPixel) + x));
      }
    }
    if (width < texWidth - 1) {
      for (int y = 0; y < texHeight; y++) {
        for (int i = 0; i < perPixel; i++) {
          scratch.put(((y + 1) * (texWidth * perPixel)) - perPixel + i, scratch.get(y * (texWidth * perPixel) + i));
          scratch.put((y * (texWidth * perPixel)) + (width * perPixel) + i,
              scratch.get((y * (texWidth * perPixel)) + ((width - 1) * perPixel) + i));
        }
      }
    }

    scratch.flip();

    return new ImageData(texWidth, texHeight, width, height, pixelDepth, scratch);
  }

  private int get2Fold(int fold) {
    int ret = 2;
    while (ret < fold) {
      ret *= 2;
    }
    return ret;
  }

  private ByteBuffer createByteBuffer(int size) {
    return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
  }
}
