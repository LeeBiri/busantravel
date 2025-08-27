package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import busantravel.dao.ReviewDAO;
import busantravel.dao.ReviewPhotoDAO;
import busantravel.model.Review;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import java.awt.image.BufferedImage;
import java.awt.Image;


public class ReviewPopup extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    private JComboBox<String> visibilityComboBox;
    private JButton submitButton;
    private JButton deleteButton;
    private JButton addPhotoButton;
    private JPanel photoPanel;
    private List<File> selectedPhotos = new ArrayList<>();

    private ReviewDAO reviewDAO;
    private ReviewPhotoDAO reviewPhotoDAO;
    private Review review; // for editing
    private Runnable onDispose;

    public ReviewPopup(JFrame parent, Review review, Runnable onDispose) {
        super(parent, review == null ? "후기 작성" : "후기 수정", true);
        this.review = review;
        this.onDispose = onDispose;
        this.reviewDAO = new ReviewDAO();
        this.reviewPhotoDAO = new ReviewPhotoDAO();
        initializeUI();
        if (review != null) {
            loadReviewData();
        }
    }

    /** ✅ 최종 단일 버전: 후기 저장 + 사진(BLOB) 저장 */
    private void submitReview() {
        if (!busantravel.auth.MemberSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "로그인이 필요합니다.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = titleField.getText();
        String content = contentArea.getText();
        int visibility = "전체 공개".equals(visibilityComboBox.getSelectedItem()) ? 0 : 1;

        String reviewId;

        if (review == null) { // 새 후기
            Review newReview = new Review();
            reviewId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            newReview.setReviewId(reviewId);
            newReview.setMemberId(busantravel.auth.MemberSession.getId());
            newReview.setTitle(title);
            newReview.setContent(content);
            newReview.setVisibility(visibility);
            reviewDAO.addReview(newReview);               // ① 후기 저장
        } else { // 수정
            review.setTitle(title);
            review.setContent(content);
            review.setVisibility(visibility);
            reviewDAO.updateReview(review);               // ① 후기 수정
            reviewId = review.getReviewId();
            // 이전 사진 제거 후 다시 저장하고 싶을 때:
            reviewPhotoDAO.deletePhotosByReviewId(reviewId);
        }

        // ② 선택된 사진들을 BLOB으로 저장
        reviewPhotoDAO.insertPhotos(reviewId, selectedPhotos);

        if (onDispose != null) onDispose.run();
        dispose();
    }

    private void initializeUI() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("제목");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleField = new JTextField();
        titleField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(titleField, BorderLayout.CENTER);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        JLabel contentLabel = new JLabel("내용");
        contentLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        contentArea = new JTextArea();
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(contentLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Photo Panel
        JPanel photoSectionPanel = new JPanel(new BorderLayout());
        photoSectionPanel.setBackground(Color.WHITE);
        addPhotoButton = new JButton("사진 추가");
        photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        photoPanel.setBackground(Color.WHITE);
        JScrollPane photoScrollPane = new JScrollPane(photoPanel);
        photoSectionPanel.add(addPhotoButton, BorderLayout.NORTH);
        photoSectionPanel.add(photoScrollPane, BorderLayout.CENTER);

        // Options Panel
        JPanel optionsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        optionsPanel.setBackground(Color.WHITE);
        JLabel visibilityLabel = new JLabel("공개 설정");
        visibilityLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        String[] visibilities = {"전체 공개", "비공개"};
        visibilityComboBox = new JComboBox<>(visibilities);
        optionsPanel.add(visibilityLabel);
        optionsPanel.add(visibilityComboBox);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        submitButton = new JButton(review == null ? "후기 등록하기" : "수정하기");

        // Style buttons
        Font buttonFont = new Font("맑은 고딕", Font.BOLD, 14);
        submitButton.setFont(buttonFont);
        submitButton.setBackground(ColorConstants.ACCENT);
        submitButton.setForeground(Color.WHITE);

        buttonPanel.add(submitButton);

        if (review != null) {
            deleteButton = new JButton("삭제하기");
            deleteButton.setFont(buttonFont);
            deleteButton.setBackground(Color.RED);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.addActionListener(e -> deleteReview());
            buttonPanel.add(deleteButton);
        }

        // Add components to main panel
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(contentPanel);
        centerPanel.add(photoSectionPanel);

        formPanel.add(centerPanel, BorderLayout.CENTER);
        formPanel.add(optionsPanel, BorderLayout.SOUTH);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Add Action Listeners
        submitButton.addActionListener(e -> submitReview());
        addPhotoButton.addActionListener(e -> addPhoto());
    }

    private void loadReviewData() {
        if (review != null) {
            titleField.setText(review.getTitle());
            contentArea.setText(review.getContent());
            visibilityComboBox.setSelectedItem(review.getVisibility() == 0 ? "전체 공개" : "비공개");
        }
    }

    private void deleteReview() {
        if (review != null) {
            int result = JOptionPane.showConfirmDialog(this, "정말로 이 후기를 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                reviewDAO.deleteReview(review.getReviewId());
                reviewPhotoDAO.deletePhotosByReviewId(review.getReviewId());
                if (onDispose != null) onDispose.run();
                dispose();
            }
        }
    }
    private boolean isImageFile(File f) {
    String name = f.getName().toLowerCase();
    return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
           name.endsWith(".png") || name.endsWith(".gif")  ||
           name.endsWith(".bmp") || name.endsWith(".webp"); // webp는 JVM마다 미지원일 수 있음
}

private JComponent createThumbTile(File file) {
    int W = 160, H = 120; // 썸네일 크기
    ImageIcon icon = null;

    try {
        // 선명한 축소를 위해 ImageIO 사용 (webp는 미지원일 수 있음)
        BufferedImage src = ImageIO.read(file);
        if (src != null) {
            double r = Math.min((double) W / src.getWidth(), (double) H / src.getHeight());
            int w = Math.max(1, (int)Math.round(src.getWidth() * r));
            int h = Math.max(1, (int)Math.round(src.getHeight() * r));
            Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled);
        }
    } catch (Exception ignore) {}

    if (icon == null) {
        // ImageIO가 못 읽는 포맷(webp 등)은 Toolkit 경로 로딩으로 폴백
        icon = new ImageIcon(file.getAbsolutePath());
        Image scaled = icon.getImage().getScaledInstance(W, H, Image.SCALE_SMOOTH);
        icon = new ImageIcon(scaled);
    }

    JLabel img = new JLabel(icon);
    img.setHorizontalAlignment(SwingConstants.CENTER);

    JLabel name = new JLabel(file.getName());
    name.setHorizontalAlignment(SwingConstants.CENTER);
    name.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

    JPanel tile = new JPanel(new BorderLayout(4, 4));
    tile.setBackground(Color.WHITE);
    tile.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
    tile.add(img, BorderLayout.CENTER);
    tile.add(name, BorderLayout.SOUTH);
    return tile;
}

    private void addPhoto() {
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(true);
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileFilter(new FileNameExtensionFilter(
            "이미지 파일 (jpg, jpeg, png, gif, bmp, webp)",
            "jpg","jpeg","png","gif","bmp","webp"));

    int result = chooser.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) return;

    File[] files = chooser.getSelectedFiles().length > 0
            ? chooser.getSelectedFiles()
            : new File[]{ chooser.getSelectedFile() };

    for (File file : files) {
        if (!isImageFile(file)) continue; // 확장자 체크
        selectedPhotos.add(file);
        photoPanel.add(createThumbTile(file)); // ← 썸네일 붙이기
    }
    photoPanel.revalidate();
    photoPanel.repaint();
}

}
