package it.unibz.inf.ontop.protege.gui.action;


import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.rdf4j.materialization.RDF4JMaterializer;
import it.unibz.inf.ontop.rdf4j.query.MaterializationGraphQuery;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/***
 * Action to create individuals into the currently open OWL Ontology using the
 * existing mappings from the current data source
 *
 * @author Mariano Rodriguez Muro
 */
public class AboxMaterializationAction extends ProtegeAction {

    private static final long serialVersionUID = -1211395039869926309L;

    private static final String RDF_XML = "RDF/XML";
//    private static final String OWL_XML = "OWL/XML";
    private static final String TURTLE = "Turtle";
    private static final String NTRIPLES = "N-Triples";

    private final Logger log = LoggerFactory.getLogger(AboxMaterializationAction.class);

    @Override
    public void actionPerformed(ActionEvent evt) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Choose a materialization option: "), BorderLayout.NORTH);

        JRadioButton radioAdd = new JRadioButton("Add triples to the current ontology", true);
        JRadioButton radioExport = new JRadioButton("Dump triples to an external file");
        ButtonGroup group = new ButtonGroup();
        group.add(radioAdd);
        group.add(radioExport);

        JLabel lFormat = new JLabel("Output format: ");
        JComboBox<String> comboFormats = new JComboBox<>(new String[] { RDF_XML, TURTLE, NTRIPLES });
        // enabled only when the Export radio button is selected
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
                "Materialization options",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null) != JOptionPane.OK_OPTION)
            return;

        if (radioAdd.isSelected()) {
            materializeOnto();
        }
        else if (radioExport.isSelected()) {
            materializeToFile((String) comboFormats.getSelectedItem());
        }
    }

    private static String getExtension(String format) {
        switch (format) {
            case RDF_XML:
                return ".xml";
            case NTRIPLES:
                return ".nt";
            case TURTLE:
                return ".ttl";
            default:
                throw new MinorOntopInternalBugException("Unexpected format: " + format);
        }
    }

    private static RDFHandler getHandler(String format, Writer writer) {
        switch (format) {
            case RDF_XML:
                return new RDFXMLWriter(writer);
            case TURTLE:
                TurtleWriter tw = new TurtleWriter(writer);
                tw.set(BasicWriterSettings.PRETTY_PRINT, false);
                return tw;
            case NTRIPLES:
                NTriplesWriter nt = new NTriplesWriter(writer);
                nt.set(BasicWriterSettings.PRETTY_PRINT, false);
                return nt;
            default:
                throw new MinorOntopInternalBugException("Unexpected format: " + format);
        }

    }

    private void materializeToFile(String format) {
        JFileChooser fc = DialogUtils.getFileChooser(getEditorKit(),
                DialogUtils.getExtensionReplacer("-materialized" + getExtension(format)));
        if (fc.showSaveDialog(getWorkspace()) != JFileChooser.APPROVE_OPTION)
            return;

        try {
            OWLEditorKit editorKit = (OWLEditorKit) getEditorKit();
            OWLOntology ontology = editorKit.getOWLModelManager().getActiveOntology();

            OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
            OntopSQLOWLAPIConfiguration configuration = obdaModelManager.getConfigurationManager()
                    .buildOntopSQLOWLAPIConfiguration(ontology);
            MaterializationGraphQuery result = RDF4JMaterializer.defaultMaterializer(
                    configuration,
                    MaterializationParams.defaultBuilder().build())
                    .materialize();

            long startTime = System.currentTimeMillis();
            File file = fc.getSelectedFile();
            try (OutputStream out = new FileOutputStream(file);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                RDFHandler handler = getHandler(format, writer);
                result.evaluate(handler);
            }
            long endTime = System.currentTimeMillis();
            JOptionPane.showMessageDialog(getWorkspace(),
                    "Materialization completed\n\n" +
                            "Number of triples: " + result.getTripleCountSoFar() + "\n" +
                            "Vocabulary size: " + result.getSelectedVocabulary().size() + "\n" +
                            "Elapsed time: " + (endTime - startTime) + "ms",
                    "ABox materialization",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Throwable e) {
            DialogUtils.showSeeLogErrorDialog(getWorkspace(), "Error materializing ABox.", log, e);
        }
    }

    private void materializeOnto() {
        if (JOptionPane.showConfirmDialog(getWorkspace(),
                "The plugin will generate several triples and save them in this current ontology.\n"
                        + "The operation may take some time and may require a lot of memory if the data volume is too high.\n\n"
                        + "Do you want to continue?",
                "Materialize?",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        try {
            OWLEditorKit editorKit = (OWLEditorKit) getEditorKit();
            OWLModelManager modelManager = editorKit.getOWLModelManager();
            OWLOntology ontology = modelManager.getActiveOntology();
            OWLOntologyManager ontoManager = modelManager.getOWLOntologyManager();

            OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
            OntopSQLOWLAPIConfiguration configuration = obdaModelManager.getConfigurationManager().buildOntopSQLOWLAPIConfiguration(ontology);

            MaterializationParams materializationParams = MaterializationParams.defaultBuilder()
                    .build();
            OntopOWLAPIMaterializer materializer = OntopOWLAPIMaterializer.defaultMaterializer(configuration, materializationParams);
            MaterializedGraphOWLResultSet graphResultSet = materializer.materialize();

            Thread thread = new Thread("MaterializeDataInstances Thread") {
                public void run() {
                    try {
                        OBDAProgressMonitor monitor = new OBDAProgressMonitor("Materializing data instances...", getWorkspace());
                        CountDownLatch latch = new CountDownLatch(1);
                        MaterializeAction action = new MaterializeAction(ontology, ontoManager, graphResultSet);
                        action.setCountdownLatch(latch);
                        monitor.addProgressListener(action);
                        monitor.start();
                        action.run();
                        latch.await();
                        monitor.stop();
                    }
                    catch (Throwable e) {
                        DialogUtils.showSeeLogErrorDialog(getWorkspace(), "Error materializing data instances.", log, e);
                    }
                }
            };
            thread.start();
        }
        catch (Throwable e) {
            DialogUtils.showSeeLogErrorDialog(getWorkspace(), "Error materializing data instances.", log, e);
        }
    }

    public class MaterializeAction implements OBDAProgressListener {

        private Thread thread;
        private CountDownLatch latch;

        private final OWLOntology currentOntology;
        private final OWLOntologyManager ontologyManager;
        private final MaterializedGraphOWLResultSet graphResultSet;

        private boolean bCancel = false;
        private boolean errorShown = false;

        public MaterializeAction(OWLOntology currentOntology, OWLOntologyManager ontologyManager,
                                 MaterializedGraphOWLResultSet graphResultSet) {
            this.currentOntology = currentOntology;
            this.ontologyManager = ontologyManager;
            this.graphResultSet = graphResultSet;
        }

        public void setCountdownLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public void run() {
            thread = new Thread("AddAxiomToOntology Thread") {
                public void run() {
                    try {
                        Set<OWLAxiom> setAxioms = Sets.newHashSet();
                        while (graphResultSet.hasNext()) {
                            setAxioms.add(graphResultSet.next());
                        }
                        graphResultSet.close();

                        ontologyManager.addAxioms(currentOntology, setAxioms);

                        latch.countDown();
                        if (!bCancel) {
                            JOptionPane.showMessageDialog(getWorkspace(),
                                    "Materialization completed\n\n" +
                                    "Number of triples: " + graphResultSet.getTripleCountSoFar() + "\n" +
                                    "Vocabulary size: " + graphResultSet.getSelectedVocabulary().size(),
                                    "Materialization",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    catch (Throwable e) {
                        latch.countDown();
                        DialogUtils.showSeeLogErrorDialog(getWorkspace(), "Could not materialize ABox.", log, e);
                    }
                }
            };
            thread.start();
        }

        @Override
        public void actionCanceled() {
            if (thread != null) {
                bCancel = true;
                latch.countDown();
                thread.interrupt();
            }
        }

        @Override
        public boolean isCancelled() {
            return bCancel;
        }

        @Override
        public boolean isErrorShown() {
            return errorShown;
        }

    }

    @Override
    public void initialise() { /* NO-OP */ }

    @Override
    public void dispose() { /* NO-OP */ }

}
