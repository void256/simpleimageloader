package de.lessvoid.simpleimageloader;

import java.nio.ByteBuffer;

/**
 * The actual image data loaded.
 * @author void
 */
public class ImageData {
  private final int width;
  private final int height;
  private final int originalWidth;
  private final int originalHeight;
  private final int bitsPerPixel;
  private final ByteBuffer data;

  /**
   * Create a new ImageData instance.
   *
   * @param width the width of the image data (possible corrected for power of two)
   * @param height the height of the image data (possible corrected for power of two)
   * @param originalWidth the original width of the image
   * @param originalHeight the original height of the image
   * @param bitsPerPixel number of bits per pixel (might be 24 or 32)
   * @param data the ByteBuffer with the actual data
   */
  public ImageData(
      final int width,
      final int height,
      final int originalWidth,
      final int originalHeight,
      final int bitsPerPixel,
      final ByteBuffer data) {
    this.bitsPerPixel = bitsPerPixel;
    this.width = width;
    this.height = height;
    this.originalWidth = originalWidth;
    this.originalHeight = originalHeight;
    this.data = data;
  }

  /**
   * The - possible adjusted for power of two - width of the image. This is the width of the ByteBuffer.
   * @return the width of the image
   */
  public int getWidth() {
    return width;
  }

  /**
   * The - possible adjusted for power of two - height of the image. This is the height of the ByteBuffer.
   * @return the height of the image
   */
  public int getHeight() {
    return height;
  }

  /**
   * The original width of the image data. This is the none power of two width.
   * @return the original width
   */
  public int getOriginalWidth() {
    return originalWidth;
  }

  /**
   * The original height of the image data. This is the none power of two height.
   * @return the original height
   */
  public int getOriginalHeight() {
    return originalHeight;
  }

  /**
   * Get bits per pixel.
   * @return returns the number of bits per pixel (usually 32 or 24)
   */
  public int getBitsPerPixel() {
    return bitsPerPixel;
  }

  /**
   * The actual bytes of the image data.
   * @return ByteBuffer with the actual image data
   */
  public ByteBuffer getData() {
    return data;
  }
}
