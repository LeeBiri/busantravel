// ImageThumbLoader.java
package busantravel.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.*;

public class ImageThumbLoader {
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(6);
    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();

    public static void load(JLabel target, String url, int w, int h, ImageIcon placeholder) {
        if (url == null || url.isBlank()) { target.setIcon(placeholder); return; }
        ImageIcon cached = CACHE.get(urlKey(url, w, h));
        if (cached != null) { target.setIcon(cached); return; }

        target.setIcon(placeholder); // 우선 플레이스홀더 표시
        EXEC.submit(() -> {
            try {
                URL u = new URL(url);
                URLConnection conn = u.openConnection();
                conn.setConnectTimeout(2500);
                conn.setReadTimeout(2500);
                BufferedImage src = ImageIO.read(conn.getInputStream());
                if (src == null) return;
                Image scaled = scale(src, w, h);
                ImageIcon icon = new ImageIcon(scaled);
                CACHE.put(urlKey(url, w, h), icon);
                SwingUtilities.invokeLater(() -> target.setIcon(icon));
            } catch (Exception ignored) { /* keep placeholder */ }
        });
    }

    private static String urlKey(String u, int w, int h){ return u+"#"+w+"x"+h; }

    private static Image scale(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}
