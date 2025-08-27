package busantravel.ui;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class MainBannerPanel extends JPanel {

    private CardLayout bannerCardLayout;
    private JPanel bannerArea;
    private Timer autoSlideTimer;
    private int currentBannerIndex = 0;

    private List<Banner> banners;
    private Font jalnanFont;

    private JPanel scheduleTitlePanel;
    private JPanel scheduleButtonPanel;
    private List<JButton> scheduleButtons;
    private List<Schedule> schedules;
    
    // ColorConstants 클래스가 없으므로 임시로 정의
    private static class ColorConstants {
        public static final Color ACCENT = new Color(0, 150, 255);
    }
    
    // MatteScrollBarUI 클래스가 없으므로 임시로 정의
    private static class MatteScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(190, 190, 190);
            this.trackColor = new Color(240, 240, 240);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            Dimension zeroDim = new Dimension(0, 0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
    }

    public MainBannerPanel() {
        loadJalnanFont();

        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // --- 1. 배너 영역 초기화 ---
        banners = new ArrayList<>();
        banners.add(new Banner("/busantravel/resources/bluelinepark.jpg", "부산 블루라인파크", "멋진 해안선을 따라 떠나는 테마 여행"));
        banners.add(new Banner("/busantravel/resources/gamcheon2.jpg", "감천문화마을", "부산의 독특한 색채와 풍경"));
        banners.add(new Banner("/busantravel/resources/busanbridge.jpg", "부산항대교", "부산 바다를 잇는 웅장한 다리"));
        banners.add(new Banner("/busantravel/resources/gwangan.jpg", "광안리", "낭만과 도시가 공존하는 해변"));
        banners.add(new Banner("/busantravel/resources/haeundae.jpg", "해운대해수욕장", "부산의 대표 해변에서의 힐링"));
        banners.add(new Banner("/busantravel/resources/haedong.jpg", "해동 용궁사", "바다와 어우러진 전통 사찰"));

        bannerCardLayout = new CardLayout();
        bannerArea = new JPanel(bannerCardLayout);
        bannerArea.setPreferredSize(new Dimension(0, 540));
        bannerArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 540));
        bannerArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (int i = 0; i < banners.size(); i++) {
            bannerArea.add(createBannerCard(banners.get(i)), String.valueOf(i));
        }
        contentPanel.add(bannerArea);

        // --- 2. 일정 추천 타이틀 영역 ---
        scheduleTitlePanel = new JPanel();
        scheduleTitlePanel.setLayout(new BoxLayout(scheduleTitlePanel, BoxLayout.Y_AXIS));
        scheduleTitlePanel.setBackground(Color.WHITE);
        scheduleTitlePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        scheduleTitlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mainTitle = new JLabel("부산 꼭 가봐야하는 곳 BEST");
        mainTitle.setFont(jalnanFont.deriveFont(Font.PLAIN, 28f));
        mainTitle.setForeground(new Color(50, 50, 50));
        mainTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subTitle = new JLabel("후회없는 부산여행을 위한 가이드");
        subTitle.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        subTitle.setForeground(new Color(120, 120, 120));
        subTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        subTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        scheduleTitlePanel.add(mainTitle);
        scheduleTitlePanel.add(subTitle);
        contentPanel.add(scheduleTitlePanel);

        // --- 3. 일정 추천 버튼 영역 ---
        schedules = new ArrayList<>();
        schedules.add(new Schedule("/busantravel/resources/Haeundae.png", "해운대"));
        schedules.add(new Schedule("/busantravel/resources/Gwangalli2.png", "광안리"));
        schedules.add(new Schedule("/busantravel/resources/Seomyeon.png", "서면"));
        schedules.add(new Schedule("/busantravel/resources/Nampo.png", "남포"));
        schedules.add(new Schedule("/busantravel/resources/Yeongdo.png", "영도"));

        scheduleButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        scheduleButtonPanel.setBackground(Color.WHITE);
        scheduleButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));
        scheduleButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        scheduleButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scheduleButtons = new ArrayList<>();
        for (Schedule s : schedules) {
            JButton btn = createSquareButton(s, 180);
            scheduleButtons.add(btn);
            scheduleButtonPanel.add(btn);
        }
        contentPanel.add(scheduleButtonPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());

        add(scrollPane, BorderLayout.CENTER);

        autoSlideTimer = new Timer(5000, e -> nextSlide());
        autoSlideTimer.start();

        bannerCardLayout.show(bannerArea, "0");
    }

    private void nextSlide() {
        currentBannerIndex = (currentBannerIndex + 1) % banners.size();
        bannerCardLayout.show(bannerArea, String.valueOf(currentBannerIndex));
    }

    private JPanel createBannerCard(Banner b) {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(1500, 540));
        panel.setBackground(new Color(0, 0, 0, 0));

        JLabel imgLabel = new JLabel(scaleImageToFill(b.imagePath, 1500, 540));
        imgLabel.setBounds(0, 0, 1500, 540);
        panel.add(imgLabel);

        JPanel textBox = new JPanel();
        textBox.setBounds(0, 45, 500, 100);
        textBox.setBackground(new Color(0, 0, 0, 200));
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        textBox.setOpaque(true);

        JLabel subtitleLabel = new JLabel(b.subtitle, SwingConstants.CENTER);
        subtitleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 34f));
        subtitleLabel.setForeground(new Color(255, 255, 255));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(b.title, SwingConstants.CENTER);
        titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 23f));
        titleLabel.setForeground(new Color(255, 255, 255, 200));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBox.add(subtitleLabel);
        textBox.add(titleLabel);
        panel.add(textBox);

        panel.setComponentZOrder(textBox, 0);
        panel.setComponentZOrder(imgLabel, 1);

        return panel;
    }

    private JButton createSquareButton(Schedule s, int size) {
        ImageIcon icon = loadAndScaleImage(s.imagePath, size, size);

        JButton btn = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(size, size));
        btn.setLayout(new BorderLayout());

        JLabel overlayLabel = new JLabel(s.labelText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());

                Area area = new Area(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));
                area.add(new Area(new Rectangle(0, 0, getWidth(), getHeight() / 2)));
                g2.fill(area);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        overlayLabel.setOpaque(false);
        overlayLabel.setForeground(Color.WHITE);
        overlayLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        overlayLabel.setBackground(new Color(0, 0, 0, 140));
        btn.add(overlayLabel, BorderLayout.SOUTH);

        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        
        final RoundedBorder roundedBorder = new RoundedBorder(Color.LIGHT_GRAY, 1, 25);
        btn.setBorder(roundedBorder);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                roundedBorder.setColor(ColorConstants.ACCENT);
                roundedBorder.setThickness(3);
                overlayLabel.setBackground(new Color(0, 0, 0, 180));
                btn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                roundedBorder.setColor(Color.LIGHT_GRAY);
                roundedBorder.setThickness(1);
                overlayLabel.setBackground(new Color(0, 0, 0, 140));
                btn.repaint();
            }
        });

        btn.addActionListener(e -> {
            showAreaDetailPopup(s.labelText);
        });

        return btn;
    }

    private ImageIcon scaleImageToFill(String path, int targetWidth, int targetHeight) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) return new ImageIcon();

            BufferedImage originalImage = ImageIO.read(url);
            if (originalImage == null) return new ImageIcon();

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            double originalAspectRatio = (double) originalWidth / originalHeight;
            double targetAspectRatio = (double) targetWidth / targetHeight;

            int scaledWidth;
            int scaledHeight;

            if (originalAspectRatio > targetAspectRatio) {
                scaledHeight = targetHeight;
                scaledWidth = (int) (targetHeight * originalAspectRatio);
            } else {
                scaledWidth = targetWidth;
                scaledHeight = (int) (targetWidth / originalAspectRatio);
            }

            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();

            int x = (scaledWidth - targetWidth) / 2;
            int y = (scaledHeight - targetHeight) / 2;

            x = Math.max(0, x);
            y = Math.max(0, y);

            int cropWidth = Math.min(targetWidth, scaledWidth - x);
            int cropHeight = Math.min(targetHeight, scaledHeight - y);

            BufferedImage croppedImage = scaledImage.getSubimage(x, y, cropWidth, cropHeight);

            return new ImageIcon(croppedImage);
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    private ImageIcon loadAndScaleImage(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) return new ImageIcon();
            ImageIcon oriIcon = new ImageIcon(url);
            Image scaled = oriIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    private void loadJalnanFont() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.PLAIN, is);
            } else {
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }
    
    // 수정된 팝업 메서드
    private void showAreaDetailPopup(String areaName) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, areaName + " 상세 정보", true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, areaName + " 상세 정보", true);
        } else {
            dialog = new JDialog((Frame) null, areaName + " 상세 정보", true);
        }
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parentWindow);
        
        // AreaDetailPanel 클래스를 사용하지 않고, 임시 JLabel을 추가
        JLabel tempLabel = new JLabel("<html><h1 style='text-align:center; padding: 20px;'>'" + areaName + "' 상세 정보 패널이 준비되지 않았습니다.</h1></html>", SwingConstants.CENTER);
        dialog.add(tempLabel);
        
        dialog.setVisible(true);
    }

    private static class Banner {
        String imagePath;
        String title;
        String subtitle;

        public Banner(String imagePath, String subtitle, String title) {
            this.imagePath = imagePath;
            this.subtitle = subtitle;
            this.title = title;
        }
    }

    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(Color color, int thickness, int radius) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.thickness, this.thickness, this.thickness, this.thickness);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2, width - thickness, height - thickness, radius, radius);
            g2.dispose();
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        public void setThickness(int thickness) {
            this.thickness = thickness;
        }
    }

    private static class Schedule {
        String imagePath;
        String labelText;

        public Schedule(String imagePath, String labelText) {
            this.imagePath = imagePath;
            this.labelText = labelText;
        }
    }
}