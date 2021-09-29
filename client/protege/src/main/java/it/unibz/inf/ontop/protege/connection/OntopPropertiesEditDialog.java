package it.unibz.inf.ontop.protege.connection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.protege.utils.SimpleDocumentListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.getButton;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.setUpAccelerator;

public class OntopPropertiesEditDialog extends JDialog {

    private final JComboBox<String> nameComboBox;
    private final JLabel typeLabel;
    private final JTextField valueField;
    private final Color defaultTextColor;
    private final JTextArea descriptionLabel;

    private final ImmutableMap<String, JsonPropertyDescription> properties;

    private Map.Entry<String, String> result;

    private static final int GAP = 2;

    public OntopPropertiesEditDialog(ImmutableMap<String, JsonPropertyDescription> properties) {

        this.properties = properties;

        setTitle("Ontop Properties");
        setModal(true);

        setLayout(new BorderLayout());
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(new JLabel("Name:"),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        nameComboBox = new JComboBox<>(properties.keySet().toArray(new String[0]));
        nameComboBox.setEditable(false);
        mainPanel.add(nameComboBox,
                new GridBagConstraints(1, 0, 2, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        mainPanel.add(new JLabel("Type:"),
                new GridBagConstraints(0, 1, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        typeLabel = new JLabel();
        mainPanel.add(typeLabel,
                new GridBagConstraints(1, 1, 2, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        mainPanel.add(new JLabel("Value:"),
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        valueField = new JTextField();
        defaultTextColor = valueField.getForeground();
        valueField.getDocument().addDocumentListener(
                (SimpleDocumentListener) e -> onValueChange(getCurrentProperty()));
        mainPanel.add(valueField,
                new GridBagConstraints(1, 2, 2, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        mainPanel.add(new JLabel("Description:"),
                new GridBagConstraints(0, 3, 1, 1, 0, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        descriptionLabel = new JTextArea();
        descriptionLabel.setEditable(false);
        descriptionLabel.setCursor(null);
        //descriptionLabel.setOpaque(false);
        descriptionLabel.setFocusable(false);
        descriptionLabel.setFont(UIManager.getFont("Label.font"));
        descriptionLabel.setWrapStyleWord(true);
        descriptionLabel.setLineWrap(true);
        descriptionLabel.setBorder(null);
        descriptionLabel.setBackground(mainPanel.getBackground());
        JScrollPane scrollPane = new JScrollPane(descriptionLabel);
        scrollPane.setPreferredSize(new Dimension(500, 100));
        mainPanel.add(scrollPane,
                new GridBagConstraints(1, 3, 1, 1, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        add(mainPanel, BorderLayout.CENTER);

        OntopAbstractAction cancelAction = DialogUtils.getStandardCloseWindowAction(DialogUtils.CANCEL_BUTTON_TEXT, this);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(getButton(cancelAction));
        JButton okButton = getButton(okAction);
        controlPanel.add(okButton);
        add(controlPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        setUpAccelerator(getRootPane(), cancelAction);

        onPropertySelect();
        nameComboBox.addItemListener(e -> onPropertySelect());
    }

    public OntopPropertiesEditDialog(String name, String value, JsonPropertyDescription description) {
        this(ImmutableMap.of(name, description));

        nameComboBox.setSelectedItem(name);
        valueField.setText(value);
    }

    private void onPropertySelect() {
        JsonPropertyDescription property = getCurrentProperty();
        typeLabel.setText(property.getType());
        descriptionLabel.setText(property.getDescription());
        onValueChange(property);
    }

    private void onValueChange(JsonPropertyDescription property) {
        boolean valid = property.isValidValue(valueField.getText());
        valueField.setForeground(valid ? defaultTextColor : Color.RED);
    }

    private JsonPropertyDescription getCurrentProperty() {
        String name = (String)nameComboBox.getSelectedItem();
        return properties.get(name);
    }

    private final OntopAbstractAction okAction = new OntopAbstractAction(DialogUtils.OK_BUTTON_TEXT, null, null, null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            result = Maps.immutableEntry((String)nameComboBox.getSelectedItem(), valueField.getText());
            dispatchEvent(new WindowEvent(OntopPropertiesEditDialog.this, WindowEvent.WINDOW_CLOSING));
        }
    };

    public Optional<Map.Entry<String, String>> getProperty() {
        return Optional.ofNullable(result);
    }
}
