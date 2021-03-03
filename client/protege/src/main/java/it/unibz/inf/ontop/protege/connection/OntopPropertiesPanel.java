package it.unibz.inf.ontop.protege.connection;

import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.Properties;

public class OntopPropertiesPanel extends JPanel implements OBDAModelManagerListener {

    private final OBDAModelManager obdaModelManager;
    private final DefaultTableModel model;

    public OntopPropertiesPanel(OWLEditorKit editorKit) {
        super(new BorderLayout());

        this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

        setBorder(new EmptyBorder(20,40,20, 40));

        model = new DefaultTableModel(new Object[]{"Property", "Value"}, 0);
        JTable table = new JTable(model);
        add(table, BorderLayout.CENTER);

        activeOntologyChanged(obdaModelManager.getCurrentOBDAModel());
    }

    @Override
    public void activeOntologyChanged(OBDAModel obdaModel) {
        DataSource datasource = obdaModel.getDataSource();

        model.setRowCount(0);
        Properties properties = datasource.asProperties();
        for (Map.Entry<Object, Object> p : properties.entrySet()) {
            model.addRow(new Object[]{ p.getKey(), p.getValue() });
        }
    }
}
