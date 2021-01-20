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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.stream.Collectors;

public class QueryIOManager {

    private static final String QUERY_GROUP = "QueryGroup";
    private static final String QUERY_ITEM = "QueryItem";
    
    private static final String QUERY_GROUP_TAG = "[" + QUERY_GROUP + "=\"%s\"]";
    private static final String QUERY_ITEM_TAG = "[" + QUERY_ITEM + "=\"%s\"]";

    private static final String START_COLLECTION_SYMBOL = "@collection [[";
    private static final String END_COLLECTION_SYMBOL = "]]";

    private static final String COMMENT_SYMBOL = ";";

    private final QueryController queryController;

    /**
     * Create an IO manager for saving/loading the user-defined queries
     * @param controller
     */
    public QueryIOManager(QueryController controller) {
        queryController = controller;
    }

    /**
     * The save/write operation.
     * 
     * @param file
     *          The target file object to which the queries are saved.
     * @throws IOException
     */
    public void save(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.append(queryController.getElements().stream()
                    .map(QueryIOManager::writeEntity)
                    .collect(Collectors.joining("\n")));
        }
        catch (IOException e) {
            throw new IOException(String.format("Error while saving the queries to file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }

    private static String writeEntity(QueryControllerEntity entity) {
        if (entity instanceof QueryControllerGroup) {
            return writeQueryGroup((QueryControllerGroup) entity);
        }
        else if (entity instanceof QueryControllerQuery) {
            return writeQueryItem((QueryControllerQuery) entity);
        }
        throw new IllegalArgumentException("Unexpected type");
    }

    /**
     * The load/write operation
     * 
     * @param file
     *          The target file object from which the saved queries are loaded.
     * @throws IOException
     */
    public void load(File file) throws IOException {
        // Clean the controller first before loading
        queryController.reset();

        if (!file.exists()) {
            System.out.println("FILE DOES NOT EXIST: " + file.getAbsolutePath());
            return; // NO-OP: Users may not have the saved file
        }
        if (!file.canRead()) {
            throw new IOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
        
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                try {
                    if (isCommentLine(line) || line.isEmpty()) {
                        continue; // skip comment lines and empty lines
                    }
                    if (line.contains(QUERY_GROUP)) {
                        // The group ID is enclosed by a double-quotes sign
                        String groupId = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                        readQueryGroup(reader, groupId);
                    } else if (line.contains(QUERY_ITEM)) {
                        // The query ID is enclosed by a double-quotes sign
                        String queryId = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                        readQueryContent(reader, "", queryId);
                    } else {
                        throw new IOException("Unknown syntax: " + line);
                    }
                }
                catch (Exception e) {
                    throw new IOException(String.format("Invalid syntax at line: %s", reader.getLineNumber()), e);
                }
            }
        }
    }

    /*
     * Private helper methods.
     */

    private void readQueryGroup(LineNumberReader reader, String groupId) throws IOException {
        String line = "";
        while (!(line = reader.readLine()).equals(END_COLLECTION_SYMBOL)) {
            if (isCommentLine(line) || line.isEmpty()) {
                continue; // skip comment lines
            }
            if (line.contains(QUERY_ITEM)) {
                // The query ID is enclosed by a double-quotes sign
                String queryId = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
                readQueryContent(reader, groupId, queryId);
            } else {
                throw new IOException(String.format("Invalid syntax at line: %s", reader.getLineNumber()));
            }
        }
    }

    private void readQueryContent(LineNumberReader reader, String groupId, String queryId) throws IOException {
        if (queryId.isEmpty()) {
           throw new IOException("Query ID is missing");
        }
        
        StringBuilder buffer = new StringBuilder();
        String line = reader.readLine();
        while (!(line==null||line.contains(QUERY_ITEM)||line.contains(QUERY_GROUP)||line.contains(END_COLLECTION_SYMBOL))) {
            reader.mark(100000);
            buffer.append(line).append("\n");
            line = reader.readLine();
        }
        reader.reset();
        
        String queryText = buffer.toString();
        if (!groupId.isEmpty()) {
            queryController.addQuery(queryText, queryId, groupId);
        } else {
            queryController.addQuery(queryText, queryId);
        }
    }

    private static String writeQueryGroup(QueryControllerGroup group) {
        return String.format(QUERY_GROUP_TAG, group.getID()) + " " + START_COLLECTION_SYMBOL + "\n"
                + group.getQueries().stream()
                    .map(QueryIOManager::writeQueryItem)
                    .collect(Collectors.joining("\n"))
                + END_COLLECTION_SYMBOL + "\n";
    }

    private static String writeQueryItem(QueryControllerQuery query) {
        return String.format(QUERY_ITEM_TAG, query.getID()) + "\n"
                + query.getQuery().trim() + "\n";
    }

    private boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }
}
