package it.unibz.krdb.obda.model;

public interface OBDAQueryReasoner {

	public OBDAStatement getStatement() throws Exception;

	public OBDAConnection getConnection() throws OBDAException;	
}
