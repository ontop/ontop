package it.unibz.inf.ontop.protege.core;

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

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class QueryManager {

	private final List<EventListener> listeners = new ArrayList<>();
	
	public void addListener(EventListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	private final Map<String, Group> groups = new LinkedHashMap<>();

	public class Query {
		private final Group group;
		private final String id;
		private String query;

		private Query(Group group, String id, String query) {
			this.group = group;
			this.id = id;
			this.query = query;

			if (id.isEmpty())
				throw new IllegalArgumentException("The query ID can't be blank!");

			if (group.isDegenerate && group.queries.size() > 0)
				throw new IllegalArgumentException("A query is added to a degenerate group");

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
			listeners.forEach(l -> l.changed(this));
		}
	}

	public class Group {
		private final String id;
		private final boolean isDegenerate; // a single query whose ID is the group ID
		private final Map<String, Query> queries = new LinkedHashMap<>();

		private Group(String id) {
			this.id = id;
			this.isDegenerate = false;
			init();
		}

		/**
			degenerate group that consists of a single query (their IDs are the same)
		 */
		private Group(String id, String query) {
			this.id = id;
			this.isDegenerate = true;
			new Query(this, id, query);
			init();
		}

		private void init() {
			if (id.isEmpty())
				throw new IllegalArgumentException("The group ID can't be blank!");

			if (groups.containsKey(id))
				throw new IllegalArgumentException("Group with ID " + id + " already exists");
			groups.put(id, this);
		}

		public String getID() { return isDegenerate ? null : id; }
		public boolean isDegenerate() { return isDegenerate; }

		public Collection<Query> getQueries() { return queries.values(); }
	}

	public interface EventListener  {
		void added(Group group);
		void added(Query query);
		void removed(Group group);
		void removed(Query query);
		void changed(Query query);
	}



	public void addQuery(Group group, String queryId, String query) {
		Query q = new Query(group, queryId, query);
		listeners.forEach(l -> l.added(q));
	}

	public void addQuery(String queryId, String query) {
		Group g = new Group(queryId, query);
		Query q = g.getQueries().iterator().next();
		listeners.forEach(l -> l.added(q));
	}

	public Group addGroup(String groupId) {
		Group g = new Group(groupId);
		listeners.forEach(l -> l.added(g));
		return g;
	}

	public Group getGroup(String groupId) {
		Group g = groups.get(groupId);
		if (g == null)
			throw new IllegalArgumentException("Group " + groupId + " not found");

		return g;
	}

	public Query getQuery(@Nullable String groupId, String queryId) {
		if (groupId == null) {
			Group group = groups.get(queryId);
			if (group == null || !group.isDegenerate())
				throw new IllegalArgumentException("Query with ID " + queryId + " not found");

			return group.getQueries().iterator().next();
		}
		else {
			Group group = getGroup(groupId);
			if (group.isDegenerate())
				throw new IllegalArgumentException("Incorrect use of degenerate groups");

			Query query = group.queries.get(queryId);
			if (query == null)
				throw new IllegalArgumentException("Query with ID " + queryId + " not found in group " + group.id);

			return query;
		}
	}

	public void removeQuery(@Nullable String groupId, String queryId) {
		Query query;
		if (groupId == null) {
			Group group = groups.remove(queryId);
			if (group == null || !group.isDegenerate())
				throw new IllegalArgumentException("Query with ID " + queryId + " not found");

			query = group.getQueries().iterator().next();
		}
		else {
			Group group = getGroup(groupId);
			if (group.isDegenerate())
				throw new IllegalArgumentException("Incorrect use of degenerate groups");

			query = group.queries.remove(queryId);
			if (query == null)
				throw new IllegalArgumentException("Query with ID " + queryId + " not found in group " + group.id);
		}
		listeners.forEach(l -> l.removed(query));
	}

	public void removeGroup(String groupId) {
		Group g = groups.remove(groupId);
		if (g == null)
			throw new IllegalArgumentException("Cannot find group: " + groupId);

		listeners.forEach(l -> l.removed(g));
	}

	public Collection<Group> getGroups() { return groups.values(); }




	private static final String QUERY_GROUP = "QueryGroup";
	private static final String QUERY_ITEM = "QueryItem";

	private static final String QUERY_GROUP_TAG = "[" + QUERY_GROUP + "=\"%s\"]";
	private static final String QUERY_ITEM_TAG = "[" + QUERY_ITEM + "=\"%s\"]";

	private static final String START_COLLECTION_SYMBOL = "@collection [[";
	private static final String END_COLLECTION_SYMBOL = "]]";

	private static final String COMMENT_SYMBOL = ";";

	/**
	 * The save/write operation.
	 */
	public String renderQueries()  {
		return groups.values().stream()
					.map(QueryManager::renderGroup)
					.collect(Collectors.joining("\n"));
	}

	private static String renderGroup(QueryManager.Group group) {
		if (!group.isDegenerate())
			return String.format(QUERY_GROUP_TAG, group.getID()) + " " + START_COLLECTION_SYMBOL + "\n"
					+ group.getQueries().stream()
					.map(QueryManager::renderQuery)
					.collect(Collectors.joining("\n"))
					+ END_COLLECTION_SYMBOL + "\n";
		else
			return renderQuery(group.getQueries().iterator().next());
	}

	private static String renderQuery(QueryManager.Query query) {
		return String.format(QUERY_ITEM_TAG, query.getID()) + "\n"
				+ query.getQuery().trim() + "\n";
	}


	/**
	 * The load/write operation
	 *
	 * @param file
	 *          The target file object from which the saved queries are loaded.
	 * @throws IOException
	 */
	public void load(FileReader file) throws IOException {
		// Clean the controller first before loading
		groups.clear();

		LineNumberReader lineNumberReader = new LineNumberReader(file);
		try {
			String line;
			while ((line = lineNumberReader.readLine()) != null) {
				if (isNotACommentOrEmptyLine(line)) {
					if (line.contains(QUERY_GROUP))
						readGroup(lineNumberReader, getID(line));
					else if (line.contains(QUERY_ITEM))
						readQuery(lineNumberReader, null, getID(line));
					else
						throw new IOException("Expected a group or query tag");
				}
			}
		}
		catch (Exception e) {
			throw new IOException(String.format("Invalid syntax at line: %s", lineNumberReader.getLineNumber()), e);
		}
	}

	private void readGroup(LineNumberReader lineNumberReader, String groupId) throws IOException {
		String line;
		while (!(line = lineNumberReader.readLine()).equals(END_COLLECTION_SYMBOL)) {
			if (isNotACommentOrEmptyLine(line)) {
				if (line.contains(QUERY_ITEM))
					readQuery(lineNumberReader, groupId, getID(line));
				else
					throw new IOException("Unexpected a query tag");
			}
		}
	}

	private void readQuery(LineNumberReader lineNumberReader, String groupId, String queryId) throws IOException {
		if (queryId.isEmpty())
			throw new IOException("Query ID is missing");

		StringBuilder buffer = new StringBuilder();
		String line;
		while ((line = lineNumberReader.readLine()) != null) {
			if (line.contains(QUERY_ITEM)
					|| line.contains(QUERY_GROUP)
					|| line.contains(END_COLLECTION_SYMBOL))
				break;

			lineNumberReader.mark(100000);
			buffer.append(line).append("\n");
		}
		lineNumberReader.reset(); // rewind back to the start of the last line
		String queryText = buffer.toString();

		if (groupId == null)
			addQuery(queryId, queryText);
		else {
			Group group = groups.get(groupId);
			if (group == null) {
				Group newGroup = addGroup(groupId);
				addQuery(newGroup, queryId, queryText);
			}
			else
				addQuery(group, queryId, queryText);
		}
	}

	private static boolean isNotACommentOrEmptyLine(String line) {
		return !line.isEmpty()
				// A comment line is always started by semi-colon (after spaces)
				&& !(line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0);
	}

	private static String getID(String line) {
		// IDs are enclosed by a double quotation marks
		return line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
	}
}
