package it.unibz.krdb.obda.model;

public interface OBDARDBMappingAxiom extends OBDAMappingAxiom {

	/**
	 * Set the source query for this mapping axiom.
	 * 
	 * @param query
	 *            a SQL Query object.
	 */
	public void setSourceQuery(OBDAQuery query);

	/**
	 * Set the target query for this mapping axiom.
	 * 
	 * @param query
	 *            a conjunctive query object;
	 */
	public void setTargetQuery(OBDAQuery query);

	public OBDASQLQuery getSourceQuery();

	public CQIE getTargetQuery();

	public OBDARDBMappingAxiom clone();
}