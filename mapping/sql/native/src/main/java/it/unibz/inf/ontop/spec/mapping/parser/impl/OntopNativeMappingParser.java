package it.unibz.inf.ontop.spec.mapping.parser.impl;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedTagException;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnparsableTargetQueryException;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import org.apache.commons.rdf.api.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.unibz.inf.ontop.exception.InvalidMappingExceptionWithIndicator.*;

/**
 * Mapping parser specific to the Ontop Native Mapping Language for SQL.
 *
 * Available through Guice.
 *
 */
public class OntopNativeMappingParser implements SQLMappingParser {

    public enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source
    }

    public static final String PREFIX_DECLARATION_TAG = "[PrefixDeclaration]";
    protected static final String CLASS_DECLARATION_TAG = "[ClassDeclaration]";
    protected static final String OBJECT_PROPERTY_DECLARATION_TAG = "[ObjectPropertyDeclaration]";
    protected static final String DATA_PROPERTY_DECLARATION_TAG = "[DataPropertyDeclaration]";
    protected static final String SOURCE_DECLARATION_TAG = "[SourceDeclaration]";
    public static final String MAPPING_DECLARATION_TAG = "[MappingDeclaration]";

    public static final String START_COLLECTION_SYMBOL = "@collection [[";
    public static final String END_COLLECTION_SYMBOL = "]]";
    protected static final String COMMENT_SYMBOL = ";";

    private static final SQLMappingFactory SQL_MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(OntopNativeMappingParser.class);

    private final TargetQueryParserFactory targetQueryParserFactory;
    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    /**
     * Create an SQL Mapping Parser for generating an OBDA model.
     */
    @Inject
    private OntopNativeMappingParser(SpecificationFactory specificationFactory,
                                     TargetQueryParserFactory targetQueryParserFactory,
                                     SQLPPMappingFactory ppMappingFactory, TermFactory termFactory,
                                     TypeFactory typeFactory) {
        this.targetQueryParserFactory = targetQueryParserFactory;
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
    }

    /**
     * Parsing is not done at construction time because our dependency
     * injection framework (Guice) does not manage exceptions nicely.
     *
     */
    @Override
    public SQLPPMapping parse(File file) throws InvalidMappingException, DuplicateMappingException, MappingIOException {
        checkFile(file);
        try (Reader reader = new FileReader(file)) {
            return load(reader, specificationFactory, ppMappingFactory, file.getName());
        } catch (IOException e) {
            throw new MappingIOException(e);
        }
    }

    @Override
    public SQLPPMapping parse(Reader reader) throws InvalidMappingException, DuplicateMappingException, MappingIOException {
        return load(reader, specificationFactory, ppMappingFactory, ".obda file");
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException, DuplicateMappingException {
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
	private SQLPPMapping load(Reader reader, SpecificationFactory specificationFactory,
                                     SQLPPMappingFactory ppMappingFactory, String fileName)
            throws MappingIOException, InvalidMappingExceptionWithIndicator, DuplicateMappingException {

        final Map<String, String> prefixes = new HashMap<>();
        final List<SQLPPTriplesMap> mappings = new ArrayList<>();
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
                        mappings.addAll(readMappingDeclaration(lineNumberReader, parsers, invalidMappingIndicators));
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

        PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.copyOf(prefixes));
        ImmutableList<SQLPPTriplesMap> mappingAxioms = ImmutableList.copyOf(mappings);

        return ppMappingFactory.createSQLPreProcessedMapping(mappingAxioms, prefixManager);
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
    private static List<SQLPPTriplesMap> readMappingDeclaration(LineNumberReader reader,
                                                                List<TargetQueryParser> parsers,
                                                                List<Indicator> invalidMappingIndicators)
            throws IOException {
        List<SQLPPTriplesMap> currentSourceMappings = new ArrayList<>();

        String mappingId = "";
        String currentLabel = ""; // the reader is working on which label
        StringBuffer sourceQuery = null;
        String targetString = null;
        ImmutableList<TargetAtom> targetQuery = null;
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
                                addNewMapping(mappingId, sourceQuery.toString(), targetString, targetQuery, currentSourceMappings);
	                    mappingId = "";
	                    sourceQuery = null;
	                    targetQuery = null;
	                }


            	}
            	else {
                    isMappingValid = true;
                }
            	continue;
            }

            if (isCommentLine(line)) {
            	continue; // skip the comment line
            }
            if (!isMappingValid) {
            	continue; // skip if the mapping is invalid
            }

            String[] tokens = line.split("[\t| ]+", 2);

            String label;
            String value;
            if(tokens.length > 1 ) {
                label = tokens[0].trim();
                value = tokens[1].trim();
            }
            else{
                value = tokens[0];
                label = "";
            }
            if (!label.isEmpty()) {
            	currentLabel = tokens[0];
            }

            if (currentLabel.equals(Label.mappingId.name())) {
                mappingId = value;
                if (mappingId.isEmpty()) { // empty or not
                    invalidMappingIndicators.add(new Indicator(lineNumber, Label.mappingId, MAPPING_ID_IS_BLANK));
                    isMappingValid = false;
                }
            } else if (currentLabel.equals(Label.target.name())) {
                targetString = value;
                if (targetString.isEmpty()) { // empty or not
                    invalidMappingIndicators.add(new Indicator(lineNumber, mappingId, TARGET_QUERY_IS_BLANK));
                    isMappingValid = false;
                } else {
	                // Load the target query
                    try {
                        targetQuery = loadTargetQuery(targetString, parsers);
                    } catch (UnparsableTargetQueryException e) {
                        invalidMappingIndicators.add(new Indicator(lineNumber, new String[] {mappingId, targetString, e.getMessage()},
                                ERROR_PARSING_TARGET_QUERY));
                        isMappingValid = false;
                    }
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
            currentSourceMappings = addNewMapping(mappingId, sourceQuery.toString(), targetString, targetQuery, currentSourceMappings);
        }

        return currentSourceMappings;
    }

	private static ImmutableList<TargetAtom> loadTargetQuery(String targetString,
                                        List<TargetQueryParser> parsers) throws UnparsableTargetQueryException {
        Map<TargetQueryParser, TargetQueryParserException> exceptions = new HashMap<>();
		for (TargetQueryParser parser : parsers) {
            try {
                ImmutableList<TargetAtom> parse = parser.parse(targetString);
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

    private static List<SQLPPTriplesMap> addNewMapping(String mappingId, String sourceQuery,
                                                       String targetString,
                                                       ImmutableList<TargetAtom> targetQuery,
                                                       List<SQLPPTriplesMap> currentSourceMappings) {
        SQLPPTriplesMap mapping = new OntopNativeSQLPPTriplesMap(
                mappingId, SQL_MAPPING_FACTORY.getSQLQuery(sourceQuery), targetString, targetQuery);
        if (!currentSourceMappings.contains(mapping)) {
            currentSourceMappings.add(mapping);
        }
        else {
            LOG.warn("Duplicate mapping {}", mappingId);
        }
        return currentSourceMappings;
    }

    private static boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }

    private List<TargetQueryParser> createParsers(ImmutableMap<String, String> prefixes) {
        return ImmutableList.of(targetQueryParserFactory.createParser(prefixes));
    }
}
