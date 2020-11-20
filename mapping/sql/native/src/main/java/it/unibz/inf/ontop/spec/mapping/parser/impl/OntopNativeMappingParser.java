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
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedTagException;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import org.apache.commons.rdf.api.Graph;

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

    private final TargetQueryParserFactory targetQueryParserFactory;
    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final SQLPPSourceQueryFactory sourceQueryFactory;

    /**
     * Create an SQL Mapping Parser for generating an OBDA model.
     */
    @Inject
    private OntopNativeMappingParser(SpecificationFactory specificationFactory,
                                     TargetQueryParserFactory targetQueryParserFactory,
                                     SQLPPMappingFactory ppMappingFactory,
                                     SQLPPSourceQueryFactory sourceQueryFactory) {
        this.targetQueryParserFactory = targetQueryParserFactory;
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.sourceQueryFactory = sourceQueryFactory;
    }

    /**
     * Parsing is not done at construction time because our dependency
     * injection framework (Guice) does not manage exceptions nicely.
     *
     */
    @Override
    public SQLPPMapping parse(File file) throws InvalidMappingException, MappingIOException {
        checkFile(file);
        try (Reader reader = new FileReader(file)) {
            return load(reader, specificationFactory, ppMappingFactory, file.getName());
        } catch (IOException e) {
            throw new MappingIOException(e);
        }
    }

    @Override
    public SQLPPMapping parse(Reader reader) throws InvalidMappingException, MappingIOException {
        return load(reader, specificationFactory, ppMappingFactory, ".obda file");
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph)  {
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
            throws MappingIOException, InvalidMappingExceptionWithIndicator {

        final ImmutableMap.Builder<String, String> prefixes = ImmutableMap.builder();
        final ImmutableList.Builder<SQLPPTriplesMap> mappings = ImmutableList.builder();
        final List<Indicator> invalidMappingIndicators = new ArrayList<>();

        try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {
            TargetQueryParser parser = null; // lazy initialization
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                try {
                    if (isCommentLine(line) || line.isEmpty()) {
                        continue; // skip comment lines and blank lines
                    }
                    if (line.contains(PREFIX_DECLARATION_TAG)) {
                        prefixes.putAll(readPrefixDeclaration(lineNumberReader));
                    }
                    else if (line.contains(CLASS_DECLARATION_TAG)) { // deprecated tag
                        throw new UnsupportedTagException(CLASS_DECLARATION_TAG);
                    }
                    else if (line.contains(OBJECT_PROPERTY_DECLARATION_TAG)) { // deprecated tag
                        throw new UnsupportedTagException(OBJECT_PROPERTY_DECLARATION_TAG);
                    }
                    else if (line.contains(DATA_PROPERTY_DECLARATION_TAG)) { // deprecated tag
                        throw new UnsupportedTagException(DATA_PROPERTY_DECLARATION_TAG);
                    }
                    else if (line.contains(SOURCE_DECLARATION_TAG)) {
                        // This exception wil be rethrown
                        throw new RuntimeException("Source declaration is not supported anymore (since 3.0). " +
                                "Please give this information with the Ontop configuration.");
                    }
                    else if (line.contains(MAPPING_DECLARATION_TAG)) {
                        if (parser == null) {
                            parser = targetQueryParserFactory.createParser(prefixes.build());
                        }
                        mappings.addAll(readMappingDeclaration(lineNumberReader, parser, invalidMappingIndicators));
                    }
                    else {
                        throw new IOException("Unknown syntax: " + line);
                    }
                }
                catch (Exception e) {
                    throw new IOException(String.format("ERROR reading %s at line: %d\nMESSAGE: %s", fileName,
                            lineNumberReader.getLineNumber(), e.getMessage()), e);
                }
            }
        }
        catch (IOException e) {
            throw new MappingIOException(e);
        }

        // Throw some validation exceptions
        if (!invalidMappingIndicators.isEmpty()) {
            throw new InvalidMappingExceptionWithIndicator(invalidMappingIndicators);
        }

        PrefixManager prefixManager = specificationFactory.createPrefixManager(prefixes.build());
        return ppMappingFactory.createSQLPreProcessedMapping(mappings.build(), prefixManager);
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


    private static final class MappingDeclarationStatus {
        private final List<Indicator> invalidMappingIndicators;

        MappingDeclarationStatus(List<Indicator> invalidMappingIndicators) {
            this.invalidMappingIndicators = invalidMappingIndicators;
        }

        boolean fail(int lineNumber, Object hint, int reason) {
            invalidMappingIndicators.add(new Indicator(lineNumber, hint, reason));
            return false;
        }
    }

    /**
     * TODO: describe
     * TODO: follow the advice of IntelliJ: split this method to make its workflow tractable.
     * @param reader
     * @param invalidMappingIndicators Read-write list of error indicators.
     * @return The updated mapping set of the current source
     * @throws IOException
     */
    private ImmutableList<SQLPPTriplesMap> readMappingDeclaration(LineNumberReader reader,
                                                                TargetQueryParser parser,
                                                                List<Indicator> invalidMappingIndicators)
            throws IOException {

        ImmutableList.Builder<SQLPPTriplesMap> currentSourceMappings = ImmutableList.builder();
        MappingDeclarationStatus status = new MappingDeclarationStatus(invalidMappingIndicators);

        String mappingId = "";
        String currentLabel = ""; // the reader is working on which label
        ImmutableList.Builder<String> sourceQuery = ImmutableList.builder();
        String targetString = null;
        ImmutableList<TargetAtom> targetQuery = null;
        boolean isMappingValid = true; // a flag to load the mapping to the model if valid

        String line;
        for(line = reader.readLine();
        		line != null && !line.trim().equals(END_COLLECTION_SYMBOL);
        		line = reader.readLine()) {

            if (line.isEmpty()) {
            	if (!mappingId.isEmpty()) {
	            	// Save the mapping to the model (if valid) at this point
	                if (isMappingValid) {
                        currentSourceMappings.add(new OntopNativeSQLPPTriplesMap(
                                mappingId, createSourceQuery(sourceQuery), targetString, targetQuery));
	                    mappingId = "";
	                    sourceQuery = ImmutableList.builder();
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
            if (tokens.length > 1) {
                label = tokens[0].trim();
                value = tokens[1].trim();
            }
            else {
                value = tokens[0];
                label = "";
            }
            if (!label.isEmpty()) {
            	currentLabel = tokens[0];
            }

            if (currentLabel.equals(Label.mappingId.name())) {
                mappingId = value;
                if (mappingId.isEmpty()) { // empty or not
                    isMappingValid = status.fail(reader.getLineNumber(), Label.mappingId, MAPPING_ID_IS_BLANK);
                }
            }
            else if (currentLabel.equals(Label.target.name())) {
                targetString = value;
                if (targetString.isEmpty()) { // empty or not
                    isMappingValid = status.fail(reader.getLineNumber(), mappingId, TARGET_QUERY_IS_BLANK);
                }
                else {
                    try {
                        targetQuery = parser.parse(targetString);
                    }
                    catch (TargetQueryParserException e) {
                        isMappingValid = status.fail(reader.getLineNumber(), new String[] {mappingId, targetString, e.getMessage()},
                                ERROR_PARSING_TARGET_QUERY);
                    }
                }
            }
            else if (currentLabel.equals(Label.source.name())) {
                if (value.isEmpty()) { // empty or not
                    isMappingValid = status.fail(reader.getLineNumber(), mappingId, SOURCE_QUERY_IS_BLANK);
                }
                else {
                    sourceQuery.add(value);
                }
            }
            else {
                throw new IOException(String.format("Unknown parameter name \"%s\" at line: %d.", tokens[0], reader.getLineNumber()));
            }
        }

        if (line == null) {
        	throw new IOException(String.format("End collection symbol %s is missing.", END_COLLECTION_SYMBOL));
        }

        // Save the last mapping entry to the model
        if (!mappingId.isEmpty() && isMappingValid) {
            currentSourceMappings.add(new OntopNativeSQLPPTriplesMap(
                    mappingId, createSourceQuery(sourceQuery), targetString, targetQuery));
        }

        return currentSourceMappings.build();
    }


    private SQLPPSourceQuery createSourceQuery(ImmutableList.Builder<String> builder) {
        return sourceQueryFactory.createSourceQuery(String.join("\n", builder.build()));
    }

    private static boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }
}
