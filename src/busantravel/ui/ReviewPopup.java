package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import busantravel.dao.ReviewDAO;
import busantravel.dao.ReviewPhotoDAO;
import busantravel.model.Review;
import busantravel.model.ReviewPhoto;
import busantravel.model.TravelPlan;
import java.util.UUID;

public class ReviewPopup extends JDialog {

    private JTextField titleField;
    private JTextArea contentArea;
    private JComboBox<String> visibilityComboBox;
    private JButton submitButton;
    private JButton cancelButton;
    private JButton addPhotoButton;
    private JPanel photoPanel;
    private List<File> selectedPhotos = new ArrayList<>();

    private ReviewDAO reviewDAO;
    private ReviewPhotoDAO reviewPhotoDAO;
    private Review review; // for editing

    public ReviewPopup(JFrame parent) {
        super(parent, "후기 작성", true);
        this.reviewDAO = new ReviewDAO();
        this.reviewPhotoDAO = new ReviewPhotoDAO();
        initializeUI();
    }

    public ReviewPopup(JFrame parent, Review review) {
        super(parent, "후기 수정", true);
        this.review = review;
        this.reviewDAO = new ReviewDAO();
        this.reviewPhotoDAO = new ReviewPhotoDAO();
        initializeUI();
        loadReviewData();
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
        cancelButton = new JButton("뒤로가기");
        
        // Style buttons
        Font buttonFont = new Font("맑은 고딕", Font.BOLD, 14);
        submitButton.setFont(buttonFont);
        submitButton.setBackground(ColorConstants.ACCENT);
        submitButton.setForeground(Color.WHITE);
        cancelButton.setFont(buttonFont);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

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
        cancelButton.addActionListener(e -> dispose());
        addPhotoButton.addActionListener(e -> addPhoto());
    }

    private void loadReviewData() {
        if (review != null) {
            titleField.setText(review.getTitle());
            contentArea.setText(review.getContent());
            visibilityComboBox.setSelectedItem(review.getVisibility() == 0 ? "전체 공개" : "비공개");
        }
    }

    private void submitReview() {
        if (!busantravel.auth.MemberSession.isLoggedIn()) {
            javax.swing.JOptionPane.showMessageDialog(this, "로그인이 필요합니다.", "알림", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String title = titleField.getText();
        String content = contentArea.getText();
        int visibility = "전체 공개".equals(visibilityComboBox.getSelectedItem()) ? 0 : 1;

        if (review == null) { // New review
            Review newReview = new Review();
            String reviewId = UUID.randomUUID().toString().substring(0, 20);
            newReview.setReviewId(reviewId);
            newReview.setMemberId(busantravel.auth.MemberSession.getId());
            newReview.setTitle(title);
            newReview.setContent(content);
            newReview.setVisibility(visibility);
            reviewDAO.addReview(newReview);
        } else { // Update review
            review.setTitle(title);
            review.setContent(content);
            review.setVisibility(visibility);
            reviewDAO.updateReview(review);
            // TODO: Handle photo updates
        }

        // Save photos
        if (!selectedPhotos.isEmpty()) {
            // This is a simplified version. In a real app, you'd get the new/updated review's ID.
            // For new reviews, you might need to get the last inserted ID.
            // For now, we'll assume we have the review ID.
            String reviewId = (review != null) ? review.getReviewId() : ""; // Placeholder for new review ID
            if (!reviewId.isEmpty()) {
                 for (int i = 0; i < selectedPhotos.size(); i++) {
                    File photoFile = selectedPhotos.get(i);
                    // In a real app, you would copy the file to a designated storage folder
                    // and save the path. Here, we'll just use the file's absolute path.
                    String photoPath = photoFile.getAbsolutePath();
                    
                    ReviewPhoto reviewPhoto = new ReviewPhoto();
                    // reviewPhoto.setPhotoId(...); // Should be generated
                    reviewPhoto.setReviewId(reviewId);
                    reviewPhoto.setPhotoPath(photoPath);
                    reviewPhoto.setOrder(i);
                    
                    reviewPhotoDAO.addReviewPhoto(reviewPhoto);
                }
            }
        }
        dispose();
    }

    private void addPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                selectedPhotos.add(file);
                // Display thumbnail
                // For simplicity, just showing the name for now
                photoPanel.add(new JLabel(file.getName()));
                photoPanel.revalidate();
                photoPanel.repaint();
            }
        }
    }
}
