package it.unibz.krdb.obda.querymanager;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * This class represents the controller for the query manager
 */
public class QueryController implements Serializable {

	private static final long serialVersionUID = 6456175226053526128L;
	
	private List<QueryControllerEntity> entities;
	private List<QueryControllerListener> listeners;
	
	private boolean eventDisabled;

	public QueryController() {
		entities = new ArrayList<QueryControllerEntity>();
		listeners = new ArrayList<QueryControllerListener>();
	}

	public void addListener(QueryControllerListener listener) {
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}

	public void removeListener(QueryControllerListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}

	/**
	 * Creates a new group and adds it to the vector QueryControllerEntity
	 */
	public void createGroup(String groupId) throws Exception {
		if (getElementPosition(groupId) == -1) {
			QueryControllerGroup group = new QueryControllerGroup(groupId);
			entities.add(group);
			fireElementAdded(group);
		}
		else {
			throw new Exception("The name is already taken, either by a query group or a query item");
		}
	}

	/**
	 * Searches a group and returns the object else returns null
	 */
	public QueryControllerGroup getGroup(String groupId) {
		int index = getElementPosition(groupId);
		if (index == -1) {
		    return null;
		}
		QueryControllerGroup group = (QueryControllerGroup) entities.get(index);
		return group;
	}
	
	/**
	 * Returns all the groups added
	 */
	public List<QueryControllerGroup> getGroups() {
		List<QueryControllerGroup> groups = new ArrayList<QueryControllerGroup>();
		for (QueryControllerEntity element : entities) {
			if (element instanceof QueryControllerGroup) {
				groups.add((QueryControllerGroup) element);
			}
		}
		return groups;
	}
	
	/**
	 * Removes a group from the vector QueryControllerEntity
	 */
	public void removeGroup(String groupId) {
		for (Iterator<QueryControllerEntity> iterator = entities.iterator(); iterator.hasNext();) {
			Object temporal = iterator.next();
			if (!(temporal instanceof QueryControllerGroup)) {
				continue;
			}
			QueryControllerGroup element = (QueryControllerGroup) temporal;
			if (element instanceof QueryControllerGroup) {
				QueryControllerGroup group = element;
				if (group.getID().equals(groupId)) {
					entities.remove(group);
					fireElementRemoved(group);
					return;
				}
			}
		}
	}

	/**
	 * Creates a new query and adds it to the vector QueryControllerEntity.
	 *
	 * @param queryStr
	 *         The new query string, replacing any existing string.
	 * @param queryId
	 *         The query id associated to the string.
	 * @return The query object.
	 */
	public QueryControllerQuery addQuery(String queryStr, String queryId) {
		int position = getElementPosition(queryId);

		QueryControllerQuery query = new QueryControllerQuery(queryId);
		query.setQuery(queryStr);

		if (position == -1) { // add to the collection for a new query.
			entities.add(query);
			fireElementAdded(query);
		} else {
			entities.set(position, query);
			fireElementChanged(query);
		}
		return query;
	}

	/**
	 * Creates a new query into a group and adds it to the vector
	 * QueryControllerEntity
	 */
	public QueryControllerQuery addQuery(String queryStr, String queryId, String groupId) {
		int position = getElementPosition(queryId);

		QueryControllerQuery query = new QueryControllerQuery(queryId);
		query.setQuery(queryStr);
		QueryControllerGroup group = getGroup(groupId);
		if (group == null) {
		    group = new QueryControllerGroup(groupId);
            entities.add(group);
            fireElementAdded(group);
		}
		if (position == -1) {
			group.addQuery(query);
			fireElementAdded(query, group);
		} else {
			group.updateQuery(query);
			fireElementChanged(query, group);
		}
		return query;
	}
	
	/**
	 * Removes all the elements from the vector QueryControllerEntity
	 */
	public void removeAllQueriesAndGroups() {
		List<QueryControllerEntity> elements = getElements();
		for (QueryControllerEntity treeElement : elements) {
			fireElementRemoved(treeElement);
		}
		entities.clear();
	}

	/**
	 * Removes a query from the vector QueryControllerEntity
	 */
	public void removeQuery(String id) {
		int index = getElementPosition(id);
		QueryControllerEntity element = entities.get(index);

		if (element instanceof QueryControllerQuery) {
			entities.remove(index);
			fireElementRemoved(element);
			return;
		} else {
			QueryControllerGroup group = (QueryControllerGroup) element;
			Vector<QueryControllerQuery> queries_ingroup = group.getQueries();
			for (QueryControllerQuery query : queries_ingroup) {
				if (query.getID().equals(id)) {
					fireElementRemoved(group.removeQuery(query.getID()), group);
					return;
				}
			}
		}
	}

	/**
	 * Returns the index of the element in the vector. If its is a query and the
	 * query is found inside a query group. The position of the group is
	 * returned instead.
	 */
	public int getElementPosition(String element_id) {
		int index = -1;
		for (int i = 0; i < entities.size(); i++) {
			QueryControllerEntity element = entities.get(i);

			if (element instanceof QueryControllerQuery) {
				QueryControllerQuery query = (QueryControllerQuery) element;
				if (query.getID().equals(element_id)) {
					index = i;
					break;
				}
			}

			if (element instanceof QueryControllerGroup) {
				QueryControllerGroup group = (QueryControllerGroup) element;
				if (group.getID().equals(element_id)) {
					index = i;
					break;
				}
				else {
					// Searching inside the group.
					Vector<QueryControllerQuery> queries_ingroup = group.getQueries();
					for (QueryControllerQuery query : queries_ingroup) {
						if (query.getID().equals(element_id)) {
							index = i;
							break;
						}
					}
				}
			}
		}
		return index;
	}

	public List<QueryControllerEntity> getElements() {
		return entities;
	}

	public void fireElementAdded(QueryControllerEntity element) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementAdded(element);
			}
		}
	}

	public void fireElementAdded(QueryControllerQuery query, QueryControllerGroup group) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementAdded(query, group);
			}
		}
	}

	public void fireElementRemoved(QueryControllerEntity element) {
		if (element instanceof QueryControllerGroup || element instanceof QueryControllerQuery) {
			if (!eventDisabled) {
				for (QueryControllerListener listener : listeners) {
					listener.elementRemoved(element);
				}
			}
		}
	}

	public void fireElementRemoved(QueryControllerQuery query, QueryControllerGroup group) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementRemoved(query, group);
			}
		}
	}

	public void fireElementChanged(QueryControllerQuery query) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementChanged(query);
			}
		}
	}

	public void fireElementChanged(QueryControllerQuery query, QueryControllerGroup group) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementChanged(query, group);
			}
		}
	}

	public void setEventsDisabled(boolean value) {
		eventDisabled = value;
		return;
	}

	public boolean getEventsDisabled() {
		return eventDisabled;
	}
	
	public void reset() {
        entities = new ArrayList<QueryControllerEntity>();
	}
}
