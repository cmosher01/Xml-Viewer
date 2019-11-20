/*
 * Created on Nov 22, 2005
 */
package nu.mine.mosher.xml.viewer.gui;



import nu.mine.mosher.xml.viewer.XmlViewer;
import nu.mine.mosher.xml.viewer.model.DomTreeModel;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.util.Optional;



public class FrameManager implements Closeable {
    private final DomTreeModel model;
    private JFrame frame;

    public FrameManager(final DomTreeModel model) {
        this.model = model;
    }

    public void init(final JMenuBar bar, final Runnable onClose) {
        // Create the window.
        this.frame = new JFrame();

        // If the user clicks the close box, we call "onClose" runnable
        // that's passed in by the caller (who is responsible for calling
        // our close method if he determines it is OK to terminate the app)
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                onClose.run();
            }
        });

        this.frame.setIconImage(getFrameIcon());

        this.frame.setTitle("GEDCOM Viewer");

        this.frame.setJMenuBar(bar);

        // Create and set up the content pane.
        this.frame.setContentPane(new MainPane(this.model));

        // Set the window's size and position.
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        //mFrame.setExtendedState(Frame.MAXIMIZED_BOTH);

        // Display the window.
        this.frame.setVisible(true);

    }

    public void repaint() {
        this.frame.repaint();
    }

    public void updateUi() {
        setUpUi();
        SwingUtilities.updateComponentTreeUI(this.frame);
    }

    private static File dir() {
        return new File(XmlViewer.prefs().get("dir", "./"));
    }

    private static void dir(final File dir) {
        XmlViewer.prefs().put("dir", dir.getAbsolutePath());
    }

    public File getFileToOpen(final Optional<File> initial) throws UserCancelled {
        final JFileChooser chooser = new JFileChooser(initial.orElse(dir()));
        final int actionType = chooser.showOpenDialog(this.frame);
        if (actionType != JFileChooser.APPROVE_OPTION) {
            throw new UserCancelled();
        }

        dir(chooser.getCurrentDirectory());

        return chooser.getSelectedFile();
    }

    public void showMessage(final String message) {
        JOptionPane.showMessageDialog(this.frame, message);
    }

    public void close() {
        this.frame.dispose();
    }


    private static Image getFrameIcon() {
        final BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        final String text = "G";
        final Rectangle rect = new Rectangle(200, 200);

        final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 128);

        final Graphics g = bufferedImage.getGraphics();
        g.setColor(Color.decode("0xfdf6e3"));
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        g.setColor(Color.decode("0xd33682"));
        g.setFont(font);

        final FontMetrics metrics = g.getFontMetrics(font);
        final int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        final int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g.drawString(text, x, y);
        return bufferedImage;
    }

    public static class UserCancelled extends Exception {
        private UserCancelled() {
        }
    }
}