package it.unibz.krdb.obda.model;


public interface SQLQuery extends Query {

	public SQLQuery clone();

	public QueryModifiers getQueryModifiers();

}