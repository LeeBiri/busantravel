package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.model.TravelPlan;
import busantravel.util.FontManager;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.*;


/**
 * 부산 여행 플래너 애플리케이션의 메인 클래스입니다.
 * 애플리케이션의 전체적인 UI 프레임, 상단 바, 네비게이션, 컨텐츠 패널을 초기화하고 관리합니다.
 * 화면 전환 로직을 담당합니다.
 */
public class TravelPlannerApp {
    private JFrame frame; // 메인 프레임
    private JPanel mainPanel; // 모든 컴포넌트를 담는 메인 패널
    private JPanel contentPanel;
    private CardLayout contentCardLayout; // 화면 전환을 위한 CardLayout
    private JPanel navigationPanel; // 하단 네비게이션 버튼을 담는 패널
    private JLabel currentTitleLabel; // 현재 화면의 제목을 표시하는 레이블
    private JLabel logoLabel; // 상단 로고 레이블
    private FullSchedulePanel fullSchedulePanel;
    private SchedulePanel schedulePanel; // '일정' 탭 패널
    private HomePanel homePanel; // '여행 홈' 탭 패널
    private MainBannerPanel mainBannerPanel; // '저장' 버튼 클릭 시 보여줄 MainBannerPanel
    private ReviewPanel reviewPanel; // '후기' 탭을 위한 패널
    private PlanTripPanel planTripPanel; // '여행 계획' 패널 인스턴스
    private ChecklistPanel checklistPanel;
    private JPanel authButtonsPanel; // 로그인, 회원가입, 마이페이지, 로그아웃을 담는 패널
    private JButton loginButton; // 로그인
    private JButton signUpButton; // 회원가입
    private JButton logoutButton; // 로그아웃
    private JButton myPageButton; // 마이페이지

    private String currentTab = "home"; // 현재 활성화된 탭을 식별하는 문자열 ("여행 홈"이 기본 화면)

