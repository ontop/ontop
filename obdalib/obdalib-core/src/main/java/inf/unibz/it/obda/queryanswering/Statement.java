package inf.unibz.it.obda.queryanswering;



public interface Statement {
	
	public String getUnfolding() throws Exception;
	public String getRewriting() throws Exception;
	public QueryResultSet getResultSet() throws Exception;
	public int getTupleCount() throws Exception;
	public void close()throws Exception;

}
