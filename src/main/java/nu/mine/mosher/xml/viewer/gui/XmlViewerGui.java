package nu.mine.mosher.xml.viewer.gui;

import nu.mine.mosher.xml.viewer.file.FileManager;
import nu.mine.mosher.xml.viewer.model.DomTreeModel;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

import static java.awt.Font.*;


public class XmlViewerGui implements Closeable, Observer {
    public static void create(final Optional<URL> xml, final Set<URL> schemata) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            try {
                new XmlViewerGui().run(Objects.requireNonNull(xml), Objects.requireNonNull(schemata));
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private final DomTreeModel model = new DomTreeModel();
    private final FrameManager framer = new FrameManager(this.model);
    private final FileManager filer = new FileManager(this.model, this.framer);

    private XmlViewerGui() {
    }


    public void run(final Optional<URL> xml, final Set<URL> schemata) throws ParserConfigurationException, SAXException, IOException, FontFormatException {
        if (xml.isPresent()) {
            this.model.open(xml.get(), schemata);
        }
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
//        this.framer.repaint();
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
        this.framer.showMessage(
            "<html>" +
                "<p style='font-size:22'>XML Viewer</p><br>" +
                "Copyright © 2019, Christopher Alan Mosher, Shelton, Connecticut, USA<br>" +
                "https://github.com/cmosher01" +
                "</html>");
    }
}
