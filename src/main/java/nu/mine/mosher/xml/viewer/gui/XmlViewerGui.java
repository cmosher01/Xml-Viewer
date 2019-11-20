package nu.mine.mosher.xml.viewer.gui;

import nu.mine.mosher.xml.viewer.file.FileManager;
import nu.mine.mosher.xml.viewer.model.DomTreeModel;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;



public class XmlViewerGui implements Runnable, Closeable, Observer {
    public static void gui(final Optional<URL> xml, final Set<URL> schemata) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> new XmlViewerGui(xml, schemata).run());
    }

    private XmlViewerGui(Optional<URL> xml, Set<URL> schemata) {
        this.xml = xml;
        this.schemata = new HashSet<>(schemata);
    }

    private Optional<URL> xml;
    private final Set<URL> schemata;
    // private Optional<Node> currentNode;
    private final DomTreeModel model = new DomTreeModel();
    private final FrameManager framer = new FrameManager(this.model);
    private final FileManager filer = new FileManager(this.model, this.framer);


    public void run() {
        this.model.addObserver(this);

        setUpUi();

        // Use look and feel's (not OS's) decorations.
        // Must be done before creating any JFrame or JDialog
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // create the application's menu bar
        final JMenuBar menubar = new JMenuBar();
        appendMenus(menubar);

        this.framer.init(menubar, this::close);

        update(this.model, null);
    }


    public void close() {
        // exit the application
        this.framer.close();
    }

    public void update(final Observable observable, final Object unused) {
        this.filer.updateMenu();
        this.framer.repaint();
    }

    private void setUpUi() {
        useUiNamed("nimbus", this::setUiDefaults);
    }

    private void setUiDefaults(final UIDefaults defs) {
        defs.put("Tree.drawHorizontalLines", true);
        defs.put("Tree.drawVerticalLines", true);
        defs.put("Tree.linesStyle", "dashed");
        defs.put("Tree.closedIcon", null);
        defs.put("Tree.openIcon", null);
        defs.put("Tree.leafIcon", null);

        defs.put("Tree.rowHeight", visualSize *1.2);
        defs.put("Tree.font", new FontUIResource(new Font(Font.SANS_SERIF, Font.PLAIN, visualSize)));
        defs.put("Tree.expandedIcon", new Ec(visualSize, -1));
        defs.put("Tree.collapsedIcon", new Ec(visualSize, +1));
        defs.put("Tree.leftChildIndent", visualSize);
        defs.put("Tree.rightChildIndent", visualSize /2);
    }

    private static void useUiNamed(final String name, final Consumer<UIDefaults> with) {
        try {
            final String className = getClassForUi(name);
            final LookAndFeel ui = (LookAndFeel)Class.forName(className).newInstance();
            with.accept(ui.getDefaults());
            UIManager.setLookAndFeel(ui);
        } catch (final Throwable e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (final Throwable e2) {
                throw new IllegalStateException(e2);
            }
        }
    }

    private static String getClassForUi(final String name) {
        for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (name.equalsIgnoreCase(info.getName())) {
                return info.getClassName();
            }
        }
        throw new IllegalStateException();
    }

    private void appendMenus(final JMenuBar bar) {
        final JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        this.filer.appendMenuItems(menuFile);
        menuFile.addSeparator();
        appendFileMenuItems(menuFile);
        bar.add(menuFile);

        final JMenu menuView = new JMenu("View");
        menuView.setMnemonic(KeyEvent.VK_V);
        appendViewMenuItems(menuView);
        bar.add(menuView);

        final JMenu menuHelp = new JMenu("Help");
        menuHelp.setMnemonic(KeyEvent.VK_H);
        appendHelpMenuItems(menuHelp);
        bar.add(menuHelp);
    }

    private static final int ACCEL = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private void appendFileMenuItems(final JMenu menu) {
        final JMenuItem itemFileExit = new JMenuItem("Quit");
        itemFileExit.setMnemonic(KeyEvent.VK_Q);
        itemFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ACCEL));
        itemFileExit.addActionListener(e -> close());
        menu.add(itemFileExit);
    }
    private void appendViewMenuItems(final JMenu menu) {
        final JMenuItem itemZoomIn = new JMenuItem("Zoom in");
        itemZoomIn.setMnemonic(KeyEvent.VK_I);
        itemZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ACCEL));
        itemZoomIn.addActionListener(e -> zoom(+1));
        menu.add(itemZoomIn);
        final JMenuItem itemZoomOut = new JMenuItem("Zoom out");
        itemZoomOut.setMnemonic(KeyEvent.VK_O);
        itemZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ACCEL));
        itemZoomOut.addActionListener(e -> zoom(-1));
        menu.add(itemZoomOut);
    }

    private void appendHelpMenuItems(final JMenu menu) {
        final JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.setMnemonic(KeyEvent.VK_A);
        itemAbout.addActionListener(e -> about());
        menu.add(itemAbout);
    }
}
