package it.unibz.inf.ontop.protege.gui.action;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithTimeIntervalMonitor;
import it.unibz.inf.ontop.rdf4j.materialization.RDF4JMaterializer;
import it.unibz.inf.ontop.rdf4j.query.MaterializationGraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

/**
 * Action to create individuals into the currently open OWL ontology
 * using the mapping from the current data source
 *
 * @author Mariano Rodriguez Muro
 */
public class AboxMaterializationAction extends ProtegeAction {

    private static final long serialVersionUID = -1211395039869926309L;

    private static final String RDF_XML = "RDF/XML";
    private static final String TURTLE = "Turtle";
    private static final String NTRIPLES = "N-Triples";

    private static final String DIALOG_TITLE = "RDF Graph materialization";

    private static final ImmutableMap<String, String> EXTENSIONS = ImmutableMap.of(
             RDF_XML, ".xml",
            NTRIPLES, ".nt",
            TURTLE, ".ttl");

    private static final ImmutableMap<String, Function<Writer, RDFHandler>> HANDLER_FACTORIES = ImmutableMap.of(
            RDF_XML, RDFXMLWriter::new,
            NTRIPLES, writer -> new TurtleWriter(writer)
                    .set(BasicWriterSettings.PRETTY_PRINT, false),
            TURTLE, writer -> new NTriplesWriter(writer)
                    .set(BasicWriterSettings.PRETTY_PRINT, false));

