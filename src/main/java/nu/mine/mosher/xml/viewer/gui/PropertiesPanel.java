package nu.mine.mosher.xml.viewer.gui;

import nu.mine.mosher.xml.viewer.file.DomUtil;
import nu.mine.mosher.xml.viewer.model.DomTreeNode;

import javax.swing.JTextArea;
import java.io.IOException;
import java.util.Optional;

import static nu.mine.mosher.xml.viewer.StringUnicodeEncoderDecoder.filter;

public class PropertiesPanel extends JTextArea {
    public PropertiesPanel() {
        setOpaque(false);
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public void display(final Optional<DomTreeNode> node) throws IOException {
        final StringBuilder sb = new StringBuilder(1024);
        if (node.isPresent()) {
            for (final DomUtil.NodeProp prop : DomUtil.prpdefs) {
                sb.append(prop.label);
                sb.append(": ");
                sb.append(getPropValue(node.get(), prop));
                sb.append("\n");
            }
        }
        setText(sb.toString());
    }

    private static String getPropValue(final DomTreeNode node, final DomUtil.NodeProp prop) {
        final short nType = node.node.getNodeType();
        final DomUtil.NodeInfo nodeInfo = DomUtil.nodeInfos.get(nType);
        if (!prop.nodeClass.isAssignableFrom(nodeInfo.typeClass)) {
            return "[N/A]";
        }

        return filter(prop.displayFn.apply(node.node));
    }
}
