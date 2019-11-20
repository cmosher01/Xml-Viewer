package nu.mine.mosher.xml.viewer.gui;

import javax.swing.Icon;
import java.awt.*;

public class TreeExpanderIcon implements Icon {
    private final float siz;
    private final int sign;

    public TreeExpanderIcon(final float siz, int sign) {
        this.siz = siz;
        this.sign = sign;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int tx, int ty) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(c.getForeground());
        g2d.translate(tx, ty);

        final float pc = this.siz/4.0f;
        final int p1 = toint(1.0f*pc);
        final int p2 = toint(2.0f*pc);
        final int p3 = toint(3.0f*pc);

        g2d.setColor(Color.MAGENTA);

        g2d.drawLine(p1, p2, p3, p2);
        if (0 < this.sign) {
            g2d.drawLine(p2, p1, p2, p3);
        }
        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return toint(this.siz*2.0f);
    }

    @Override
    public int getIconHeight() {
        return toint(this.siz);
    }

    private static int toint(float v) {
        return Math.round(v);
    }
}
