package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.dao.PlaceDAO;
import busantravel.model.Festival;
import busantravel.model.Interest;
import busantravel.model.Lodging;
import busantravel.model.Member;
import busantravel.model.Restaurant;
import busantravel.model.Tourist;
import busantravel.model.TravelPlan;
import busantravel.service.InterestService;
import busantravel.service.MemberService;
import busantravel.service.ReviewService;
import busantravel.service.TravelPlanService;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MyPagePanel extends JPanel {

    private Font jalnanFont;
    private Color mainColor = new Color(255, 51, 51); // 빨간색
    private Color backgroundColor = new Color(240, 240, 240); // 부드러운 회색

    private JButton btnHome, btnModifyInfo, btnChangePwd, btnLogout;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private String loggedInUserId;
    private TravelPlannerApp mainApp;
    private PlaceDAO placeDAO;
    private ReviewService reviewService;
    private JLabel nicknameLabel;
    private JPanel statsPanel;


    public MyPagePanel(JFrame frame, TravelPlannerApp mainApp) {
        loadJalnanFont();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        loggedInUserId = MemberSession.getId();
        this.mainApp = mainApp;

        JPanel menuPanel = createMenuPanel();
        add(menuPanel, BorderLayout.WEST);

        placeDAO = new PlaceDAO();
        reviewService = new ReviewService();
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(createTravelListPanel(), "travelList");
        contentPanel.add(createFavoritePlacePanel(), "favorite");
        contentPanel.add(createMemberInfoPanel(), "info");
        contentPanel.add(createChangePasswordPanel(), "changePw");

        add(contentPanel, BorderLayout.CENTER);
        contentCardLayout.show(contentPanel, "home");
    }

    public void refreshData() {
        loggedInUserId = MemberSession.getId();
        if (loggedInUserId == null) {
            return;
        }
        
        nicknameLabel.setText(MemberSession.getName() + " 님, 환영합니다!");

        statsPanel.removeAll();
        JPanel item1 = createStatsItem("" + TravelPlanService.getAllPlansByMember(loggedInUserId).size(), "총 여행 수");
        item1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        JPanel item2 = createStatsItem("" + InterestService.getInterestById(loggedInUserId).size(), "관심장소 수");
        item2.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        JPanel item3 = createStatsItem("" + reviewService.getReviewCountByMember(loggedInUserId), "내가 쓴 리뷰 수");
        statsPanel.add(item1);
        statsPanel.add(item2);
        statsPanel.add(item3);
        statsPanel.revalidate();
        statsPanel.repaint();

        // Recreate and re-add all dynamic panels to ensure they are up-to-date
        contentPanel.removeAll();
        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(createTravelListPanel(), "travelList");
        contentPanel.add(createFavoritePlacePanel(), "favorite");
        contentPanel.add(createMemberInfoPanel(), "info");
        contentPanel.add(createChangePasswordPanel(), "changePw");
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void loadJalnanFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/JalnanGothicTTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(jalnanFont);
            } else {
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 16);
            }
        } catch (Exception e) {
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 16);
        }
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 60, 0, 20));

        JLabel lblMyInfo = new JLabel("나의 정보");
        lblMyInfo.setFont(jalnanFont.deriveFont(Font.BOLD, 28f));
        lblMyInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblMyInfo);
        panel.add(Box.createRigidArea(new Dimension(0, 50)));

        btnHome = createMenuButton("마이페이지 홈");
        btnModifyInfo = createMenuButton("회원 정보 수정");
        btnChangePwd = createMenuButton("비밀번호 변경");
        btnLogout = createMenuButton("로그아웃");

        btnHome.addActionListener(e -> contentCardLayout.show(contentPanel, "home"));
        btnModifyInfo.addActionListener(e -> contentCardLayout.show(contentPanel, "info"));
        btnChangePwd.addActionListener(e -> contentCardLayout.show(contentPanel, "changePw"));
        btnLogout.addActionListener(e -> {
            MemberSession.logout();
            JOptionPane.showMessageDialog(this, "로그아웃 되었습니다.");
            mainApp.updateContent();
            mainApp.refreshAuthButtons();
            mainApp.switchToHome();
        });

        panel.add(btnHome);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(btnModifyInfo);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(btnChangePwd);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(btnLogout);

        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        button.setForeground(new Color(60, 60, 60));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(mainColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(new Color(60, 60, 60));
            }
        });
        return button;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(60, 80, 60, 80));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.WHITE);

        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        nicknameLabel = new JLabel((MemberSession.getName() != null ? MemberSession.getName() : "") + " 님, 환영합니다!");
        nicknameLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 28f));
        profilePanel.add(nicknameLabel);

        topPanel.add(profilePanel);
        topPanel.add(Box.createVerticalStrut(40));

        statsPanel = createStatsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(statsPanel);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        middlePanel.setBackground(Color.WHITE);
        middlePanel.setBorder(new EmptyBorder(40, 0, 80, 0));
        middlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnTravelList = createMainFeatureButton("나의 여행 전체보기", "calendar.png");
        btnTravelList.addActionListener(e -> contentCardLayout.show(contentPanel, "travelList"));

        JButton btnFavorite = createMainFeatureButton("저장한 관심 장소", "heart.png");
        btnFavorite.addActionListener(e -> contentCardLayout.show(contentPanel, "favorite"));

        middlePanel.add(Box.createHorizontalGlue());
        middlePanel.add(btnTravelList);
        middlePanel.add(Box.createRigidArea(new Dimension(50, 0)));
        middlePanel.add(btnFavorite);
        middlePanel.add(Box.createHorizontalGlue());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(200, 200, 200)));
        panel.setBackground(Color.WHITE);

        String userId = MemberSession.getId();
        if (userId != null) {
            JPanel item1 = createStatsItem("" + TravelPlanService.getAllPlansByMember(userId).size(), "총 여행 수");
            item1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

            JPanel item2 = createStatsItem("" + InterestService.getInterestById(userId).size(), "관심장소 수");
            item2.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

            JPanel item3 = createStatsItem("" + reviewService.getReviewCountByMember(userId), "내가 쓴 리뷰 수");

            panel.add(item1);
            panel.add(item2);
            panel.add(item3);
        }

        return panel;
    }

    private JPanel createStatsItem(String value, String label) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setPreferredSize(new Dimension(0, 150));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 35f));
        valueLabel.setForeground(mainColor);

        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 30f));

        itemPanel.add(valueLabel, BorderLayout.CENTER);
        itemPanel.add(labelLabel, BorderLayout.SOUTH);

        return itemPanel;
    }

    private JPanel createGoBackButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JButton btnBack = new JButton("뒤로가기");
        btnBack.setFont(jalnanFont.deriveFont(Font.BOLD, 18f));
        btnBack.setBackground(mainColor);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnBack.addActionListener(e -> contentCardLayout.show(contentPanel, "home"));
        panel.add(btnBack);

        return panel;
    }

    private JPanel createTravelListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(60, 80, 60, 80));

        JLabel title = new JLabel("나의 여행 일정");
        title.setFont(jalnanFont.deriveFont(Font.BOLD, 30f));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(40, 0, 0, 0));

        JPanel statsPanel = new JPanel(new GridLayout(1, 2));
        statsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200)));

        JPanel upcomingStatsPanel = createStatsItem(
            "" + TravelPlanService.getUpcomingPlansByMember(loggedInUserId).size(),
            "다가오는 여행"
        );
        upcomingStatsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        JPanel pastStatsPanel = createStatsItem(
            "" + TravelPlanService.getPastPlansByMember(loggedInUserId).size(),
            "지난 여행"
        );

        statsPanel.add(upcomingStatsPanel);
        statsPanel.add(pastStatsPanel);

        content.add(statsPanel, BorderLayout.NORTH);

        JPanel listAreaPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        listAreaPanel.setBackground(Color.WHITE);
        listAreaPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        java.util.List<TravelPlan> pastTravelPlan =
            TravelPlanService.getPastPlansByMember(loggedInUserId);
        java.util.List<TravelPlan> upcomingTravelPlan =
            TravelPlanService.getUpcomingPlansByMember(loggedInUserId);

        listAreaPanel.add(createTravelListArea("다가오는 여행", upcomingTravelPlan.size(), upcomingTravelPlan));
        listAreaPanel.add(createTravelListArea("지난 여행", pastTravelPlan.size(), pastTravelPlan));

        content.add(listAreaPanel, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.add(createGoBackButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTravelListArea(String categoryTitle, int itemCount, java.util.List<TravelPlan> planList) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JLabel titleLabel = new JLabel(categoryTitle);
        titleLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 22f));
        titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new GridBagLayout());
        listPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        for (int i = 0; i < itemCount; i++) {
            gbc.gridy = i;
            JPanel item = createTravelItem(
                planList.get(i).getPlan_title(),
                planList.get(i).getPlan_period_start(),
                planList.get(i).getPlan_period_end(),
                planList.get(i).getPlan_id()
            );
            item.setPreferredSize(new Dimension(300, 80));
            listPanel.add(item, gbc);
        }

        listPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTravelItem(String title, LocalDateTime startDate, LocalDateTime endDate, String roomCode) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel("●");
        iconLabel.setForeground(new Color(150, 150, 255));
        iconLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));

        JPanel textPanel = new JPanel(new GridLayout(0, 1));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 18f));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStart = startDate.format(formatter);
        String formattedEnd = endDate.format(formatter);

        JLabel dateLabel = new JLabel(formattedStart + " ~ " + formattedEnd);
        dateLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

        JLabel roomCodeLabel = new JLabel("방 코드: " + roomCode);
        roomCodeLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

        textPanel.add(titleLabel);
        textPanel.add(dateLabel);
        textPanel.add(roomCodeLabel);

        contentPanel.add(iconLabel);
        contentPanel.add(textPanel);

        JLabel arrowLabel = new JLabel(">");
        arrowLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 24f));

        itemPanel.add(contentPanel, BorderLayout.WEST);
        itemPanel.add(arrowLabel, BorderLayout.EAST);

        return itemPanel;
    }

    private JPanel createFavoritePlacePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(60, 80, 60, 80));

        JLabel title = new JLabel("저장한 관심 장소");
        title.setFont(jalnanFont.deriveFont(Font.BOLD, 30f));
        panel.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        listPanel.setBackground(Color.WHITE);
        listPanel.setMaximumSize(null);

        java.util.List<Interest> interests = InterestService.getInterestById(loggedInUserId);
        if (interests.isEmpty()) {
            JLabel emptyLabel = new JLabel("저장한 관심 장소가 없습니다.", SwingConstants.CENTER);
            emptyLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
            listPanel.add(emptyLabel);
        } else {
            for (Interest interest : interests) {
                Object place = placeDAO.getPlaceByTypeAndId(interest.getP_type(), interest.getP_id());
                String placeName = "";
                String thumbUrl = "";

                if (place instanceof Tourist) {
                    Tourist t = (Tourist) place;
                    placeName = t.getT_place();
                    thumbUrl = t.getT_thum();
                } else if (place instanceof Restaurant) {
                    Restaurant r = (Restaurant) place;
                    placeName = r.getR_place();
                    thumbUrl = r.getR_thum();
                } else if (place instanceof Lodging) {
                    Lodging l = (Lodging) place;
                    placeName = l.getL_place();
                    thumbUrl = l.getL_thum();
                } else if (place instanceof Festival) {
                    Festival f = (Festival) place;
                    placeName = f.getF_name();
                    thumbUrl = f.getF_thum();
                }

                JPanel box = createFavoritePlaceBox(placeName, thumbUrl);
                listPanel.add(box);
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        ((JPanel) scroll.getViewport().getView()).setAlignmentY(Component.TOP_ALIGNMENT);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(createGoBackButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMemberInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(100, 200, 100, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("회원 정보 수정");
        title.setFont(jalnanFont.deriveFont(Font.BOLD, 36f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblId = new JLabel("아이디");
        JLabel lblIdValue = new JLabel(loggedInUserId);
        lblId.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        lblIdValue.setFont(jalnanFont.deriveFont(Font.BOLD, 24f));
        lblIdValue.setForeground(Color.GRAY);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblId, gbc);
        gbc.gridx = 1;
        panel.add(lblIdValue, gbc);

        JLabel lblName = new JLabel("이름");
        JTextField tfName = new JTextField(MemberSession.getName(), 20);
        lblName.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        tfName.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblName, gbc);
        gbc.gridx = 1;
        panel.add(tfName, gbc);

        JLabel lblEmail = new JLabel("이메일");
        JTextField tfEmail = new JTextField(20);
        if (MemberSession.getCurrentUser() != null) {
            tfEmail.setText(MemberSession.getCurrentUser().getM_email());
        }
        lblEmail.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        tfEmail.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblEmail, gbc);
        gbc.gridx = 1;
        panel.add(tfEmail, gbc);

        JPanel buttonAndBackPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSave = createActionButton("정보 저장");
        JButton btnWithdrawal = createActionButton("회원 탈퇴", mainColor);

        btnSave.addActionListener(e -> {
            Member member = new Member(
                loggedInUserId,
                tfName.getText(),
                MemberSession.getCurrentUser().getM_password(),
                tfEmail.getText()
            );
            MemberService.updateId(member);
            showStyledMessage("회원 정보가 성공적으로 저장되었습니다.", "저장 완료");
        });

        btnWithdrawal.addActionListener(e -> {
            int confirm = showStyledConfirm("정말로 회원 탈퇴를 하시겠습니까? 모든 정보가 삭제됩니다.", "회원 탈퇴 확인");
            if (confirm == JOptionPane.YES_OPTION) {
                if (TravelPlanService.getUpcomingPlansByMember(loggedInUserId).isEmpty()) {
                    MemberService.deleteId(loggedInUserId, MemberSession.getCurrentUser().getM_password());
                    showStyledMessage("회원 탈퇴 했습니다..", "탈퇴");
                    MemberSession.logout();
                    mainApp.updateContent();
                    mainApp.refreshAuthButtons();
                    mainApp.switchToHome();
                } else {
                    showStyledMessage("회원 탈퇴에 실패했습니다. 현재 참여중인 여행 계획이 있습니다.", "탈퇴 실패");
                }
            }
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnWithdrawal);

        buttonAndBackPanel.add(buttonPanel, BorderLayout.CENTER);
        buttonAndBackPanel.add(createGoBackButtonPanel(), BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(buttonAndBackPanel, gbc);

        return panel;
    }
    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(100, 200, 100, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("비밀번호 변경");
        title.setFont(jalnanFont.deriveFont(Font.BOLD, 36f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblOld = new JLabel("현재 비밀번호");
        JPasswordField pfOld = new JPasswordField(20);
        lblOld.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        pfOld.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblOld, gbc);
        gbc.gridx = 1;
        panel.add(pfOld, gbc);

        JLabel lblNew = new JLabel("새 비밀번호");
        JPasswordField pfNew = new JPasswordField(20);
        lblNew.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        pfNew.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblNew, gbc);
        gbc.gridx = 1;
        panel.add(pfNew, gbc);

        JLabel lblConfirm = new JLabel("새 비밀번호 확인");
        JPasswordField pfConfirm = new JPasswordField(20);
        lblConfirm.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        pfConfirm.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblConfirm, gbc);
        gbc.gridx = 1;
        panel.add(pfConfirm, gbc);

        JPanel buttonAndBackPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnChange = createActionButton("변경하기");
        buttonPanel.add(btnChange);

        buttonAndBackPanel.add(buttonPanel, BorderLayout.CENTER);
        buttonAndBackPanel.add(createGoBackButtonPanel(), BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonAndBackPanel, gbc);

        btnChange.addActionListener(e -> {
            String oldPass = new String(pfOld.getPassword());
            String newPass = new String(pfNew.getPassword());
            String confirmPass = new String(pfConfirm.getPassword());

            if (!newPass.equals(confirmPass)) {
                showStyledMessage("새 비밀번호와 확인이 일치하지 않습니다.", "오류");
                return;
            } else {
                if (oldPass.equals(MemberSession.getCurrentUser().getM_password())) {
                    MemberService.updateId(new Member(
                        loggedInUserId,
                        MemberSession.getName(),
                        newPass,
                        MemberSession.getCurrentUser().getM_email()
                    ));
                    showStyledMessage("비밀번호가 성공적으로 변경되었습니다.", "변경 완료");
                } else {
                    showStyledMessage("현재 비밀번호가 일치하지 않습니다.", "오류");
                }
            }
        });

        return panel;
    }

    private JButton createMainFeatureButton(String text, String iconPath) {
        JButton btn = new JButton(text);
        btn.setFont(jalnanFont.deriveFont(Font.BOLD, 22f));
        btn.setBackground(mainColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(250, 80));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/" + iconPath);
            if (is != null) {
                ImageIcon icon = new ImageIcon(is.readAllBytes());
                Image scaledImage = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaledImage));
                btn.setHorizontalTextPosition(SwingConstants.CENTER);
                btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Icon file not found: " + iconPath);
        }

        return btn;
    }

    private JButton createActionButton(String text) {
        return createActionButton(text, mainColor);
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 50));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        return btn;
    }

    private JPanel createSubFeatureBox(String text) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(250, 250, 250));
        box.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));

        box.add(lbl, BorderLayout.CENTER);
        return box;
    }

    private void showStyledMessage(String message, String title) {
        JLabel label = new JLabel(message);
        label.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        label.setForeground(new Color(50, 50, 50));
        JOptionPane.showMessageDialog(this, label, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private int showStyledConfirm(String message, String title) {
        JLabel label = new JLabel(message);
        label.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        label.setForeground(new Color(50, 50, 50));
        return JOptionPane.showConfirmDialog(this, label, title, JOptionPane.YES_NO_OPTION);
    }

    private JPanel createFavoritePlaceBox(String name, String thumbUrl) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(460, 70));
        card.setMaximumSize(new Dimension(460, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(48, 48));
        imageLabel.setMaximumSize(new Dimension(48, 48));
        imageLabel.setMinimumSize(new Dimension(48, 48));
        try {
            if (thumbUrl != null && !thumbUrl.isEmpty()) {
                URL imageUrl = new URL(thumbUrl);
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                imageLabel.setText("No Image");
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            }
        } catch (Exception e) {
            imageLabel.setText("X");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(new EmptyBorder(5, 15, 5, 5));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 16f));
        textPanel.add(nameLabel);

        card.add(imageLabel);
        card.add(textPanel);

        // 💡 마우스 클릭 시 상세보기 다이얼로그 호출
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // 타입과 placeId를 통해 재조회
                for (Interest interest : InterestService.getInterestById(loggedInUserId)) {
                    Object place = placeDAO.getPlaceByTypeAndId(interest.getP_type(), interest.getP_id());
                    String pName = "";

                    if (place instanceof Tourist && ((Tourist) place).getT_place().equals(name)) {
                        pName = ((Tourist) place).getT_place();
                    } else if (place instanceof Restaurant && ((Restaurant) place).getR_place().equals(name)) {
                        pName = ((Restaurant) place).getR_place();
                    } else if (place instanceof Lodging && ((Lodging) place).getL_place().equals(name)) {
                        pName = ((Lodging) place).getL_place();
                    } else if (place instanceof Festival && ((Festival) place).getF_name().equals(name)) {
                        pName = ((Festival) place).getF_name();
                    }

                    if (!pName.isEmpty()) {
                        // 🎯 PlaceCard 다이얼로그 재사용
                        new PlaceCard((JFrame) SwingUtilities.getWindowAncestor(MyPagePanel.this), interest.getP_id(), pName, interest.getP_type(), thumbUrl, PlaceCard.ViewType.GRID, jalnanFont)
                            .showPlaceDetailDialog((JFrame) SwingUtilities.getWindowAncestor(MyPagePanel.this), pName, interest.getP_type());
                        break;
                    }
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });
        return card;
    }
}
