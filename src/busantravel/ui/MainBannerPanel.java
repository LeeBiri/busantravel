package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.model.Tourist;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList; // Image 클래스를 위해 추가
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class MainBannerPanel extends JPanel {

    private CardLayout bannerCardLayout;
    private JPanel bannerArea; // 배너 이미지가 슬라이드될 영역
    private Timer autoSlideTimer;
    private int currentBannerIndex = 0;
    private PlaceDAO placeDAO;

    private List<Banner> banners;
    private Font jalnanFont;

    private JPanel busanBestPanel; // 기존 scheduleButtonPanel
    private JPanel haeundaeContentPanel; // 새로 추가할 해운대 정보 패널
    private boolean isHaeundaeContentVisible = false;

    // MainBannerPanel 필드 추가
    private JPanel areaContentContainer;       // 지역 컨텐츠 카드 영역
    private CardLayout areaCardLayout;         // 지역별 카드 전환
    private String currentArea = null;         // 현재 표시 중인 지역
    private java.util.Map<String, JPanel> areaPanelCache = new java.util.HashMap<>();


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
        loadJalnanFont(); // 폰트 로드

        // MainBannerPanel 자체는 BorderLayout을 사용하고, 중앙에 스크롤 가능한 contentPanel을 넣음
        setLayout(new BorderLayout());
        this.placeDAO = new PlaceDAO();

        // 전체 콘텐츠를 담을 패널 (BoxLayout 사용하여 세로로 쌓음)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE); // 배경색 통일

        // --- 1. 배너 영역 초기화 ---
        banners = new ArrayList<>();
        // 배너 데이터 추가 (경로, 부제목, 제목)
        banners.add(new Banner("/busantravel/resources/bluelinepark.jpg", "부산 블루라인파크", "멋진 해안선을 따라 떠나는 테마 여행"));
        banners.add(new Banner("/busantravel/resources/gamcheon2.jpg", "감천문화마을", "부산의 독특한 색채와 풍경"));
        banners.add(new Banner("/busantravel/resources/busanbridge.jpg", "부산항대교", "부산 바다를 잇는 웅장한 다리"));
        banners.add(new Banner("/busantravel/resources/gwangan.jpg", "광안리", "낭만과 도시가 공존하는 해변"));
        banners.add(new Banner("/busantravel/resources/haeundae.jpg", "해운대해수욕장", "부산의 대표 해변에서의 힐링"));
        banners.add(new Banner("/busantravel/resources/haedong.jpg", "해동 용궁사", "바다와 어우러진 전통 사찰"));

        bannerCardLayout = new CardLayout();
        bannerArea = new JPanel(bannerCardLayout);
        bannerArea.setPreferredSize(new Dimension(0, 540)); // 배너 영역 높이 고정 (가로는 유동적)
        bannerArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 540)); // BoxLayout에서 높이 고정 유지
        bannerArea.setAlignmentX(Component.CENTER_ALIGNMENT); // BoxLayout에서 중앙 정렬

        for (int i = 0; i < banners.size(); i++) {
            bannerArea.add(createBannerCard(banners.get(i)), String.valueOf(i));
        }
        contentPanel.add(bannerArea); // 전체 콘텐츠 패널에 배너 영역 추가

        // --- 2. 일정 추천 타이틀 영역 ---
        JPanel scheduleTitlePanel = new JPanel();
        scheduleTitlePanel.setLayout(new BoxLayout(scheduleTitlePanel, BoxLayout.Y_AXIS));
        scheduleTitlePanel.setBackground(Color.WHITE);
        scheduleTitlePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40)); // 상하좌우 여백
        scheduleTitlePanel.setAlignmentX(Component.CENTER_ALIGNMENT); // BoxLayout에서 중앙 정렬

        JLabel mainTitle = new JLabel("부산여행 일정 BEST");
        mainTitle.setFont(jalnanFont.deriveFont(Font.PLAIN, 28f));
        mainTitle.setForeground(new Color(50, 50, 50));
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("후회없는 부산여행을 위한 코스 추천");
        subTitle.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        subTitle.setForeground(new Color(120, 120, 120));
        subTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        scheduleTitlePanel.add(mainTitle);
        scheduleTitlePanel.add(subTitle);
        contentPanel.add(scheduleTitlePanel); // 전체 콘텐츠 패널에 타이틀 영역 추가

        // --- 3. 일정 추천 버튼 영역 ---
        List<Schedule> schedules = new ArrayList<>();
        schedules.add(new Schedule("/busantravel/resources/Haeundae.png", "해운대"));
        schedules.add(new Schedule("/busantravel/resources/Gwangalli2.png", "광안리"));
        schedules.add(new Schedule("/busantravel/resources/Seomyeon.png", "서면"));
        schedules.add(new Schedule("/busantravel/resources/Nampo.png", "남포"));
        schedules.add(new Schedule("/busantravel/resources/Yeongdo.png", "영도"));

        busanBestPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        busanBestPanel.setBackground(Color.WHITE);
        busanBestPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));
        busanBestPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        busanBestPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (Schedule s : schedules) {
            JButton btn = createSquareButton(s, 180);
            busanBestPanel.add(btn);
        }
        contentPanel.add(busanBestPanel);

        // --- 4. 지역 컨텐츠 컨테이너 추가 ---
        areaCardLayout = new CardLayout();
        areaContentContainer = new JPanel(areaCardLayout);
        areaContentContainer.setBackground(new Color(250, 250, 250));
        areaContentContainer.setVisible(false); 
        contentPanel.add(areaContentContainer); 

        initAreaConfigs();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // 세로 스크롤바 필요 시 자동 표시
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 가로 스크롤바는 표시 안 함
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 스크롤팬 자체 테두리 제거
        scrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);

        add(scrollPane, BorderLayout.CENTER); // MainBannerPanel의 중앙에 스크롤팬 추가

        // 자동 슬라이드 타이머 (5초 간격)
        autoSlideTimer = new Timer(5000, e -> nextSlide());
        autoSlideTimer.start();

        bannerCardLayout.show(bannerArea, "0"); // 초기 배너 표시
    }

    private void nextSlide() {
        currentBannerIndex = (currentBannerIndex + 1) % banners.size();
        bannerCardLayout.show(bannerArea, String.valueOf(currentBannerIndex));
    }
  
    private JPanel createBannerCard(Banner b) {
        JPanel panel = new JPanel(null); // null 레이아웃으로 이미지와 텍스트박스 자유롭게 배치
        panel.setPreferredSize(new Dimension(1500, 540)); // 배너 영역 크기 지정
        panel.setBackground(new Color(0, 0, 0, 0)); // 투명 배경

        // 사진을 비율을 유지하면서 크롭하여 채우기
        JLabel imgLabel = new JLabel(scaleImageToFill(b.imagePath, 1500, 540));
        imgLabel.setBounds(0, 0, 1500, 540); // 패널 크기에 맞춰 이미지 위치 및 크기 지정
        panel.add(imgLabel);

        // 텍스트 박스: 사진 중앙 하단에 반투명 박스 (Z-Order 최상단)
        JPanel textBox = new JPanel();
        textBox.setBounds(0, 45, 500, 100); // X, Y, 너비, 높이 직접 지정 (사진 하단, 가로 중앙)
        textBox.setBackground(new Color(0, 0, 0, 200)); // 약 80% 불투명 흰색 배경
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS)); // 세로 정렬
        textBox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // 내부 여백
        textBox.setOpaque(true); // 배경색 적용을 위해 Opaque 설정 (필수)

        // 텍스트 중앙 정렬
        JLabel subtitleLabel = new JLabel(b.subtitle, SwingConstants.CENTER); // 수평 중앙 정렬
        subtitleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 34f));
        subtitleLabel.setForeground(new Color(255, 255, 255));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(b.title, SwingConstants.CENTER); // 수평 중앙 정렬
        titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 23f));
        titleLabel.setForeground(new Color(255, 255, 255, 200));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textBox.add(subtitleLabel);
        textBox.add(titleLabel);
        panel.add(textBox);

        // 텍스트 박스가 사진 위에 보이도록 Z-Order 조정 (나중에 추가된 컴포넌트가 위에 놓이므로 순서 재지정)
        panel.setComponentZOrder(textBox, 0); // textBox를 가장 위로 (0이 최상위 레이어)
        panel.setComponentZOrder(imgLabel, 1); // imgLabel을 textBox 바로 아래 (1은 그 다음 레이어)

        return panel;
    }

    // createSquareButton(): 둥근 모서리를 가진 정사각형 일정 추천 버튼 생성
    private JButton createSquareButton(Schedule s, int size) {
        ImageIcon icon = loadAndScaleImage(s.imagePath, size, size);

        // 둥근 모서리를 위해 JButton을 상속하는 익명 클래스 사용
        JButton btn = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 둥근 사각형으로 클리핑 영역 설정
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(size, size));
        btn.setLayout(new BorderLayout());

        // 텍스트 배경의 아래쪽 모서리만 둥글게 처리하기 위한 커스텀 JLabel
        JLabel overlayLabel = new JLabel(s.labelText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());

                // 아래쪽 모서리만 둥근 사각형 생성
                Area area = new Area(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));
                area.add(new Area(new Rectangle(0, 0, getWidth(), getHeight() / 2)));
                g2.fill(area);
                
                g2.dispose();
                super.paintComponent(g); // 텍스트 렌더링
            }
        };
        overlayLabel.setOpaque(false); // 배경은 paintComponent에서 직접 그리므로 false로 설정
        overlayLabel.setForeground(Color.WHITE);
        overlayLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        overlayLabel.setBackground(new Color(0, 0, 0, 140));
        btn.add(overlayLabel, BorderLayout.SOUTH);

        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // 배경을 투명하게 하여 클리핑된 이미지가 보이도록 함
        
        // 커스텀 둥근 테두리 생성
        final RoundedBorder roundedBorder = new RoundedBorder(Color.LIGHT_GRAY, 1, 25);
        btn.setBorder(roundedBorder);

        // 호버 효과를 위한 MouseListener 추가
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                roundedBorder.setColor(ColorConstants.ACCENT); // 악센트 컬러 사용
                roundedBorder.setThickness(3); // 두께 3으로 변경
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

        btn.addActionListener(e -> showAreaContent(s.labelText));
        return btn;
    }

    // scaleImageToFill(): 이미지를 지정된 크기에 맞춰 비율을 유지하며 크롭하여 채움
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
                // 원본 이미지가 더 넓음 -> 높이에 맞춰 스케일링
                scaledHeight = targetHeight;
                scaledWidth = (int) (targetHeight * originalAspectRatio);
            } else {
                // 원본 이미지가 더 높거나 비율이 같음 -> 너비에 맞춰 스케일링
                scaledWidth = targetWidth;
                scaledHeight = (int) (targetWidth / originalAspectRatio);
            }

            // 스케일링된 이미지 생성
            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();

            // 중앙을 기준으로 크롭할 위치 계산
            int x = (scaledWidth - targetWidth) / 2;
            int y = (scaledHeight - targetHeight) / 2;

            // x, y가 음수가 되지 않도록 보정
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

    // loadAndScaleImage(): 이미지를 지정된 크기 (width, height)로 스케일링 (버튼용)
    private ImageIcon loadAndScaleImage(String path, int width, int height) {
        try {
            Image img = null;

            if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
                // 외부 URL
                BufferedImage bi = ImageIO.read(new java.net.URL(path));
                img = bi;
            } else {
                // 클래스패스 리소스
                URL url = getClass().getResource(path);
                if (url != null) {
                    img = new ImageIcon(url).getImage();
                }
            }

            if (img == null) return new ImageIcon(); // 실패 시 빈 아이콘

            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
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
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14); // 로드 실패 시 대체 폰트
            }
        } catch (Exception e) {
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14); // 예외 발생 시 대체 폰트
        }
    }
  
    private JPanel createSectionTitle(String title, String subtitle, float titleSize, float subtitleSize) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(jalnanFont.deriveFont(Font.BOLD, titleSize));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, subtitleSize));
        subtitleLabel.setForeground(new Color(150, 150, 150));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);
        return panel;
    }

    private JPanel createDetailCard(String title, String subtitle, String imagePath, String placeId, String category) {
        // 카드 컨테이너
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 그림자 (회색 투명)
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 20, 20);

                // 흰색 배경
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-10, getHeight()-10, 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false); // paintComponent로 배경 그림
        card.setPreferredSize(new Dimension(260, 280));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(0, 5));
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        ImageIcon placeholder = new ImageIcon(
            new BufferedImage(250, 150, BufferedImage.TYPE_INT_RGB)
        );

        JLabel imgLabel = new JLabel(placeholder);
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(250, 150));

        if (imagePath != null && (imagePath.startsWith("http://") || imagePath.startsWith("https://"))) {
            ImageThumbLoader.load(imgLabel, imagePath, 250, 150, placeholder);
        } else {
            ImageIcon icon = loadAndScaleImage(imagePath, 250, 150);
            imgLabel.setIcon(icon != null ? icon : placeholder);
        }

        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setPreferredSize(new Dimension(250, 150));
        imgPanel.add(imgLabel, BorderLayout.CENTER);

        JLabel overlay = new JLabel(title, SwingConstants.CENTER);
        overlay.setFont(jalnanFont.deriveFont(Font.BOLD, 16f));
        overlay.setForeground(Color.WHITE);
        overlay.setOpaque(true);
        overlay.setBackground(new Color(0,0,0,120));
        overlay.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        imgPanel.add(overlay, BorderLayout.SOUTH);

        // --- 설명 텍스트 ---
        JLabel subtitleLabel = new JLabel(
            "<html><div style='width:220px; text-align:center;'>" 
            + ellipsize(subtitle, 80) + "</div></html>", SwingConstants.CENTER
        );
        subtitleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        subtitleLabel.setForeground(new Color(90,90,90));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));

        card.add(imgPanel, BorderLayout.NORTH);
        card.add(subtitleLabel, BorderLayout.CENTER);

        // --- Hover 효과 ---
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(245, 245, 245));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.setBorder(BorderFactory.createLineBorder(new Color(0,150,255), 2)); 
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            }
        });

        card.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            // 최상위 JFrame 찾기
            Window w = SwingUtilities.getWindowAncestor(card);
            JFrame frame = (w instanceof JFrame) ? (JFrame) w : null;

            if (frame != null) {
                PlaceCard pc = new PlaceCard(frame, placeId, title, category, imagePath, 
                                             PlaceCard.ViewType.GRID, jalnanFont, null);
                pc.showPlaceDetailDialog(frame, title, category);
            }
        }
    });

        return card;
    }
    private static class Banner {
        String imagePath;
        String title;
        String subtitle;

        // 생성자 인자 순서: imagePath, subtitle, title
        public Banner(String imagePath, String subtitle, String title) {
            this.imagePath = imagePath;
            this.subtitle = subtitle;
            this.title = title;
        }
    }

    // 내부 데이터 클래스: 둥근 테두리를 위한 커스텀 Border 클래스
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

    // 내부 데이터 클래스: Schedule
    private static class Schedule {
        String imagePath;
        String labelText;

        public Schedule(String imagePath, String labelText) {
            this.imagePath = imagePath;
            this.labelText = labelText;
        }
    }
  
    // MainBannerPanel 안에 “지역 추천 설정” 예시
    private static class AreaConfig {
        List<String> touristIds = new ArrayList<>();
        List<String> restaurantIds = new ArrayList<>();
        List<String> lodgingIds = new ArrayList<>();
    }

    private java.util.Map<String, AreaConfig> areaConfigs;

    private void initAreaConfigs() {
        areaConfigs = new java.util.HashMap<>();

        AreaConfig haeundae = new AreaConfig();
        haeundae.touristIds = java.util.Arrays.asList("373","280","361"); 
        haeundae.restaurantIds = java.util.Arrays.asList("1604","2370","1850"); 
        haeundae.lodgingIds    = java.util.Arrays.asList("L000601","L000603","L000602");   
        areaConfigs.put("해운대", haeundae);

        AreaConfig gwangalli = new AreaConfig();
        gwangalli.touristIds = java.util.Arrays.asList("377","285","1305");
        gwangalli.restaurantIds = java.util.Arrays.asList("1251","1519","942");
        gwangalli.lodgingIds    = java.util.Arrays.asList("L000615","L000616");
        areaConfigs.put("광안리", gwangalli);

        AreaConfig seomyeon = new AreaConfig();
        seomyeon.touristIds = java.util.Arrays.asList("354","1329","458");
        seomyeon.restaurantIds = java.util.Arrays.asList("124","2330","2329");
        seomyeon.lodgingIds    = java.util.Arrays.asList("L000617","L000620");
        areaConfigs.put("서면", seomyeon);

        AreaConfig nampo = new AreaConfig();
        nampo.touristIds = java.util.Arrays.asList("365","257","369");
        nampo.restaurantIds = java.util.Arrays.asList("1237","1247","102");
        nampo.lodgingIds    = java.util.Arrays.asList("L000618");
        areaConfigs.put("남포", nampo);

        AreaConfig yeongdo = new AreaConfig();
        yeongdo.touristIds = java.util.Arrays.asList("1697","1179","2218");
        yeongdo.restaurantIds = java.util.Arrays.asList("1445","1437","1487");
        yeongdo.lodgingIds    = java.util.Arrays.asList("L000618");
        areaConfigs.put("영도", yeongdo);
    }

    private void showAreaContent(String areaName) {
        if (currentArea != null && currentArea.equals(areaName)) {
            // 같은 지역을 다시 누르면 접었다/펼치기 토글 원하면 아래를 사용:
            boolean toShow = !areaContentContainer.isVisible();
            areaContentContainer.setVisible(toShow);
            revalidate(); repaint();
            return;
        }

        // 새 지역
        JPanel panel = areaPanelCache.get(areaName);
        if (panel == null) {
            panel = buildAreaPanel(areaName);
            if (panel == null) return; // 설정값 없음
            areaContentContainer.add(panel, areaName);
            areaPanelCache.put(areaName, panel);
        }
        currentArea = areaName;
        areaCardLayout.show(areaContentContainer, areaName);
        areaContentContainer.setVisible(true);

        // 스크롤 위치를 자연스럽게 아래로
        SwingUtilities.invokeLater(() -> {
            Container top = getParent();
            while (top != null && !(top instanceof JScrollPane)) top = top.getParent();
            if (top instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) top;
                smoothScrollTo(sp, areaContentContainer.getY());
            }
        });
    }

    private JPanel buildAreaPanel(String areaName) {
        AreaConfig cfg = areaConfigs.get(areaName);
        if (cfg == null) {
            JOptionPane.showMessageDialog(this, "'" + areaName + "' 추천 설정이 없습니다.");
            return null;
        }

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(250,250,250));
        main.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));

        // 1) 타이틀
        main.add(createSectionTitle(areaName + " 추천 : 한 번에 보는 " + areaName + " 핫플", "", 30f, 24f));

        // 2) 관광지
        java.util.List<Tourist> tourists = placeDAO.getTouristsByIds(cfg.touristIds);
        main.add(createSectionTitle("가볼만한 명소", areaName + "에서 꼭 들러야 할 스팟", 24f, 18f));
        main.add(createGridSectionFromTourists(tourists));

        // 3) 맛집
        java.util.List<busantravel.model.Restaurant> rests = placeDAO.getRestaurantsByIds(cfg.restaurantIds);
        main.add(createSectionTitle("인기 맛집", "줄 서서 먹는 " + areaName + " 맛집", 24f, 18f));
        main.add(createGridSectionFromRestaurants(rests));

        // 4) 숙소
        java.util.List<busantravel.model.Lodging> lodgings = placeDAO.getLodgingsByIds(cfg.lodgingIds);
        main.add(createSectionTitle("인기 숙소", "오션뷰/접근성 좋은 호텔 & 숙소", 24f, 18f));
        main.add(createGridSectionFromLodgings(lodgings));

        return main;
    }

    private static String ellipsize(String text, int maxChars) {
        if (text == null) return "";
        text = text.trim().replaceAll("\\s+", " ");
        if (text.length() <= maxChars) return text;

        int cut = Math.max(text.lastIndexOf(' ', maxChars - 3), 0);
        if (cut < maxChars / 2) cut = maxChars - 3; 
        return text.substring(0, Math.min(cut, text.length())) + "...";
    }

    private JPanel createGridSectionFromTourists(List<Tourist> list) {
        JPanel grid = new JPanel(new GridLayout(1, 0, 20, 0));
        grid.setBackground(new Color(250,250,250));
        for (Tourist t : list) {
            String title = nvl(t.getT_place());
            String sub   = ellipsize(nvl(t.getT_intro()), 70);
            String img   = nvl(t.getT_thum()); // URL일 수도, 리소스일 수도
            grid.add(createDetailCard(title, sub, img, t.getT_id(), "관광지"));
        }
        return grid;
    }

    private JPanel createGridSectionFromRestaurants(List<busantravel.model.Restaurant> list) {
        JPanel grid = new JPanel(new GridLayout(1, 0, 20, 0));
        grid.setBackground(new Color(250,250,250));
        for (busantravel.model.Restaurant r : list) {
            String title = nvl(r.getR_place());
            String sub   = ellipsize(nvl(r.getR_intro()), 70);
            String img   = nvl(r.getR_thum());
            grid.add(createDetailCard(title, sub, img, r.getR_id(), "맛집"));
        }
        return grid;
    }

    private JPanel createGridSectionFromLodgings(List<busantravel.model.Lodging> list) {
        JPanel grid = new JPanel(new GridLayout(1, 0, 20, 0));
        grid.setBackground(new Color(250,250,250));
        for (busantravel.model.Lodging l : list) {
            String title = nvl(l.getL_place());
            String sub   = ellipsize(nvl(l.getL_intro()), 50);
            String img   = nvl(l.getL_thum());
            grid.add(createDetailCard(title, sub, img, l.getL_id(), "숙소"));
        }
        return grid;
    }

    private static String nvl(String s) { return (s == null) ? "" : s; }

    private void smoothScrollTo(JScrollPane sp, int target) {
        javax.swing.Timer t = new javax.swing.Timer(8, null);
        t.addActionListener(ev -> {
            JScrollBar bar = sp.getVerticalScrollBar();
            int cur = bar.getValue();
            int step = (target - cur) / 5; // 감속
            if (Math.abs(target - cur) <= 2) { bar.setValue(target); t.stop(); }
            else bar.setValue(cur + step);
        });
        t.start();
    }
}

