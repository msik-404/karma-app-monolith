package com.msik404.karmaapp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TestingImageDataCreator {

    private final static int WIDTH = 10;
    private final static int HEIGHT = 10;

    public static byte[] getTestingImage() {

        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        int fillColor = 0xFFFFFF; // White color in RGB
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                bufferedImage.setRGB(x, y, fillColor);
            }
        }

        var outputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, "PNG", outputStream);
        } catch (IOException ex) {
            // this will never be called
        }

        return outputStream.toByteArray();
    }

    public static byte[] jpegCompress(byte[] imageData) {

        final var input = new ByteArrayInputStream(imageData);

        try {
            var bufferedImage = ImageIO.read(input);
            var byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
}
