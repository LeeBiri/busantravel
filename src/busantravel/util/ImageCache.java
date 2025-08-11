package busantravel.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static ImageCache instance;
    private Map<String, ImageIcon> memoryCache;
    private final Path cacheDir = Paths.get("cache");

    private ImageCache() {
        memoryCache = new HashMap<>();
        try {
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    public ImageIcon getImage(String key) {
        if (memoryCache.containsKey(key)) {
            return memoryCache.get(key);
        }

        File imageFile = cacheDir.resolve(getFileName(key)).toFile();
        if (imageFile.exists()) {
            try {
                Image image = ImageIO.read(imageFile);
                if (image != null) {
                    ImageIcon imageIcon = new ImageIcon(image);
                    memoryCache.put(key, imageIcon);
                    return imageIcon;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void putImage(String key, ImageIcon image) {
        memoryCache.put(key, image);
        File imageFile = cacheDir.resolve(getFileName(key)).toFile();
        try {
            ImageIO.write(ImageIO.read(new URL(key)), "png", imageFile);
        } catch (IOException e) {
            // 로컬 파일이거나 URL이 아닌 경우 처리
            try {
                ImageIO.write((java.awt.image.BufferedImage) image.getImage(), "png", imageFile);
            } catch (IOException | ClassCastException ex) {
                // ex.printStackTrace();
            }
        }
    }

    private String getFileName(String key) {
        return key.replaceAll("[^a-zA-Z0-9.-]", "_") + ".png";
    }
}
