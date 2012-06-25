package it.unibz.krdb.obda.owlapi3.model;

import java.util.List;

public interface OWLOBDAMappingAxiom {

	
	public String getSQLQuery();
	
	public String getID();
	
	public List<? extends OWLOBDAAssertionTemplate> getAssertionTemplates();
}
