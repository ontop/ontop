package it.unibz.krdb.obda.querymanager;

import java.util.Vector;

public class QueryControllerGroup extends QueryControllerEntity {

  private Vector<QueryControllerQuery> queries = null;
  private String group_id = "";

  public QueryControllerGroup(String group_id) {
    this.setID(group_id);
    queries = new Vector<QueryControllerQuery>();
  }

  public void setID(String group_id) {
    this.group_id = group_id;
  }

  @Override
  public String getID() {
    return group_id;
  }

  /**
   * Search a query in case it is found, it is removed and returns the object
   * query else returns null
   *
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
   *
   * @return
   */
  public Vector<QueryControllerQuery> getQueries() {
    return queries;
  }

  /**
   * Search a query with the given id and returns the object query else returns
   * null
   *
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
   *
   * @param query
   */
  public void addQuery(QueryControllerQuery query) {
    queries.add(query);

  }

  /**
   * Removes a query with the given id into QueryControllerQuery's vector
   *
   * @param query
   */
  public void removeQuery(QueryControllerQuery query) {
    queries.remove(query);
  }

  /**
   * Updates the existing query.
   *
   * @param query
   */
  public void updateQuery(QueryControllerQuery query) {
    int position = getElementPosition(query.getID());
    queries.set(position, query);
  }

  public int getElementPosition(String id) {
    int index = -1;
    for (int i = 0; i < queries.size(); i++) {
      QueryControllerEntity element = queries.get(i);
      QueryControllerQuery query = (QueryControllerQuery) element;
      if (query.getID().equals(id)) {
        index = i;
      }
    }
    return index;
  }

  @Override
  public String getNodeName() {
    return null;
  }
}
