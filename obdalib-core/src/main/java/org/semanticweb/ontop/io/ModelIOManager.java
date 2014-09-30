package org.semanticweb.ontop.io;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.Indicator;
import org.semanticweb.ontop.exception.InvalidMappingExceptionWithIndicator;
import org.semanticweb.ontop.exception.InvalidPredicateDeclarationException;
import org.semanticweb.ontop.exception.UnsupportedTagException;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.parser.TargetQueryParser;
import org.semanticweb.ontop.parser.TargetQueryParserException;
import org.semanticweb.ontop.parser.TurtleOBDASyntaxParser;
import org.semanticweb.ontop.parser.TurtleSyntaxParser;
import org.semanticweb.ontop.parser.UnparsableTargetQueryException;
import org.semanticweb.ontop.renderer.SourceQueryRenderer;
import org.semanticweb.ontop.renderer.TargetQueryRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages saving and loading an OBDA file.
 */
public class ModelIOManager {

    private enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source
    }

    private static final String PREFIX_DECLARATION_TAG = "[PrefixDeclaration]";
    private static final String CLASS_DECLARATION_TAG = "[ClassDeclaration]";
    private static final String OBJECT_PROPERTY_DECLARATION_TAG = "[ObjectPropertyDeclaration]";
    private static final String DATA_PROPERTY_DECLARATION_TAG = "[DataPropertyDeclaration]";
    private static final String SOURCE_DECLARATION_TAG = "[SourceDeclaration]";
    private static final String MAPPING_DECLARATION_TAG = "[MappingDeclaration]";

    private static final String START_COLLECTION_SYMBOL = "@collection [[";
    private static final String END_COLLECTION_SYMBOL = "]]";
    private static final String COMMENT_SYMBOL = ";";
    
    private OBDAModel model;
    private PrefixManager prefixManager;
    private OBDADataFactory dataFactory;
    
    private List<Indicator> invalidMappingIndicators = new ArrayList<Indicator>();
    
    private List<TargetQueryParser> listOfParsers = new ArrayList<TargetQueryParser>();
    
    private static final Logger log = LoggerFactory.getLogger(ModelIOManager.class);
    
    /**
     * Create an IO manager for saving/loading the OBDA model.
     * 
     * @param model
     *          The target OBDA model.
     */
    public ModelIOManager(OBDAModel model) {
        this.model = model;
        prefixManager = model.getPrefixManager();
        dataFactory = model.getDataFactory();

        // Register available parsers for target query
        register(new TurtleOBDASyntaxParser(prefixManager));
        register(new TurtleSyntaxParser(prefixManager));
    }
    
    private void register(TargetQueryParser parser) {
    	listOfParsers.add(parser);
    }

    private List<TargetQueryParser> getParsers() {
    	return listOfParsers;
    }
    
    /**
     * The save/write operation.
     * 
     * @param fileLocation
     *          The target file location to which the model is saved.
     * @throws IOException
     */
    public void save(String fileLocation) throws IOException {
        save(new File(fileLocation));
    }

    /**
     * The save/write operation.
     * 
     * @param file
     *          The target file object to which the model is saved.
     * @throws IOException
     */
    public void save(File file) throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writePrefixDeclaration(writer);
            for (OBDADataSource source : model.getSources()) {
                writeSourceDeclaration(source, writer);
                writeMappingDeclaration(source, writer);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new IOException(String.format("Error while saving the OBDA model to file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }

    /**
     * The load/read operation.
     * 
     * @param fileLocation
     *          The target file location from which the model is loaded.
     * @throws IOException
     * @throws InvalidPredicateDeclarationException
     * @throws InvalidMappingExceptionWithIndicator 
     */
    public void load(String fileLocation) throws IOException, InvalidPredicateDeclarationException, InvalidMappingExceptionWithIndicator {
        load(new File(fileLocation));
    }
    
    /**
     * The load/read operation.
     * 
     * @param fileLocation
     *          The target file object from which the model is loaded.
     * @throws IOException
     * @throws InvalidPredicateDeclarationException
     * @throws InvalidMappingExceptionWithIndicator 
     */
    public void load(File file) throws IOException, InvalidMappingExceptionWithIndicator {
        if (!file.exists()) {
            log.warn("WARNING: Cannot locate OBDA file at: " + file.getPath());
            return;
        }
        if (!file.canRead()) {
            throw new IOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
        
        
        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        load(reader);
    }

    /**
     * load from an input stream
     * 
     * @param stream
     * @throws IOException
     * @throws InvalidMappingExceptionWithIndicator
     */
	public void load(InputStream stream) throws IOException, InvalidMappingExceptionWithIndicator {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(stream));
		load(reader);
	}

	private void load(LineNumberReader reader) throws IOException,
			InvalidMappingExceptionWithIndicator {
	    // Clean the model first before loading
        model.reset();
    
		
		String line = "";
        URI sourceUri = null;
        while ((line = reader.readLine()) != null) {
        	try {
	            if (isCommentLine(line) || line.isEmpty()) {
	                continue; // skip comment lines and blank lines
	            }
	            if (line.contains(PREFIX_DECLARATION_TAG)) {
	                readPrefixDeclaration(reader);
	            } else if (line.contains(CLASS_DECLARATION_TAG)) {
	            	// deprecated tag
	            	throw new UnsupportedTagException(CLASS_DECLARATION_TAG);
	            } else if (line.contains(OBJECT_PROPERTY_DECLARATION_TAG)) {
	            	// deprecated tag
	            	throw new UnsupportedTagException(OBJECT_PROPERTY_DECLARATION_TAG);
	            } else if (line.contains(DATA_PROPERTY_DECLARATION_TAG)) {
	            	// deprecated tag
	            	throw new UnsupportedTagException(DATA_PROPERTY_DECLARATION_TAG);
	            } else if (line.contains(SOURCE_DECLARATION_TAG)) {
	                sourceUri = readSourceDeclaration(reader);
	            } else if (line.contains(MAPPING_DECLARATION_TAG)) {
	                readMappingDeclaration(reader, sourceUri);
	            } else {
	                throw new IOException("Unknown syntax: " + line);
	            }
	       	} catch (Exception e) {
	        	throw new IOException(String.format("ERROR reading .obda file at line: %s", reader.getLineNumber() + " \nMESSAGE: " + e.getMessage()), e);
	        }
        }
        
        // Throw some validation exceptions
        if (!invalidMappingIndicators.isEmpty()) {
            throw new InvalidMappingExceptionWithIndicator(invalidMappingIndicators);
        }
	}

    /*
     * Helper methods related to save file.
     */

    private void writePrefixDeclaration(BufferedWriter writer) throws IOException {
        final Map<String, String> prefixMap = model.getPrefixManager().getPrefixMap();

        if (prefixMap.size() == 0) {
            return; // do nothing if there is no prefixes to write
        }

        writer.write(PREFIX_DECLARATION_TAG);
        writer.write("\n");
        for (String prefix : prefixMap.keySet()) {
            String uri = prefixMap.get(prefix);
            writer.write(prefix + (prefix.length() >= 9 ? "\t" : "\t\t") + uri + "\n");
        }
        writer.write("\n");
    }
    
    private void writeSourceDeclaration(OBDADataSource source, BufferedWriter writer) throws IOException {
        writer.write(SOURCE_DECLARATION_TAG);
        writer.write("\n");
        writer.write(Label.sourceUri.name() + "\t" + source.getSourceID() + "\n");
        writer.write(Label.connectionUrl.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_URL) + "\n");
        writer.write(Label.username.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME) + "\n");
        writer.write(Label.password.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD) + "\n");
        writer.write(Label.driverClass.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER) + "\n");
        writer.write("\n");
    }

    private void writeMappingDeclaration(OBDADataSource source, BufferedWriter writer) throws IOException {
        final URI sourceUri = source.getSourceID();

        writer.write(MAPPING_DECLARATION_TAG + " " + START_COLLECTION_SYMBOL);
        writer.write("\n");
        
        boolean needLineBreak = false;
        for (OBDAMappingAxiom mapping : model.getMappings(sourceUri)) {
            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(Label.mappingId.name() + "\t" + mapping.getId() + "\n");
            
            OBDAQuery targetQuery = mapping.getTargetQuery();
            writer.write(Label.target.name() + "\t\t" + printTargetQuery(targetQuery) + "\n");
            
            OBDAQuery sourceQuery = mapping.getSourceQuery();
            writer.write(Label.source.name() + "\t\t" + printSourceQuery(sourceQuery) + "\n");
            needLineBreak = true;
        }
        writer.write(END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private String printTargetQuery(OBDAQuery query) {
    	return TargetQueryRenderer.encode(query, prefixManager);
    }
    
    private String printSourceQuery(OBDAQuery query) {
    	String sourceString = SourceQueryRenderer.encode(query);
    	String toReturn = convertTabToSpaces(sourceString);
    	return toReturn.replaceAll("\n", "\n\t\t\t");
    }
    
    /*
     * Helper methods related to load file.
     */

    private void readPrefixDeclaration(BufferedReader reader) throws IOException {
        final PrefixManager pm = model.getPrefixManager();
        String line = "";
        while (!(line = reader.readLine()).isEmpty()) {
            String[] tokens = line.split("[\t| ]+");
            pm.addPrefix(tokens[0], tokens[1]);
        }
    }
    
    private URI readSourceDeclaration(LineNumberReader reader) throws IOException {
        String line = "";
        URI sourceUri = null;
        OBDADataSource datasource = null;
        while (!(line = reader.readLine()).isEmpty()) {
            int lineNumber = reader.getLineNumber();
            String[] tokens = line.split("[\t| ]+", 2);
            
            final String parameter = tokens[0].trim();
            final String inputParamter = tokens[1].trim();
            if (parameter.equals(Label.sourceUri.name())) {
                sourceUri = URI.create(inputParamter);
                // TODO: BAD CODE! The data source id should be part of the parameters!
                datasource = model.getDataFactory().getDataSource(sourceUri);
            } else if (parameter.equals(Label.connectionUrl.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, inputParamter);
            } else if (parameter.equals(Label.username.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, inputParamter);
            } else if (parameter.equals(Label.password.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, inputParamter);
            } else if (parameter.equals(Label.driverClass.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, inputParamter);
            } else {
                String msg = String.format("Unknown parameter name \"%s\" at line: %d.", parameter, lineNumber);
                throw new IOException(msg);
            }
        }
        // Save the source to the model.
        model.addSource(datasource);
        return sourceUri;
    }

    private void readMappingDeclaration(LineNumberReader reader, URI dataSourceUri) throws IOException {
        String mappingId = "";
        String currentLabel = ""; // the reader is working on which label
        StringBuffer sourceQuery = null;
        CQIE targetQuery = null;
        int wsCount = 0;  // length of whitespace used as the separator
        boolean isMappingValid = true; // a flag to load the mapping to the model if valid
        
        String line = "";
        for(line = reader.readLine(); 
        		line != null && !line.trim().equals(END_COLLECTION_SYMBOL); 
        		line = reader.readLine()) {
            int lineNumber = reader.getLineNumber();
            if (line.isEmpty()) {
            	if (!mappingId.isEmpty()) {
	            	// Save the mapping to the model (if valid) at this point
	                if (isMappingValid) {
	                    saveMapping(dataSourceUri, mappingId, sourceQuery.toString(), targetQuery);
	                    mappingId = "";
	                    sourceQuery = null;
	                    targetQuery = null;
	                }
            	}
            	isMappingValid = true;
            	continue;
            }

            if (isCommentLine(line)) {
            	continue; // skip the comment line
            }
            if (!isMappingValid) {
            	continue; // skip if the mapping is invalid
            }
            
            String[] tokens = line.split("[\t| ]+", 2);
            String label = tokens[0].trim();
            String value = tokens[1].trim();
            if (!label.isEmpty()) {
            	currentLabel = tokens[0];
            	wsCount = getSeparatorLength(line, currentLabel.length());
            } else {
            	if (currentLabel.equals(Label.source.name())) {
            		int beginIndex = wsCount + 1; // add one tab to replace the "source" label
            		value = line.substring(beginIndex, line.length());
            	}       	
            }
            
            if (currentLabel.equals(Label.mappingId.name())) {
                mappingId = value;
                if (mappingId.isEmpty()) { // empty or not
                    register(invalidMappingIndicators, new Indicator(lineNumber, Label.mappingId, InvalidMappingExceptionWithIndicator.MAPPING_ID_IS_BLANK));
                    isMappingValid = false;
                }
            } else if (currentLabel.equals(Label.target.name())) {
                String targetString = value;
                if (targetString.isEmpty()) { // empty or not
                    register(invalidMappingIndicators, new Indicator(lineNumber, mappingId, InvalidMappingExceptionWithIndicator.TARGET_QUERY_IS_BLANK));
                    isMappingValid = false;
                } else {
	                // Load the target query
	                targetQuery = loadTargetQuery(targetString);
                }
            } else if (currentLabel.equals(Label.source.name())) {
                String sourceString = value;
                if (sourceString.isEmpty()) { // empty or not
                    register(invalidMappingIndicators, new Indicator(lineNumber, mappingId, InvalidMappingExceptionWithIndicator.SOURCE_QUERY_IS_BLANK));
                    isMappingValid = false;
                } else {
	                // Build the source query string.
	                if (sourceQuery == null) {
	                	sourceQuery = new StringBuffer();
	                	sourceQuery.append(sourceString);
	                } else {
	                	sourceQuery.append("\n");
	                	sourceQuery.append(sourceString);
	                }
                }
            } else {
                String msg = String.format("Unknown parameter name \"%s\" at line: %d.", tokens[0], lineNumber);
                throw new IOException(msg);
            }
        }
        
        if (line == null) {
        	throw new IOException(String.format("End collection symbol %s is missing.", END_COLLECTION_SYMBOL));
        }
        
        // Save the last mapping entry to the model
        if (!mappingId.isEmpty() && isMappingValid) {
            saveMapping(dataSourceUri, mappingId, sourceQuery.toString(), targetQuery);
        }
    }

	private CQIE loadTargetQuery(String targetString) throws UnparsableTargetQueryException {
        Map<TargetQueryParser, TargetQueryParserException> exceptions = new HashMap<TargetQueryParser, TargetQueryParserException>();
		for (TargetQueryParser parser : getParsers()) {
            try {
            	CQIE parse = parser.parse(targetString);
				return parse;
            } catch (TargetQueryParserException e) {
            	exceptions.put(parser, e);
            }     
    	}
		throw new UnparsableTargetQueryException(exceptions);
    }

	private int getSeparatorLength(String input, int beginIndex) {
		int count = 0;
		for (int i = beginIndex; i < input.length(); i++) {
			if (input.charAt(i) != '\u0009' || input.charAt(i) != '\t') { // a tab
				break;
			}
			count++;
		}
		return count;
	}
	
	private String convertTabToSpaces(String input) {
		return input.replaceAll("\t", "   ");
	}

	private void register(List<Indicator> list, Indicator indicator) {
        list.add(indicator);
    }

    private void saveMapping(URI dataSourceUri, String mappingId, String sourceQuery, CQIE targetQuery) {
        try {
            OBDAMappingAxiom mapping = dataFactory.getRDBMSMappingAxiom(mappingId, sourceQuery, targetQuery);
            model.addMapping(dataSourceUri, mapping);
        } catch (DuplicateMappingException e) {
            // NO-OP: Ignore it as duplicates won't be loaded to the model
        }
    }

    private boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }
}
