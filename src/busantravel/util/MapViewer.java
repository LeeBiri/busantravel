package busantravel.util;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * 이미지 URL을 받아 GUI 창(JFrame)에 해당 이미지를 표시하거나 HTML 파일을 기본 브라우저에서 여는 클래스입니다.
 */
public class MapViewer {

    /**
     * 지정된 이미지 URL을 사용하여 지도 이미지를 새로운 창에 표시합니다.
     * Swing GUI 작업은 Event Dispatch Thread(EDT)에서 실행되도록 보장합니다.
     *
     * @param imageUrl   표시할 이미지의 완전한 URL
     * @param tmapApiKey TMAP API 키
     */
    public static void showMap(String imageUrl, String tmapApiKey) {
        // Swing GUI는 항상 Event Dispatch Thread에서 생성하고 수정해야 합니다.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Static Map Viewer");
            
            // 창을 닫을 때 전체 프로그램이 종료되지 않고 해당 창만 닫히도록 설정합니다.
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

            try {
                // curl 명령어 생성 및 출력
                String curlCommand = String.format("curl \"%s\" --header \"appKey: %s\"", imageUrl, tmapApiKey);
                System.out.println("\n--- API Call (curl) ---");
                System.out.println(curlCommand);

                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("appKey", tmapApiKey);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream inputStream = connection.getInputStream()) {
                        Image image = ImageIO.read(inputStream);
                        if (image != null) {
                            ImageIcon imageIcon = new ImageIcon(image);
                            JLabel imageLabel = new JLabel(imageIcon);
                            frame.add(imageLabel);
                        } else {
                            frame.add(new JLabel("이미지를 로드할 수 없습니다."));
                        }
                    }
                } else {
                    try (InputStream errorStream = connection.getErrorStream()) {
                        String errorResponse = new String(errorStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        frame.add(new JLabel("<html><pre>" + errorResponse + "</pre></html>"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                frame.add(new JLabel("오류: 이미지를 로드하는 중 문제가 발생했습니다."));
            }

            frame.pack(); // 프레임 안의 내용물(이미지)에 맞게 창 크기를 자동으로 조절합니다.
            frame.setLocationRelativeTo(null); // 창을 화면 중앙에 위치시킵니다.
            frame.setVisible(true); // 창을 화면에 보이게 합니다.
        });
    }

    /**
     * 지정된 경로의 HTML 파일을 시스템의 기본 웹 브라우저에서 엽니다.
     *
     * @param filePath 열고자 하는 HTML 파일의 경로
     */
    public static void showHtmlFile(String filePath) {
        File htmlFile = new File(filePath);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(htmlFile.toURI());
                System.out.println("\n" + filePath + " 파일을 기본 브라우저에서 열었습니다.");
            } catch (IOException e) {
                System.err.println("오류: 파일을 여는 중 문제가 발생했습니다.");
                e.printStackTrace();
            }
        } else {
            System.err.println("오류: 데스크탑 환경이 지원되지 않아 브라우저를 열 수 없습니다.");
        }
    }
}
