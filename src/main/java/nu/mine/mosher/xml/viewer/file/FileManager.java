package nu.mine.mosher.xml.viewer.file;


import nu.mine.mosher.xml.viewer.gui.FrameManager;
import nu.mine.mosher.xml.viewer.model.DomTreeModel;
import org.slf4j.*;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.Objects;

import static nu.mine.mosher.xml.viewer.gui.XmlViewerGui.ACCEL;


public class FileManager {
    public FileManager(final DomTreeModel model, final FrameManager framer) {
        this.model = model;
        this.framer = framer;
    }

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



    private class FileOpenTask extends SwingWorker<Void, Void> {
        private final URL opened;
        private JDialog dialog;
        private Document document;

        public FileOpenTask(final URL o) {
            this.opened = o;
        }

        @Override
        protected Void doInBackground() {
            try {
                try {
                    this.document = FileUtil.asDom(opened, true);
                } catch (final Exception e) {
                    LOG.warn("XML validation failed for {}", opened, e);
                }
                this.document = FileUtil.asDom(opened, false);
            } catch (final Throwable e) {
                LOG.error("Error parsing XML file", e);
                FileManager.this.framer.showMessage(e.toString());
            }
            return null;
        }

        @Override
        protected void done() {
            if (Objects.nonNull(this.document)) {
                FileManager.this.model.open(this.document);
            }

            super.done();
            this.dialog.setVisible(false);
            this.dialog.dispose();
        }

        public void setCanceller(final JDialog dialog) {
            this.dialog = dialog;
        }
    }



    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final FrameManager framer;
    private final DomTreeModel model;

    private JMenuItem itemFileClose;
    private JMenuItem itemFileOpen;



    private void fileOpen() {
        try {
            final File opened = this.framer.getFileToOpen();
            final FileOpenTask task = new FileOpenTask(opened.toURI().toURL());
            canceller(task, this.framer.frame());
            task.execute();
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

    private void canceller(final FileOpenTask task, final JFrame frame) {
        final JDialog dialog = new JDialog(frame, "Wait");
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final JLabel label = new JLabel("Please wait...");
        label.setHorizontalAlignment(JLabel.CENTER);

        final JButton closeButton = new JButton("Cancel");
        closeButton.addActionListener(e -> {
            task.cancel(true);
        });
        final JPanel closePanel = new JPanel();
        closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
        closePanel.add(Box.createHorizontalGlue());
        closePanel.add(closeButton);
        closePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));

        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(label, BorderLayout.CENTER);
        contentPane.add(closePanel, BorderLayout.PAGE_END);
        contentPane.setOpaque(true);
        dialog.setContentPane(contentPane);

        dialog.setSize(new Dimension(300, 150));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        task.setCanceller(dialog);
    }
}
