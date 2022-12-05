package it.unibz.inf.ontop.protege.query;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

public class QueryInterfaceLimitPanel extends JPanel {

    private final JCheckBox showAllCheckBox;
    private final JFormattedTextField fetchSizeTextField;

    public QueryInterfaceLimitPanel() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createHorizontalStrut(5));
        add(new JLabel("Show"));

        fetchSizeTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        fetchSizeTextField.setValue(100);
        fetchSizeTextField.setColumns(4);
        fetchSizeTextField.setHorizontalAlignment(JTextField.RIGHT);
        add(fetchSizeTextField);

        add(Box.createHorizontalStrut(5));
        add(new JLabel("or"));

        showAllCheckBox = new JCheckBox("all results.");
        showAllCheckBox.addActionListener(new ActionListener() {
            private int fetchSizeSaved = 100;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showAllCheckBox.isSelected()) {
                    fetchSizeSaved = getFetchSize();
                    fetchSizeTextField.setValue(0);
                }
                else
                    fetchSizeTextField.setValue(fetchSizeSaved);

                fetchSizeTextField.setEnabled(!showAllCheckBox.isSelected());
            }
        });
        add(showAllCheckBox);
        add(Box.createHorizontalStrut(5));
    }

    public int getFetchSize() {
        return ((Number) fetchSizeTextField.getValue()).intValue();
    }

    public boolean isFetchAllSelected() { return showAllCheckBox.isSelected(); }

}
