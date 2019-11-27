/*
 * Created on Nov 22, 2005
 */
package nu.mine.mosher.xml.viewer.file;


import nu.mine.mosher.xml.viewer.gui.FrameManager;
import nu.mine.mosher.xml.viewer.model.DomTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
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
            final File opened = this.framer.getFileToOpen(this.file);
            final FileOpenTask task = new FileOpenTask(opened.toURI().toURL());
            create(task, this.framer.frame());
            task.execute();
            this.file = Optional.of(opened);
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

        public void create(final FileOpenTask task, JFrame frame) {
            final JDialog dialog = new JDialog(frame, "Wait");
            JLabel label = new JLabel("This could take awhile. Please wait...");
            label.setHorizontalAlignment(JLabel.CENTER);

            JButton closeButton = new JButton("Cancel");
            closeButton.addActionListener(e -> {
                System.err.println("clicked cancel");
                System.err.flush();

                task.cancel(true);
            });
            JPanel closePanel = new JPanel();
            closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
            closePanel.add(Box.createHorizontalGlue());
            closePanel.add(closeButton);
            closePanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));

            JPanel contentPane = new JPanel(new BorderLayout());
            contentPane.add(label, BorderLayout.CENTER);
            contentPane.add(closePanel, BorderLayout.PAGE_END);
            contentPane.setOpaque(true);
            dialog.setContentPane(contentPane);

            dialog.setSize(new Dimension(300, 150));
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);

            task.setCanceller(dialog);
    }


    private class FileOpenTask extends SwingWorker<Void, Void> {
        private final URL opened;
        private JDialog dialog;

        public FileOpenTask(URL opened) {
            this.opened = opened;
        }

        @Override
        protected Void doInBackground() {
            try {
                tryRun();
            } catch (final Throwable e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            return null;
        }

        private void tryRun() {
            try {
                FileManager.this.model.open(opened);
            } catch (final Throwable e) {
                e.printStackTrace();
                LOG.error("Error parsing XML file", e);
                framer.showMessage(e.toString());
            }
        }

        @Override
        protected void done() {
            super.done();
            System.err.println("done");
            System.err.flush();
            dialog.setVisible(false);
            this.dialog.dispose();
            // if success:
//            FileManager.this.file = Optional.of(original file);
        }

        public void setCanceller(JDialog dialog) {
            this.dialog = dialog;
        }
    }
}
