package it.unibz.krdb.obda.protege4.gui.action;

public interface OBDADataQueryAction{
	
	public void run(String query);
	
	public long getExecutionTime();
	
	public int getNumberOfRows();
}
