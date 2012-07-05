package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class QueryIOManager {

    private static final String QUERY_GROUP = "QueryGroup";
    private static final String QUERY_ITEM = "QueryItem";
    
    private static final String QUERY_GROUP_TAG = "[" + QUERY_GROUP + "=\"%s\"]";
    private static final String QUERY_ITEM_TAG = "[" + QUERY_ITEM + "=\"%s\"]";

    private static final String START_COLLECTION_SYMBOL = "@collection [[";
    private static final String END_COLLECTION_SYMBOL = "]]";

    private static final String COMMENT_SYMBOL = ";";
    
    private QueryController queryController;

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
     * @param fileLocation
     *          The target file location to which the queries are saved.
     * @throws IOException
     */
    public void save(String fileLocation) throws IOException {
        save(new File(fileLocation));
    }

    /**
     * The save/write operation.
     * 
     * @param file
     *          The target file object to which the queries are saved.
     * @throws IOException
     */
    public void save(File file) throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (QueryControllerEntity entity : queryController.getElements()) {
                if (entity instanceof QueryControllerGroup) {
                    writeQueryGroup((QueryControllerGroup) entity, writer);
                } else if (entity instanceof QueryControllerQuery) {
                    writeQueryItem((QueryControllerQuery) entity, writer);
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new IOException(String.format("Error while saving the queries to file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }
    

    /**
     * The load/write operation
     * 
     * @param fileLocation
     *          The target file location from which the saved queries are loaded.
     * @throws IOException
     */
    public void load(String fileLocation) throws IOException {
        load(new File(fileLocation));
    }

    /**
     * The load/write operation
     * 
     * @param file
     *          The target file object from which the saved queries are loaded.
     * @throws IOException
     */
    public void load(File file) throws IOException {
        if (!file.exists()) {
            // NO-OP: Users may not have the saved file
        }
        if (!file.canRead()) {
            throw new IOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }

        // Clean the controller first before loading
        queryController.reset();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        String groupId = "";
        String queryId = "";
        StringBuffer buffer = new StringBuffer();
        
        /* This flag is used to tell the query controller when to load the query item.
         * The system won't take into account line-breaks to start loading the query item 
         * since there is a possibility that users put line-breaks in their query string.
         * Instead, the system will load the query when it recognizes the next query group 
         * tag or the next query item tag or the closing collection tag has been reached 
         * by the file reader.
         */
        boolean hasUnsavedQuery = false;
        while ((line = reader.readLine()) != null) {
            if (isCommentLine(line)) {
                continue; // skip comment lines
            }
            if (line.contains(QUERY_GROUP)) {
                if (hasUnsavedQuery) {
                    String queryText = removeBlankLines(buffer.toString());
                    addQueryItem(queryText, queryId, groupId);
                    hasUnsavedQuery = false;
                    buffer = new StringBuffer();
                }
                // The group ID is enclosed by a double-quotes sign
                groupId = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
            } else if (line.contains(QUERY_ITEM)) {
                if (hasUnsavedQuery) {
                    String queryText = removeBlankLines(buffer.toString());
                    addQueryItem(queryText, queryId, groupId);
                    buffer = new StringBuffer();
                }
                // The query ID is enclosed by a double-quotes sign
                queryId = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
                hasUnsavedQuery = true;
            } else if (line.contains(END_COLLECTION_SYMBOL)) {
                if (hasUnsavedQuery) {
                    String queryText = removeBlankLines(buffer.toString());
                    addQueryItem(queryText, queryId, groupId);
                    hasUnsavedQuery = false;
                    groupId = ""; // reset the previous group id
                    buffer = new StringBuffer();
                }
            } else {
                buffer.append(line + "\n");
            }
        }
        if (hasUnsavedQuery) {
            String queryText = removeBlankLines(buffer.toString());
            addQueryItem(queryText, queryId, groupId);
        }
    }

    /*
     * Private helper methods.
     */

    private void writeQueryGroup(QueryControllerGroup group, BufferedWriter writer) throws IOException {
        writer.append("\n");
        writer.append(String.format(QUERY_GROUP_TAG, group.getID()) + " ");
        writer.append(START_COLLECTION_SYMBOL + "\n");
        
        boolean needLineBreak = false;
        for (QueryControllerQuery query : group.getQueries()) {
            if (needLineBreak) {
                writer.write("\n");
            }
            writeQueryItem(query, writer);
            needLineBreak = true;
        }
        writer.append(END_COLLECTION_SYMBOL);
        writer.append("\n");
    }

    private void writeQueryItem(QueryControllerQuery query, BufferedWriter writer) throws IOException {
        writer.append(String.format(QUERY_ITEM_TAG, query.getID()) + "\n");
        writer.append(query.getQuery() + "\n");
    }

    private void addQueryItem(String queryText, String queryId, String groupId) {
        if (!groupId.isEmpty()) {
            queryController.addQuery(queryText, queryId, groupId);
        } else {
            queryController.addQuery(queryText, queryId);
        }
    }

    private String removeBlankLines(String input) {
        return input.replaceFirst("\\A\\n{1,}", "").replaceFirst("\\n{1,}\\z", "");
    }

    private boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }
}