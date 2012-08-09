package de.lessvoid.simpleimageloader.type;

import java.io.IOException;
import java.io.InputStream;

import de.lessvoid.simpleimageloader.ImageData;
import de.lessvoid.simpleimageloader.SimpleImageLoaderConfig;

/**
 * The interface to load image data from an InputStream and convert it into a ImageData instance.
 * @author void
 */
public interface ImageType {

  /**
   * Load image data from the given InputStream taking the SimpleImageLoaderConfig into account and return an ImageData
   * instance.
   *
   * @param config the SimpleImageLoaderConfig
   * @param inputStream the actual InputStream to load data from
   * @return the ImageData instance with the image data
   * @throws IOException
   */
  ImageData load(SimpleImageLoaderConfig config, InputStream inputStream) throws IOException;
}
