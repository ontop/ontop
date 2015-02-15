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

import java.io.*;
import java.net.URI;
import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.openrdf.model.Model;
import org.semanticweb.ontop.exception.*;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDAFactoryWithException;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.parser.TargetQueryParser;
import org.semanticweb.ontop.parser.TargetQueryParserException;
import org.semanticweb.ontop.parser.TurtleOBDASyntaxParser;
import org.semanticweb.ontop.parser.UnparsableTargetQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.semanticweb.ontop.exception.InvalidMappingExceptionWithIndicator.*;
import static org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants.*;

/**
 * Mapping parser specific to the Ontop Native Mapping Language for SQL.
 *
 * Available through a Guice-enabled factory.
 *
 */
public class OntopNativeMappingParser implements MappingParser {

    protected enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source
    }

    protected static final String PREFIX_DECLARATION_TAG = "[PrefixDeclaration]";
    protected static final String CLASS_DECLARATION_TAG = "[ClassDeclaration]";
    protected static final String OBJECT_PROPERTY_DECLARATION_TAG = "[ObjectPropertyDeclaration]";
    protected static final String DATA_PROPERTY_DECLARATION_TAG = "[DataPropertyDeclaration]";
    protected static final String SOURCE_DECLARATION_TAG = "[SourceDeclaration]";
    protected static final String MAPPING_DECLARATION_TAG = "[MappingDeclaration]";

    protected static final String START_COLLECTION_SYMBOL = "@collection [[";
    protected static final String END_COLLECTION_SYMBOL = "]]";
    protected static final String COMMENT_SYMBOL = ";";

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(OntopNativeMappingParser.class);

    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;

    private OBDAModel model;

    /**
     * Temporary (removed after parsing)
     */
    private Reader reader;
    private final File file;

    @AssistedInject
    private OntopNativeMappingParser(@Assisted Reader reader, NativeQueryLanguageComponentFactory nativeQLFactory,
                                     OBDAFactoryWithException obdaFactory) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.model = null;
        this.reader = reader;
        this.file = null;
    }
    
    /**
     * Create an SQL Mapping Parser for generating an OBDA model.
     */
    @AssistedInject
    private OntopNativeMappingParser(@Assisted File file, NativeQueryLanguageComponentFactory nativeQLFactory,
                                     OBDAFactoryWithException obdaFactory) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.model = null;
        this.file = file;
        this.reader = null;
    }

    /**
     * Not supported.
     */
    @AssistedInject
    private OntopNativeMappingParser(@Assisted File file, @Assisted OBDADataSource dataSource) {
        throw new IllegalArgumentException("Data sources must be configured instead the mapping file of" +
                "the Ontop native mapping language, not outside.");
    }

    /**
     * RDF graph argument is not supported. This constructor is required by the factory.
     */
    @AssistedInject
    private OntopNativeMappingParser(@Assisted Model mappingGraph, NativeQueryLanguageComponentFactory factory) {
        throw new IllegalArgumentException("The Ontop native mapping language has no RDF serialization. Passing a RDF graph" +
                "to the OntopNativeMappingParser is thus invalid.");
    }

    /**
     * Parsing is not done at construction time because our dependency
     * injection framework (Guice) does not manage exceptions nicely.
     *
     */
    @Override
    public OBDAModel getOBDAModel() throws InvalidMappingException, IOException, DuplicateMappingException {
        if (model == null) {
            this.model = load(reader, file, nativeQLFactory, obdaFactory);
            reader = null;
        }
        return model;
    }

    private static void checkFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("WARNING: Cannot locate OBDA file at: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new IOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
    }

    /**
     *
     * File may be null.
     *
     * TODO: refactor it. Way too complex.
     */
	private static OBDAModel load(Reader reader, File file, NativeQueryLanguageComponentFactory nativeQLFactory,
                                  OBDAFactoryWithException obdaFactory)
            throws IOException, InvalidMappingExceptionWithIndicator, DuplicateMappingException {

		/**
		 * File and reader are not supposed to be both initially defined.
		 */
        if (file != null) {
            checkFile(file);
            reader = new FileReader(file);
        }

        LineNumberReader lineNumberReader = new LineNumberReader(reader);

        final Map<String, String> prefixes = new HashMap<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();
        final Set<OBDADataSource> sources = new HashSet<>();
        final List<Indicator> invalidMappingIndicators = new ArrayList<>();

        List<TargetQueryParser> parsers = null;
		
		String line;
        OBDADataSource currentDataSource = null;
        List<OBDAMappingAxiom> currentSourceMappings = new ArrayList<>();

        while ((line = lineNumberReader.readLine()) != null) {
        	try {
	            if (isCommentLine(line) || line.isEmpty()) {
	                continue; // skip comment lines and blank lines
	            }
	            if (line.contains(PREFIX_DECLARATION_TAG)) {
	                prefixes.putAll(readPrefixDeclaration(lineNumberReader));

                    /*
                     * In case of late prefix declaration
                     */
                    if (parsers != null) {
                        parsers = createParsers(ImmutableMap.copyOf(prefixes));
                    }

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
                    if (currentDataSource != null) {
                        if (!currentSourceMappings.isEmpty()) {
                            mappingIndex.put(currentDataSource.getSourceID(), ImmutableList.copyOf(currentSourceMappings));
                            currentSourceMappings = new ArrayList<>();
                        }
                    }
	                currentDataSource = readSourceDeclaration(lineNumberReader);
                    if (!sources.contains(currentDataSource)) {
                        sources.add(currentDataSource);
                    }
                    else {
                        LOG.warn("Duplicated data source %s", currentDataSource.getSourceID());
                    }
	            } else if (line.contains(MAPPING_DECLARATION_TAG)) {
                    if (parsers == null) {
                        parsers = createParsers(ImmutableMap.copyOf(prefixes));
                    }
	                currentSourceMappings = readMappingDeclaration(lineNumberReader, currentSourceMappings, parsers,
                            invalidMappingIndicators);
	            } else {
	                throw new IOException("Unknown syntax: " + line);
	            }
	       	} catch (Exception e) {
                String fileName =  (file != null) ? file.getName() : ".obda file";

	        	throw new IOException(String.format("ERROR reading %s at line: %s", fileName,
                        lineNumberReader.getLineNumber()
                        + " \nMESSAGE: " + e.getMessage()), e);
	        }
        }
        reader.close();
        
        // Throw some validation exceptions
        if (!invalidMappingIndicators.isEmpty()) {
            throw new InvalidMappingExceptionWithIndicator(invalidMappingIndicators);
        }

        if (!currentSourceMappings.isEmpty()) {
            mappingIndex.put(currentDataSource.getSourceID(), ImmutableList.copyOf(currentSourceMappings));
        }

        PrefixManager prefixManager = nativeQLFactory.create(prefixes);
        OBDAModel model = obdaFactory.createOBDAModel(sources, mappingIndex, prefixManager);
        return model;
	}
    
    /*
     * Helper methods related to load file.
     */

    private static Map<String, String> readPrefixDeclaration(BufferedReader reader) throws IOException {
        Map<String, String> prefixes = new HashMap<>();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] tokens = line.split("[\t| ]+");
            prefixes.put(tokens[0], tokens[1]);
        }
        return prefixes;
    }

    /**
     * TODO: modernize this method (stop using the DataSource in a mutable way).
     */
    private static OBDADataSource readSourceDeclaration(LineNumberReader reader) throws IOException {
        String line;
        OBDADataSource dataSource = null;
        while (!(line = reader.readLine()).isEmpty()) {
            int lineNumber = reader.getLineNumber();
            String[] tokens = line.split("[\t| ]+", 2);
            
            final String parameter = tokens[0].trim();
            final String inputParameter = tokens[1].trim();
            if (parameter.equals(Label.sourceUri.name())) {
                URI sourceUri = URI.create(inputParameter);
                // TODO: use a modern factory instead
                dataSource = DATA_FACTORY.getDataSource(sourceUri);
            } else if (parameter.equals(Label.connectionUrl.name())) {
                dataSource.setParameter(DATABASE_URL, inputParameter);
            } else if (parameter.equals(Label.username.name())) {
                dataSource.setParameter(DATABASE_USERNAME, inputParameter);
            } else if (parameter.equals(Label.password.name())) {
                dataSource.setParameter(DATABASE_PASSWORD, inputParameter);
            } else if (parameter.equals(Label.driverClass.name())) {
                dataSource.setParameter(DATABASE_DRIVER, inputParameter);
            } else {
                String msg = String.format("Unknown parameter name \"%s\" at line: %d.", parameter, lineNumber);
                throw new IOException(msg);
            }
        }
        return dataSource;
    }

    /**
     * TODO: describe
     * TODO: follow the advice of IntelliJ: split this method to make its workflow tractable.
     * @param reader
     * @param currentSourceMappings
     * @param invalidMappingIndicators Read-write list of error indicators.
     * @return The updated mapping set of the current source
     * @throws IOException
     */
    private static List<OBDAMappingAxiom> readMappingDeclaration(LineNumberReader reader,
                                                                 List<OBDAMappingAxiom> currentSourceMappings,
                                                                 List<TargetQueryParser> parsers,
                                                                 List<Indicator> invalidMappingIndicators)
            throws IOException {
        String mappingId = "";
        String currentLabel = ""; // the reader is working on which label
        StringBuffer sourceQuery = null;
        CQIE targetQuery = null;
        int wsCount = 0;  // length of whitespace used as the separator
        boolean isMappingValid = true; // a flag to load the mapping to the model if valid
        
        String line;
        for(line = reader.readLine(); 
        		line != null && !line.trim().equals(END_COLLECTION_SYMBOL); 
        		line = reader.readLine()) {
            int lineNumber = reader.getLineNumber();
            if (line.isEmpty()) {
            	if (!mappingId.isEmpty()) {
	            	// Save the mapping to the model (if valid) at this point
	                if (isMappingValid) {
	                    currentSourceMappings =
                                addNewMapping(mappingId, sourceQuery.toString(), targetQuery, currentSourceMappings);
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
                    invalidMappingIndicators.add(new Indicator(lineNumber, Label.mappingId, MAPPING_ID_IS_BLANK));
                    isMappingValid = false;
                }
            } else if (currentLabel.equals(Label.target.name())) {
                String targetString = value;
                if (targetString.isEmpty()) { // empty or not
                    invalidMappingIndicators.add(new Indicator(lineNumber, mappingId, TARGET_QUERY_IS_BLANK));
                    isMappingValid = false;
                } else {
	                // Load the target query
	                targetQuery = loadTargetQuery(targetString, parsers);
                }
            } else if (currentLabel.equals(Label.source.name())) {
                String sourceString = value;
                if (sourceString.isEmpty()) { // empty or not
                    invalidMappingIndicators.add(new Indicator(lineNumber, mappingId, SOURCE_QUERY_IS_BLANK));
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
            currentSourceMappings = addNewMapping(mappingId, sourceQuery.toString(), targetQuery,
                    currentSourceMappings);
        }

        return currentSourceMappings;
    }

	private static CQIE loadTargetQuery(String targetString,
                                        List<TargetQueryParser> parsers) throws UnparsableTargetQueryException {
        Map<TargetQueryParser, TargetQueryParserException> exceptions = new HashMap<>();
		for (TargetQueryParser parser : parsers) {
            try {
            	CQIE parse = parser.parse(targetString);
				return parse;
            } catch (TargetQueryParserException e) {
            	exceptions.put(parser, e);
            }     
    	}
		throw new UnparsableTargetQueryException(exceptions);
    }

	private static int getSeparatorLength(String input, int beginIndex) {
		int count = 0;
		for (int i = beginIndex; i < input.length(); i++) {
			if (input.charAt(i) != '\u0009' || input.charAt(i) != '\t') { // a tab
				break;
			}
			count++;
		}
		return count;
	}

    private static List<OBDAMappingAxiom> addNewMapping(String mappingId, String sourceQuery, CQIE targetQuery,
                                                       List<OBDAMappingAxiom> currentSourceMappings) {
        OBDAMappingAxiom mapping = DATA_FACTORY.getRDBMSMappingAxiom(mappingId, sourceQuery, targetQuery);
        if (!currentSourceMappings.contains(mapping)) {
            currentSourceMappings.add(mapping);
        }
        else {
            LOG.warn("Duplicate mapping %s", mappingId);
        }
        return currentSourceMappings;
    }

    private static boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }

    private static List<TargetQueryParser> createParsers(Map<String, String> prefixes) {
        List<TargetQueryParser> parsers = new ArrayList<>();
        // TODO: consider using a factory instead.
        parsers.add(new TurtleOBDASyntaxParser(prefixes));
        return ImmutableList.copyOf(parsers);
    }
}
