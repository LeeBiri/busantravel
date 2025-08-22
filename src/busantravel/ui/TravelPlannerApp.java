package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.model.TravelPlan;
import busantravel.util.FontManager;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.*;


public class TravelPlannerApp {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JPanel navigationPanel;
    private JLabel currentTitleLabel;
    private JLabel logoLabel;
    private FullSchedulePanel fullSchedulePanel;
    private SchedulePanel schedulePanel;
    private HomePanel homePanel;
    private MainBannerPanel mainBannerPanel;
    private ReviewPanel reviewPanel;
    private PlanTripPanel planTripPanel;
    private ChecklistPanel checklistPanel;
    private MyPagePanel myPagePanel;
    private JPanel authButtonsPanel;
    private JButton loginButton;
    private JButton signUpButton;
    private JButton logoutButton;
    private JButton myPageButton;

    private String currentTab = "home";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TravelPlannerApp().initialize());
    }

    public void initialize() {
        frame = new JFrame("부산 여행 플래너");
        frame.setSize(1500, 1000);
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());

        JPanel topAreaPanel = new JPanel(new GridBagLayout());
        topAreaPanel.setBackground(Color.WHITE);
        topAreaPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        GridBagConstraints gbc = new GridBagConstraints();

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
                logoLabel = new JLabel("<html><font color='#222222'>부산</font><font color='#DE927C'>어때</font></html>");
                logoLabel.setFont(FontManager.getJalnanFont(Font.BOLD, 26f));
            }
        } catch (Exception e) {
            logoLabel = new JLabel("<html><font color='#222222'>부산</font><font color='#DE927C'>어때</font></html>");
            logoLabel.setFont(FontManager.getJalnanFont(Font.BOLD, 26f));
        }

        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
        logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchToHome();
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        topAreaPanel.add(logoLabel, gbc);

        currentTitleLabel = new JLabel("", SwingConstants.CENTER);
        currentTitleLabel.setFont(FontManager.getJalnanFont(30f));
        currentTitleLabel.setForeground(new Color(50, 50, 50));
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topAreaPanel.add(currentTitleLabel, gbc);

        JPanel searchAndAuthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchAndAuthPanel.setBackground(Color.WHITE);

        authButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        authButtonsPanel.setBackground(Color.WHITE);
        initAuthButtons();

        searchAndAuthPanel.add(authButtonsPanel);
        searchAndAuthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        topAreaPanel.add(searchAndAuthPanel, gbc);

        mainPanel.add(topAreaPanel, BorderLayout.NORTH);

        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Color.WHITE);

        homePanel = new HomePanel(frame);
        schedulePanel = new SchedulePanel(frame, this);
        mainBannerPanel = new MainBannerPanel();
        myPagePanel = new MyPagePanel(frame, this);

        contentPanel.add(mainBannerPanel, "home");
        contentPanel.add(schedulePanel, "schedule");
        contentPanel.add(homePanel, "search");
        contentPanel.add(myPagePanel, "myPage");

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        navigationPanel = new JPanel(new GridLayout(1, 4));
        navigationPanel.setBackground(ColorConstants.ACCENT);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        initNavigationButtons();

        mainPanel.add(navigationPanel, BorderLayout.SOUTH);

        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public void switchToPlanTrip(TravelPlan plan, int dayIndex) {
        if (planTripPanel != null) {
            contentPanel.remove(planTripPanel);
        }
        
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
    
    public void goToScheduleTab() {
        currentTab = "schedule";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
    }

    public void updateContent() {
        contentCardLayout.show(contentPanel, currentTab);

        if ("home".equals(currentTab)) {
            contentPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorConstants.ACCENT));
        } else {
            contentPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorConstants.ACCENT));
        }
        
        if ("schedule".equals(currentTab)) {
            schedulePanel.getController().refreshRoomLists();
        }
        if ("review".equals(currentTab)) {
            if (reviewPanel != null) {
                contentPanel.remove(reviewPanel);
            }
            reviewPanel = new ReviewPanel();
            contentPanel.add(reviewPanel, "review");
            contentCardLayout.show(contentPanel, "review");
        }
    }

    private void initNavigationButtons() {
        navigationPanel.add(createNavButton("여행 홈", "home"));
        navigationPanel.add(createNavButton("검색", "search"));
        navigationPanel.add(createNavButton("일정", "schedule"));
        navigationPanel.add(createNavButton("후기", "review"));
    }

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

    private void updateNavButtonStyles() {
        for (Component comp : navigationPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String buttonText = btn.getText();
                String currentTabText = getTabText(currentTab);

                if (!"plan".equals(currentTab) && !"myPage".equals(currentTab)) {
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

    private String getTabText(String tabName) {
        switch (tabName) {
            case "home": return "여행 홈";
            case "schedule": return "일정";
            case "search": return "검색";
            case "review": return "후기";
            case "fullSchedule": return "일정 상세";
            case "checklist": return "체크리스트";
            case "myPage": return "마이페이지";
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
    
    public void switchToFullSchedule(TravelPlan plan) {
        if (fullSchedulePanel != null) {
            contentPanel.remove(fullSchedulePanel);
        }

        Runnable onBackCallback = this::goToScheduleTab;
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
        
        Runnable onBackCallback = this::goToScheduleTab;
        
        fullSchedulePanel = new FullSchedulePanel(this, plan, onBackCallback);
        contentPanel.add(fullSchedulePanel, "fullSchedule");
        
        contentCardLayout.show(contentPanel, "fullSchedule");
        
        updateTopPanelTitle();
        updateNavButtonStyles();
    }

    private void initAuthButtons() {
        authButtonsPanel.removeAll();

        if (MemberSession.isLoggedIn()) {
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
                switchToHome();
                refreshAuthButtons();
            });

            myPageButton.addActionListener(e -> switchToMyPage());

            authButtonsPanel.add(myPageButton);
            authButtonsPanel.add(logoutButton);

        } else {
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
                    refreshAuthButtons();
                    updateContent();
                    updateTopPanelTitle();
                    updateNavButtonStyles();
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

    public void switchToHome() {
        currentTab = "home";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
    }

    public void switchToMyPage() {
        currentTab = "myPage";
        updateContent();
        updateTopPanelTitle();
        updateNavButtonStyles();
    }
}
