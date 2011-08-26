package it.unibz.krdb.obda.model;


public interface OBDARDBMappingAxiom extends OBDAMappingAxiom {

	/***
	 * @param query An RDBMSSQLQuery object.
	 */
	public void setSourceQuery(OBDAQuery query);

	/***
	 * @param query An OntologyQuery object;
	 */
	public void setTargetQuery(OBDAQuery query);

	public OBDASQLQuery getSourceQuery();

	public CQIE getTargetQuery();

	public OBDARDBMappingAxiom clone();

}