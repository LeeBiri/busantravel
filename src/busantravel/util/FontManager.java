package busantravel.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class FontManager {

    private static Font jalnanFont;
    private static Font jalnanGothicFont;

    static {
        loadFonts();
    }

    private static void loadFonts() {
        jalnanFont = loadFont("/busantravel/resources/Jalnan2TTF.ttf");
        jalnanGothicFont = loadFont("/busantravel/resources/JalnanGothicTTF.ttf");
    }

    private static Font loadFont(String path) {
        try {
            InputStream is = FontManager.class.getResourceAsStream(path);
            if (is != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            } else {
                System.err.println("폰트 파일을 찾을 수 없습니다: " + path);
                return new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            System.err.println("폰트 로드 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return new Font("맑은 고딕", Font.PLAIN, 14);
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

    public static Font getJalnanGothicFont() {
        return jalnanGothicFont;
    }

    public static Font getJalnanGothicFont(float size) {
        return jalnanGothicFont.deriveFont(size);
    }

    public static Font getJalnanGothicFont(int style, float size) {
        return jalnanGothicFont.deriveFont(style, size);
    }
}
