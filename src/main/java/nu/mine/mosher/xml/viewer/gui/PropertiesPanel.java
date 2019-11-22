package nu.mine.mosher.xml.viewer.gui;



import nu.mine.mosher.xml.viewer.model.DomTreeNode;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.GridLayout;
import java.util.Optional;



public class PropertiesPanel extends JPanel {
    private final JTextArea text;
    public PropertiesPanel() {
        super(new GridLayout(1, 1), true);
        this.text = new JTextArea();
        add(this.text);
    }

    public void display(Optional<DomTreeNode> currentNode)
    {
        final String s;
        if (currentNode.isPresent()) {
            s = currentNode.get().toString();
        } else {
            s = "[no node is selected";
        }
        this.text.setText(s);
    }
}
