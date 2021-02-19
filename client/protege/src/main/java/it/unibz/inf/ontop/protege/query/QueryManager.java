package it.unibz.inf.ontop.protege.query;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class QueryManager {

	private final Item root = new Item(null, "Queries", null);

	public Item getRoot() {
		return root;
	}

	public class Item {
		@Nullable
		private final Item parent;
		private final List<Item> children = new ArrayList<>();
		@Nonnull
		private String id;
		@Nullable
		private String queryString;

		private Item(@Nullable Item parent, @Nonnull String id, @Nullable  String queryString) {
			this.parent = parent;
			this.id = id;
			this.queryString = queryString;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Item) {
				Item other = (Item)obj;
				return this.parent == other.parent && this.id.equals(other.id);
			}
			return false;
		}

		@Override
		public String toString() {
			return "QueryManager.Item " + id + " > " + children;
		}

		public Item addGroupChild(String id) {
			return addChild(id, null);
		}

		public Item addQueryChild(String id, String queryString) {
			if (queryString == null)
				throw new IllegalArgumentException("Query string cannot be null in a query.");

			return addChild(id, queryString);
		}

		private Item addChild(String id, @Nullable String queryString) {
			if (id == null || id.isEmpty())
				throw new IllegalArgumentException("The query ID cannot be null or blank.");

			if (isQuery())
				throw new IllegalArgumentException("Cannot add entities to a query.");

			Item item = new Item(this, id, queryString);

			if (children.contains(item))
				throw new IllegalArgumentException("The parent group already contains this group / query.");

			children.add(item);
			listeners.forEach(l -> l.inserted(item, children.size() - 1));
			return item;
		}

		public void removeChild(Item item) {
			int indexInParent = children.indexOf(item);
			if (indexInParent == -1)
				throw new IllegalArgumentException("Cannot find the child");

			children.remove(item);
			listeners.forEach(l -> l.removed(item, indexInParent));
		}

		public String getID() { return id; }

		public Item getParent() { return parent; }

		public boolean isQuery() { return queryString != null; }

		public String getQueryString() { return queryString; }

		public void setQueryString(String queryString) {
			if (queryString == null)
				throw new IllegalArgumentException("The query string cannot be null in a query.");

			if (!children.isEmpty() || parent == null)
				throw new IllegalArgumentException("Cannot set a query string for a group / root.");

			this.queryString = queryString;
			//listeners.forEach(l -> l.changed(this, parent.children.indexOf(this)));
		}

		public Optional<Item> getChild(String id) {
			for (Item child : children)
				if (child.getID().equals(id))
					return Optional.of(child);

			return Optional.empty();
		}

		public int getIndexOfChild(Item child) {
			return children.indexOf(child);
		}

		public Item getChild(int index) {
			return children.get(index);
		}

		public int getChildCount() { return children.size(); }

		public List<Item> getChildren() { return Collections.unmodifiableList(children); }
	}


	private final List<QueryManagerEventListener> listeners = new ArrayList<>();

	public void addListener(QueryManagerEventListener listener) {
		if (listener != null && !listeners.contains(listener))
			listeners.add(listener);
	}



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
		return renderItem(root);
	}

	private static String renderItem(Item item) {
		return item.isQuery()
				? String.format(QUERY_ITEM_TAG, item.getID()) + "\n"
					+ item.getQueryString().trim() + "\n"
				: String.format(QUERY_GROUP_TAG, item.getID()) + " " + START_COLLECTION_SYMBOL + "\n"
					+ item.children.stream()
						.map(QueryManager::renderItem)
						.collect(Collectors.joining("\n"))
					+ END_COLLECTION_SYMBOL + "\n";
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
		root.children.clear();

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
						throw new IOException("Expected group or query tag");
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
					throw new IOException("Unexpected query tag");
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
		String queryString = buffer.toString();

		Item group = (groupId == null)
				? root
				: root.getChild(groupId).orElseGet(() -> root.addGroupChild(groupId));

		group.addQueryChild(queryId, queryString);
	}

	private static boolean isNotACommentOrEmptyLine(String line) {
		return !line.isEmpty()
				// A comment line is always started by a semi-colon (after spaces)
				&& !(line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0);
	}

	private static String getID(String line) {
		// IDs are enclosed in double quotation marks
		return line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
	}
}
