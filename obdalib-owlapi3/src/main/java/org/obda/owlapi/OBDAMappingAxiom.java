package org.obda.owlapi;

import java.util.List;

public interface OBDAMappingAxiom {

	public String getSQLQuery();

	public String getID();

	public List<? extends OBDAAssertionTemplate> assertionTemplates();
}
