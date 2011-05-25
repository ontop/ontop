package inf.unibz.it.obda.queryanswering;

public interface Statement {

	public String getUnfolding(String query) throws Exception;

	public String getRewriting(String query) throws Exception;

	public QueryResultSet executeQuery(String query) throws Exception;

	public int getTupleCount(String query) throws Exception;

	public void close() throws Exception;

}
