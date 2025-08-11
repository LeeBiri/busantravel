package busantravel.util;

import javax.swing.ImageIcon;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static ImageCache instance;
    private Map<String, ImageIcon> cache;

    private ImageCache() {
        cache = new HashMap<>();
    }

    public static synchronized ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    public ImageIcon getImage(String key) {
        return cache.get(key);
    }

    public void putImage(String key, ImageIcon image) {
        cache.put(key, image);
    }
}
