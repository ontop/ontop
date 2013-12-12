package it.unibz.krdb.obda.querymanager;

public class QueryControllerQuery extends QueryControllerEntity{

	private static final long serialVersionUID = 3885574857162247553L;
	
	private String id = "";
	private String query = "";
	
	public QueryControllerQuery(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public String getNodeName() {
		return id + ": " + query.toString();
	}
	
	public String toString() {
		return getNodeName();
	}
}
