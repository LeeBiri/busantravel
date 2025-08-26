package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import busantravel.auth.MemberSession;
import busantravel.dao.TravelPlanDAO;
import busantravel.model.ScheduledPlace;
import busantravel.model.TravelPlan;
import busantravel.service.ScheduledPlaceService;
import busantravel.util.FontManager;
import java.time.LocalDateTime;

public class ProfileSidePanel extends JPanel {

    private ReviewPanel reviewPanel;
    private TravelPlanDAO travelPlanDAO;

    public ProfileSidePanel(ReviewPanel reviewPanel) {
        this.reviewPanel = reviewPanel;
        this.travelPlanDAO = new TravelPlanDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            new EmptyBorder(15, 15, 15, 15)
        ));
        setBackground(Color.WHITE);
        Dimension fixedSize = new Dimension(300, 0);
        setPreferredSize(fixedSize);
        setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(300, 0));

        // Profile Title
        JLabel titleLabel = new JLabel(MemberSession.getId() + "님 안녕하세요.");
        titleLabel.setFont(FontManager.getJalnanFont(22f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titleLabel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Review filter buttons
        contentPanel.add(createStyledButton("내가 작성한 후기 보기", e -> reviewPanel.loadMyReviews()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(createStyledButton("전체 후기 보기", e -> reviewPanel.loadAllReviews()));

        // Past trips list
        JLabel pastTripsTitle = new JLabel("지난 일정 목록");
        pastTripsTitle.setFont(FontManager.getJalnanFont(18f));
        pastTripsTitle.setBorder(BorderFactory.createEmptyBorder(25, 0, 15, 0));
        pastTripsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(pastTripsTitle);

        JPanel pastTripsPanel = new JPanel();
        pastTripsPanel.setLayout(new BoxLayout(pastTripsPanel, BoxLayout.Y_AXIS));
        pastTripsPanel.setOpaque(false);

        List<TravelPlan> pastPlans = travelPlanDAO.findPastByMember(MemberSession.getId(), LocalDateTime.now());
        if (pastPlans.isEmpty()) {
            JLabel noPlansLabel = new JLabel("지난 여행 기록이 없습니다.");
            noPlansLabel.setFont(FontManager.getJalnanFont(14f));
            pastTripsPanel.add(noPlansLabel);
        } else {
            for (TravelPlan plan : pastPlans) {
                pastTripsPanel.add(createPastTripCard(plan));
                pastTripsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JScrollPane pastTripsScrollPane = new JScrollPane(pastTripsPanel);
        pastTripsScrollPane.setBorder(null);
        pastTripsScrollPane.setOpaque(false);
        pastTripsScrollPane.getViewport().setOpaque(false);
        pastTripsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(pastTripsScrollPane);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(FontManager.getJalnanFont(14f));
        button.setBackground(ColorConstants.ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addActionListener(listener);
        return button;
    }

    private JPanel createPastTripCard(TravelPlan plan) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(249, 249, 249));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel planNameLabel = new JLabel(plan.getPlan_title());
        planNameLabel.setFont(FontManager.getJalnanFont(14f));
        card.add(planNameLabel, BorderLayout.CENTER);

        JButton writeReviewButton = new JButton("후기 작성");
        writeReviewButton.setFont(FontManager.getJalnanFont(12f));
        writeReviewButton.setBackground(Color.WHITE);
        writeReviewButton.setForeground(ColorConstants.ACCENT);
        writeReviewButton.setFocusPainted(false);
        writeReviewButton.addActionListener(e -> {
            List<ScheduledPlace> scheduledPlaces = ScheduledPlaceService.getSchedulesByPlanId(plan.getPlan_id());
            new ReviewPopup((JFrame) SwingUtilities.getWindowAncestor(this), null, reviewPanel::refreshReviews, scheduledPlaces, plan.getPlan_id()).setVisible(true);
        });
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(writeReviewButton);
        
        card.add(buttonWrapper, BorderLayout.EAST);

        return card;
    }
}
