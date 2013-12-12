package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;

public abstract class AbstractOBDAMappingAxiom implements OBDAMappingAxiom {

	private static final long serialVersionUID = 5512895151633505075L;

	private String id = "";
	
	public AbstractOBDAMappingAxiom(String id) {
		this.id = id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public abstract Object clone();
}
