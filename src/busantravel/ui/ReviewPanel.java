package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.dao.ReviewDAO;
import busantravel.dao.ReviewPhotoDAO;
import busantravel.model.Review;
import busantravel.util.FontManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewPanel extends JPanel {

    private JPanel reviewsContainerPanel;
    private ReviewDAO reviewDAO;
    private JLabel reviewListTitle;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // 후기 썸네일 조회용 DAO
    private final ReviewPhotoDAO photoDAO = new ReviewPhotoDAO();

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
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setLeftComponent(reviewListPanel);

            ProfileSidePanel profileSidePanel = new ProfileSidePanel(this);
            splitPane.setRightComponent(profileSidePanel);

            splitPane.setResizeWeight(0.9);
            splitPane.setBorder(null);

            add(splitPane, BorderLayout.CENTER);
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
        if ("내 후기".equals(reviewListTitle.getText())) {
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

        // ===== 왼쪽: 썸네일 =====
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(150, 150));
        imagePanel.setBackground(Color.WHITE);

        JLabel imageLabel = new JLabel("사진", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(150, 120));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(240, 240, 240)); // 플레이스홀더
        imageLabel.setForeground(Color.DARK_GRAY);
        imageLabel.setFont(FontManager.getJalnanFont(12f));

        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // DB에서 첫 번째 사진을 읽어와 비동기로 라벨에 세팅
        loadThumbAsync(review.getReviewId(), imageLabel);

        // ===== 가운데: 텍스트 정보 =====
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

        // ===== 하단: 버튼들 =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton readButton = new JButton("리뷰 읽기");
        readButton.setFont(FontManager.getJalnanFont(12f));
        readButton.setBackground(ColorConstants.ACCENT);
        readButton.setForeground(Color.WHITE);
        readButton.addActionListener(e ->
                new ReviewDetailPopup((JFrame) SwingUtilities.getWindowAncestor(this), review).setVisible(true)
        );
        buttonPanel.add(readButton);

        String currentUserId = MemberSession.getId();
        boolean isAuthor = currentUserId != null && currentUserId.equals(review.getMemberId());

        if (isAuthor) {
            JButton editButton = new JButton("후기 편집");
            editButton.setFont(FontManager.getJalnanFont(12f));
            editButton.setBackground(ColorConstants.ACCENT);
            editButton.setForeground(Color.WHITE);
            editButton.addActionListener(e ->
                    new ReviewPopup((JFrame) SwingUtilities.getWindowAncestor(this), review, this::refreshReviews).setVisible(true)
            );
            buttonPanel.add(editButton);
        }
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    /* ---------------------- 썸네일 로딩/스케일링 유틸 ---------------------- */

    // 목록 카드에 썸네일 비동기 로딩 (target 라벨에 그려줌)
    private void loadThumbAsync(String reviewId, JLabel target) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                byte[] bytes = photoDAO.getFirstPhotoData(reviewId);
                if (bytes == null || bytes.length == 0) return null;
                return toScaledIcon(bytes, 150, 120);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        target.setText(null);
                        target.setIcon(icon);
                        target.revalidate();
                        target.repaint();
                    }
                } catch (Exception ignore) {}
            }
        }.execute();
    }

    // byte[] → 축소 아이콘
    private ImageIcon toScaledIcon(byte[] bytes, int maxW, int maxH) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
            if (src != null) {
                Image scaled = scaleToFit(src, maxW, maxH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignore) {}
        // 포맷 인식 실패 시 폴백
        ImageIcon raw = new ImageIcon(bytes);
        Image scaled = raw.getImage().getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // 비율 유지 축소
    private Image scaleToFit(BufferedImage src, int maxW, int maxH) {
        double r = Math.min((double) maxW / src.getWidth(), (double) maxH / src.getHeight());
        int w = Math.max(1, (int) Math.round(src.getWidth() * r));
        int h = Math.max(1, (int) Math.round(src.getHeight() * r));
        return src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }
}
