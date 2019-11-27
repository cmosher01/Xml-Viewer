package nu.mine.mosher.xml.viewer.gui;



import nu.mine.mosher.xml.viewer.file.FileManager;
import nu.mine.mosher.xml.viewer.model.DomTreeModel;

import javax.swing.*;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.util.Observable;
import java.util.Observer;

import static java.awt.Font.DIALOG;
import static java.awt.Font.PLAIN;


public class XmlViewerGui implements Closeable, Observer {
    private static XmlViewerGui INSTANCE;

    public static void create() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            try {
                INSTANCE = new XmlViewerGui();
                INSTANCE.run();
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public static void showMessage(final String message) {
        INSTANCE.framer.showMessage(message);
    }

    private final DomTreeModel model = new DomTreeModel();
    private final FrameManager framer = new FrameManager(this.model);
    private final FileManager filer = new FileManager(this.model, this.framer);

    private XmlViewerGui() {
    }


    public void run() {
        this.model.addObserver(this);

        setLookAndFeel();

        // Use look and feel's (not OS's) decorations.
        // Must be done before creating any JFrame or JDialog
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        final Font font = new Font(DIALOG, PLAIN, 10);
        UIManager.put("MenuBar.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);

        final JMenuBar menubar = new JMenuBar();
        this.framer.init(menubar, this::close);
        appendMenus(menubar);

        update(this.model, null);
    }


    public void update(final Observable observable, final Object unused) {
        this.filer.updateMenu();
    }

    public void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (final Throwable e) {
            // probably won't fail; but if it does, just continue with whatever the default UI is
        }
    }

    private void appendMenus(final JMenuBar bar) {
        final JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        this.filer.appendMenuItems(menuFile);
        menuFile.addSeparator();
        addQuitTo(menuFile);
        bar.add(menuFile);

        final JMenu menuView = new JMenu("View");
        menuView.setMnemonic(KeyEvent.VK_V);
        this.framer.appendViewMenuItems(menuView);
        bar.add(menuView);

        final JMenu menuHelp = new JMenu("Help");
        menuHelp.setMnemonic(KeyEvent.VK_H);
        addAboutTo(menuHelp);
        bar.add(menuHelp);
    }

    public static final int ACCEL = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private void addQuitTo(final JMenu menu) {
        final JMenuItem itemFileExit = new JMenuItem("Quit");
        itemFileExit.setMnemonic(KeyEvent.VK_Q);
        itemFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ACCEL));
        itemFileExit.addActionListener(e -> close());
        menu.add(itemFileExit);
    }

    public void close() {
        // exit the application
        this.framer.close();
    }

    private void addAboutTo(final JMenu menu) {
        final JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.setMnemonic(KeyEvent.VK_A);
        itemAbout.addActionListener(e -> about());
        menu.add(itemAbout);
    }

    private void about() {
        showMessage(
            "<html>" +
            "<p style='font-size:22'>XML Viewer</p><br>" +
            "Copyright © 2019, Christopher Alan Mosher, Shelton, Connecticut, USA<br>" +
            "https://github.com/cmosher01" +
            "</html>");
    }
}
