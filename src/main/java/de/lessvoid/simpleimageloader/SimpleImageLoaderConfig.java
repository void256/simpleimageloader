package de.lessvoid.simpleimageloader;

/**
 * Fluent API to configure loading of image data.
 * @author void
 */
public class SimpleImageLoaderConfig {
  private boolean flipped;
  private boolean forceAlpha;
  private int[] transparent;
  private boolean powerOfTwoSupport;
  private boolean modeARGB;

  /**
   * When being called the returned image will be vertically flipped.
   * @return this
   */
  public SimpleImageLoaderConfig flipped() {
    this.flipped = true;
    return this;
  }

  /**
   * When set forces the returned image data to be 32 Bit. When not being set images without alpha will be returned as
   * 24 Bit.
   * @return this
   */
  public SimpleImageLoaderConfig forceAlpha() {
    this.forceAlpha = true;
    return this;
  }

  /**
   * Set the transparent color to be detected in the image pixels and will be treated as alpha 0.
   * @param transparent the transparent color bytes (R, G, B)
   * @return this
   */
  public SimpleImageLoaderConfig transparent(final int[] transparent) {
    this.transparent = transparent;
    return this;
  }

  /**
   * When this is being set the returned image data will be automatically updated to a power of 2 size. This might be
   * necessary to load textures for older hardware.
   * @return this
   */
  public SimpleImageLoaderConfig powerOfTwoSupport() {
    this.powerOfTwoSupport = true;
    return this;
  }

  /**
   * Changes the position of the alpha value from the lowest byte to the highest byte (RGBA to ARGB)
   * @return this
   */
  public SimpleImageLoaderConfig modeARGB() {
    this.modeARGB = true;
    return this;
  }

  /**
   * Returns if flipped is being set to true.
   * @return true when flipped is true
   */
  public boolean isFlipped() {
    return flipped;
  }

  /**
   * Returns true if alpha is set to true.
   * @return true when force alpha is set to true
   */
  public boolean isForceAlpha() {
    return forceAlpha;
  }

  /**
   * Returns the transparent color bytes set or null.
   * @return the transparent color bytes set
   */
  public int[] getTransparent() {
    return transparent;
  }

  /**
   * Returns true when powerOfTwo support is enabled.
   * @return power of two support
   */
  public boolean isPowerOfTwoSupport() {
    return powerOfTwoSupport;
  }

  /**
   * Returns true when the mode has been changed to ARGB (from the default value of RGBA)
   * @return true if ARGB and false if not
   */
  public boolean isModeARGB() {
    return modeARGB;
  }
}
