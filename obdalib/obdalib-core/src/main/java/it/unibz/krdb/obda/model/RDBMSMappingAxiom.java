package it.unibz.krdb.obda.model;


public interface RDBMSMappingAxiom extends OBDAMappingAxiom {

	/***
	 * @param query An RDBMSSQLQuery object.
	 */
	public void setSourceQuery(Query query);

	/***
	 * @param query An OntologyQuery object;
	 */
	public void setTargetQuery(Query query);

	public SQLQuery getSourceQuery();

	public CQIE getTargetQuery();

	public RDBMSMappingAxiom clone();

}