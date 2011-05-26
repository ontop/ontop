package inf.unibz.it.obda.queryanswering;


public interface DataQueryReasoner {

	public Statement getStatement() throws Exception;
	
	public void startProgressMonitor(String msg);
	
	public void finishProgressMonitor();

}
