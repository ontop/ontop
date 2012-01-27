package it.unibz.krdb.obda.owlapi3;

import java.util.Properties;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public interface OBDAOWLReasonerFactory extends OWLReasonerFactory {

//	public abstract void setOBDAController(OBDAModel controller);

	public abstract void setPreferenceHolder(Properties preference);

//	public OBDAOWLReasoner createReasoner(OWLOntologyManager manager);

}