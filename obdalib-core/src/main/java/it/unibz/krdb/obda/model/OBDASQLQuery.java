package it.unibz.krdb.obda.model;

public interface OBDASQLQuery extends OBDAQuery {

	public OBDASQLQuery clone();

	public OBDAQueryModifiers getQueryModifiers();
}