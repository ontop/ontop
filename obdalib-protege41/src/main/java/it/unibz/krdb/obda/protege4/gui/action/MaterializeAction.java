/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.gui.swing.utils.OBDAProgressListener;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPI3Materializer;

import java.awt.Container;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaterializeAction implements OBDAProgressListener {

	private Logger log = LoggerFactory.getLogger(MaterializeAction.class);
	private Thread thread = null;
	private CountDownLatch latch = null;
	
	private OWLOntology currentOntology = null;
	private OWLOntologyManager ontologyManager = null;
	private OWLAPI3Materializer materializer = null;
	private Iterator<OWLIndividualAxiom> iterator = null;
	private Container cont = null;
	private boolean bCancel = false;

	public MaterializeAction(OWLOntology currentOntology, OWLOntologyManager ontologyManager, OWLAPI3Materializer materialize, Container cont) {
		this.currentOntology = currentOntology;
		this.ontologyManager = ontologyManager;			
		this.materializer = materialize;
		this.iterator = materializer.getIterator();
		this.cont = cont;  
	}

	public void setCountdownLatch(CountDownLatch cdl){
		latch = cdl;
	}
	
	public void run() {
		if (latch == null){
			try {
				throw new Exception("No CountDownLatch set");
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(null, "ERROR: could not materialize abox.");
				return;
			}
		}
		
		thread = new Thread() {
			public void run() {
				try {
					while(iterator.hasNext()) {
						ontologyManager.addAxiom(currentOntology, iterator.next());
					}
					
					latch.countDown();
					if(!bCancel){
						JOptionPane.showMessageDialog(cont, "Task is completed", "Done", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				catch (Exception e) {
					latch.countDown();
					log.error("Materialization of Abox failed", e);
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

}