    private final Logger log = LoggerFactory.getLogger(AboxMaterializationAction.class);

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!DialogUtils.getOntopProtegeReasoner(getEditorKit()).isPresent())
            return;

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("<html><b>Choose the materialization option:</b></html>"), BorderLayout.NORTH);

        JRadioButton radioAdd = new JRadioButton("Add triples to the current ontology", true);
        JRadioButton radioExport = new JRadioButton("Store triples in an external file");
        ButtonGroup group = new ButtonGroup();
        group.add(radioAdd);
        group.add(radioExport);

        JLabel lFormat = new JLabel("Output format: ");
        JComboBox<String> comboFormats = new JComboBox<>(EXTENSIONS.keySet().toArray(new String[0]));
        comboFormats.setEnabled(false);
        radioExport.addItemListener(e ->
                comboFormats.setEnabled(e.getStateChange() == ItemEvent.SELECTED));

        JLabel info = new JLabel("<html><br><b>The operation may take some time " +
                "and may require a lot of memory.<br>Use the command-line tool " +
                "when data volume is too high.</b><br></html> ");
        info.setIcon(IconLoader.getImageIcon("images/alert.png"));

        JPanel radioAddPanel = new JPanel(new BorderLayout());
        radioAddPanel.add(radioAdd, BorderLayout.NORTH);

        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        formatPanel.add(lFormat);
        formatPanel.add(comboFormats);

        JPanel radioExportPanel = new JPanel(new BorderLayout());
        radioExportPanel.add(radioExport, BorderLayout.NORTH);
        radioExportPanel.add(formatPanel, BorderLayout.CENTER);
        radioExportPanel.add(info, BorderLayout.SOUTH);

        panel.add(radioAddPanel, BorderLayout.CENTER);
        panel.add(radioExportPanel, BorderLayout.SOUTH);

        if (JOptionPane.showOptionDialog(getWorkspace(),
                panel,
                DIALOG_TITLE,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                IconLoader.getOntopIcon(),
                null,
                null) != JOptionPane.OK_OPTION)
            return;

        if (radioAdd.isSelected()) {
            if (JOptionPane.showConfirmDialog(getWorkspace(),
                    "<html>The plugin will materialize triples and insert them into the current ontology.<br>"
                            + "The operation may take some time and may require a lot of memory if the database is large.<br><br>"
                            + "Do you wish to <b>continue</b>?<br></html>",
                    "Materialize the RDF Graph?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    IconLoader.getOntopIcon()) != JOptionPane.YES_OPTION)
                return;

            MaterializeToOntologyWorker worker = new MaterializeToOntologyWorker();
            worker.execute();
        }
        else if (radioExport.isSelected()) {
            String format = (String) comboFormats.getSelectedItem();

            JFileChooser fc = DialogUtils.getFileChooser(getEditorKit(),
                    DialogUtils.getExtensionReplacer("-materialized" + EXTENSIONS.get(format)));
            if (fc.showSaveDialog(getWorkspace()) != JFileChooser.APPROVE_OPTION)
                return;

            File file = fc.getSelectedFile();
            if (file.exists() && JOptionPane.showConfirmDialog(getWorkspace(),
                    "<html><br>The file " + file.getPath() + " exists.<br><br>"
                            + "Do you want to <b>overwrite</b> it?<br></html>",
                    DIALOG_TITLE,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    IconLoader.getOntopIcon()) != JOptionPane.YES_OPTION)
                return;

            MaterializeToFileWorker worker = new MaterializeToFileWorker(file, HANDLER_FACTORIES.get(format));
            worker.execute();
        }
    }

    private class MaterializeToFileWorker extends SwingWorkerWithTimeIntervalMonitor<Void, Void> {
        private final File file;
        private final Function<Writer, RDFHandler> handlerFactory;

        private OntopStandaloneSQLSettings settings;
        private long vocabularySize;

        MaterializeToFileWorker(File file, Function<Writer, RDFHandler> handlerFactory) {
            super(getWorkspace(),
                    "<html><h3>RDF Graph materialization:</h3></html>",
                    100);
            this.file = file;
            this.handlerFactory = handlerFactory;
        }

        @Override
        protected Void doInBackground() throws Exception {
            start("initializing...");

            OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
            OntopSQLOWLAPIConfiguration configuration = obdaModelManager.getConfigurationForOntology();
            settings = configuration.getSettings();

            RDF4JMaterializer materializer = RDF4JMaterializer.defaultMaterializer(
                    configuration,
                    MaterializationParams.defaultBuilder().build());
            MaterializationGraphQuery query = materializer.materialize();

            startLoop(() -> 50, () -> String.format("%d triples materialized...", getCount()));
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file), StandardCharsets.UTF_8))) {
                RDFHandler handler = handlerFactory.apply(writer);
                try (GraphQueryResult result = query.evaluate()) {
                    handler.startRDF();
                    while (result.hasNext()) {
                        handler.handleStatement(result.next());
                        tick();
                    }
                    endLoop("closing file...");
                    handler.endRDF();
                    vocabularySize = query.getSelectedVocabulary().size();
                }
            }
            end();
            return null;
        }

        @Override
        public void done() {
            try {
                complete();
                JOptionPane.showMessageDialog(getWorkspace(),
                        "<html><h3>RDF Graph materialization completed.</h3><br>" +
                                HTML_TAB + "<b>" + getCount() + "</b> triples materialized and stored in<br>" +
                                HTML_TAB + HTML_TAB + file.getPath() + ".<br>" +
                                HTML_TAB + "<b>" + vocabularySize + "</b> ontology classes and properties used.<br>" +
                                HTML_TAB + "Elapsed time: <b>" + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + "</b>.<br></html>",
                        DIALOG_TITLE,
                        JOptionPane.INFORMATION_MESSAGE,
                        IconLoader.getOntopIcon());
            }
            catch (CancellationException | InterruptedException e) {
                try {
                    Files.deleteIfExists(file.toPath());
                }
                catch (IOException ioException) {
                    /* NO-OP */
                }
                DialogUtils.showCancelledActionDialog(getWorkspace(), DIALOG_TITLE);
            }
            catch (ExecutionException e) {
                DialogUtils.showErrorDialog(getWorkspace(), DIALOG_TITLE, "RDF Graph materialization error.", log, e, settings);
            }
        }
    }

    private class MaterializeToOntologyWorker extends SwingWorkerWithTimeIntervalMonitor<Void, Void> {

        private OntopStandaloneSQLSettings settings;
        private long vocabularySize;

        MaterializeToOntologyWorker() {
            super(getWorkspace(),
                    "<html><h3>RDF Graph materialization:</h3></html>",
                    100);
        }

        @Override
        protected Void doInBackground() throws Exception {
            start("initializing...");

            OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
            OntopSQLOWLAPIConfiguration configuration = obdaModelManager.getConfigurationForOntology();
            settings = configuration.getSettings();

            OntopOWLAPIMaterializer materializer = OntopOWLAPIMaterializer.defaultMaterializer(
                    configuration,
                    MaterializationParams.defaultBuilder().build());

            startLoop(() -> 50, () -> String.format("%d triples materialized...", getCount()));
            Set<OWLAxiom> setAxioms = new HashSet<>();
            try (MaterializedGraphOWLResultSet graphResultSet = materializer.materialize()) {
                while (graphResultSet.hasNext()) {
                    setAxioms.add(graphResultSet.next());
                    tick();
                }
                vocabularySize = graphResultSet.getSelectedVocabulary().size();
            }
            endLoop("storing " + getCount() + " triples in the ontology...");
            obdaModelManager.addAxiomsToOntology(setAxioms);
            end();
            return null;
        }

        @Override
        public void done() {
            try {
                complete();
                JOptionPane.showMessageDialog(getWorkspace(),
                        "<html><h3>RDF Graph materialization completed.</h3><br>" +
                                HTML_TAB + "<b>" + getCount() + "</b> triples materialized.<br>" +
                                HTML_TAB + "<b>" + vocabularySize + "</b> ontology classes and properties used.<br>" +
                                HTML_TAB + "Elapsed time: <b>" + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + "</b>.<br></html>",
                        DIALOG_TITLE,
                        JOptionPane.INFORMATION_MESSAGE,
                        IconLoader.getOntopIcon());
            }
            catch (CancellationException | InterruptedException e) {
                DialogUtils.showCancelledActionDialog(getWorkspace(), DIALOG_TITLE);
            }
			catch (ExecutionException e) {
                DialogUtils.showErrorDialog(getWorkspace(), DIALOG_TITLE, "RDF Graph materialization error.", log, e, settings);
            }
        }
    }

    @Override
    public void initialise() { /* NO-OP */ }

    @Override
    public void dispose() { /* NO-OP */ }

}
