package busantravel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class TravelPlannerApp {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JPanel navigationPanel;
    private JLabel currentTitleLabel;
    private JLabel logoLabel;

    private SchedulePanel schedulePanel;
    private HomePanel homePanel;
    private JPanel savedPanel; // 임시
    private JPanel reviewPanel; // 임시
    private PlanTripPanel planTripPanel; // PlanTripPanel 인스턴스

    private String currentTab = "home"; // 초기 탭 설정

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TravelPlannerApp().initialize());
    }

    public void initialize() {
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
            // 프로젝트 구조에 따라 /busantravel/logo.jpg 또는 /logo.jpg 로 변경 필요
            URL imageUrl = getClass().getResource("/busantravel/logo.jpg");
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image originalImage = originalIcon.getImage();
                int targetWidth = 150;
                double aspectRatio = (double) originalImage.getHeight(null) / originalImage.getWidth(null);
                int targetHeight = (int) (targetWidth * aspectRatio);
                Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);// getScaledInstance는 Image 객체에서 호출
                logoLabel = new JLabel(new ImageIcon(scaledImage));
            } else {
                System.err.println("로고 이미지 파일을 찾을 수 없습니다! 기본 텍스트 로고 사용.");
                logoLabel = new JLabel("<html><font color='#222222'>부산</font><font color='#DE927C'>어때</font></html>");
                logoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
            }
        } catch (Exception e) {
            System.err.println("로고 이미지 로드 중 예외 발생: " + e.getMessage());
            logoLabel = new JLabel("<html><font color='#222222'>부산</font><font color='#DE927C'>어때</font></html>");
            logoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
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
        currentTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        currentTitleLabel.setForeground(new Color(50, 50, 50));
        topAreaPanel.add(currentTitleLabel, BorderLayout.CENTER);

        // 로그인/회원가입 버튼
        JPanel authButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        authButtonsPanel.setBackground(Color.WHITE);
        JButton loginButton = new JButton("로그인");
        JButton signUpButton = new JButton("회원가입");

        loginButton.setBackground(new Color(240, 240, 240));
        loginButton.setForeground(new Color(50, 50, 50));
        signUpButton.setBackground(new Color(240, 240, 240));
        signUpButton.setForeground(new Color(50, 50, 50));
        loginButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        signUpButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        loginButton.setFocusPainted(false);
        signUpButton.setFocusPainted(false);

        loginButton.addActionListener(e -> new LoginDialog(frame).showDialog());
        signUpButton.addActionListener(e -> new SignUpDialog(frame).showDialog());

        authButtonsPanel.add(loginButton);
        authButtonsPanel.add(signUpButton);
        authButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        topAreaPanel.add(authButtonsPanel, BorderLayout.EAST);

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
        // planTripPanel은 동적으로 추가되므로 여기서 미리 생성하지 않습니다.

        contentPanel.add(homePanel, "home");
        contentPanel.add(schedulePanel, "schedule");
        contentPanel.add(savedPanel, "saved");
        contentPanel.add(reviewPanel, "review");
        // PlanTripPanel은 switchToPlanTrip 메서드에서 필요할 때 "plan" 이라는 이름으로 추가될 것입니다.

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 네비게이션 바
        navigationPanel = new JPanel(new GridLayout(1, 4));
        navigationPanel.setBackground(new Color(222, 146, 124));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        initNavigationButtons();

        mainPanel.add(navigationPanel, BorderLayout.SOUTH);

        // 초기 화면 세팅 (모든 패널이 contentPanel에 add 된 후 호출되어야 합니다.)
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // --- 새로운 메서드: PlanTripPanel로 전환하고 TripRoom 정보 전달 ---
    public void switchToPlanTrip(TripRoom room) {
        // 이미 "plan"이라는 이름의 카드가 있다면 contentPanel에서 제거하여 최신화
        // contentPanel은 CardLayout이므로 이름으로 카드를 제거하기 위해선 내부적으로
        // contentPanel.remove(Component)를 사용해야 함.
        // 이를 위해 미리 PlanTripPanel 객체를 변수로 관리하거나, removeAll() 후 다시 add 하는 방법이 있는데
        // 여기서는 PlanTripPanel 객체를 매번 새로 생성하여 추가하고 기존 것을 제거하는 방식 (간단함)

        // 중요: CardLayout은 add/remove 후 show를 통해 갱신되어야 함
        // 기존 "plan" 카드가 있을 수 있으므로 이를 제거 (PlanTripPanel 객체를 미리 변수로 가지고 있어야 함)
        if (planTripPanel != null) {
            contentPanel.remove(planTripPanel); // 이전 PlanTripPanel 인스턴스 제거
        }
        
        // 새로운 PlanTripPanel 인스턴스 생성 및 contentPanel에 "plan"이라는 이름으로 추가
        // PlanTripPanel 생성자에 `this` (TravelPlannerApp 인스턴스)와 `room.getTitle()` 전달
        planTripPanel = new PlanTripPanel(room.getStartDate(), room.getEndDate(), this, room.getTitle());
        contentPanel.add(planTripPanel, "plan"); 

        currentTab = "plan"; // 현재 탭 상태 변경
        updateContent(); // 변경된 탭으로 화면 전환 (cardLayout.show 호출)
        updateTopPanelTitle(); // 상단 타이틀 업데이트
        updateNavButtonStyles(); // 내비게이션 바 스타일 업데이트
    }
    
    // --- PlanTripPanel에서 SchedulePanel로 돌아오기 위한 메서드 ---
    public void goToScheduleTab() {
        currentTab = "schedule"; // 상태를 schedule 탭으로 변경
        updateContent(); // schedule 탭으로 화면 전환
        updateTopPanelTitle(); // 타이틀 업데이트
        updateNavButtonStyles(); // 네비게이션 버튼 스타일 업데이트 (schedule 버튼 강조)
        schedulePanel.refreshRoomLists(); // 스케줄 패널 목록 갱신 (선택사항, 필요시)
    }

    // 현재 탭에 따라 contentPanel의 내용을 업데이트
    private void updateContent() {
        // 이 메서드는 contentPanel에 컴포넌트가 모두 add된 후에 호출되어야 합니다.
        // initialize() 메서드의 마지막 부분에서 호출됩니다.
        contentCardLayout.show(contentPanel, currentTab); // CardLayout으로 해당 탭의 패널 표시

        // 특정 탭이 활성화될 때 추가적인 동작 (예: 일정 목록 갱신)
        if ("schedule".equals(currentTab)) {
            // schedulePanel.refreshRoomLists(); // 여기서는 안해도 됨. goToScheduleTab에서 호출함.
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
        button.setFont(new Font("맑은 고딕", Font.BOLD, 14));

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
                // 현재 탭이 'plan' (PlanTripPanel) 일 때는 어떤 네비게이션 버튼도 선택된 것처럼 보이지 않도록 함
                if (!"plan".equals(currentTab)) {
                    btn.setBackground(btn.getText().equals(getTabText(currentTab)) ? new Color(180, 90, 70) : new Color(222, 146, 124));
                } else {
                    btn.setBackground(new Color(222, 146, 124)); // 'plan' 탭일 땐 모든 버튼을 기본색으로
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