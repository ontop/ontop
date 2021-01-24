package it.unibz.inf.ontop.protege.core.querymanager;

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

import java.util.*;

/**
 * This class represents the controller for the query manager
 */
public class QueryController {

	private final List<EventListener> listeners = new ArrayList<>();
	
	public void addListener(EventListener listener) {
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}

	private final Map<String, Group> groups = new LinkedHashMap<>();

	public class Query {
		private final Group group;
		private final String id;
		private String query;

		private Query(Group group, String id, String query) {
			if (id.isEmpty())
				throw new IllegalArgumentException("The query ID can't be blank!");

			this.group = group;
			this.id = id;
			this.query = query;
			if (!group.isDegenerate && id.equals(group.id))
				throw new IllegalArgumentException("The group ID can't be the same as the query ID!");

			if (group.queries.containsKey(id))
				throw new IllegalArgumentException("The query ID already exists!");

			group.queries.put(id, this);
		}

		public Group getGroup() { return group; }
		public String getID() { return id; }
		public String getQuery() { return query; }

		public void setQuery(String query) {
			this.query = query;
			QueryController.this.listeners.forEach(l -> l.changed(this));
		}
	}

	public class Group {
		private final String id;
		private boolean isDegenerate; // a single query whose ID is the group ID
		private final Map<String, Query> queries = new LinkedHashMap<>();

		private Group(String id) {
			if (id.isEmpty())
				throw new IllegalArgumentException("The group ID can't be blank!");

			this.id = id;
			this.isDegenerate = false;
			QueryController.this.groups.put(id, this);
		}

		private Group(String id, String query) {
			this(id);
			this.isDegenerate = true;
			Query q = new Query(this, id, query);
		}

		public String getID() { return id; }
		public Query getQuery(String queryId) { return queries.get(queryId); }
		public Collection<Query> getQueries() { return queries.values(); }
		public boolean isDegenerate() { return isDegenerate; }
	}

	public interface EventListener  {
		void added(Group group);
		void added(Query query);
		void removed(Group group);
		void removed(Query query);
		void changed(Query query);
	}



	public void addQuery(Group group, String queryId, String query) {
		if (group.isDegenerate())
			throw new IllegalArgumentException("Internal error: a query is added to a degenerate group");

		Query q = new Query(group, queryId, query);
		listeners.forEach(l -> l.added(q));
	}

	public void addQuery(String queryId, String query) {
		if (groups.containsKey(queryId))
			throw new IllegalArgumentException("The query with ID " + queryId + " already exists");

		Query q = new Group(queryId, query).getQueries().iterator().next();
		listeners.forEach(l -> l.added(q));
	}

	public Group addGroup(String groupId) {
		if (groups.containsKey(groupId))
			throw new IllegalArgumentException("The group with ID " + groupId + " already exists");

		Group g = new Group(groupId);
		listeners.forEach(l -> l.added(g));
		return g;
	}

	public Group getGroup(String groupId) {
		return groups.get(groupId);
	}

	public Query getQuery(String queryId) {
		Group g = groups.get(queryId);
		if (g == null || !g.isDegenerate())
			throw new IllegalArgumentException("Query " + queryId + " not found");

		return g.getQueries().iterator().next();
	}

	public void removeQuery(String groupId, String queryId) {
		Query q;
		if (groupId == null) {
			Group group = groups.remove(queryId);
			if (group == null)
				throw new IllegalArgumentException("Cannot find query: " + queryId);

			q = group.getQueries().iterator().next();
		}
		else {
			Group group = groups.computeIfAbsent(groupId, id -> {
				throw new IllegalArgumentException("Cannot find group: " + groupId);
			});
			if (group.isDegenerate())
				throw new IllegalArgumentException();

			q = group.queries.remove(queryId);
			if (q == null)
				throw new IllegalArgumentException("Cannot find query: " + queryId);
		}
		listeners.forEach(l -> l.removed(q));
	}

	public void removeGroup(String groupId) {
		Group g = groups.remove(groupId);
		if (g == null)
			throw new IllegalArgumentException("Cannot find group: " + groupId);

		listeners.forEach(l -> l.removed(g));
	}

	public Collection<Group> getGroups() { return groups.values(); }

	void reset() {
        groups.clear();
	}
}
