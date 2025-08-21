package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import busantravel.dao.ReviewDAO;
import busantravel.model.Review;

public class ReviewPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private JPanel allReviewsPanel;
    private JPanel myReviewsPanel;
    private ReviewDAO reviewDAO;

    public ReviewPanel() {
        this.reviewDAO = new ReviewDAO();
        initializeUI();
        loadAllReviews();
        loadMyReviews();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(ColorConstants.ACCENT);

        allReviewsPanel = new JPanel();
        allReviewsPanel.setLayout(new BoxLayout(allReviewsPanel, BoxLayout.Y_AXIS));
        JScrollPane allReviewsScrollPane = new JScrollPane(allReviewsPanel);
        allReviewsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        allReviewsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        myReviewsPanel = new JPanel();
        myReviewsPanel.setLayout(new BoxLayout(myReviewsPanel, BoxLayout.Y_AXIS));
        JScrollPane myReviewsScrollPane = new JScrollPane(myReviewsPanel);
        myReviewsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        myReviewsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        tabbedPane.addTab("전체 후기", allReviewsScrollPane);
        tabbedPane.addTab("내 후기", myReviewsScrollPane);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadAllReviews() {
        allReviewsPanel.removeAll();
        List<Review> reviews = reviewDAO.getAllPublicReviews();
        for (Review review : reviews) {
            allReviewsPanel.add(createReviewCard(review, false));
        }
        allReviewsPanel.revalidate();
        allReviewsPanel.repaint();
    }

    private void loadMyReviews() {
        myReviewsPanel.removeAll();
        String memberId = busantravel.auth.MemberSession.getId();
        if (memberId != null && !memberId.isEmpty()) {
            List<Review> reviews = reviewDAO.getReviewsByMember(memberId);
            for (Review review : reviews) {
                myReviewsPanel.add(createReviewCard(review, true));
            }
        }
        myReviewsPanel.revalidate();
        myReviewsPanel.repaint();
    }

    public void refreshReviews() {
        loadAllReviews();
        loadMyReviews();
    }

    private JPanel createReviewCard(Review review, boolean isMyReview) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Left panel for image (placeholder)
        JPanel imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(120, 120));
        imagePanel.setBackground(Color.GRAY);
        JLabel imageLabel = new JLabel("사진");
        imagePanel.add(imageLabel);

        // Center panel for review info
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

        if (isMyReview) {
            JButton editButton = new JButton("편집하기");
            editButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            editButton.setBackground(ColorConstants.ACCENT);
            editButton.setForeground(Color.WHITE);
            editButton.addActionListener(e -> {
                ReviewPopup popup = new ReviewPopup((JFrame) SwingUtilities.getWindowAncestor(this), review);
                popup.setVisible(true);
                loadMyReviews(); // Refresh after edit
            });
            buttonPanel.add(editButton);
        }
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }
}
