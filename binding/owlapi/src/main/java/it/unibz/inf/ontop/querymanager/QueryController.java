package it.unibz.inf.ontop.querymanager;

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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the controller for the query manager
 */
public class QueryController {

	private final List<QueryControllerEntity> entities = new ArrayList<>();
	private final List<QueryControllerListener> listeners = new ArrayList<>();
	
	public void addListener(QueryControllerListener listener) {
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}

	/**
	 * Returns all the groups added
	 */
	public List<QueryControllerGroup> getGroups() {
		List<QueryControllerGroup> groups = new ArrayList<>();
		for (QueryControllerEntity element : entities) {
			if (element instanceof QueryControllerGroup) {
				groups.add((QueryControllerGroup) element);
			}
		}
		return groups;
	}
	

	/**
	 * Creates a new query and adds it to the vector QueryControllerEntity.
	 *
	 * @param queryStr
	 *         The new query string, replacing any existing string.
	 * @param queryId
	 *         The query id associated to the string.
	 */
	public void addQuery(String queryStr, String queryId) {
		QueryControllerQuery query = new QueryControllerQuery(queryId, queryStr);

		int position = getElementPosition(queryId);
		if (position == -1) { // add to the collection for a new query.
			entities.add(query);
			listeners.forEach(l -> l.elementAdded(query));
		}
		else {
			entities.set(position, query);
			listeners.forEach(l -> l.elementChanged(query));
		}
	}

	/**
	 * Creates a new query in the group or updates the existing query
	 * Creates a group if necessary
	 */
	public void addQuery(String queryStr, String queryId, String groupId) {
		QueryControllerQuery query = new QueryControllerQuery(queryId, queryStr);

		int index = getElementPosition(groupId);
		QueryControllerGroup group;
		if (index != -1) {
			group = (QueryControllerGroup) entities.get(index);
		}
		else {
		    group = new QueryControllerGroup(groupId);
            entities.add(group);
			listeners.forEach(l -> l.elementAdded(group));
		}

		int position = getElementPosition(queryId);
		if (position == -1) {
			group.addQuery(query);
			listeners.forEach(l -> l.elementAdded(query, group));
		}
		else {
			int i = 0;
			for (QueryControllerQuery q : group.getQueries()) {
				if (q.getID().equals(queryId)) {
					group.updateQuery(i, query);
					listeners.forEach(l -> l.elementChanged(query, group));
					return;
				}
				i++;
			}
		}
	}

	/**
	 * Removes a query from the vector QueryControllerEntity
	 */
	public void removeElement(String id) {
		for (int i = 0; i < entities.size(); i++) {
			QueryControllerEntity element = entities.get(i);
			if (element.getID().equals(id)) {
				entities.remove(i);
				listeners.forEach(l -> l.elementRemoved(element));
				return;
			}
			else if (element instanceof QueryControllerGroup) {
				QueryControllerGroup group = (QueryControllerGroup) element;
				int position = 0;
				for (QueryControllerQuery query : group.getQueries()) {
					if (query.getID().equals(id)) {
						group.removeQuery(position);
						listeners.forEach(l -> l.elementRemoved(query, group));
						return;
					}
					position++;
				}
			}
		}
	}

	/**
	 * Returns the index of the element in the list.
	 * If it's is a query and the query is found inside a query group,
	 * then the position of the group is returned instead.
	 */
	public int getElementPosition(String id) {
		for (int i = 0; i < entities.size(); i++) {
			QueryControllerEntity element = entities.get(i);
			if (element.getID().equals(id)) {
				return i;
			}

			if (element instanceof QueryControllerGroup) {
				QueryControllerGroup group = (QueryControllerGroup) element;
				for (QueryControllerQuery query : group.getQueries()) {
					if (query.getID().equals(id)) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	public List<QueryControllerEntity> getElements() {
		return entities;
	}

	public void reset() {
        entities.clear();
	}
}
