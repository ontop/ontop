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

import com.google.common.collect.Sets;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class MaterializeAction implements OBDAProgressListener {

	private Logger log = LoggerFactory.getLogger(MaterializeAction.class);
	private Thread thread = null;
	private CountDownLatch latch = null;
	
	private OWLOntology currentOntology = null;
	private OWLOntologyManager ontologyManager = null;
	private final MaterializedGraphOWLResultSet graphResultSet;
	private Container cont = null;
	private boolean bCancel = false;
	private boolean errorShown = false;

	public MaterializeAction(OWLOntology currentOntology, OWLOntologyManager ontologyManager,
                             MaterializedGraphOWLResultSet graphResultSet, Container cont) {
		this.currentOntology = currentOntology;
		this.ontologyManager = ontologyManager;			
		this.graphResultSet = graphResultSet;
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
				JOptionPane.showMessageDialog(cont, "ERROR: could not materialize Abox.");
				this.errorShown = true;
				return;
			}
		}

		thread = new Thread("AddAxiomToOntology Thread") {
			public void run() {
				try {
					Set<OWLAxiom> setAxioms = Sets.newHashSet();
					while(graphResultSet.hasNext()) {
						setAxioms.add(graphResultSet.next());
					}
					graphResultSet.close();

					ontologyManager.addAxioms(currentOntology, setAxioms);

					latch.countDown();
					if (!bCancel) {
						JOptionPane.showMessageDialog(cont, "Task is completed", "Done", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (Exception e) {
					latch.countDown();
					log.error("Materialization of Abox failed", e);

				} catch (Error e) {

					latch.countDown();
					log.error("Materialization of Abox failed", e);
					JOptionPane.showMessageDialog(null, "An error occurred. For more info, see the logs.");

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
		return this.bCancel;
	}

	@Override
	public boolean isErrorShown() {
		return this.errorShown;
	}

}

