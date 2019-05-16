package it.unibz.inf.ontop.protege.gui.action;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.rdf4j.materialization.RDF4JMaterializer;
import it.unibz.inf.ontop.rdf4j.query.MaterializationGraphQuery;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.n3.N3Writer;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
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
    private static final String OWL_XML = "OWL/XML";
    private static final String TURTLE = "Turtle";
    private static final String N3 = "N3";

    private static final boolean DO_STREAM_RESULTS = true;

    private OWLEditorKit editorKit = null;
    private OWLWorkspace workspace;
    private OWLModelManager modelManager;
    private String lineSeparator;
    private OBDAModelManager obdaModelManager;

    private Logger log = LoggerFactory.getLogger(AboxMaterializationAction.class);

    @Override
    public void initialise() {
        editorKit = (OWLEditorKit) getEditorKit();
        workspace = editorKit.getWorkspace();
        modelManager = editorKit.getOWLModelManager();
        obdaModelManager = (OBDAModelManager) editorKit.get(SQLPPMappingImpl.class.getName());
        lineSeparator = System.getProperty("line.separator");
    }

    @Override
    public void dispose() {
        // Does nothing!
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        //materialization panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Choose a materialization option: "), BorderLayout.NORTH);

        //panel for adding triples to ontology
        JPanel radioAddPanel = new JPanel();
        radioAddPanel.setLayout(new BorderLayout());
        JRadioButton radioAdd = new JRadioButton("Add triples to current ontology", true);

        //panel for exporting triples to file
        JPanel radioExportPanel = new JPanel(new BorderLayout());
        JRadioButton radioExport = new JRadioButton("Dump triples to an external file");

        ButtonGroup group = new ButtonGroup();
        group.add(radioAdd);
        group.add(radioExport);

        //combo box for output format,
        JLabel lFormat = new JLabel("Output format:\t");
        String[] fileOptions = {RDF_XML, OWL_XML, TURTLE, N3};
        final JComboBox comboFormats = new JComboBox(fileOptions);
        //should be enabled only when radio button export is selected
        comboFormats.setEnabled(false);

        //info: materialization is expensive

        JLabel info = new JLabel("<html><br><b>The operation may take some time and may require a lot of memory.<br>Use the command line version when data volume is too high.</b><br></html> ");
        info.setIcon(IconLoader.getImageIcon("images/alert.png"));

        //add a listener for the radio button, allows to enable combo box and check box when the radio button is selected

        radioExport.addItemListener(e -> {

            if (e.getStateChange() == ItemEvent.SELECTED) {
                comboFormats.setEnabled(true);

            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                comboFormats.setEnabled(false);
            }
        });

        //add values to the panels

        radioAddPanel.add(radioAdd, BorderLayout.NORTH);

        radioExportPanel.add(radioExport, BorderLayout.NORTH);
        radioExportPanel.add(lFormat, BorderLayout.CENTER);
        radioExportPanel.add(comboFormats, BorderLayout.EAST);
        radioExportPanel.add(info, BorderLayout.SOUTH);

        panel.add(radioAddPanel, BorderLayout.CENTER);
        panel.add(radioExportPanel, BorderLayout.SOUTH);


        //actions when OK BUTTON has been pressed here
        int res = JOptionPane.showOptionDialog(workspace, panel, "Materialization options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (res == JOptionPane.OK_OPTION) {


            if (radioAdd.isSelected()) {
                //add to current ontology
                materializeOnto(modelManager.getActiveOntology(), modelManager.getOWLOntologyManager());

            } else if (radioExport.isSelected()) {
                //choose file format
                String outputFormat = (String) comboFormats.getSelectedItem();

                //save materialized values in a new file
                materializeToFile(outputFormat);


            }
        }

    }

    private void materializeToFile(String format) {
        String fileName = "";
        long count = 0;
        long time = 0;
        int vocab = 0;
        final JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileName));
        fc.showSaveDialog(workspace);

        try {
            File file = fc.getSelectedFile();
            if (file != null) {

                OWLOntology ontology = modelManager.getActiveOntology();
                OntopSQLOWLAPIConfiguration configuration = obdaModelManager.getConfigurationManager().buildOntopSQLOWLAPIConfiguration(ontology);
                MaterializationParams params = MaterializationParams.defaultBuilder()
                        .enableDBResultsStreaming(DO_STREAM_RESULTS)
                        .build();
                RDF4JMaterializer materializer = RDF4JMaterializer.defaultMaterializer(configuration, params);
                final long startTime = System.currentTimeMillis();
                MaterializationGraphQuery graphQuery = materializer.materialize();
                try (OutputStream out = new FileOutputStream(file); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
                    RDFHandler handler;
                    switch (format) {
                        case RDF_XML:
                        case OWL_XML:
                            handler = new RDFXMLWriter(writer);
                            break;
                        case TURTLE:
                            handler = new TurtleWriter(writer);
                            break;
                        case N3:
                            handler = new N3Writer(writer);
                            break;
                        default:
                            throw new Exception("Unknown format: " + format);
                    }
                    graphQuery.evaluate(handler);
                    count = graphQuery.getTripleCountSoFar();
                    vocab = graphQuery.getSelectedVocabulary().size();

                }
                final long endTime = System.currentTimeMillis();
                time = endTime - startTime;

                JOptionPane.showMessageDialog(this.workspace,
                        "Task is completed" + lineSeparator + "Nr. of triples: " + count
                                + lineSeparator + "Vocabulary size: " + vocab
                                + lineSeparator + "Elapsed time: " + time + " ms.", "Done",
                        JOptionPane.INFORMATION_MESSAGE);

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(workspace, "ERROR: could not materialize data instances. ");
        }
    }


    private void materializeOnto(OWLOntology ontology, OWLOntologyManager ontoManager) {

        String message = "The plugin will generate several triples and save them in this current ontology." + lineSeparator +
                "The operation may take some time and may require a lot of memory if the data volume is too high." + lineSeparator + lineSeparator +
                "Do you want to continue?";

        int response = JOptionPane.showConfirmDialog(workspace, message, "Confirmation", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            try {

                OntopSQLOWLAPIConfiguration configuration = obdaModelManager.getConfigurationManager().buildOntopSQLOWLAPIConfiguration(ontology);

                MaterializationParams materializationParams = MaterializationParams.defaultBuilder()
                        .enableDBResultsStreaming(DO_STREAM_RESULTS)
                        .build();
                OntopOWLAPIMaterializer materializer = OntopOWLAPIMaterializer.defaultMaterializer(configuration, materializationParams);
                MaterializedGraphOWLResultSet graphResultSet = materializer.materialize();

                Container container = workspace.getRootPane().getParent();
                final MaterializeAction action = new MaterializeAction(ontology, ontoManager, graphResultSet, container);

                Thread th = new Thread("MaterializeDataInstances Thread") {
                    public void run() {
                        try {
                            OBDAProgressMonitor monitor = new OBDAProgressMonitor("Materializing data instances...", workspace);
                            CountDownLatch latch = new CountDownLatch(1);
                            action.setCountdownLatch(latch);
                            monitor.addProgressListener(action);
                            monitor.start();
                            action.run();
                            latch.await();
                            monitor.stop();
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                            JOptionPane.showMessageDialog(workspace, "ERROR: could not materialize data instances.");
                        }
                    }
                };
                th.start();
            } catch (Exception e) {
                Container container = getWorkspace().getRootPane().getParent();
                JOptionPane.showMessageDialog(container, "Cannot create individuals! See the log information for the details.", "Error", JOptionPane.ERROR_MESSAGE);
                log.error(e.getMessage(), e);
            }
        }
    }


}
