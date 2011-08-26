package it.unibz.krdb.obda.model;


public interface OBDAQueryReasoner {

	public OBDAStatement getStatement() throws Exception;
	
	public void startProgressMonitor(String msg);
	
	public void finishProgressMonitor();
	
}
