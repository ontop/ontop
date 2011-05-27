package inf.unibz.it.obda.queryanswering;

import inf.unibz.it.obda.model.QueryControllerEntity;

import java.util.Vector;

public class QueryControllerGroup extends QueryControllerEntity {

	
	private Vector<QueryControllerQuery>	queries		= null;
	private String						    group_id	= "";
    
public QueryControllerGroup(String group_id) {
		this.setID(group_id);
		queries = new Vector<QueryControllerQuery>();
}



public void setID(String group_id) {
	this.group_id = group_id;
}

public String getID() {
	return group_id;
}
/**
 * Search a query in case it is found, it is removed and returns the object query  else returns null
 * @param query_id
 * @return
 */
public QueryControllerQuery removeQuery(String query_id) {
	for (QueryControllerQuery query : queries) {
		if (query.getID().equals(query_id)) {
			queries.remove(query);
			return query;
		}
	}
	return null;
}
/**
 * Return all queries of the vector QueryControllerQuery
 * @return
 */
public Vector<QueryControllerQuery> getQueries() {
	return queries;
}
/**
 * Search a query with the given id and returns the object query else returns null
 * @param id
 * @return
 */
public QueryControllerQuery getQuery(String id) {
	for (QueryControllerQuery query : queries) {
		if (query.getID().equals(id)) {
			return query;
		}
	}
	return null;
}

/**
 * Adds a new query into QueryControllerQuery's vector
 * @param query
 */
public void addQuery(QueryControllerQuery query) {
	queries.add(query);
	
}
/**
 * Removes a query with the given id into QueryControllerQuery's vector
 * @param query
 */
public void removeQuery(QueryControllerQuery query) {
	queries.remove(query);
}



@Override
public String getNodeName() {
	// TODO Auto-generated method stub
	return null;
}





}
