package busantravel.ui;

import busantravel.model.Checklist;
import busantravel.model.TravelPlan;
import busantravel.service.ChecklistService;
import java.awt.*;
import java.io.InputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChecklistPanel extends JPanel {

    private final TravelPlannerApp mainApp;
    private final TravelPlan plan;
    private final String memberId;              // “내 체크리스트”용
    private final Runnable onBack;
    private final DefaultListModel<Item> publicModel = new DefaultListModel<>();
    private final DefaultListModel<Item> myModel = new DefaultListModel<>();
    private Font jalnanFont;

    public ChecklistPanel(TravelPlannerApp mainApp, TravelPlan plan, String memberId, Runnable onBack) {
        this.mainApp = mainApp;
        this.plan = plan;
        this.memberId = memberId;
        this.onBack = onBack;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadInitialData(); // 최초 로드
    }

    // 상단: 제목 + (선택) 뒤로가기
    private JComponent buildTop() {
        loadFont();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(16, 24, 8, 24));
        p.setOpaque(false);

        JButton back = new JButton("뒤로");
        back.setFocusPainted(false);

        back.setBackground(ColorConstants.ACCENT);
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        
        back.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        back.addActionListener(e -> { if (onBack != null) onBack.run(); });
        p.add(back, BorderLayout.EAST);
        return p;
    }

    // 본문: 좌(추가버튼) / 중(전체) / 우(내 것)
    private JComponent buildCenter() {
        loadFont();
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; c.insets = new Insets(0, 24, 0, 24); c.fill = GridBagConstraints.BOTH; c.weighty = 1;

        c.gridy = 1;

        // 좌: 추가 버튼 스택
        c.gridx = 0; c.weightx = 0.25;
        root.add(buildAddColumn(), c);

        // 중: 전체 체크리스트
        c.gridx = 1; c.weightx = 0.4;
        root.add(buildListColumn("전체 체크리스트", publicModel, true), c);

        // 우: 내 체크리스트
        c.gridx = 2; c.weightx = 0.35;
        root.add(buildListColumn("내 체크리스트", myModel, false), c);

        return root;
    }

    private JComponent buildAddColumn() {
        Box box = Box.createVerticalBox();
        box.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("여행 준비 체크리스트");
        title.setFont(jalnanFont.deriveFont(Font.PLAIN, 35f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton publicAddBtn = makeAddButton("전체 체크리스트 추가", true);
        JButton privateAddBtn = makeAddButton("나의 체크리스트 추가", false);
        publicAddBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        privateAddBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(Box.createVerticalGlue());  
        box.add(title);
        box.add(Box.createVerticalStrut(10));
        box.add(publicAddBtn);
        box.add(Box.createVerticalStrut(16));
        box.add(privateAddBtn);
        box.add(Box.createVerticalGlue());   
        return wrapCard(box);
    }

    private JButton makeAddButton(String text, boolean isPublic) {
        loadFont();
        JButton b = new JButton("[+]  " + text);
        b.setPreferredSize(new Dimension(260, 64));
        b.setFocusPainted(false);
        b.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        b.setBackground(ColorConstants.ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.addActionListener(e -> {
            String content = JOptionPane.showInputDialog(this, "체크리스트 내용을 입력하세요");
            if (content == null || content.isBlank()) return;

                if (isPublic) {
                    ChecklistService.insertPublicChecklist(plan.getPlan_id(), content);
                    publicModel.addElement(new Item(0, content, false)); 
                } else {
                    ChecklistService.insertChecklist(plan.getPlan_id(), memberId, content);
                    myModel.addElement(new Item(0, content, false));
                }

        });
        return b;
    }

    private JComponent buildListColumn(String title, DefaultListModel<Item> model, boolean isPublic) {
        loadFont();
        JPanel column = new JPanel(new BorderLayout());
        column.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setBorder(new EmptyBorder(8, 8, 8, 8));
        t.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        column.add(t, BorderLayout.NORTH);

        JList<Item> list = new JList<>(model);
        list.setCellRenderer(new ChecklistRenderer(jalnanFont));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 클릭 시 완료 토글
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int idx = list.locationToIndex(e.getPoint());
                if (idx < 0) return;
                Item it = model.get(idx);
                Item updated = new Item(it.id, it.text, !it.done);
                model.set(idx, updated);
                // DB 완료 상태 반영
                ChecklistService.completeChecklist(it.id, updated.done);
            }
        });

        column.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton edit = new JButton("수정");
        edit.setFocusPainted(false);
        edit.setBackground(ColorConstants.ACCENT);
        edit.setForeground(Color.WHITE);
        edit.setFocusPainted(false);
        edit.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        JButton del  = new JButton("삭제");
        del.setFocusPainted(false);
        del.setBackground(ColorConstants.ACCENT);
        del.setForeground(Color.WHITE);
        del.setFocusPainted(false);
        del.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        actions.add(edit); actions.add(del);
        column.add(actions, BorderLayout.SOUTH);

        edit.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) return;
            Item it = model.get(idx);
            String next = JOptionPane.showInputDialog(this, "내용 수정", it.text);
            if (next == null || next.isBlank()) return;
            model.set(idx, new Item(it.id, next, it.done));
            ChecklistService.updateChecklist(it.id, next);
        });

        del.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) return;
            Item it = model.get(idx);
            int ok = JOptionPane.showConfirmDialog(this, "삭제할까요?", "확인", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                ChecklistService.deleteChecklist(it.id);
                model.remove(idx);
            }
        });

        return wrapCard(column);
    }

    private void loadInitialData() {
        // 전체 체크리스트
        for (Checklist c : ChecklistService.getPublicChecklist(plan.getPlan_id())) {
            publicModel.addElement(new Item(
                c.getChecklist_id(), 
                c.getChecklist_content(),
                c.isIs_completed()));
        }

        // 내 체크리스트
        for (Checklist c : ChecklistService.getChecklistByid(plan.getPlan_id(), memberId)) {
            myModel.addElement(new Item(
                c.getChecklist_id(),
                c.getChecklist_content(),
                c.isIs_completed()));
        }
    }

    // 카드 스타일 감싸기
    private JComponent wrapCard(JComponent inner) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0xFFF5F5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E5E5)),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // --------- 유틸/어댑터 ---------
    private static class Item {
        final int id;
        final String text;
        final boolean done;
        Item(int id, String text, boolean done) { this.id = id; this.text = text; this.done = done; }
        @Override public String toString() { return text; }
    }

    private static class ChecklistRenderer extends JPanel implements ListCellRenderer<Item> {
        private final JCheckBox check = new JCheckBox();
        private final JLabel label = new JLabel();

        ChecklistRenderer(Font customFont) {
            setLayout(new BorderLayout(12, 0));
            setOpaque(true);
            check.setFocusable(false);
            check.setOpaque(false);
            check.setFont(customFont.deriveFont(Font.PLAIN, 18f));
            label.setFont(customFont.deriveFont(Font.PLAIN, 18f));
            label.setOpaque(false);
            setBackground(Color.WHITE);

            // check.setIcon(...); 체크 아이콘 변경

            setPreferredSize(new Dimension(280, 64));
            setBorder(new EmptyBorder(16, 16, 16, 16));

            add(check, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list, Item value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            check.setSelected(value.done);
            label.setText(value.text);

            setBackground(isSelected ? new Color(0xFFE8E8) : Color.WHITE);
            return this;
        }
    }

    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f); // 기본 크기 14로 설정
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
