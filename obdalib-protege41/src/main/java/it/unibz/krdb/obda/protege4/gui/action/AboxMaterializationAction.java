package it.unibz.krdb.obda.protege4.gui.action;

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
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPI3Materializer;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.utils.OBDAProgessMonitor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.SesameMaterializer;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/***
 * Action to create individuals into the currently open OWL Ontology using the
 * existing mappings from ALL datasources
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
			
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Choose a materialization option: "), BorderLayout.NORTH);
		
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BorderLayout());
		
		JRadioButton b1 = new JRadioButton("Add individuals to current ontology", true);
		JRadioButton b2 = new JRadioButton("Write individuals to file in format:\t");
		String[] fileoptions = {"RDFXML", "N3", "TTL", "OWLXML"};
		JComboBox combo = new JComboBox(fileoptions);
		JRadioButton b3 = new JRadioButton("Create a new ontology with the individuals");
		
		ButtonGroup group = new ButtonGroup();
		group.add(b1); group.add(b2); group.add(b3);
		
		radioPanel.add(b1, BorderLayout.NORTH);
		radioPanel.add(b2, BorderLayout.CENTER);
		radioPanel.add(combo, BorderLayout.EAST);
		radioPanel.add(b3, BorderLayout.SOUTH);
		
		panel.add(radioPanel, BorderLayout.SOUTH);
		
		int res = JOptionPane.showOptionDialog(workspace, panel, "Materialization options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (res == JOptionPane.OK_OPTION)
		{
			//we got something to do 
			//take radio button choice
			
			//add to current onto
			if (b1.isSelected())
			{
				materializeOnto(modelManager.getActiveOntology(), modelManager.getOWLOntologyManager());	
			}
			//write to file
			else if (b2.isSelected())
			{
				int outputFormat = combo.getSelectedIndex();
				try {
					materializeToFile(outputFormat);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					JOptionPane.showMessageDialog(null, "ERROR: could not materialize data instances. ");
				}
			}
			//create new onto, add
			else if (b3.isSelected())
			{
				//open new onto in new window
				try {
					String fileName = "";
					final JFileChooser fc = new JFileChooser();
					fc.setSelectedFile(new File(fileName));
					fc.showSaveDialog(workspace);
					File file = fc.getSelectedFile();
					if (file!=null){
					OWLOntology newOnto = cloneOnto(file);
					OWLOntologyManager newMan = newOnto.getOWLOntologyManager();
					materializeOnto(newOnto, newMan);
					newMan.saveOntology(newOnto, new RDFXMLOntologyFormat(), new FileOutputStream(file));
				//	OWLOntology o = newMan.loadOntologyFromOntologyDocument((file));
				//	modelManager.setActiveOntology(o);
				//	materializeOnto(o, modelManager.getOWLOntologyManager());
					}
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
					JOptionPane.showMessageDialog(null, "ERROR: could not materialize data instances. ");
				}
			}
		}
		
	}
	
	private void materializeToFile(int format) throws Exception
	{
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
				//OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);
				Ontology onto = OWLAPI3TranslatorUtility.translate(ontology);
				obdaModel.getOntologyVocabulary().declareAll(onto.getVocabulary());
				
				final long startTime = System.currentTimeMillis();
				if (format != 3) {
					// we are going to use SESAME MATERIALIZER
				
					SesameMaterializer materializer = new SesameMaterializer(
							obdaModel, onto, DO_STREAM_RESULTS);
					Iterator<Statement> iterator = materializer.getIterator();
					RDFWriter writer = null;

					if (format == 0) // rdfxml
					{
						writer = new RDFXMLWriter(fileWriter);

					} else if (format == 1) // n3
					{
						writer = new N3Writer(fileWriter);

					} else if (format == 2) // ttl
					{
						writer = new TurtleWriter(fileWriter);
					}
					writer.startRDF();
					while (iterator.hasNext())
						writer.handleStatement(iterator.next());
					writer.endRDF();
					count = materializer.getTriplesCount();
					vocab = materializer.getVocabularySize();
					materializer.disconnect();
				}

				else {
					// owlxml, OWL materializer
					try (OWLAPI3Materializer materializer = new OWLAPI3Materializer(
							obdaModel, onto, DO_STREAM_RESULTS)) {
						Iterator<OWLIndividualAxiom> iterator = materializer.getIterator();
						while (iterator.hasNext())
							manager.addAxiom(ontology, iterator.next());
						manager.saveOntology(ontology, new OWLXMLOntologyFormat(),
								new WriterDocumentTarget(fileWriter));

						count = materializer.getTriplesCount();
						vocab = materializer.getVocabularySize();
					}
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
		} catch (FileNotFoundException e) {
			throw e;
		}
	}
	
	private OWLOntology cloneOnto(File file)
	{
		//create new onto by cloning this one
		OWLOntology currentOnto = modelManager.getActiveOntology();
		OWLOntologyManager ontologyManager = modelManager.getOWLOntologyManager();	
		OWLOntologyID id = new OWLOntologyID(IRI.create(currentOnto.getOntologyID().getOntologyIRI().toString()));
		OWLOntology newOnto = new OWLOntologyImpl(ontologyManager, id);
		Set<OWLAxiom> axioms = currentOnto.getAxioms();
		for(OWLAxiom axiom: axioms)
			ontologyManager.addAxiom(newOnto, axiom);
		return newOnto;
	}
	
	private void materializeOnto(OWLOntology onto, OWLOntologyManager ontoManager)
	{
		String message = "The plugin will generate several triples and save them in this current ontology.\n" +
				"The operation may take some time and may require a lot of memory if the data volume is too high.\n\n" +
				"Do you want to continue?";
		
		int response = JOptionPane.showConfirmDialog(workspace, message, "Confirmation", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION) {			
			try {
			
				OWLAPI3Materializer individuals = new OWLAPI3Materializer(obdaModel, DO_STREAM_RESULTS);
				Container container = workspace.getRootPane().getParent();
				final MaterializeAction action = new MaterializeAction(onto, ontoManager, individuals, container);
				
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							OBDAProgessMonitor monitor = new OBDAProgessMonitor("Materializing data instances...");
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
				});
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
