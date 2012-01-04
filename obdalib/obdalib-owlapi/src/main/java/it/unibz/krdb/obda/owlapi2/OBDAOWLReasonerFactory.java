package it.unibz.krdb.obda.owlapi2;

import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.OWLOntologyManager;

public interface OBDAOWLReasonerFactory extends OWLReasonerFactory {

//	public abstract void setOBDAController(OBDAModel controller);

	public abstract void setPreferenceHolder(ReformulationPlatformPreferences preference);

	public OBDAOWLReasoner createReasoner(OWLOntologyManager manager);

}