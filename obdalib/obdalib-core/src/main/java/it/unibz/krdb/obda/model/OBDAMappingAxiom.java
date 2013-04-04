package it.unibz.krdb.obda.model;

import java.io.Serializable;

public interface OBDAMappingAxiom extends Cloneable, Serializable {

	public void setSourceQuery(OBDAQuery query);

	public OBDAQuery getSourceQuery();

	public void setTargetQuery(OBDAQuery query);

	public OBDAQuery getTargetQuery();

	public Object clone();

	public void setId(String id);

	public String getId();
}