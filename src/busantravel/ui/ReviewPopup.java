package busantravel.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import busantravel.dao.PlaceDAO;
import busantravel.dao.ReviewDAO;
import busantravel.dao.ReviewPhotoDAO;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.Review;
import busantravel.model.ScheduledPlace;
import busantravel.model.Tourist;
import busantravel.util.FontManager;

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
    private List<ScheduledPlace> scheduledPlaces;
    private PlaceDAO placeDAO;
    private String planId;

    public ReviewPopup(JFrame parent, Review review, Runnable onDispose) {
        this(parent, review, onDispose, null, null);
    }

    public ReviewPopup(JFrame parent, Review review, Runnable onDispose, List<ScheduledPlace> scheduledPlaces, String planId) {
        super(parent, review == null ? "후기 작성" : "후기 수정", true);
        this.review = review;
        this.onDispose = onDispose;
        this.scheduledPlaces = scheduledPlaces;
        this.planId = planId;
        this.reviewDAO = new ReviewDAO();
        this.reviewPhotoDAO = new ReviewPhotoDAO();
        this.placeDAO = new PlaceDAO();
        initializeUI();
        if (review != null) {
            loadReviewData();
        }
    }

    private void initializeUI() {
        setSize(850, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel reviewFormPanel = createReviewFormPanel();

        if (scheduledPlaces != null && !scheduledPlaces.isEmpty()) {
            JComponent sidebar = createSidebar();
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, reviewFormPanel, sidebar);
            splitPane.setDividerLocation(550);
            splitPane.setResizeWeight(0.7);
            add(splitPane, BorderLayout.CENTER);
        } else {
            add(reviewFormPanel, BorderLayout.CENTER);
        }
    }

    private JPanel createReviewFormPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("제목");
        titleLabel.setFont(FontManager.getJalnanFont(18f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField = new JTextField();
        titleField.setFont(FontManager.getJalnanGothicFont(14f));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel contentLabel = new JLabel("내용");
        contentLabel.setFont(FontManager.getJalnanFont(18f));
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea = new JTextArea();
        contentArea.setFont(FontManager.getJalnanGothicFont(14f));
        contentArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photoLabel = new JLabel("사진");
        photoLabel.setFont(FontManager.getJalnanFont(18f));
        photoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPhotoButton = new JButton("사진 추가");
        addPhotoButton.setFont(FontManager.getJalnanFont(14f));
        addPhotoButton.setBackground(ColorConstants.ACCENT);
        addPhotoButton.setForeground(Color.WHITE);
        addPhotoButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        photoPanel.setBackground(Color.WHITE);
        JScrollPane photoScrollPane = new JScrollPane(photoPanel);
        photoScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel visibilityLabel = new JLabel("공개 설정");
        visibilityLabel.setFont(FontManager.getJalnanFont(18f));
        String[] visibilities = {"전체 공개", "비공개"};
        visibilityComboBox = new JComboBox<>(visibilities);
        visibilityComboBox.setFont(FontManager.getJalnanGothicFont(14f));
        JPanel visibilityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        visibilityPanel.setBackground(Color.WHITE);
        visibilityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        visibilityPanel.add(visibilityLabel);
        visibilityPanel.add(visibilityComboBox);

        formPanel.add(titleLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(titleField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(contentLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(scrollPane);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(photoLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(addPhotoButton);
        formPanel.add(photoScrollPane);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(visibilityPanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        submitButton = new JButton(review == null ? "후기 등록하기" : "수정하기");
        submitButton.setFont(FontManager.getJalnanFont(16f));
        submitButton.setBackground(ColorConstants.ACCENT);
        submitButton.setForeground(Color.WHITE);
        buttonPanel.add(submitButton);

        if (review != null) {
            deleteButton = new JButton("삭제하기");
            deleteButton.setFont(FontManager.getJalnanFont(16f));
            deleteButton.setBackground(Color.RED);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.addActionListener(e -> deleteReview());
            buttonPanel.add(deleteButton);
        }
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submitReview());
        addPhotoButton.addActionListener(e -> addPhoto());

        return mainPanel;
    }

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
            newReview.setPlanId(planId);
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

    private JComponent createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        sidebar.setMinimumSize(new Dimension(250, 0));

        JLabel titleLabel = new JLabel("방문한 장소");
        titleLabel.setFont(FontManager.getJalnanFont(18f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        sidebar.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(245, 245, 245));

        for (ScheduledPlace scheduledPlace : scheduledPlaces) {
            Object place = placeDAO.getPlaceByTypeAndId(scheduledPlace.getP_type(), scheduledPlace.getP_id());
            String placeName = "";
            String thumbUrl = "";

            if (place instanceof Tourist) {
                Tourist t = (Tourist) place;
                placeName = t.getT_place();
                thumbUrl = t.getT_thum();
            } else if (place instanceof Restaurant) {
                Restaurant r = (Restaurant) place;
                placeName = r.getR_place();
                thumbUrl = r.getR_thum();
            } else if (place instanceof Lodging) {
                Lodging l = (Lodging) place;
                placeName = l.getL_place();
                thumbUrl = l.getL_thum();
            } else if (place instanceof Festival) {
                Festival f = (Festival) place;
                placeName = f.getF_name();
                thumbUrl = f.getF_thum();
            }
            listPanel.add(createPlaceEntry(placeName, thumbUrl));
            listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(245, 245, 245));
        sidebar.add(scrollPane, BorderLayout.CENTER);

        return sidebar;
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
               name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private JComponent createThumbTile(File file) {
        int W = 160, H = 120;
        ImageIcon icon = null;
        try {
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
            if (!isImageFile(file)) continue;
            selectedPhotos.add(file);
            photoPanel.add(createThumbTile(file));
        }
        photoPanel.revalidate();
        photoPanel.repaint();
    }

    private JPanel createPlaceEntry(String name, String thumbUrl) {
        JPanel entryPanel = new JPanel(new BorderLayout(10, 0));
        entryPanel.setBackground(Color.WHITE);
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(60, 60));
        try {
            if (thumbUrl != null && !thumbUrl.isEmpty()) {
                URL imageUrl = new URL(thumbUrl);
                ImageIcon icon = new ImageIcon(imageUrl);
                Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                imageLabel.setText("No Img");
            }
        } catch (Exception e) {
            imageLabel.setText("Error");
        }
        entryPanel.add(imageLabel, BorderLayout.WEST);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(FontManager.getJalnanGothicFont(14f));
        entryPanel.add(nameLabel, BorderLayout.CENTER);

        return entryPanel;
    }
}
