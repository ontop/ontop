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

import com.google.common.base.Optional;
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
import org.semanticweb.ontop.protege.utils.OBDAProgressMonitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.Iterator;
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
	private static final boolean DO_STREAM_RESULTS = true;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModel = null;
	private OWLWorkspace workspace;	
	private OWLModelManager modelManager;
	
	private Logger log = LoggerFactory.getLogger(AboxMaterializationAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();
		workspace = editorKit.getWorkspace();	
		modelManager = editorKit.getOWLModelManager();
		obdaModel = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName())).getActiveOBDAModel();
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
		JRadioButton radioExport = new JRadioButton("Export triples to an external file");

		ButtonGroup group = new ButtonGroup();
		group.add(radioAdd);
		group.add(radioExport);

		//combo box for output format,
		JLabel lFormat = new JLabel("Output format:\t");
		String[] fileOptions = {"rdfxml", "owlxml", "turtle", "n3"};
		final JComboBox comboFormats = new JComboBox(fileOptions);
		//should be enabled only when radio button export is selected
		comboFormats.setEnabled(false);

		//check box to save the materialized ontology in multiple files and avoid outofmemory error
		final JCheckBox cbSeparateFiles = new JCheckBox("Use separate files", null, true);
		//should be enabled only when radio button export is selected
		cbSeparateFiles.setEnabled(false);

		//add a listener for the radio button, allows to enable combo box and check box when the radio button is selected

		radioExport.addItemListener(e -> {

            if (e.getStateChange() == ItemEvent.SELECTED) {
                cbSeparateFiles.setEnabled(true);
                comboFormats.setEnabled(true);

            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                cbSeparateFiles.setEnabled(false);
                comboFormats.setEnabled(false);
            }
        });

		//add values to the panels

		radioAddPanel.add(radioAdd, BorderLayout.NORTH);

		radioExportPanel.add(radioExport, BorderLayout.NORTH);
		radioExportPanel.add(lFormat, BorderLayout.CENTER);
		radioExportPanel.add(comboFormats, BorderLayout.EAST);
		radioExportPanel.add(cbSeparateFiles, BorderLayout.SOUTH);

		panel.add(radioAddPanel, BorderLayout.CENTER);
		panel.add(radioExportPanel, BorderLayout.SOUTH);


		//actions when OK BUTTON has been pressed here
		int res = JOptionPane.showOptionDialog(workspace, panel, "Materialization options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (res == JOptionPane.OK_OPTION) {


			if (radioAdd.isSelected()) {
				//add to current ontology
				materializeOnto(modelManager.getActiveOntology(), modelManager.getOWLOntologyManager());
			}

			else if (radioExport.isSelected()) {
				//write to file and create an ontology if requested
				String outputFormat = (String)comboFormats.getSelectedItem();

				if (cbSeparateFiles.isSelected()) {
					//save materialized values in multiple files
					materializeMultipleFiles();

				} else {
					//save materialized values in a new file
					materializeToFile(outputFormat);

				}

			}
		}

	}

	private void materializeMultipleFiles() {
		try {
            String fileName = "";
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(fileName));
            fc.showSaveDialog(workspace);

            File file = fc.getSelectedFile();
            if (file != null)
            {
                //clone the ontology
                OWLOntologyManager newMan = OWLManager.createOWLOntologyManager();
//                OWLOntology newOnto = cloneOnto(newMan);
//                materializeOnto(newOnto, newMan);
//                OWLDocumentFormat format = modelManager.getOWLOntologyManager().getOntologyFormat(modelManager.getActiveOntology());
//                newMan.saveOntology(newOnto, format, new FileOutputStream(file));
            }
        } catch (Exception e) {

            log.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(null, "ERROR: could not materialize data instances. ");
        }
	}

	private OWLOntology cloneOnto(OWLOntologyManager newMan) throws OWLOntologyCreationException {

		//clone current ontology
		OWLOntology currentOnto = modelManager.getActiveOntology();
		OWLOntologyID ontologyID = currentOnto.getOntologyID();
		Optional<IRI> ontologyIRI = ontologyID.getOntologyIRI();
		Optional<IRI> versionIRI = ontologyID.getVersionIRI();
		OWLOntologyID newOntologyID = new OWLOntologyID(Optional.of(IRI.create(ontologyIRI.toString())),Optional.of(IRI.create(versionIRI.toString())));
		OWLOntology newOnto = newMan.createOntology(newOntologyID);
		Set<OWLAxiom> axioms = currentOnto.getAxioms();
		for(OWLAxiom axiom: axioms)
			newMan.addAxiom(newOnto, axiom);


		return newOnto;
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
				obdaModel.getOntologyVocabulary().merge(onto.getVocabulary());

				final long startTime = System.currentTimeMillis();
				OWLDocumentFormat ontoFormat;

				switch (format) {
					case "rdfxml":
						ontoFormat = new RDFXMLDocumentFormat();
						break;
					case "owlxml":
						ontoFormat = new OWLXMLDocumentFormat();
						break;
					case "turtle":
						ontoFormat = new TurtleDocumentFormat();
						break;
					case "n3":
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
						"Task is completed\nNr. of triples: " + count
								+ "\nVocabulary size: " + vocab
								+ "\nElapsed time: " + time + " ms.", "Done",
						JOptionPane.INFORMATION_MESSAGE);
			}


		} catch (Exception e) {
			log.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(null, "ERROR: could not materialize data instances. ");
		}

	}

	
	private void materializeOnto(OWLOntology onto, OWLOntologyManager ontoManager)
	{
		String message = "The plugin will generate several triples and save them in this current ontology.\n" +
				"The operation may take some time and may require a lot of memory if the data volume is too high.\n\n" +
				"Do you want to continue?";
		
		int response = JOptionPane.showConfirmDialog(workspace, message, "Confirmation", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION) {
			try {

				OWLAPIMaterializer individuals = new OWLAPIMaterializer(obdaModel, DO_STREAM_RESULTS);
				Container container = workspace.getRootPane().getParent();
				final MaterializeAction action = new MaterializeAction(onto, ontoManager, individuals, container);

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
