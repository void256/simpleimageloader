package de.lessvoid.simpleimageloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.lessvoid.simpleimageloader.type.ImageType;
import de.lessvoid.simpleimageloader.type.ImageTypeImageIO;
import de.lessvoid.simpleimageloader.type.ImageTypeTGA;

/**
 * The SimpleImageLoader just loads image data from an InputStream and returns the plain image data as a ByteBuffer.
 * @author void
 */
public class SimpleImageLoader {
  private static final SimpleImageLoaderConfig defaultConfig = new SimpleImageLoaderConfig();
  private final Map<String, ImageType> imageTypes = new HashMap<String, ImageType>();
  private ImageType defaultImageType;

  /**
   * Create a new SimpleImageLoader.
   */
  public SimpleImageLoader() {
    defaultImageType = new ImageTypeImageIO();
    registerImageType("tga", new ImageTypeTGA());
  }

  /**
   * Register a new file extension with the given ImageType. This allows support for new image types to be added.
   * @param extension the file extension, e.g. "tga"
   * @param type the ImageType to handle this extension
   */
  public void registerImageType(final String extension, final ImageType type) {
    imageTypes.put(extension, type);
  }

  /**
   * Change the default image type to a different ImageType. The default defaultImageType ;-) is ImageTypeImageIO();
   * @param defaultImageType the new default image type
   */
  public void setDefaultImageType(final ImageType defaultImageType) {
    this.defaultImageType = defaultImageType;
  }

  /**
   * Load image date from the given inputStream. To easily allow the SimpleImageLoader to figure out the correct ImageType
   * to use the original filename needs to be given.
   *
   * @param filename the original filename including the file extension.
   * @param inputStream the InputStream to load image data from
   * @return a new ImageData instance that gives you access to the loaded image data
   * @throws IOException
   */
  public ImageData load(
      final String filename,
      final InputStream inputStream) throws IOException {
    return loadInternal(filename, inputStream, defaultConfig);
  }

  /**
   * @see load(String, InputStream) with the added possibility to configure how the image is loaded with an
   * SimpleImageLoaderConfig instance.
   *
   * @param filename the original filename including the file extension.
   * @param inputStream the InputStream to load image data from
   * @param config a SimpleImageLoaderConfig instance that allows to configure details on how the image should be loaded
   * @return a new ImageData instance that gives you access to the loaded image data
   * @throws IOException
   */
  public ImageData load(
      final String filename,
      final InputStream inputStream,
      final SimpleImageLoaderConfig config) throws IOException {
    return loadInternal(filename, inputStream, config);
  }

  private ImageData loadInternal(
      final String filename,
      final InputStream inputStream,
      final SimpleImageLoaderConfig config) throws IOException {
    String extension = extractExtension(filename);
    ImageType source = imageTypes.get(extension);
    if (source == null) {
      source = defaultImageType;
    }
    return source.load(config, inputStream);
  }

  private String extractExtension(final String filename) {
    int lastIndexOf = filename.lastIndexOf('.');
    if (lastIndexOf == -1) {
      return null;
    }
    if (lastIndexOf == filename.length()) {
      return null;
    }
    return filename.substring(lastIndexOf + 1, filename.length());
  }
}
