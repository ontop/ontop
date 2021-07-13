package it.unibz.inf.ontop.protege.connection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;

import static it.unibz.inf.ontop.protege.connection.DataSource.CONNECTION_PARAMETER_NAMES;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.getKeyStrokeWithCtrlMask;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.setUpAccelerator;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_ENTER;

public class OntopPropertiesPanel extends JPanel implements OBDAModelManagerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopPropertiesPanel.class);

    private static final ImmutableMap<String, JsonPropertyDescription> ONTOP_PROPERTIES;

    private final OWLEditorKit editorKit;
    private final OBDAModelManager obdaModelManager;
    private final OntopPropertiesTableModel model;
    private final JTable table;

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
        super(new GridBagLayout());

        this.editorKit = editorKit;
        this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

        setBorder(new EmptyBorder(20,40,20, 40));

        model = new OntopPropertiesTableModel();
        table = new JTable(model);
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String name = model.getName(row);
                Color fg = ONTOP_PROPERTIES.containsKey(name)
                        && (column == 0 || ONTOP_PROPERTIES.get(name).isValidValue(model.getValue(row)))
                        ? (isSelected ? table.getSelectionForeground() : table.getForeground())
                        : Color.RED;

                component.setForeground(fg);

                return component;
            }
        });

        add(new JScrollPane(table),
                new GridBagConstraints(0, 0, 3, 3, 1, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(DialogUtils.getButton(addAction));
        controlPanel.add(DialogUtils.getButton(deleteAction));
        controlPanel.add(DialogUtils.getButton(editAction));
        add(controlPanel,
                new GridBagConstraints(0, 4, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> setActionsEnabled());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && editAction.isEnabled())
                        editAction.actionPerformed(null);
            }
        });

        setActionsEnabled();

        setUpAccelerator(table, editAction);
        setUpAccelerator(table, deleteAction);

        activeOntologyChanged(obdaModelManager.getCurrentOBDAModel());
    }

    private void setActionsEnabled() {
        boolean selectNonEmpty = !table.getSelectionModel().isSelectionEmpty();
        editAction.setEnabled(selectNonEmpty && ONTOP_PROPERTIES.containsKey(model.getName(table.getSelectedRow())));
        deleteAction.setEnabled(selectNonEmpty);
    }

    @Override
    public void activeOntologyChanged(OBDAModel obdaModel) {
        model.clear(obdaModel.getDataSource());
    }

    private final OntopAbstractAction addAction = new OntopAbstractAction("Add",
            null, null, null) {
        @Override
        public void actionPerformed(ActionEvent evt) {
            ImmutableSet<String> names = model.getNames();
            OntopPropertiesEditDialog dialog = new OntopPropertiesEditDialog(
                    ONTOP_PROPERTIES.entrySet().stream()
                        .filter(e -> !names.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));
            DialogUtils.setLocationRelativeToProtegeAndOpen(editorKit, dialog);
            dialog.getProperty().ifPresent(i -> model.setProperty(i.getKey(), i.getValue()));
        }
    };

    private final OntopAbstractAction editAction = new OntopAbstractAction("Edit",
            null,
            "Change property value",
            getKeyStrokeWithCtrlMask(VK_ENTER)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            String name = model.getName(row);
            String value = model.getValue(row);
            OntopPropertiesEditDialog dialog = new OntopPropertiesEditDialog(
                    name, value, ONTOP_PROPERTIES.get(name));
            DialogUtils.setLocationRelativeToProtegeAndOpen(editorKit, dialog);
            dialog.getProperty().ifPresent(i -> model.setProperty(i.getKey(), i.getValue()));
        }
    };

    private final OntopAbstractAction deleteAction = new OntopAbstractAction("Delete",
            null,
            "Delete property",
            getKeyStrokeWithCtrlMask(VK_BACK_SPACE)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            String name = (String)model.getValueAt(row, 0);

            if (!DialogUtils.confirmation(OntopPropertiesPanel.this,
                    "<html>Proceed deleting property <b>" + name + "</b>?</html>",
                    "Delete Ontop Property Confirmation"))
                return;

            model.removeRow(row);
        }
    };
}
