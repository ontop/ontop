package org.semanticweb.ontop.protege.gui.action;

/*
 * #%L
 * ontop-protege4
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

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPITranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPIMaterializer;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.ontop.protege.core.OBDAModelManager;
import org.semanticweb.ontop.protege.gui.IconLoader;
import org.semanticweb.ontop.protege.utils.OBDAProgressMonitor;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.Iterator;
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
	private OBDAModel obdaModel = null;
	private OWLWorkspace workspace;
	private OWLModelManager modelManager;
	private String lineSeparator;

	private Logger log = LoggerFactory.getLogger(AboxMaterializationAction.class);

	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();
		workspace = editorKit.getWorkspace();
		modelManager = editorKit.getOWLModelManager();
		obdaModel = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName())).getActiveOBDAModel();
		lineSeparator = System.getProperty("line.separator");
	}

	@Override
	public void dispose() throws Exception {
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
		JRadioButton radioExport = new JRadioButton("Dump triples to an external file (including current ontology)");

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

				OutputStream out = new FileOutputStream(file);
				BufferedWriter fileWriter = new BufferedWriter(
						new OutputStreamWriter(out, "UTF-8"));

				OWLOntology ontology = modelManager.getActiveOntology();
				OWLOntologyManager manager = modelManager.getOWLOntologyManager();

				Ontology onto = OWLAPITranslatorUtility.translate(ontology);

				final long startTime = System.currentTimeMillis();
				OWLDocumentFormat ontoFormat;

				switch (format) {
					case RDF_XML:
						ontoFormat = new RDFXMLDocumentFormat();
						break;
					case OWL_XML:
						ontoFormat = new OWLXMLDocumentFormat();
						break;
					case TURTLE:
						ontoFormat = new TurtleDocumentFormat();
						break;
					case N3:
						ontoFormat = new N3DocumentFormat();
						break;
					default:
						throw new Exception("Unknown format: " + format);
				}

				try (OWLAPIMaterializer materializer = new OWLAPIMaterializer(obdaModel, onto, DO_STREAM_RESULTS)) {

					Iterator<OWLIndividualAxiom> iterator = materializer.getIterator();
					while (iterator.hasNext()) {
						manager.addAxiom(ontology, iterator.next());
					}
					manager.saveOntology(ontology, ontoFormat, new WriterDocumentTarget(fileWriter));

					count = materializer.getTriplesCount();
					vocab = materializer.getVocabularySize();
				}

				fileWriter.close();
				out.close();
				final long endTime = System.currentTimeMillis();
				time = endTime - startTime;

				JOptionPane.showMessageDialog(this.workspace,
						"Task is completed"+lineSeparator+"Nr. of triples: " + count
								+ lineSeparator+"Vocabulary size: " + vocab
								+ lineSeparator+"Elapsed time: " + time + " ms.", "Done",
						JOptionPane.INFORMATION_MESSAGE);
			}


		} catch (Exception e) {
			log.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(null, "ERROR: could not materialize data instances. ");
		}

	}


	private void materializeOnto(OWLOntology ontology, OWLOntologyManager ontoManager)
	{

		String message = "The plugin will generate several triples and save them in this current ontology."+lineSeparator +
				"The operation may take some time and may require a lot of memory if the data volume is too high."+lineSeparator + lineSeparator +
				"Do you want to continue?";

		int response = JOptionPane.showConfirmDialog(workspace, message, "Confirmation", JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION) {
			try {
				Ontology onto = OWLAPITranslatorUtility.translate(ontology);

				OWLAPIMaterializer individuals = new OWLAPIMaterializer(obdaModel, onto, DO_STREAM_RESULTS);
				Container container = workspace.getRootPane().getParent();
				final MaterializeAction action = new MaterializeAction(ontology, ontoManager, individuals, container);

				Thread th = new Thread("MaterializeDataInstances") {
					public void run() {
                    try {
                        OBDAProgressMonitor monitor = new OBDAProgressMonitor("Materializing data instances...");
                        CountDownLatch latch = new CountDownLatch(1);
                        action.setCountdownLatch(latch);
                        monitor.addProgressListener(action);
                        monitor.start();
                        action.run();
                        latch.await();
                        monitor.stop();
                    }
                    catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        JOptionPane.showMessageDialog(null, "ERROR: could not materialize data instances.");
                    }
                }
				};
				th.start();
			}
			catch (Exception e) {
				Container container = getWorkspace().getRootPane().getParent();
				JOptionPane.showMessageDialog(container, "Cannot create individuals! See the log information for the details.", "Error", JOptionPane.ERROR_MESSAGE);
				log.error(e.getMessage(), e);
			}
		}
	}


}
