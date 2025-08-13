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

/**
 * 원격 URL에서 이미지를 비동기적으로 로드하고, 리사이징하며, 캐싱하는 유틸리티 클래스입니다.
 * Swing 애플리케이션에서 이미지 로딩으로 인한 UI 멈춤 현상을 방지합니다.
 */
public class ImageThumbLoader {
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(6); // 이미지 로딩을 위한 스레드 풀
    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>(); // 로드된 이미지 아이콘을 캐싱하는 맵

    /**
     * 지정된 URL에서 이미지를 비동기적으로 로드하여 JLabel에 설정합니다.
     * 로딩 중에는 플레이스홀더 아이콘이 표시됩니다. 이미지는 캐시되어 중복 로딩을 방지합니다.
     *
     * @param target      이미지를 표시할 대상 JLabel.
     * @param url         이미지의 URL.
     * @param w           이미지가 리사이징될 너비.
     * @param h           이미지가 리사이징될 높이.
     * @param placeholder 이미지가 로드되는 동안 표시될 임시 아이콘.
     */
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

    /**
     * 캐시 키를 생성합니다. URL과 크기를 조합하여 고유한 키를 만듭니다.
     *
     * @param u 대상 이미지 URL
     * @param w 리사이징될 너비
     * @param h 리사이징될 높이
     * @return 생성된 캐시 키 문자열
     */
    private static String urlKey(String u, int w, int h){ return u+"#"+w+"x"+h; }

    /**
     * 원본 이미지를 지정된 너비와 높이로 리사이징합니다.
     * 고품질 렌더링 힌트를 사용하여 부드러운 이미지를 생성합니다.
     *
     * @param src 원본 BufferedImage 객체.
     * @param w   목표 너비.
     * @param h   목표 높이.
     * @return 리사이징된 Image 객체.
     */
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
