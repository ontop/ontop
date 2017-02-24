package it.unibz.inf.ontop.io;

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
import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.SQLMappingFactoryImpl;
import org.eclipse.rdf4j.model.Model;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.mapping.SQLMappingParser;

import it.unibz.inf.ontop.parser.TargetQueryParser;
import it.unibz.inf.ontop.parser.TargetQueryParserException;
import it.unibz.inf.ontop.parser.TurtleOBDASyntaxParser;
import it.unibz.inf.ontop.parser.UnparsableTargetQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.exception.InvalidMappingExceptionWithIndicator.*;

/**
 * Mapping parser specific to the Ontop Native Mapping Language for SQL.
 *
 * Available through Guice.
 *
 */
public class OntopNativeMappingParser implements SQLMappingParser {

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

    private static final SQLMappingFactory SQL_MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(OntopNativeMappingParser.class);

    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;
    private final MappingFactory mappingFactory;

    /**
     * Create an SQL Mapping Parser for generating an OBDA model.
     */
    @Inject
    private OntopNativeMappingParser(NativeQueryLanguageComponentFactory nativeQLFactory,
                                     MappingFactory mappingFactory,
                                     OBDAFactoryWithException obdaFactory) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.mappingFactory = mappingFactory;
    }

    /**
     * Parsing is not done at construction time because our dependency
     * injection framework (Guice) does not manage exceptions nicely.
     *
     */
    @Override
    public OBDAModel parse(File file) throws InvalidMappingException, DuplicateMappingException, MappingIOException {
        checkFile(file);
        try (Reader reader = new FileReader(file)) {
            return load(reader, mappingFactory, nativeQLFactory, obdaFactory, file.getName());
        } catch (IOException e) {
            throw new MappingIOException(e);
        }
    }

    @Override
    public OBDAModel parse(Reader reader) throws InvalidMappingException, DuplicateMappingException, MappingIOException {
        return load(reader, mappingFactory, nativeQLFactory, obdaFactory, ".obda file");
    }

    @Override
    public OBDAModel parse(Model mappingGraph) throws InvalidMappingException, DuplicateMappingException {
        throw new IllegalArgumentException("The Ontop native mapping language has no RDF serialization. Passing a RDF graph" +
                "to the OntopNativeMappingParser is thus invalid.");
    }


    private static void checkFile(File file) throws MappingIOException {
        if (!file.exists()) {
            throw new MappingIOException("WARNING: Cannot locate OBDA file at: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new MappingIOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
    }

    /**
     *
     * File may be null.
     *
     * TODO: refactor it. Way too complex.
     */
	private static OBDAModel load(Reader reader, MappingFactory mappingFactory,
                                  NativeQueryLanguageComponentFactory nativeQLFactory,
                                  OBDAFactoryWithException obdaFactory, String fileName)
            throws MappingIOException, InvalidMappingExceptionWithIndicator, DuplicateMappingException {

        final Map<String, String> prefixes = new HashMap<>();
        final List<OBDAMappingAxiom> mappings = new ArrayList<>();
        final List<Indicator> invalidMappingIndicators = new ArrayList<>();

        List<TargetQueryParser> parsers = null;
		
		String line;

        try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {

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
                        // This exception wil be rethrown
                        throw new RuntimeException("Source declaration is not supported anymore (since 3.0). " +
                                "Please give this information with the Ontop configuration.");
                    } else if (line.contains(MAPPING_DECLARATION_TAG)) {
                        if (parsers == null) {
                            parsers = createParsers(ImmutableMap.copyOf(prefixes));
                        }
                        mappings.addAll(readMappingDeclaration(lineNumberReader, parsers, invalidMappingIndicators,
                                nativeQLFactory));
                    } else {
                        throw new IOException("Unknown syntax: " + line);
                    }
                } catch (Exception e) {
                    throw new IOException(String.format("ERROR reading %s at line: %s", fileName,
                            lineNumberReader.getLineNumber()
                                    + " \nMESSAGE: " + e.getMessage()), e);
                }
            }
        } catch (IOException e) {
            throw new MappingIOException(e);
        }

        // Throw some validation exceptions
        if (!invalidMappingIndicators.isEmpty()) {
            throw new InvalidMappingExceptionWithIndicator(invalidMappingIndicators);
        }

        PrefixManager prefixManager = mappingFactory.create(ImmutableMap.copyOf(prefixes));
        ImmutableList<OBDAMappingAxiom> mappingAxioms = ImmutableList.copyOf(mappings);

        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                mappingAxioms.stream()
                        .flatMap(ax -> ax.getTargetQuery().stream())
                        .flatMap(atom -> atom.getTerms().stream())
                        .filter(t -> t instanceof Function)
                        .map(t -> (Function) t));

        MappingMetadata metadata = mappingFactory.create(prefixManager, uriTemplateMatcher);
        return obdaFactory.createOBDAModel(mappingAxioms, metadata);
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
     * TODO: describe
     * TODO: follow the advice of IntelliJ: split this method to make its workflow tractable.
     * @param reader
     * @param invalidMappingIndicators Read-write list of error indicators.
     * @return The updated mapping set of the current source
     * @throws IOException
     */
    private static List<OBDAMappingAxiom> readMappingDeclaration(LineNumberReader reader,
                                                                 List<TargetQueryParser> parsers,
                                                                 List<Indicator> invalidMappingIndicators,
                                                                 NativeQueryLanguageComponentFactory nativeQLFactory)
            throws IOException {
        List<OBDAMappingAxiom> currentSourceMappings = new ArrayList<>();

        String mappingId = "";
        String currentLabel = ""; // the reader is working on which label
        StringBuffer sourceQuery = null;
        List<Function> targetQuery = null;
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
                                addNewMapping(mappingId, sourceQuery.toString(), targetQuery, currentSourceMappings,
                                        nativeQLFactory);
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
                    currentSourceMappings, nativeQLFactory);
        }

        return currentSourceMappings;
    }

	private static List<Function> loadTargetQuery(String targetString,
                                        List<TargetQueryParser> parsers) throws UnparsableTargetQueryException {
        Map<TargetQueryParser, TargetQueryParserException> exceptions = new HashMap<>();
		for (TargetQueryParser parser : parsers) {
            try {
            	List<Function> parse = parser.parse(targetString);
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

    private static List<OBDAMappingAxiom> addNewMapping(String mappingId, String sourceQuery, List<Function> targetQuery,
                                                        List<OBDAMappingAxiom> currentSourceMappings,
                                                        NativeQueryLanguageComponentFactory nativeQLFactory) {
        OBDAMappingAxiom mapping = nativeQLFactory.create(mappingId, SQL_MAPPING_FACTORY.getSQLQuery(sourceQuery), targetQuery);
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
