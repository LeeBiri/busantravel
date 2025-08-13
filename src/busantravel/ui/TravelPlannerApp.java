package busantravel.ui;

import busantravel.model.TravelPlan;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URL;
import javax.swing.*;

public class TravelPlannerApp {
    private Font jalnanFont; // 폰트 필드 추가
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JPanel navigationPanel;
    private JLabel currentTitleLabel;
    private JLabel logoLabel;
    private JTextField searchField; // 검색 필드 추가
    private JButton searchButton; // 검색 버튼 추가

    private SchedulePanel schedulePanel;
    private HomePanel homePanel;
    private JPanel savedPanel; // 임시
    private JPanel reviewPanel; // 임시
    private PlanTripPanel planTripPanel; // PlanTripPanel 인스턴스

    private String currentTab = "home"; // 초기 탭 설정

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TravelPlannerApp().initialize());
    }

    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f); // 기본 크기 14로 설정
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

    public void initialize() {
        loadFont(); // 폰트 로드
        frame = new JFrame("부산 여행 플래너");
        frame.setSize(1500, 1000); // 화면 크기 확장
        frame.setMinimumSize(new Dimension(1000, 700)); // 최소 크기 유지
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // 화면 중앙 배치

        mainPanel = new JPanel(new BorderLayout());

        // 상단 패널
        JPanel topAreaPanel = new JPanel(new BorderLayout());
        topAreaPanel.setBackground(Color.WHITE);
        topAreaPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // 상단/하단 패딩 조정

        // 로고 이미지 로드 및 스케일링
        try {
            URL imageUrl = getClass().getResource("/busantravel/ui/logo.jpg");
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image originalImage = originalIcon.getImage();
                int targetWidth = 150;
                double aspectRatio = (double) originalImage.getHeight(null) / originalImage.getWidth(null);
                int targetHeight = (int) (targetWidth * aspectRatio);
                Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(scaledImage));
            } else {
                System.err.println("로고 이미지 파일을 찾을 수 없습니다! 기본 텍스트 로고 사용.");
                logoLabel = new JLabel("<html><font color='#222222'>부산</font><font color='#DE927C'>어때</font></html>");
                logoLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 26f));
            }
        } catch (Exception e) {
            System.err.println("로고 이미지 로드 중 예외 발생: " + e.getMessage());
            logoLabel = new JLabel("<html><font color='#222222'>부산</font><font color='#DE927C'>어때</font></html>");
            logoLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 26f));
        }

        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10)); // 로고 내부 패딩
        logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 로고 클릭 시 여행 홈으로 이동
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentTab = "home";
                updateContent();
                updateTopPanelTitle();
                updateNavButtonStyles();
            }
        });

        topAreaPanel.add(logoLabel, BorderLayout.WEST);

        // 동적 타이틀
        currentTitleLabel = new JLabel("", SwingConstants.CENTER);
        currentTitleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 30f));
        currentTitleLabel.setForeground(new Color(50, 50, 50));
        topAreaPanel.add(currentTitleLabel, BorderLayout.CENTER);

        // 검색 및 인증 패널
        JPanel searchAndAuthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchAndAuthPanel.setBackground(Color.WHITE);

        // 검색 UI
        searchField = new JTextField(15);
        searchButton = new JButton("검색");
        searchButton.setPreferredSize(new Dimension(80, 25));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(new Color(180, 90, 70));
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setFont(jalnanFont.deriveFont(Font.PLAIN, 11f));
        
        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            if (homePanel != null) {
                homePanel.performSearch(query.trim());
            }
            currentTab = "home"; // 검색 후 홈 탭으로 이동
            updateContent(); // 홈 탭으로 전환
            updateTopPanelTitle();
            updateNavButtonStyles();
        });

        searchAndAuthPanel.add(searchField);
        searchAndAuthPanel.add(searchButton);

        // 로그인/회원가입 버튼
        JPanel authButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        authButtonsPanel.setBackground(Color.WHITE);
        JButton loginButton = new JButton("로그인");
        JButton signUpButton = new JButton("회원가입");

        loginButton.setBackground(new Color(240, 240, 240));
        loginButton.setForeground(new Color(50, 50, 50));
        signUpButton.setBackground(new Color(240, 240, 240));
        signUpButton.setForeground(new Color(50, 50, 50));
        loginButton.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        signUpButton.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        loginButton.setFocusPainted(false);
        signUpButton.setFocusPainted(false);

        loginButton.addActionListener(e -> new LoginDialog(frame).showDialog());
        signUpButton.addActionListener(e -> new SignUpDialog(frame).showDialog());

        authButtonsPanel.add(loginButton);
        authButtonsPanel.add(signUpButton);
        searchAndAuthPanel.add(authButtonsPanel);
        searchAndAuthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        topAreaPanel.add(searchAndAuthPanel, BorderLayout.EAST);

        mainPanel.add(topAreaPanel, BorderLayout.NORTH);

        // --- contentPanel (CardLayout 적용) ---
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Color.WHITE); // 전체 배경색

        // 모든 패널 인스턴스를 미리 생성하고 contentPanel에 추가
        homePanel = new HomePanel(frame);
        schedulePanel = new SchedulePanel(frame, this); // SchedulePanel에 TravelPlannerApp 인스턴스 (this) 전달
        savedPanel = createPlaceholderPanel("저장된 장소 화면 (추후 구현)");
        reviewPanel = createPlaceholderPanel("후기 화면 (추후 구현)");

        contentPanel.add(homePanel, "home");
        contentPanel.add(schedulePanel, "schedule");
        contentPanel.add(savedPanel, "saved");
        contentPanel.add(reviewPanel, "review");

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 네비게이션 바
        navigationPanel = new JPanel(new GridLayout(1, 4));
        navigationPanel.setBackground(new Color(222, 146, 124));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        initNavigationButtons();

        mainPanel.add(navigationPanel, BorderLayout.SOUTH);

        // 초기 화면 세팅
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public void switchToPlanTrip(TravelPlan plan) {
        if (planTripPanel != null) {
            contentPanel.remove(planTripPanel);
        }
        
        planTripPanel = new PlanTripPanel(plan.getPlan_period_start(), plan.getPlan_period_end(), this, plan.getPlan_title());
        contentPanel.add(planTripPanel, "plan"); 

        currentTab = "plan";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
    }
    
    public void goToScheduleTab() {
        currentTab = "schedule";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
        //여행계획 새로고침
    }

    private void updateContent() {
        contentCardLayout.show(contentPanel, currentTab);
        
        boolean isHomeTab = "home".equals(currentTab);
        searchField.setVisible(isHomeTab);
        searchButton.setVisible(isHomeTab);

        if ("schedule".equals(currentTab)) {
            schedulePanel.controller.refreshRoomLists();
        }
    }

    private void initNavigationButtons() {
        navigationPanel.add(createNavButton("여행 홈", "home"));
        navigationPanel.add(createNavButton("일정", "schedule"));
        navigationPanel.add(createNavButton("저장", "saved"));
        navigationPanel.add(createNavButton("후기", "review"));
    }

    private JButton createNavButton(String text, String tabName) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(222, 146, 124));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));

        button.addActionListener(e -> {
            currentTab = tabName;
            updateContent();
            updateTopPanelTitle();
            updateNavButtonStyles();
        });
        return button;
    }

    private void updateNavButtonStyles() {
        for (Component comp : navigationPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (!"plan".equals(currentTab)) {
                    btn.setBackground(btn.getText().equals(getTabText(currentTab)) ? new Color(180, 90, 70) : new Color(222, 146, 124));
                } else {
                    btn.setBackground(new Color(222, 146, 124));
                }
            }
        }
    }

    private String getTabText(String tabName) {
        switch (tabName) {
            case "home": return "여행 홈";
            case "schedule": return "일정";
            case "saved": return "저장";
            case "review": return "후기";
            default: return "";
        }
    }

    private void updateTopPanelTitle() {
        if ("plan".equals(currentTab) && planTripPanel != null) {
            currentTitleLabel.setText(planTripPanel.getTripTitle() + " 계획"); 
        } else {
            currentTitleLabel.setText(getTabText(currentTab));
        }
    }
    

    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(text, SwingConstants.CENTER));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    
    
}
