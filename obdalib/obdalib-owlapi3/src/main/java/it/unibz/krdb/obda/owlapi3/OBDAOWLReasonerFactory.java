package it.unibz.krdb.obda.owlapi3;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public interface OBDAOWLReasonerFactory extends OWLReasonerFactory {

//	public abstract void setOBDAController(OBDAModel controller);

	public abstract void setPreferenceHolder(QuestPreferences preference);

	public OBDAOWLReasoner createReasoner(OWLOntologyManager manager);

}