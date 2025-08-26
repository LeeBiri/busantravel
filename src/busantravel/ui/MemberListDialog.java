package busantravel.ui;

import busantravel.model.Member;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class MemberListDialog extends JDialog {
    private Font jalnanFont;

    public MemberListDialog(JFrame parent, List<Member> members) {
        super(parent, "멤버 리스트", true);
        loadFont();

        setSize(460, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 제목
        JLabel titleLabel = new JLabel("참여 멤버 목록", SwingConstants.CENTER);
        titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 22f));
        titleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        titleLabel.setForeground(new Color(80, 40, 40));
        add(titleLabel, BorderLayout.NORTH);

        // 테이블
        String[] columnNames = {"이름", "이메일"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        for (Member m : members) {
            String name = (m.getM_name() != null) ? m.getM_name() : "탈퇴한 사용자";
            String email = (m.getM_email() != null) ? m.getM_email() : "-";
            model.addRow(new String[]{name, email});
        }

        JTable table = new JTable(model);
        table.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        table.setRowHeight(28);
        table.setEnabled(false);
        table.setGridColor(new Color(220, 220, 220));
        table.setBackground(Color.WHITE);

        // 중앙 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 헤더 꾸미기
        JTableHeader header = table.getTableHeader();
        header.setFont(jalnanFont.deriveFont(Font.BOLD, 15f));
        header.setBackground(new Color(250, 204, 204));
        header.setForeground(Color.DARK_GRAY);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // 닫기 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.setPreferredSize(new Dimension(100, 36));
        closeButton.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        closeButton.setBackground(ColorConstants.ACCENT);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(jalnanFont);
            } else {
                System.err.println("폰트 파일을 찾을 수 없습니다! 기본 폰트 사용.");
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            System.err.println("폰트 로드 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }
}
