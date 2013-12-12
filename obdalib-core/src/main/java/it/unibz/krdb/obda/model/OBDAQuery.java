package it.unibz.krdb.obda.model;

import java.io.Serializable;

public interface OBDAQuery extends Serializable {

	public OBDAQueryModifiers getQueryModifiers();

	public void setQueryModifiers(OBDAQueryModifiers modifiers);

	public boolean hasModifiers();
}
