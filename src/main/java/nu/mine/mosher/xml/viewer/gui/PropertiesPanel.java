package nu.mine.mosher.xml.viewer.gui;

import nu.mine.mosher.xml.viewer.file.DomUtil;
import nu.mine.mosher.xml.viewer.model.DomTreeNode;

import javax.swing.JTextArea;
import java.io.IOException;
import java.util.*;

import static nu.mine.mosher.xml.viewer.StringUnicodeEncoderDecoder.filter;

class PropertiesPanel extends JTextArea {
    public PropertiesPanel() {
        setOpaque(false);
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public void display(final Optional<DomTreeNode> domTreeNode) throws IOException {
        final StringBuilder sb = new StringBuilder(1024);
        if (domTreeNode.isPresent()) {
            for (final DomUtil.NodeProp prop : DomUtil.prpdefs) {
                final DomTreeNode node = domTreeNode.get();
                final short nType = node.node.getNodeType();
                final DomUtil.NodeInfo nodeInfo = DomUtil.nodeInfos.get(nType);
                if (prop.nodeClass.isAssignableFrom(nodeInfo.typeClass)) {
                    final String val = prop.displayFn.apply(node.node);
                    if (Objects.nonNull(val) && !val.isEmpty()) {
                        sb.append(prop.label);
                        sb.append(": ");
                        sb.append(filter(val));
                        sb.append("\n");
                    }
                }

            }
        }
        setText(sb.toString());
    }
}
