/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;

import org.semanticweb.owlapi.model.OWLOntology;

public class OBDAModelRefactorer {
	
	private OBDAModel obdaModel;
	private TargetQueryVocabularyValidator validator;
	
	// TODO We should reduce the dependency to OWL-API to define the ontology.
	public OBDAModelRefactorer(OBDAModel obdaModel, OWLOntology ontology) {
		this.obdaModel = obdaModel;
		validator = new TargetQueryValidator(obdaModel);
	}
	
	public void run() throws Exception {
		Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappingTable = obdaModel.getMappings();
		for (URI datasourceUri : mappingTable.keySet()) {
			for (OBDAMappingAxiom mapping : mappingTable.get(datasourceUri)) {
				CQIE tq = (CQIE)mapping.getTargetQuery();
				boolean bSuccess = validator.validate(tq);
				if (!bSuccess) {
					throw new Exception("Found an invalid target query: " + tq.toString());
				}
			}
		}
	}
}
