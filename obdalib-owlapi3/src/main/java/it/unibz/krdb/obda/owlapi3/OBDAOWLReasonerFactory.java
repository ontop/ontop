/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlapi3;

import java.util.Properties;

import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public interface OBDAOWLReasonerFactory extends OWLReasonerFactory {

//	public abstract void setOBDAController(OBDAModel controller);

	public abstract void setPreferenceHolder(Properties preference);

//	public OBDAOWLReasoner createReasoner(OWLOntologyManager manager);

}