    /**
     * 애플리케이션의 메인 진입점입니다.
     * @param args 커맨드 라인 인수 (사용되지 않음)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TravelPlannerApp().initialize());
    }

    /**
     * 애플리케이션의 모든 UI 컴포넌트를 초기화하고 메인 프레임을 설정합니다.
     */
    public void initialize() {
        frame = new JFrame("부산 여행 플래너");
        frame.setSize(1500, 1000); // 화면 크기 확장
        frame.setMinimumSize(new Dimension(1000, 700)); // 최소 크기 유지
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // 화면 중앙 배치

        mainPanel = new JPanel(new BorderLayout());

        // 상단 패널
        JPanel topAreaPanel = new JPanel(new GridBagLayout());
        topAreaPanel.setBackground(Color.WHITE);
        topAreaPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        GridBagConstraints gbc = new GridBagConstraints();

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
                logoLabel.setFont(FontManager.getJalnanFont(Font.BOLD, 26f));
            }
        } catch (Exception e) {
            System.err.println("로고 이미지 로드 중 예외 발생: " + e.getMessage());
            logoLabel = new JLabel("<html><font color='#222222'>부산</font><font color='#DE927C'>어때</font></html>");
            logoLabel.setFont(FontManager.getJalnanFont(Font.BOLD, 26f));
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
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // 로고는 가중치 없음
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        topAreaPanel.add(logoLabel, gbc);

        // 동적 타이틀
        currentTitleLabel = new JLabel("", SwingConstants.CENTER);
        currentTitleLabel.setFont(FontManager.getJalnanFont(30f));
        currentTitleLabel.setForeground(new Color(50, 50, 50));
        
        gbc.gridx = 1;
        gbc.weightx = 1; // 제목이 남은 공간을 모두 차지하도록 설정
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topAreaPanel.add(currentTitleLabel, gbc);

        // 검색 및 인증 패널
        JPanel searchAndAuthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchAndAuthPanel.setBackground(Color.WHITE);

        // 로그인/회원가입 버튼
        authButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        authButtonsPanel.setBackground(Color.WHITE);
        initAuthButtons();

        searchAndAuthPanel.add(authButtonsPanel);
        searchAndAuthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        
        gbc.gridx = 2;
        gbc.weightx = 0; // 검색/인증 패널은 가중치 없음
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        topAreaPanel.add(searchAndAuthPanel, gbc);

        mainPanel.add(topAreaPanel, BorderLayout.NORTH);

        // --- contentPanel (CardLayout 적용) ---
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Color.WHITE); // 전체 배경색


        // 모든 패널 인스턴스를 미리 생성하고 contentPanel에 추가
        homePanel = new HomePanel(frame);
        schedulePanel = new SchedulePanel(frame, this); // SchedulePanel에 TravelPlannerApp 인스턴스 (this) 전달
        mainBannerPanel = new MainBannerPanel(); // MainBannerPanel 객체 생성
        // reviewPanel is now created on tab switch

        contentPanel.add(mainBannerPanel, "home"); // "저장" 패널을 "home" 식별자로 변경 (새로운 "여행 홈")
        contentPanel.add(schedulePanel, "schedule");
        contentPanel.add(homePanel, "search"); // "여행 홈" 패널을 "search" 식별자로 변경 (새로운 "검색")
        // contentPanel.add(reviewPanel, "review"); // Removed from here

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 네비게이션 바
        navigationPanel = new JPanel(new GridLayout(1, 4));
        navigationPanel.setBackground(ColorConstants.ACCENT);
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

    /**
     * 특정 여행 계획을 편집하기 위해 '여행 계획' 패널(PlanTripPanel)로 전환합니다.
     *
     * @param plan 사용자가 편집할 여행 계획(TravelPlan) 객체
     */
    public void switchToPlanTrip(TravelPlan plan, int dayIndex) {
        if (planTripPanel != null) {
            contentPanel.remove(planTripPanel);
        }
        
        // '뒤로가기' 시 호출될 콜백 정의
        Runnable onBackCallback = () -> switchToFullSchedule(plan);

        planTripPanel = new PlanTripPanel(
                plan.getPlan_period_start(),
                plan.getPlan_period_end(),
                this,
                plan.getPlan_title(),
                plan.getPlan_id(), 
                dayIndex,
                onBackCallback     
        );
        contentPanel.add(planTripPanel, "plan");

        currentTab = "plan";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
    }
    
    /**
     * '일정' 탭으로 화면을 전환합니다.
     * 주로 PlanTripPanel에서 '뒤로가기'를 눌렀을 때 호출됩니다.
     */
    public void goToScheduleTab() {
    	System.out.println("[App] goToScheduleTab app@" + System.identityHashCode(this));
        currentTab = "schedule";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
        //여행계획 새로고침
    }

    /**
     * 현재 선택된 탭(`currentTab`)에 따라 메인 컨텐츠 패널의 내용을 업데이트(전환)합니다.
     */
    private void updateContent() {
        contentCardLayout.show(contentPanel, currentTab);

        if ("home".equals(currentTab)) {
            contentPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorConstants.ACCENT)); // 홈: 하단 선만
        } else {
            contentPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorConstants.ACCENT)); // 나머지: 상하단 선
        }
        
        if ("schedule".equals(currentTab)) {
            schedulePanel.getController().refreshRoomLists();
        }
        if ("review".equals(currentTab)) {
            // 매번 reviewPanel을 새로 생성하여 로그인 상태를 반영
            if (reviewPanel != null) {
                contentPanel.remove(reviewPanel);
            }
            reviewPanel = new ReviewPanel();
            contentPanel.add(reviewPanel, "review");
            contentCardLayout.show(contentPanel, "review");
        }
    }

    /**
     * 하단 네비게이션 바에 버튼들을 생성하고 추가합니다.
     * 사용자의 요청에 따라 "저장" 탭을 맨 왼쪽으로 옮기고, "여행 홈" 탭의 이름을 "검색"으로 변경합니다.
     */
    private void initNavigationButtons() {
        // "저장" 탭을 "여행 홈"으로 변경하고 "home" 식별자로 설정, 맨 왼쪽으로 이동
        navigationPanel.add(createNavButton("여행 홈", "home"));
        // "여행 홈" 탭을 "검색"으로 변경하고 "search" 식별자로 설정
        navigationPanel.add(createNavButton("검색", "search"));
        // "일정" 탭
        navigationPanel.add(createNavButton("일정", "schedule"));
        // "후기" 탭
        navigationPanel.add(createNavButton("후기", "review"));
    }

    /**
     * 네비게이션 바에 사용될 개별 버튼을 생성합니다.
     *
     * @param text    버튼에 표시될 텍스트
     * @param tabName 버튼 클릭 시 전환될 탭의 이름
     * @return 생성된 JButton 객체
     */
    private JButton createNavButton(String text, String tabName) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(ColorConstants.ACCENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(FontManager.getJalnanFont(18f));

        button.addActionListener(e -> {
            currentTab = tabName;
            updateContent();
            updateTopPanelTitle();
            updateNavButtonStyles();
        });
        return button;
    }

    /**
     * 현재 활성화된 탭에 따라 네비게이션 버튼들의 스타일(배경색 등)을 업데이트합니다.
     */
    private void updateNavButtonStyles() {
        for (Component comp : navigationPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String buttonText = btn.getText();
                String currentTabText = getTabText(currentTab);

                if (!"plan".equals(currentTab)) {
                    if (buttonText.equals(currentTabText)) {
                        btn.setBackground(ColorConstants.SELECTED);
                    } else {
                        btn.setBackground(ColorConstants.ACCENT);
                    }
                } else {
                    btn.setBackground(ColorConstants.ACCENT);
                }
            }
        }
    }

    /**
     * 탭 이름(영문)에 해당하는 한글 텍스트를 반환합니다.
     *
     * @param tabName 탭의 고유 이름 (e.g., "home", "schedule")
     * @return 해당 탭의 한글 이름
     */
    private String getTabText(String tabName) {
        switch (tabName) {
            case "home": return "여행 홈"; // "home" 식별자는 이제 "여행 홈"을 의미
            case "schedule": return "일정";
            case "search": return "검색"; // "search" 식별자는 이제 "검색"을 의미
            case "review": return "후기";
            case "fullSchedule": return "일정 상세";
            case "checklist": return "체크리스트";
            default: return "";
        }
    }

    /**
     * 현재 활성화된 탭에 맞춰 상단 패널의 제목을 업데이트합니다.
     */
    private void updateTopPanelTitle() {
        if ("plan".equals(currentTab) && planTripPanel != null) {
            currentTitleLabel.setText(planTripPanel.getTripTitle() + " 계획"); 
        } else {
            currentTitleLabel.setText(getTabText(currentTab));
        }
    }
    

    /**
     * 아직 구현되지 않은 화면을 위한 임시 플레이스홀더 패널을 생성합니다.
     *
     * @param text 패널 중앙에 표시될 텍스트
     * @return 생성된 JPanel 객체
     */
    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(text, SwingConstants.CENTER));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    public void switchToFullSchedule(TravelPlan plan) {
        if (fullSchedulePanel != null) {
            contentPanel.remove(fullSchedulePanel);
        }

        Runnable onBackCallback = () -> switchToFullSchedule(plan);
        fullSchedulePanel = new FullSchedulePanel(this, plan, onBackCallback);
        contentPanel.add(fullSchedulePanel, "fullSchedule");

        currentTab = "fullSchedule";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
    }


    public void switchToChecklist(TravelPlan plan) {
        if (checklistPanel != null) {
            contentPanel.remove(checklistPanel);
        }

        Runnable onBack = () -> switchToFullSchedule(plan);
        checklistPanel = new ChecklistPanel(this, plan, MemberSession.getId(), onBack); 
        contentPanel.add(checklistPanel, "checklist");

        currentTab = "checklist";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
    }
    
    public void refreshFullSchedule(TravelPlan plan) {
        if (fullSchedulePanel != null) {
            contentPanel.remove(fullSchedulePanel);
        }
        
        Runnable onBackCallback = () -> switchToFullSchedule(plan);
        
        fullSchedulePanel = new FullSchedulePanel(this, plan, onBackCallback);
        contentPanel.add(fullSchedulePanel, "fullSchedule");
        
        contentCardLayout.show(contentPanel, "fullSchedule");
        
        // 현재 탭 상태는 유지
        updateTopPanelTitle();
        updateNavButtonStyles();
    }

    private void initAuthButtons() {
        authButtonsPanel.removeAll(); // 기존 버튼 제거

        if (MemberSession.isLoggedIn()) {
            // 로그인 상태일 경우: 마이페이지 + 로그아웃
            myPageButton = new JButton("마이페이지");
            logoutButton = new JButton("로그아웃");

            myPageButton.setFont(FontManager.getJalnanFont(12f));
            logoutButton.setFont(FontManager.getJalnanFont(12f));
            myPageButton.setBackground(new Color(240, 240, 240));
            logoutButton.setBackground(new Color(240, 240, 240));
            myPageButton.setForeground(new Color(50, 50, 50));
            logoutButton.setForeground(new Color(50, 50, 50));
            myPageButton.setFocusPainted(false);
            logoutButton.setFocusPainted(false);

            logoutButton.addActionListener(e -> {
                MemberSession.logout();
                JOptionPane.showMessageDialog(frame, "로그아웃 되었습니다.");
                currentTab = "home";
                updateContent(); 
                refreshAuthButtons();
            });

            myPageButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(frame, "마이페이지는 아직 준비 중입니다.");
        });

        authButtonsPanel.add(myPageButton);
        authButtonsPanel.add(logoutButton);

    } else {
        // 비로그인 상태일 경우: 로그인 + 회원가입
        loginButton = new JButton("로그인");
        signUpButton = new JButton("회원가입");

        loginButton.setFont(FontManager.getJalnanFont(12f));
        signUpButton.setFont(FontManager.getJalnanFont(12f));
        loginButton.setBackground(new Color(240, 240, 240));
        signUpButton.setBackground(new Color(240, 240, 240));
        loginButton.setForeground(new Color(50, 50, 50));
        signUpButton.setForeground(new Color(50, 50, 50));
        loginButton.setFocusPainted(false);
        signUpButton.setFocusPainted(false);

        loginButton.addActionListener(e -> {
            new LoginDialog(frame, this, () -> {
                refreshAuthButtons();            // 로그인/로그아웃 버튼 새로고침
                updateContent();                 // 패널 업데이트 (여기서 schedulePanel 새로고침됨)
                updateTopPanelTitle();           // 제목 바꾸기
                updateNavButtonStyles();         // 네비게이션 스타일 갱신
            }).showDialog();
        });
        signUpButton.addActionListener(e -> new SignUpDialog(frame, this).showDialog());

        authButtonsPanel.add(loginButton);
        authButtonsPanel.add(signUpButton);
    }

    authButtonsPanel.revalidate();
    authButtonsPanel.repaint();
}

public void refreshAuthButtons() {
    initAuthButtons();
}


}
