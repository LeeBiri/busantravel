package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import busantravel.dao.ReviewPhotoDAO;
import busantravel.model.Review;
import busantravel.model.ReviewPhoto;

public class ReviewDetailPopup extends JDialog {

    private Review review;
    private ReviewPhotoDAO reviewPhotoDAO;

    public ReviewDetailPopup(JFrame parent, Review review) {
        super(parent, "리뷰 상세보기", true);
        this.review = review;
        this.reviewPhotoDAO = new ReviewPhotoDAO();
        initializeUI();
    }

    private void initializeUI() {
        setSize(600, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel(review.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Content
        JTextArea contentArea = new JTextArea(review.getContent());
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        
        // Photo Panel
        JPanel photoPanel = new JPanel();
        photoPanel.setLayout(new BoxLayout(photoPanel, BoxLayout.Y_AXIS));
        photoPanel.setBackground(Color.WHITE);
        
        List<ReviewPhoto> photos = reviewPhotoDAO.getPhotosByReviewId(review.getReviewId());
        for (ReviewPhoto photo : photos) {
            try {
                // In a real app, you'd handle image loading more robustly
                ImageIcon icon = new ImageIcon(photo.getPhotoPath());
                Image image = icon.getImage().getScaledInstance(500, -1, Image.SCALE_SMOOTH);
                photoPanel.add(new JLabel(new ImageIcon(image)));
            } catch (Exception e) {
                // Handle exception
            }
        }
        JScrollPane photoScrollPane = new JScrollPane(photoPanel);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.add(contentScrollPane);
        centerPanel.add(photoScrollPane);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton closeButton = new JButton("닫기");
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}
