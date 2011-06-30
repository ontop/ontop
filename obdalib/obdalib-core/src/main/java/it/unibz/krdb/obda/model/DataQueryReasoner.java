package it.unibz.krdb.obda.model;


public interface DataQueryReasoner {

	public Statement getStatement() throws Exception;
	
	public void startProgressMonitor(String msg);
	
	public void finishProgressMonitor();
	
	public void loadOBDAModel(OBDAModel model);

}
