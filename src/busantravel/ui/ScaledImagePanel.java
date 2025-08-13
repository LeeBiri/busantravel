package busantravel.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 이미지를 패널의 크기에 맞게 비율을 유지하면서 중앙에 표시하는 커스텀 JPanel 클래스입니다.
 * 이미지가 패널보다 크거나 작을 경우, 자동으로 스케일이 조정됩니다.
 */
public class ScaledImagePanel extends JPanel {
    private Image image;

    /**
     * ScaledImagePanel의 새 인스턴스를 생성합니다.
     * 기본 배경색을 설정합니다.
     */
    public ScaledImagePanel() {
        setBackground(new Color(230, 240, 250));
        setOpaque(true);
    }

    /**
     * 패널에 표시할 이미지를 설정합니다.
     * 이미지가 설정되면 패널을 다시 그려서 변경사항을 반영합니다.
     *
     * @param image 표시할 Image 객체
     */
    public void setImage(Image image) {
        this.image = image;
        repaint();
    }

    /**
     * 컴포넌트를 그리는 메서드를 재정의합니다.
     * 이미지가 설정되어 있을 경우, 패널 크기에 맞게 비율을 계산하여 이미지를 중앙에 그립니다.
     *
     * @param g 페인팅에 사용되는 Graphics 컨텍스트
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = image.getWidth(this);
            int imgHeight = image.getHeight(this);

            if (imgWidth <= 0 || imgHeight <= 0) {
                return;
            }

            double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);

            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;

            g.drawImage(image, x, y, scaledWidth, scaledHeight, this);
        }
    }
}
