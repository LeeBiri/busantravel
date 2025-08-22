package busantravel.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class WrapLayout extends FlowLayout {
    public WrapLayout() {
        super();
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int maxWidth = target.getParent().getWidth();
            if (maxWidth == 0) maxWidth = Integer.MAX_VALUE;

            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (getHgap() * 2);
            int maxWidthAvailable = maxWidth - horizontalInsetsAndGap;

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    if (rowWidth + d.width > maxWidthAvailable) {
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + getVgap();
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    rowWidth += d.width + getHgap();
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;
            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + getVgap() * 2;

            return dim;
        }
    }
}
