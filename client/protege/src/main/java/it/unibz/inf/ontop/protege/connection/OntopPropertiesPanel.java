package it.unibz.inf.ontop.protege.connection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;

import static it.unibz.inf.ontop.protege.connection.DataSource.CONNECTION_PARAMETER_NAMES;
import static it.unibz.inf.ontop.protege.connection.OntopPropertiesTableModel.*;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;

public class OntopPropertiesPanel extends JPanel implements OBDAModelManagerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopPropertiesPanel.class);

    private static final ImmutableMap<String, JsonPropertyDescription> ONTOP_PROPERTIES;
    
    private final OBDAModelManager obdaModelManager;
    private final OntopPropertiesTableModel model;

    static {
        Map<String, JsonPropertyDescription> map;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            map = objectMapper.readValue(
                    OntopPropertiesPanel.class.getResource("/property_description.json"),
                    new TypeReference<Map<String, JsonPropertyDescription>>(){});
        }
        catch (IOException e) {
            LOGGER.error("Unable to open the property_description.json file: " + e);
            map = ImmutableMap.of();
        }
        ONTOP_PROPERTIES = map.entrySet().stream()
                .filter(e -> !CONNECTION_PARAMETER_NAMES.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap());
    }

    public OntopPropertiesPanel(OWLEditorKit editorKit) {
        super(new BorderLayout());

        this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

        setBorder(new EmptyBorder(20,40,20, 40));

        model = new OntopPropertiesTableModel();
        JTable table = new JTable(model) {
            @Override
            public boolean editCellAt(int row, int column, EventObject e) {
                boolean result = super.editCellAt(row, column, e);
                if (column == 1) {
                    Component editor = getEditorComponent();
                    if (editor instanceof JTextComponent) {
                        JTextComponent textComponent = (JTextComponent) editor;
                        String contents = textComponent.getText();
                        if (NEW_VALUE.equals(contents)) {
                            if (e instanceof MouseEvent)
                                //  Avoids the problem with a double click,
                                //  when the second click makes the editor to cancel selection
                                EventQueue.invokeLater(textComponent::selectAll);
                            else
                                textComponent.selectAll();
                        }
                    }
                }
                return result;
            }
        };
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);
        JComboBox<String> keysComboBox = new JComboBox<>(new DefaultComboBoxModel<String>(ONTOP_PROPERTIES.keySet().toArray(new String[0])) {
            @Override
            public void setSelectedItem(Object item) { // disables selection of keys
                if (model.canBeInRow(item, table.getSelectedRow()))
                    super.setSelectedItem(item);
            }

        });
        keysComboBox.setFont(table.getFont());
        keysComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                if (index == -1)
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                int row = table.getSelectedRow();
                boolean itemEnabled = model.canBeInRow(value, row);

                Component component = super.getListCellRendererComponent(list, value, index,
                        isSelected && itemEnabled, cellHasFocus);

                if (!itemEnabled)
                    component.setForeground(UIManager.getColor("Label.disabledForeground"));

                if (row != -1 && value.equals(model.getValueAt(row, 0)))
                    component.setFont(component.getFont().deriveFont(Font.BOLD));

                return component;
            }
        });
        TableColumn keysColumn = table.getColumnModel().getColumn(0);
        keysColumn.setCellEditor(new DefaultCellEditor(keysComboBox) {
            @Override
            public boolean stopCellEditing() {
                JComboBox<String> comboBox = (JComboBox<String>) getComponent();
                return model.canBeInRow(comboBox.getSelectedItem(), table.getSelectedRow())
                        && super.stopCellEditing();
            }
        });
        keysColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setForeground(!NEW_KEY.equals(value) && !ONTOP_PROPERTIES.containsKey(value)
                        ? Color.RED
                        : (isSelected ? table.getSelectionForeground() : table.getForeground()));

                return component;
            }
        });

        OntopAbstractAction removeAction = new OntopAbstractAction(
                "Remove",
                null,
                null,
                DialogUtils.getKeyStrokeWithCtrlMask(VK_BACK_SPACE)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                table.getCellEditor().cancelCellEditing();
                if (row != -1 && row != model.getRowCount() - 1) {
                    model.removeRow(row);
                    table.getSelectionModel().setSelectionInterval(row, row);
                }
            }
        };

        DialogUtils.setUpAccelerator(table, removeAction);
        add(new JScrollPane(table), BorderLayout.CENTER);

        activeOntologyChanged(obdaModelManager.getCurrentOBDAModel());
    }

    @Override
    public void activeOntologyChanged(OBDAModel obdaModel) {
        model.clear(obdaModel.getDataSource());
    }

}
