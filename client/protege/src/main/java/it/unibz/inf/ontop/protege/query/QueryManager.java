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

import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryManager.class);

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
			this.queryString = queryString == null ? null : queryString.trim();
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
			listeners.fire(l -> l.inserted(item, children.size() - 1));
			return item;
		}

		public void removeChild(Item item) {
			int indexInParent = children.indexOf(item);
			if (indexInParent == -1)
				throw new IllegalArgumentException("Cannot find the child");

			children.remove(item);
			listeners.fire(l -> l.removed(item, indexInParent));
		}

		public String getID() { return id; }

		public void setID(String newId) {
			if (parent == null)
				throw new IllegalArgumentException("Cannot change the ID of the root");

			if (parent.getChild(newId).isPresent())
				throw new IllegalArgumentException("The parent group already contains this group / query.");

			this.id = newId;
			listeners.fire(l -> l.renamed(this, parent.children.indexOf(this)));
		}

		public Item getParent() { return parent; }

		public boolean isQuery() { return queryString != null; }

		public String getQueryString() { return queryString; }

		public void setQueryString(String queryString) {
			if (queryString == null)
				throw new IllegalArgumentException("The query string cannot be null in a query.");

			if (!children.isEmpty() || parent == null)
				throw new IllegalArgumentException("Cannot set a query string for a group / root.");

			this.queryString = queryString.trim();
			listeners.fire(l -> l.changed(this, parent.children.indexOf(this)));
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


	private final EventListenerList<QueryManagerListener> listeners = new EventListenerList<>();

	/**
	 * No need to remove listeners - this is handled by OBDAModelManager
	 * @param listener
	 */
	public void addListener(QueryManagerListener listener) {
		listeners.add(listener);
	}



	private static final String QUERY_GROUP = "QueryGroup";
	private static final String QUERY_ITEM = "QueryItem";

	private static final String QUERY_GROUP_TAG = "[" + QUERY_GROUP + "=\"%s\"]";
	private static final String QUERY_ITEM_TAG = "[" + QUERY_ITEM + "=\"%s\"]";

	private static final String START_COLLECTION_SYMBOL = "@collection [[";
	private static final String END_COLLECTION_SYMBOL = "]]";

	private static final String COMMENT_SYMBOL = ";";


	public void store(File queriesFile) throws IOException {
		DialogUtils.saveFileOrDeleteEmpty(root.getChildCount() == 0, queriesFile, file -> {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(root.getChildren().stream()
						.flatMap(QueryManager::renderItem)
						.collect(Collectors.joining("\n")));
			}
			catch (IOException e) {
				throw new IOException(String.format("Error while saving the SPARQL queries to the file located at %s.\n" +
						"Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
			}
		}, LOGGER);
	}

	private static Stream<String> renderItem(Item item) {
		return item.isQuery()
				? Stream.of(String.format(QUERY_ITEM_TAG, item.getID()), item.getQueryString())
				: Stream.concat(Stream.concat(
						Stream.of(String.format(QUERY_GROUP_TAG, item.getID()) + " " + START_COLLECTION_SYMBOL),
						item.children.stream()
							.flatMap(QueryManager::renderItem)),
					Stream.of(END_COLLECTION_SYMBOL));
	}

	public void clear() {
		root.children.clear();
	}

	/**
	 * Should not be called twice in a row without clear() in between.
	 */

	public void load(File queriesFile) throws IOException {
		if (queriesFile.exists()) {
			try (FileReader reader = new FileReader(queriesFile)) {
				LineNumberReader lineNumberReader = new LineNumberReader(reader);
				try {
					String lastLine = readGroup(lineNumberReader, root);
					if (lastLine != null)
						throw new IOException("Unexpected file contents");
				}
				catch (Exception e) {
					throw new IOException(String.format("Invalid syntax at line: %s", lineNumberReader.getLineNumber()), e);
				}
			}
			catch (IOException ex) {
				throw new IOException("Exception occurred while loading query document: " + queriesFile + "\n\n" + ex.getMessage());
			}
		}
	}

	private String readGroup(LineNumberReader lineNumberReader, Item group) throws IOException {
		String line;
		while (!isEndOfGroup(line = lineNumberReader.readLine())) {
			if (!isEmptyLineOrComment(line)) {
				if (line.contains(QUERY_GROUP)) {
					String groupId = extractItemId(line);
					Item subGroup = group.addGroupChild(groupId);
					String lastLine = readGroup(lineNumberReader, subGroup);
					if (lastLine == null)
						throw new IOException("Unexpected EOF");
				}
				else if (line.contains(QUERY_ITEM)) {
					String queryId = extractItemId(line);
					String queryString = readQueryString(lineNumberReader);
					group.addQueryChild(queryId, queryString);
				}
				else
					throw new IOException("Expected a group or a query tag");
			}
		}
		return line;
	}

	private String readQueryString(LineNumberReader lineNumberReader) throws IOException {
		StringBuilder buffer = new StringBuilder();
		String line;
		while (!isEndOfQuery(line = lineNumberReader.readLine())) {
			lineNumberReader.mark(100000);
			buffer.append(line).append("\n");
		}
		lineNumberReader.reset(); // rewind back to the start of the last line
		return buffer.toString();
	}

	private static boolean isEndOfGroup(String line) {
		return line == null || line.trim().startsWith(END_COLLECTION_SYMBOL);
	}

	private static boolean isEndOfQuery(String line) {
		return isEndOfGroup(line) || line.contains(QUERY_ITEM) || line.contains(QUERY_GROUP);
	}

	private static boolean isEmptyLineOrComment(String line) {
		return line.isEmpty()
				// A comment line is always started by a semi-colon (after spaces)
				|| (line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0);
	}

	private static String extractItemId(String line) {
		// IDs are enclosed in double quotation marks
		return line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
	}
}
