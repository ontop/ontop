package org.semanticweb.ontop.protege4.gui.action;

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

import java.awt.Container;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

import org.semanticweb.ontop.owlrefplatform.owlapi3.OWLAPI3Materializer;
import org.semanticweb.ontop.protege4.utils.OBDAProgressListener;
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

