package it.unibz.krdb.obda.owlapi2;

import java.util.Properties;

import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.OWLOntologyManager;

public interface OBDAOWLReasonerFactory extends OWLReasonerFactory {

//	public abstract void setOBDAController(OBDAModel controller);

	public abstract void setPreferenceHolder(Properties preference);

	public OBDAOWLReasoner createReasoner(OWLOntologyManager manager);

}