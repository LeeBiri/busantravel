package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.dao.MemberDAO;
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
import busantravel.service.TravelPlanService;
import busantravel.util.FontManager;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MyPagePanel extends JPanel {

    private Font jalnanFont;
    private Color mainColor = ColorConstants.ACCENT;
    private Color backgroundColor = new Color(245, 245, 245);

    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private String loggedInUserId;
    private TravelPlannerApp mainApp;
    private PlaceDAO placeDAO;
    private TravelPlanService travelPlanService;
    private MemberService memberService;
    private MemberDAO memberDAO;

    private JLabel nicknameLabel;
    private JLabel totalTravelsLabel;
    private JLabel totalInterestsLabel;
    private JTextField tfName;
    private JTextField tfEmail;
    private JLabel lblIdValue;

    public MyPagePanel(JFrame frame, TravelPlannerApp mainApp) {
        loadJalnanFont();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        loggedInUserId = MemberSession.getId();
        this.mainApp = mainApp;
        this.travelPlanService = new TravelPlanService();
        this.memberService = new MemberService();
        this.memberDAO = new MemberDAO();
        this.placeDAO = new PlaceDAO();

        add(createMenuPanel(), BorderLayout.WEST);

        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(createTravelListPanel(), "travelList");
        contentPanel.add(createFavoritePlacePanel(), "favorite");
        contentPanel.add(createReviewPanel(), "review");
        contentPanel.add(createMemberInfoPanel(), "info");
        contentPanel.add(createChangePasswordPanel(), "changePw");

        add(contentPanel, BorderLayout.CENTER);
        contentCardLayout.show(contentPanel, "home");
    }

    public void refreshData() {
        loggedInUserId = MemberSession.getId();
        Member member = memberDAO.getMemberById(loggedInUserId);

        if (member != null) {
            nicknameLabel.setText(member.getM_name() + " 님, 환영합니다!");
            totalTravelsLabel.setText("" + travelPlanService.getAllPlansByMember(loggedInUserId).size());
            totalInterestsLabel.setText("" + InterestService.getInterestById(loggedInUserId).size());
            lblIdValue.setText(loggedInUserId);
            tfName.setText(member.getM_name());
            tfEmail.setText(member.getM_email());
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadJalnanFont() {
        jalnanFont = FontManager.getJalnanFont(16f);
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 60, 0, 20));

        JLabel lblMyInfo = new JLabel("나의 정보");
        lblMyInfo.setFont(jalnanFont.deriveFont(Font.BOLD, 28f));
        panel.add(lblMyInfo);
        panel.add(Box.createRigidArea(new Dimension(0, 50)));

        JButton btnHome = createMenuButton("마이페이지 홈");
        btnHome.addActionListener(e -> contentCardLayout.show(contentPanel, "home"));
        panel.add(btnHome);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton btnModifyInfo = createMenuButton("회원 정보 수정");
        btnModifyInfo.addActionListener(e -> contentCardLayout.show(contentPanel, "info"));
        panel.add(btnModifyInfo);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton btnChangePwd = createMenuButton("비밀번호 변경");
        btnChangePwd.addActionListener(e -> contentCardLayout.show(contentPanel, "changePw"));
        panel.add(btnChangePwd);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton btnLogout = createMenuButton("로그아웃");
        btnLogout.addActionListener(e -> {
                MemberSession.logout();
                JOptionPane.showMessageDialog(this, "로그아웃 되었습니다.");
                mainApp.switchToHome();
                mainApp.refreshAuthButtons();
        });
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
        nicknameLabel = new JLabel();
        nicknameLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 28f));
        profilePanel.add(nicknameLabel);
        topPanel.add(profilePanel);
        
        topPanel.add(Box.createVerticalStrut(40));

        JPanel statsPanel = createStatsPanel();
        topPanel.add(statsPanel);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        middlePanel.setBackground(Color.WHITE);
        middlePanel.setBorder(new EmptyBorder(40, 0, 80, 0));

        JButton btnTravelList = createMainFeatureButton("나의 여행 전체보기");
        btnTravelList.addActionListener(e -> contentCardLayout.show(contentPanel, "travelList"));
        JButton btnFavorite = createMainFeatureButton("저장한 관심 장소");
        btnFavorite.addActionListener(e -> contentCardLayout.show(contentPanel, "favorite"));
        JButton btnReview = createMainFeatureButton("내가 쓴 리뷰");
        btnReview.addActionListener(e -> contentCardLayout.show(contentPanel, "review"));

        middlePanel.add(Box.createHorizontalGlue());
        middlePanel.add(btnTravelList);
        middlePanel.add(Box.createRigidArea(new Dimension(50, 0)));
        middlePanel.add(btnFavorite);
        middlePanel.add(Box.createRigidArea(new Dimension(50, 0)));
        middlePanel.add(btnReview);
        middlePanel.add(Box.createHorizontalGlue());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(200, 200, 200)));
        panel.setBackground(Color.WHITE);
        
        totalTravelsLabel = new JLabel("0", SwingConstants.CENTER);
        JPanel item1 = createStatsItem(totalTravelsLabel, "총 여행 수");
        item1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        
        totalInterestsLabel = new JLabel("0", SwingConstants.CENTER);
        JPanel item2 = createStatsItem(totalInterestsLabel, "관심장소 수");
        item2.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        JPanel item3 = createStatsItem(new JLabel("0"), "내가 쓴 리뷰 수");

        panel.add(item1);
        panel.add(item2);
        panel.add(item3);
        
        return panel;
    }

    private JPanel createStatsItem(JLabel valueLabel, String label) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setPreferredSize(new Dimension(0, 150));
        
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

    private JPanel createTravelListPanel() { return new JPanel(); }
    private JPanel createFavoritePlacePanel() { return new JPanel(); }
    private JPanel createReviewPanel() { return new JPanel(); }

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
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblId = new JLabel("아이디");
        lblIdValue = new JLabel();
        lblId.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        lblIdValue.setFont(jalnanFont.deriveFont(Font.BOLD, 24f));
        lblIdValue.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblId, gbc);
        gbc.gridx = 1; panel.add(lblIdValue, gbc);

        JLabel lblName = new JLabel("이름");
        tfName = new JTextField(20);
        lblName.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        tfName.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0; gbc.gridy = 2; panel.add(lblName, gbc);
        gbc.gridx = 1; panel.add(tfName, gbc);

        JLabel lblEmail = new JLabel("이메일");
        tfEmail = new JTextField(20);
        lblEmail.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        tfEmail.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0; gbc.gridy = 3; panel.add(lblEmail, gbc);
        gbc.gridx = 1; panel.add(tfEmail, gbc);

        JPanel buttonAndBackPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSave = createActionButton("정보 저장");
        btnSave.addActionListener(e -> {
            Member updatedMember = new Member(loggedInUserId, tfName.getText(), MemberSession.getCurrentUser().getM_password(), tfEmail.getText());
            memberService.updateId(updatedMember);
            showStyledMessage("회원 정보가 성공적으로 저장되었습니다.", "저장 완료");
        });

        JButton btnWithdrawal = createActionButton("회원 탈퇴");
        btnWithdrawal.addActionListener(e -> {
            int confirm = showStyledConfirm("정말로 회원 탈퇴를 하시겠습니까? 모든 정보가 삭제됩니다.", "회원 탈퇴 확인");
            if (confirm == JOptionPane.YES_OPTION) {
                if (travelPlanService.getUpcomingPlansByMember(loggedInUserId).isEmpty()) {
                    memberService.deleteId(loggedInUserId, MemberSession.getCurrentUser().getM_password());
                    showStyledMessage("회원 탈퇴 했습니다..", "탈퇴");
                    MemberSession.logout();
                    mainApp.switchToHome();
                    mainApp.refreshAuthButtons();
                } else {
                    showStyledMessage("회원 탈퇴에 실패했습니다. 현재 참여중인 여행 계획이 있습니다.", "탈퇴 실패");
                }
            }
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnWithdrawal);
        
        buttonAndBackPanel.add(buttonPanel, BorderLayout.CENTER);
        buttonAndBackPanel.add(createGoBackButtonPanel(), BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
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
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblOld = new JLabel("현재 비밀번호");
        JPasswordField pfOld = new JPasswordField(20);
        lblOld.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        pfOld.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblOld, gbc);
        gbc.gridx = 1; panel.add(pfOld, gbc);

        JLabel lblNew = new JLabel("새 비밀번호");
        JPasswordField pfNew = new JPasswordField(20);
        lblNew.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        pfNew.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0; gbc.gridy = 2; panel.add(lblNew, gbc);
        gbc.gridx = 1; panel.add(pfNew, gbc);

        JLabel lblConfirm = new JLabel("새 비밀번호 확인");
        JPasswordField pfConfirm = new JPasswordField(20);
        lblConfirm.setFont(jalnanFont.deriveFont(Font.PLAIN, 24f));
        pfConfirm.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        gbc.gridx = 0; gbc.gridy = 3; panel.add(lblConfirm, gbc);
        gbc.gridx = 1; panel.add(pfConfirm, gbc);

        JPanel buttonAndBackPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnChange = createActionButton("변경하기");
        buttonPanel.add(btnChange);
        
        buttonAndBackPanel.add(buttonPanel, BorderLayout.CENTER);
        buttonAndBackPanel.add(createGoBackButtonPanel(), BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
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
                    memberService.updateId(new Member(loggedInUserId, MemberSession.getName(), newPass, MemberSession.getCurrentUser().getM_email()));
                    showStyledMessage("비밀번호가 성공적으로 변경되었습니다.", "변경 완료");
                } else {
                    showStyledMessage("현재 비밀번호가 일치하지 않습니다.", "오류");
                }
            }
        });

        return panel;
    }

    private JButton createMainFeatureButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(jalnanFont.deriveFont(Font.BOLD, 22f));
        btn.setBackground(mainColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(250, 80));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
        btn.setBackground(mainColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 50));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void showStyledMessage(String message, String title) {
        JLabel label = new JLabel(message);
        label.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
        JOptionPane.showMessageDialog(this, label, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private int showStyledConfirm(String message, String title) {
        JLabel label = new JLabel(message);
        label.setFont(jalnanFont.deriveFont(Font.PLAIN, 20f));
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

        // 이미지 영역
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

        // 텍스트 영역
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(new EmptyBorder(5, 15, 5, 5));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 16f));

        textPanel.add(nameLabel);

        // 컴포넌트 배치
        card.add(imageLabel);
        card.add(textPanel);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
                        new PlaceCard(null, interest.getP_id(), pName, interest.getP_type(), thumbUrl, PlaceCard.ViewType.GRID, jalnanFont)
                            .showPlaceDetailDialog(null, pName, interest.getP_type());
                        break;
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });
        return card;
    }


}

