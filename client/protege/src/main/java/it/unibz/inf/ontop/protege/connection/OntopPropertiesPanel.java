package it.unibz.inf.ontop.protege.connection;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.*;

import static it.unibz.inf.ontop.injection.OntopMappingSettings.*;
import static it.unibz.inf.ontop.injection.OntopReformulationSettings.*;
import static it.unibz.inf.ontop.protege.connection.OntopPropertiesTableModel.*;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;

public class OntopPropertiesPanel extends JPanel implements OBDAModelManagerListener {

    private static final ImmutableSet<String> KEYS = ImmutableSet.of(
            QUERY_ONTOLOGY_ANNOTATIONS,
            INFER_DEFAULT_DATATYPE,
            TOLERATE_ABSTRACT_DATATYPE,
            IS_CANONICAL_IRI_COMPLETE,
            CARDINALITY_MODE,
            TEST_MODE,
            EXISTENTIAL_REASONING,
            AVOID_POST_PROCESSING,
            EXCLUDE_INVALID_TRIPLES_FROM_RESULT_SET,
            QUERY_CACHE_MAX_SIZE,
            QUERY_LOGGING,
            INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE);
    
    private final OBDAModelManager obdaModelManager;
    private final OntopPropertiesTableModel model;

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
        JComboBox<String> keysComboBox = new JComboBox<>(new DefaultComboBoxModel<String>(KEYS.toArray(new String[0])) {
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
                component.setForeground(!NEW_KEY.equals(value) && !KEYS.contains(value)
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
