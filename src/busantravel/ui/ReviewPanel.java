package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import busantravel.auth.MemberSession;
import busantravel.dao.ReviewDAO;
import busantravel.model.Review;

public class ReviewPanel extends JPanel {

    private JPanel reviewsContainerPanel;
    private ReviewDAO reviewDAO;
    private JLabel reviewListTitle;

    public ReviewPanel() {
        this.reviewDAO = new ReviewDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        reviewsContainerPanel = new JPanel();
        reviewsContainerPanel.setLayout(new BoxLayout(reviewsContainerPanel, BoxLayout.Y_AXIS));
        reviewsContainerPanel.setBackground(Color.WHITE);

        JScrollPane reviewsScrollPane = new JScrollPane(reviewsContainerPanel);
        reviewsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        reviewsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        reviewsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        reviewsScrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        reviewsScrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());

        JPanel reviewListPanel = new JPanel(new BorderLayout());
        reviewListPanel.setBackground(Color.WHITE);

        reviewListTitle = new JLabel("전체 후기");
        reviewListTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        reviewListTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        reviewListPanel.add(reviewListTitle, BorderLayout.NORTH);
        reviewListPanel.add(reviewsScrollPane, BorderLayout.CENTER);

        String memberId = MemberSession.getId();
        if (memberId != null && !memberId.isEmpty()) {
            // Logged in: Use JSplitPane
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setLeftComponent(reviewListPanel);

            // Use the new ProfileSidePanel class
            ProfileSidePanel profileSidePanel = new ProfileSidePanel(this);
            splitPane.setRightComponent(profileSidePanel);

            splitPane.setResizeWeight(0.9);
            splitPane.setBorder(null);

            add(splitPane, BorderLayout.CENTER);
        } else {
            // Not logged in: Show only the review list
            add(reviewListPanel, BorderLayout.CENTER);
        }

        loadAllReviews(); // Load all reviews by default
    }

    public void loadAllReviews() {
        reviewListTitle.setText("전체 후기");
        List<Review> reviews = reviewDAO.getAllPublicReviews();
        displayReviews(reviews);
    }

    public void loadMyReviews() {
        String memberId = MemberSession.getId();
        if (memberId != null && !memberId.isEmpty()) {
            reviewListTitle.setText("내 후기");
            List<Review> reviews = reviewDAO.getReviewsByMember(memberId);
            displayReviews(reviews);
        }
    }

    private void displayReviews(List<Review> reviews) {
        reviewsContainerPanel.removeAll();
        for (Review review : reviews) {
            reviewsContainerPanel.add(createReviewCard(review));
        }
        reviewsContainerPanel.revalidate();
        reviewsContainerPanel.repaint();
    }

    public void refreshReviews() {
        loadAllReviews();
    }

    private JPanel createReviewCard(Review review) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(120, 120));
        imagePanel.setBackground(Color.GRAY);
        JLabel imageLabel = new JLabel("사진");
        imagePanel.add(imageLabel);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(review.getTitle());
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        String previewContent = review.getContent().split("\n")[0];
        if (previewContent.length() > 50) {
            previewContent = previewContent.substring(0, 50) + "...";
        }
        JLabel contentLabel = new JLabel(previewContent);
        contentLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        infoPanel.add(titleLabel);
        infoPanel.add(contentLabel);

        card.add(imagePanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton readButton = new JButton("리뷰 읽기");
        readButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        readButton.setBackground(ColorConstants.ACCENT);
        readButton.setForeground(Color.WHITE);
        readButton.addActionListener(e -> {
            ReviewDetailPopup popup = new ReviewDetailPopup((JFrame) SwingUtilities.getWindowAncestor(this), review);
            popup.setVisible(true);
        });
        buttonPanel.add(readButton);

        String currentUserId = MemberSession.getId();
        boolean isAuthor = currentUserId != null && currentUserId.equals(review.getMemberId());

        if (isAuthor) {
            JButton editButton = new JButton("후기 편집");
            editButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            editButton.setBackground(ColorConstants.ACCENT);
            editButton.setForeground(Color.WHITE);
            editButton.addActionListener(e -> {
                new ReviewPopup((JFrame) SwingUtilities.getWindowAncestor(this), review, this::refreshReviews).setVisible(true);
            });
            buttonPanel.add(editButton);
        }
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }
}
