package busantravel.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class FontManager {

    private static Font jalnanFont;

    static {
        loadFonts();
    }

    private static void loadFonts() {
        try {
            InputStream is = FontManager.class.getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(jalnanFont);
            } else {
                System.err.println("폰트 파일을 찾을 수 없습니다! 기본 폰트 사용.");
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            System.err.println("폰트 로드 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    public static Font getJalnanFont() {
        return jalnanFont;
    }
    
    public static Font getJalnanFont(float size) {
        return jalnanFont.deriveFont(size);
    }

    public static Font getJalnanFont(int style, float size) {
        return jalnanFont.deriveFont(style, size);
    }
}
