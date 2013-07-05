package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

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
            
            boolean needLineBreakForGroup = false;
            boolean needLineBreakForItem = false;
            for (QueryControllerEntity entity : queryController.getElements()) {
                if (entity instanceof QueryControllerGroup) {
                    if (needLineBreakForGroup) {
                        writer.write("\n");
                    }
                    writeQueryGroup((QueryControllerGroup) entity, writer);
                    needLineBreakForGroup = true;
                } else if (entity instanceof QueryControllerQuery) {
                    if (needLineBreakForItem) {
                        writer.write("\n");
                    }
                    writeQueryItem((QueryControllerQuery) entity, writer);
                    needLineBreakForItem = true;
                    needLineBreakForGroup = true;
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
            return; // NO-OP: Users may not have the saved file
        }
        if (!file.canRead()) {
            throw new IOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
        
        // Clean the controller first before loading
        queryController.reset();
        
        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        String line = "";
        while ((line = reader.readLine()) != null) {
        	try {
	            if (isCommentLine(line) || line.isEmpty()) {
	                continue; // skip comment lines and empty lines
	            }
	            if (line.contains(QUERY_GROUP)) {
	                // The group ID is enclosed by a double-quotes sign
	                String groupId = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
	                readQueryGroup(reader, groupId);
	            } else if (line.contains(QUERY_ITEM)) {
	                // The query ID is enclosed by a double-quotes sign
	                String queryId = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
	                readQueryContent(reader, "", queryId);
	            } else {
	            	throw new IOException("Unknown syntax: " + line);
	            }
        	} catch (Exception e) {
        		throw new IOException(String.format("Invalid syntax at line: %s", reader.getLineNumber()), e);
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
        
        StringBuffer buffer = new StringBuffer();
        String line = reader.readLine();
        while (!(line==null||line.contains(QUERY_ITEM)||line.contains(QUERY_GROUP)||line.contains(END_COLLECTION_SYMBOL))) {
            reader.mark(100000);
            buffer.append(line + "\n");
            line = reader.readLine();
        }
        reader.reset();
        
        String queryText = buffer.toString();
        addQueryItem(queryText, queryId, groupId);
    }

    private void writeQueryGroup(QueryControllerGroup group, BufferedWriter writer) throws IOException {
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
        writer.append(query.getQuery().trim() + "\n");
    }

    private void addQueryItem(String queryText, String queryId, String groupId) {
        if (!groupId.isEmpty()) {
            queryController.addQuery(queryText, queryId, groupId);
        } else {
            queryController.addQuery(queryText, queryId);
        }
    }

    private boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }
}