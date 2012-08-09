Simple Image Loader
===================

Just loads images and provides a ByteBuffer of the loaded data. The data is suitable as a source for textures.

### Example Usage

    SimpleImageLoader loader = new SimpleImageLoader();
    loader.load("demo.png", SimpleImageLoaderTest.class.getResourceAsStream("/demo.png")

