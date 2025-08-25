package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import busantravel.auth.MemberSession;
import busantravel.dao.ReviewDAO;
import busantravel.model.Review;
import busantravel.util.FontManager;

public class ReviewPanel extends JPanel {

    private JPanel reviewsContainerPanel;
    private ReviewDAO reviewDAO;
    private JLabel reviewListTitle;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

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
        reviewsContainerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane reviewsScrollPane = new JScrollPane(reviewsContainerPanel);
        reviewsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        reviewsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        reviewsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        reviewsScrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        reviewsScrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());

        JPanel reviewListPanel = new JPanel(new BorderLayout());
        reviewListPanel.setBackground(Color.WHITE);

        reviewListTitle = new JLabel("전체 후기");
        reviewListTitle.setFont(FontManager.getJalnanFont(24f));
        reviewListTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        reviewListPanel.add(reviewListTitle, BorderLayout.NORTH);
        reviewListPanel.add(reviewsScrollPane, BorderLayout.CENTER);

        String memberId = MemberSession.getId();
        if (memberId != null && !memberId.isEmpty()) {
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.WHITE);
            
            mainPanel.add(reviewListPanel, BorderLayout.CENTER);
            
            ProfileSidePanel profileSidePanel = new ProfileSidePanel(this);
            mainPanel.add(profileSidePanel, BorderLayout.EAST);

            add(mainPanel, BorderLayout.CENTER);
        } else {
            add(reviewListPanel, BorderLayout.CENTER);
        }

        loadAllReviews();
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
            List<Review> reviews = reviewDAO.getReviewsByMemberId(memberId);
            displayReviews(reviews);
        }
    }

    private void displayReviews(List<Review> reviews) {
        reviewsContainerPanel.removeAll();
        for (Review review : reviews) {
            reviewsContainerPanel.add(createReviewCard(review));
            reviewsContainerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        reviewsContainerPanel.revalidate();
        reviewsContainerPanel.repaint();
    }

    public void refreshReviews() {
        if (reviewListTitle.getText().equals("내 후기")) {
            loadMyReviews();
        } else {
            loadAllReviews();
        }
    }

    private JPanel createReviewCard(Review review) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(150, 150));
        imagePanel.setBackground(Color.GRAY);
        JLabel imageLabel = new JLabel("사진");
        imagePanel.add(imageLabel);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(review.getTitle());
        titleLabel.setFont(FontManager.getJalnanFont(18f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String previewContent = review.getContent().replaceAll("\\n", " ").replaceAll("\\s+", " ");
        if (previewContent.length() > 80) {
            previewContent = previewContent.substring(0, 80) + "...";
        }
        JLabel contentLabel = new JLabel("<html><p style='width:100%'>" + previewContent + "</p></html>");
        contentLabel.setFont(FontManager.getJalnanFont(14f));
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        metaPanel.setBackground(Color.WHITE);
        metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String authorText = review.getMemberId();
        if (authorText == null || authorText.trim().isEmpty()) {
            authorText = review.getMemberId();
        }
        JLabel authorLabel = new JLabel(authorText);
        authorLabel.setFont(FontManager.getJalnanFont(12f));
        authorLabel.setForeground(Color.GRAY);
        
        JLabel dateLabel = new JLabel(" | " + sdf.format(review.getCreatedAt()));
        dateLabel.setFont(FontManager.getJalnanFont(12f));
        dateLabel.setForeground(Color.GRAY);

        metaPanel.add(authorLabel);
        metaPanel.add(dateLabel);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(contentLabel);
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(metaPanel);

        card.add(imagePanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton readButton = new JButton("후기 읽기");
        readButton.setFont(FontManager.getJalnanFont(12f));
        readButton.setBackground(ColorConstants.ACCENT);
        readButton.setForeground(Color.WHITE);
        readButton.addActionListener(e -> {
            new ReviewDetailPopup((JFrame) SwingUtilities.getWindowAncestor(this), review).setVisible(true);
        });
        buttonPanel.add(readButton);

        String currentUserId = MemberSession.getId();
        boolean isAuthor = currentUserId != null && currentUserId.equals(review.getMemberId());

        if (isAuthor) {
            JButton editButton = new JButton("후기 편집");
            editButton.setFont(FontManager.getJalnanFont(12f));
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
