package busantravel.ui;

import busantravel.dao.ReviewPhotoDAO;
import busantravel.model.Review;
import busantravel.model.ReviewPhoto;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * 후기 상세 팝업 - 이미지(BLOB) 표시 버전
 *  - 경로(getPhotoPath) 사용하지 않음
 *  - ReviewPhotoDAO.getPhotosByReviewId(reviewId) 로 BLOB 로딩
 */
public class ReviewDetailPopup extends JDialog {

    private final Review review;
    private final ReviewPhotoDAO reviewPhotoDAO = new ReviewPhotoDAO();

    private JLabel titleValue;
    private JTextArea contentArea;
    private JLabel visibilityValue;
    private JPanel photoPanel;

    public ReviewDetailPopup(JFrame parent, Review review) {
        super(parent, "후기 상세", true);
        this.review = review;

        if (review == null) {
            JOptionPane.showMessageDialog(parent, "리뷰 정보가 없습니다.", "알림", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        initUI();
        loadData();

        setSize(650, 720);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(16, 16, 16, 16));
        main.setBackground(Color.WHITE);
        add(main, BorderLayout.CENTER);

        // ===== 상단: 제목 + 공개여부 =====
        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("제목");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleValue = new JLabel();
        titleValue.setFont(new Font("맑은 고딕", Font.PLAIN, 16));

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setBackground(Color.WHITE);
        titleBox.add(titleLabel, BorderLayout.NORTH);
        titleBox.add(titleValue, BorderLayout.CENTER);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        meta.setBackground(Color.WHITE);
        JLabel visLabel = new JLabel("공개 설정");
        visLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        visibilityValue = new JLabel();
        visibilityValue.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        meta.add(visLabel);
        meta.add(visibilityValue);

        top.add(titleBox, BorderLayout.CENTER);
        top.add(meta, BorderLayout.SOUTH);

        main.add(top, BorderLayout.NORTH);

        // ===== 중앙: 내용 + 사진 =====
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        JLabel contentLabel = new JLabel("내용");
        contentLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        contentArea = new JTextArea();
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentPanel.add(contentLabel, BorderLayout.NORTH);
        contentPanel.add(contentScroll, BorderLayout.CENTER);

        JPanel photosSection = new JPanel(new BorderLayout());
        photosSection.setBackground(Color.WHITE);
        JLabel photosLabel = new JLabel("사진");
        photosLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        photoPanel.setBackground(Color.WHITE);
        JScrollPane photoScroll = new JScrollPane(photoPanel);
        photosSection.add(photosLabel, BorderLayout.NORTH);
        photosSection.add(photoScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
        center.setBackground(Color.WHITE);
        center.add(contentPanel);
        center.add(photosSection);

        main.add(center, BorderLayout.CENTER);

        // ===== 하단: 닫기 버튼 =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);

        main.add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        titleValue.setText(safe(review.getTitle()));
        contentArea.setText(safe(review.getContent()));
        visibilityValue.setText(review.getVisibility() == 0 ? "전체 공개" : "비공개");

        loadPhotos(review.getReviewId());
    }

    /** DB의 BLOB 이미지를 읽어 썸네일로 표시 */
    private void loadPhotos(String reviewId) {
        photoPanel.removeAll();

        List<ReviewPhoto> photos = reviewPhotoDAO.getPhotosByReviewId(reviewId);
        if (photos == null || photos.isEmpty()) {
            photoPanel.add(new JLabel("등록된 사진이 없습니다."));
        } else {
            for (ReviewPhoto p : photos) {
                byte[] bytes = p.getData();
                if (bytes == null || bytes.length == 0) continue;

                ImageIcon icon = toScaledIcon(bytes, 260, 180); // 필요 시 크기 조절
                JLabel lbl = new JLabel(icon);
                lbl.setToolTipText(safe(p.getPhName()));
                photoPanel.add(lbl);
            }
        }

        photoPanel.revalidate();
        photoPanel.repaint();
    }

    /** byte[] → 썸네일 아이콘 */
    private ImageIcon toScaledIcon(byte[] bytes, int maxW, int maxH) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
            if (src != null) {
                Image scaled = scaleToFit(src, maxW, maxH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignore) {
        }
        // 포맷 인식 실패 시 fallback
        ImageIcon raw = new ImageIcon(bytes);
        Image scaled = raw.getImage().getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /** 비율 유지 축소 */
    private Image scaleToFit(BufferedImage src, int maxW, int maxH) {
        double r = Math.min((double) maxW / src.getWidth(), (double) maxH / src.getHeight());
        int w = Math.max(1, (int) Math.round(src.getWidth() * r));
        int h = Math.max(1, (int) Math.round(src.getHeight() * r));
        return src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
