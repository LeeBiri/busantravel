package busantravel.ui;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList; // Image 클래스를 위해 추가
import java.util.List;
import javax.swing.*;

public class MainBannerPanel extends JPanel {

    private CardLayout bannerCardLayout;
    private JPanel bannerArea; // 배너 이미지가 슬라이드될 영역
    private Timer autoSlideTimer;
    private int currentBannerIndex = 0;

    private List<Banner> banners;
    private Font jalnanFont;

    private JPanel scheduleTitlePanel; // "부산여행 일정 BEST" 타이틀 영역
    private JPanel scheduleButtonPanel; // 일정 버튼들을 담는 패널
    private List<JButton> scheduleButtons;
    private List<Schedule> schedules; // 일정 데이터 리스트

    public MainBannerPanel() {
        loadJalnanFont(); // 폰트 로드

        // MainBannerPanel 자체는 BorderLayout을 사용하고, 중앙에 스크롤 가능한 contentPanel을 넣음
        setLayout(new BorderLayout());

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
        scheduleTitlePanel = new JPanel();
        scheduleTitlePanel.setLayout(new BoxLayout(scheduleTitlePanel, BoxLayout.Y_AXIS));
        scheduleTitlePanel.setBackground(Color.WHITE);
        scheduleTitlePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40)); // 상하좌우 여백
        scheduleTitlePanel.setAlignmentX(Component.CENTER_ALIGNMENT); // BoxLayout에서 중앙 정렬

        JLabel mainTitle = new JLabel("부산여행 일정 BEST");
        mainTitle.setFont(jalnanFont.deriveFont(Font.BOLD, 28f));
        mainTitle.setForeground(new Color(50, 50, 50));
        mainTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // 텍스트 좌측 정렬

        JLabel subTitle = new JLabel("후회없는 부산여행을 위한 코스 추천");
        subTitle.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        subTitle.setForeground(new Color(120, 120, 120));
        subTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // 위쪽 여백
        subTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // 텍스트 좌측 정렬

        scheduleTitlePanel.add(mainTitle);
        scheduleTitlePanel.add(subTitle);
        contentPanel.add(scheduleTitlePanel); // 전체 콘텐츠 패널에 타이틀 영역 추가

        // --- 3. 일정 추천 버튼 영역 ---
        schedules = new ArrayList<>();
        schedules.add(new Schedule("/busantravel/resources/0.png", "당일치기"));
        schedules.add(new Schedule("/busantravel/resources/1,2.png", "1박2일"));
        schedules.add(new Schedule("/busantravel/resources/2,3.png", "2박3일"));
        schedules.add(new Schedule("/busantravel/resources/3,4.png", "3박4일"));
        schedules.add(new Schedule("/busantravel/resources/4,5.png", "4박5일"));

        scheduleButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15)); // 수평 간격 30, 수직 간격 15
        scheduleButtonPanel.setBackground(Color.WHITE);
        scheduleButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40)); // 상하좌우 여백
        scheduleButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220)); // 높이 고정 유지
        scheduleButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // BoxLayout에서 중앙 정렬

        scheduleButtons = new ArrayList<>();
        for (Schedule s : schedules) {
            JButton btn = createSquareButton(s, 180); // 버튼 크기 180x180
            scheduleButtons.add(btn);
            scheduleButtonPanel.add(btn);
        }
        contentPanel.add(scheduleButtonPanel); // 전체 콘텐츠 패널에 버튼 영역 추가

        // --- JScrollPane으로 contentPanel을 감싸 MainBannerPanel에 추가 ---
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // 세로 스크롤바 필요 시 자동 표시
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 가로 스크롤바는 표시 안 함
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 스크롤팬 자체 테두리 제거

        add(scrollPane, BorderLayout.CENTER); // MainBannerPanel의 중앙에 스크롤팬 추가

        // 자동 슬라이드 타이머 (5초 간격)
        autoSlideTimer = new Timer(5000, e -> nextSlide());
        autoSlideTimer.start();

        bannerCardLayout.show(bannerArea, "0"); // 초기 배너 표시
    }

    // nextSlide(): 다음 배너로 화면 전환 로직 (오류 수정됨)
    private void nextSlide() {
        currentBannerIndex = (currentBannerIndex + 1) % banners.size();
        bannerCardLayout.show(bannerArea, String.valueOf(currentBannerIndex));
    }

    // createBannerCard(): 배너 한 장 구성 (사진 + 중앙 하단 반투명 텍스트 박스)
    private JPanel createBannerCard(Banner b) {
        JPanel panel = new JPanel(null); // null 레이아웃으로 이미지와 텍스트박스 자유롭게 배치
        panel.setPreferredSize(new Dimension(1500, 540)); // 배너 영역 크기 지정
        panel.setBackground(new Color(0, 0, 0, 0)); // 투명 배경

        // 사진을 영역에 꽉 채우기 (비율 깨져도 좋음)
        JLabel imgLabel = new JLabel(scaleImageToFill(b.imagePath, 1500, 540));
        imgLabel.setBounds(0, 0, 1500, 540); // 패널 크기에 맞춰 이미지 위치 및 크기 지정
        panel.add(imgLabel);

        // 텍스트 박스: 사진 중앙 하단에 반투명 박스 (Z-Order 최상단)
        JPanel textBox = new JPanel();
        textBox.setBounds(390, 425, 720, 90); // X, Y, 너비, 높이 직접 지정 // X, Y, 너비, 높이 직접 지정 (사진 하단, 가로 중앙)
        textBox.setBackground(new Color(255, 255, 255, 200)); // 약 80% 불투명 흰색 배경
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS)); // 세로 정렬
        textBox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // 내부 여백
        textBox.setOpaque(true); // 배경색 적용을 위해 Opaque 설정 (필수)

        // 텍스트 중앙 정렬
        JLabel subtitleLabel = new JLabel(b.subtitle, SwingConstants.CENTER); // 수평 중앙 정렬
        subtitleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        subtitleLabel.setForeground(new Color(50, 50, 50));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // BoxLayout에서 중앙 정렬

        JLabel titleLabel = new JLabel("<html><b>" + b.title + "</b></html>", SwingConstants.CENTER); // 수평 중앙 정렬
        titleLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // 위쪽 여백
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // BoxLayout에서 중앙 정렬

        textBox.add(subtitleLabel);
        textBox.add(titleLabel);
        panel.add(textBox);

        // 텍스트 박스가 사진 위에 보이도록 Z-Order 조정 (나중에 추가된 컴포넌트가 위에 놓이므로 순서 재지정)
        panel.setComponentZOrder(textBox, 0); // textBox를 가장 위로 (0이 최상위 레이어)
        panel.setComponentZOrder(imgLabel, 1); // imgLabel을 textBox 바로 아래 (1은 그 다음 레이어)

        return panel;
    }

    // createSquareButton(): 정사각형 일정 추천 버튼 생성 (이미지 + 텍스트 오버레이)
    private JButton createSquareButton(Schedule s, int size) {
        ImageIcon icon = loadAndScaleImage(s.imagePath, size, size); // 정사각형으로 스케일된 이미지 로드

        JButton btn = new JButton(icon); // 이미지 아이콘을 버튼에 설정
        btn.setPreferredSize(new Dimension(size, size)); // 버튼 크기 설정
        btn.setLayout(new BorderLayout()); // 버튼 내부 레이아웃

        JLabel overlayLabel = new JLabel(s.labelText, SwingConstants.CENTER); // 텍스트 오버레이
        overlayLabel.setForeground(Color.WHITE); // 텍스트 색상
        overlayLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 18f)); // 텍스트 폰트
        overlayLabel.setOpaque(true); // 배경색 적용
        overlayLabel.setBackground(new Color(0, 0, 0, 140)); // 반투명 검정 배경
        btn.add(overlayLabel, BorderLayout.SOUTH); // 버튼 하단에 배치

        btn.setFocusPainted(false); // 포커스 테두리 제거
        btn.setContentAreaFilled(false); // 버튼 콘텐츠 영역 채움 제거 (이미지 노출)
        btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // 테두리

        btn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                s.labelText + " 일정 추천 내용은 준비 중입니다.",
                "일정 추천", JOptionPane.INFORMATION_MESSAGE)); // 클릭 이벤트

        return btn;
    }

    // scaleImageToFill(): 이미지를 지정된 크기 (width, height)에 맞춰 꽉 채우도록 스케일링 (비율 무시)
    private ImageIcon scaleImageToFill(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) return new ImageIcon();
            ImageIcon oriIcon = new ImageIcon(url);
            // 비율 무시하고 강제로 이미지 크기 조절하여 영역 꽉 채움
            Image scaled = oriIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    // loadAndScaleImage(): 이미지를 지정된 크기 (width, height)로 스케일링 (버튼용)
    private ImageIcon loadAndScaleImage(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) return new ImageIcon();
            ImageIcon oriIcon = new ImageIcon(url);
            // 비율 유지하며 스케일링 (정사각형 버튼에 적합)
            Image scaled = oriIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    // loadJalnanFont(): 커스텀 폰트 로딩
    private void loadJalnanFont() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/busantravel/resources/JalnanGothicTTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14); // 로드 실패 시 대체 폰트
            }
        } catch (Exception e) {
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14); // 예외 발생 시 대체 폰트
        }
    }

    // 내부 데이터 클래스: Banner (생성자 인자 순서 변경)
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

    // 내부 데이터 클래스: Schedule
    private static class Schedule {
        String imagePath;
        String labelText;

        public Schedule(String imagePath, String labelText) {
            this.imagePath = imagePath;
            this.labelText = labelText;
        }
    }
}