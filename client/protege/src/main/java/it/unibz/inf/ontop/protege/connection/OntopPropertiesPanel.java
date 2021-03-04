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
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Map;
import java.util.Properties;

import static java.awt.event.KeyEvent.VK_BACK_SPACE;

public class OntopPropertiesPanel extends JPanel implements OBDAModelManagerListener {

    private final OBDAModelManager obdaModelManager;
    private final DefaultTableModel model;
    private final java.util.List<String> keys = new ArrayList<>();
    private DataSource datasource;

    public OntopPropertiesPanel(OWLEditorKit editorKit) {
        super(new BorderLayout());

        this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

        setBorder(new EmptyBorder(20,40,20, 40));

        model = new DefaultTableModel(new Object[]{ "Property", "Value" }, 0);
        JTable table = new JTable(model) {
            @Override
            public boolean editCellAt(int row, int column, EventObject e) {
                boolean result = super.editCellAt(row, column, e);
                Component editor = getEditorComponent();
                if (editor instanceof JTextComponent) {
                    JTextComponent textComponent = (JTextComponent) editor;
                    String contents = textComponent.getText();
                    if (contents.startsWith("<") && contents.endsWith(">")) {
                        if (e instanceof MouseEvent)
                            //  Avoids the problem with a double click,
                            //  when the second click makes the editor to cancel selection
                            EventQueue.invokeLater(textComponent::selectAll);
                        else
                            textComponent.selectAll();
                    }
                }
                return result;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);
        OntopAbstractAction removeAction = new OntopAbstractAction(
                "Remove",
                null,
                null,
                DialogUtils.getKeyStrokeWithCtrlMask(VK_BACK_SPACE)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                table.getCellEditor().cancelCellEditing();
                table.getSelectionModel().clearSelection();
                if (row != -1 && row != model.getRowCount() - 1) {
                    model.removeRow(row);
                    table.getSelectionModel().setSelectionInterval(row, row);
                }
            }
        };

        DialogUtils.setUpAccelerator(table, removeAction);
        add(new JScrollPane(table), BorderLayout.CENTER);

        model.addTableModelListener(e -> {
            int row = e.getFirstRow();
            DefaultTableModel model = (DefaultTableModel)e.getSource();
            if (e.getType() == TableModelEvent.UPDATE) {
                String key = model.getValueAt(row, 0).toString();
                if ("<new key>".equals(key))
                    return;

                Object value = model.getValueAt(row, 1);
                if (row == model.getRowCount() - 1) {
                    datasource.resetProperty(null, key, value);
                    keys.add(row, key);
                    model.addRow(new Object[]{"<new key>", "<new value>"});
                }
                else {
                    String oldKey = keys.set(row, key);
                    datasource.resetProperty(oldKey, key, value);
                }
            }
            else if (e.getType() == TableModelEvent.DELETE) {
                String key = keys.remove(row);
                datasource.removeProperty(key);
            }
        });

        activeOntologyChanged(obdaModelManager.getCurrentOBDAModel());
    }

    @Override
    public void activeOntologyChanged(OBDAModel obdaModel) {
        datasource = obdaModel.getDataSource();

        model.setRowCount(0);
        keys.clear();
        Properties properties = datasource.asProperties();
        ImmutableSet<String> connectionParameterNames = DataSource.getConnectionParameterNames();
        for (Map.Entry<Object, Object> p : properties.entrySet())
            if (!connectionParameterNames.contains(p.getKey())) {
                model.addRow(new Object[]{ p.getKey(), p.getValue() });
                keys.add(p.getKey().toString());
            }
        model.addRow(new Object[]{ "<new key>", "<new value>" });
    }
}
