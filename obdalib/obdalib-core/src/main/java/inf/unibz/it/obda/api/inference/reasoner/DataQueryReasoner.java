package inf.unibz.it.obda.api.inference.reasoner;

import inf.unibz.it.obda.queryanswering.Statement;

public interface DataQueryReasoner {

	public Statement getStatement() throws Exception;
	
	public void startProgressMonitor(String msg);
	
	public void finishProgressMonitor();

}
