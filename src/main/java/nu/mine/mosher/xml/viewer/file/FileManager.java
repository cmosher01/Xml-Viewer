/*
 * Created on Nov 22, 2005
 */
package nu.mine.mosher.xml.viewer.file;


import nu.mine.mosher.xml.viewer.gui.FrameManager;
import nu.mine.mosher.xml.viewer.model.DomTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;


public class FileManager {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private JMenuItem itemFileOpen;
    private JMenuItem itemFileClose;

    private final DomTreeModel model;
    private final FrameManager framer;

    private Optional<File> file = Optional.empty();

    public FileManager(final DomTreeModel model, final FrameManager framer) {
        this.model = model;
        this.framer = framer;
    }

    private static final int ACCEL = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public void appendMenuItems(final JMenu appendTo) {
        this.itemFileOpen = new JMenuItem("Open\u2026");
        this.itemFileOpen.setMnemonic(KeyEvent.VK_O);
        this.itemFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ACCEL));
        this.itemFileOpen.addActionListener(e -> fileOpen());
        appendTo.add(this.itemFileOpen);

        this.itemFileClose = new JMenuItem("Close");
        this.itemFileClose.setMnemonic(KeyEvent.VK_C);
        this.itemFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ACCEL));
        this.itemFileClose.addActionListener(e -> fileClose());
        appendTo.add(this.itemFileClose);
    }

    public void updateMenu() {
        this.itemFileOpen.setEnabled(true);
        this.itemFileClose.setEnabled(Objects.nonNull(this.model.getRoot()));
    }


    private void fileOpen() {
        try {
            this.file = Optional.of(this.framer.getFileToOpen(this.file));
            this.model.open(this.file.get().toURI().toURL());
        } catch (final FrameManager.UserCancelled cancelled) {
            // user pressed the cancel button, so just return
        } catch (final Throwable e) {
            LOG.error("Error parsing XML file", e);
            this.framer.showMessage(e.toString());
        }
    }

    private void fileClose() {
        this.model.close();
    }
}
