package busantravel.ui;

import javax.swing.*;
import java.awt.*;

public class ScaledImagePanel extends JPanel {
    private Image image;

    public ScaledImagePanel() {
        setBackground(new Color(230, 240, 250));
        setOpaque(true);
    }

    public void setImage(Image image) {
        this.image = image;
        repaint();
    }

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
