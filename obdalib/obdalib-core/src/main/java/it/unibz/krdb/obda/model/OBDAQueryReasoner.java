package it.unibz.krdb.obda.model;


public interface OBDAQueryReasoner {

	public OBDAStatement getStatement() throws Exception;

	OBDAConnection getConnection() throws OBDAException;
		
}
