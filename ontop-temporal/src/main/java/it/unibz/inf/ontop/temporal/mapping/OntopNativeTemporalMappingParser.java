package it.unibz.inf.ontop.temporal.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.QuadrupleElements;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnparsableTargetQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedTagException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDASQLParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.temporal.mapping.impl.SQLPPTemporalTriplesMapImpl;
import it.unibz.inf.ontop.temporal.model.term.BooleanConstant;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.exception.InvalidMappingTemporalExceptionwithIndicator.*;


public class OntopNativeTemporalMappingParser implements TemporalMappingParser {

    public enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source, interval
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
    private static final Logger LOG = LoggerFactory.getLogger(OntopNativeTemporalMappingParser.class);

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;

    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final IntervalQueryParser intervalQueryParser;
    /**
     * Create an SQL Temporal Mapping Parser for generating an OBDA model.
     */
    @Inject
    public OntopNativeTemporalMappingParser(SpecificationFactory specificationFactory,
                                            SQLPPMappingFactory ppMappingFactory, AtomFactory atomFactory, TermFactory termFactory, TypeFactory typeFactory, IntervalQueryParser intervalQueryParser) {
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.intervalQueryParser = intervalQueryParser;
    }

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
        return null;
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException, DuplicateMappingException {
        throw new IllegalArgumentException("The Ontop native temporal mapping language has no RDF serialization. Passing a RDF graph" +
                "to the OntopNativeTemporalMappingParser is thus invalid.");
    }

    private static void checkFile(File file) throws MappingIOException {
        if (!file.exists()) {
            throw new MappingIOException("WARNING: Cannot locate TOBDA file at: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new MappingIOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
    }

    private SQLPPMapping load(Reader reader, SpecificationFactory specificationFactory,
                                     SQLPPMappingFactory ppMappingFactory, String fileName)
            throws MappingIOException, InvalidMappingExceptionWithIndicator, DuplicateMappingException {

        final Map<String, String> prefixes = new HashMap<>();
        final List<SQLPPTemporalTriplesMap> mappings = new ArrayList<>();
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
                        //add time prefix for the temporal named graphs
                        prefixes.putIfAbsent("time:", "http://www.w3.org/2006/time#");
                        prefixes.putIfAbsent("tobda:", "https://w3id.org/tobda/vocabulary#");

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

        //TODO: expand into NAMED GRAPHS here!
        //List<SQLPPTemporalTriplesMap> expandedMappings =  expandTemporalTriplesMapsIntoNamedGraphs(mappings);

        it.unibz.inf.ontop.spec.mapping.PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.copyOf(prefixes));
        ImmutableList<SQLPPTriplesMap> mappingAxioms = ImmutableList.copyOf(mappings);

        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                mappingAxioms.stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream())
                        .flatMap(atom -> atom.getArguments().stream())
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .map(t -> (ImmutableFunctionalTerm) t),
                termFactory);

        MappingMetadata metadata = specificationFactory.createMetadata(prefixManager, uriTemplateMatcher);
        return ppMappingFactory.createSQLPreProcessedMapping(mappingAxioms, metadata);
    }

    private static boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }

    public List<TargetQueryParser> createParsers(Map<String, String> prefixes) {
        List<TargetQueryParser> parsers = new ArrayList<>();
        // TODO: consider using a factory instead.
        parsers.add(new TurtleOBDASQLParser(prefixes, atomFactory, termFactory));
        return ImmutableList.copyOf(parsers);
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

//    private static List<SQLPPTemporalTriplesMap> expandTemporalTriplesMapsIntoNamedGraphs(List<SQLPPTemporalTriplesMap> mappings){
//        List<SQLPPTemporalTriplesMap> expandedTriplesMapList = new ArrayList<>();
//
//        for(SQLPPTemporalTriplesMap temporalTriplesMap : mappings){
//            for(ImmutableFunctionalTerm targetAtom : temporalTriplesMap.getTargetAtoms()){
//                List<ImmutableFunctionalTerm> targetAtomList = new LinkedList<>();
//                ImmutableFunctionalTerm quadAtom = convertToQuadAtom(targetAtom, temporalTriplesMap.getTemporalMappingInterval());
//                targetAtomList.add(quadAtom);
//                expandedTriplesMapList.add(new SQLPPTemporalTriplesMapImpl(temporalTriplesMap.getId(),
//                        temporalTriplesMap.getSourceQuery(),
//                        targetAtomList.stream().collect(ImmutableCollectors.toList()),
//                        temporalTriplesMap.getTemporalMappingInterval()));
//                expandedTriplesMapList.add(createTemporalIntervalNamedGraphComponents(quadAtom,temporalTriplesMap));
//            }
//        }
//        return expandedTriplesMapList;
//    }

    private List<SQLPPTemporalTriplesMap> expandTemporalTriplesMapsIntoNamedGraphs(List<SQLPPTemporalTriplesMap> mappings){
        List<SQLPPTemporalTriplesMap> expandedTriplesMapList = new ArrayList<>();

        for(SQLPPTemporalTriplesMap temporalTriplesMap : mappings){
            for(ImmutableFunctionalTerm targetAtom : temporalTriplesMap.getTargetAtoms()){
                expandedTriplesMapList.add(createTemporalIntervalNamedGraphComponents(targetAtom,temporalTriplesMap));
            }
        }
        return expandedTriplesMapList;
    }

    private SQLPPTemporalTriplesMap createTemporalIntervalNamedGraphComponents(ImmutableFunctionalTerm targetAtom, SQLPPTemporalTriplesMap temporalTriplesMap){

        List<ImmutableFunctionalTerm> ngComponentsList = new LinkedList<>();

        ImmutableFunctionalTerm quadAtom = convertToQuadAtom(targetAtom, temporalTriplesMap.getTemporalMappingInterval());
        ngComponentsList.add(quadAtom);

        //hasTime
        ImmutableTerm graphURITemplate = quadAtom.getArguments().get(3);
        Predicate hasTime = atomFactory.getAtomPredicate(QuadrupleElements.hasTime.toString(),2);
        Predicate interval = atomFactory.getAtomPredicate(QuadrupleElements.interval.toString(),1);

        ImmutableFunctionalTerm intervalURITemplate  =
                termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(interval.getName()),
                temporalTriplesMap.getTemporalMappingInterval().getBegin(),
                temporalTriplesMap.getTemporalMappingInterval().getEnd());

        ngComponentsList.add(termFactory.getImmutableFunctionalTerm(hasTime, graphURITemplate, intervalURITemplate));

        //isBeginInclusive
        Predicate beginInclusive = atomFactory.getAtomPredicate(QuadrupleElements.isBeginInclusive.toString(),2);

        ngComponentsList.add(termFactory.getImmutableFunctionalTerm(beginInclusive, intervalURITemplate, termFactory.getBooleanConstant(((BooleanConstant)temporalTriplesMap.getTemporalMappingInterval().isBeginInclusive()).getBooleanValue())));

        //hasBeginning
        Predicate hasBeginning = atomFactory.getAtomPredicate(QuadrupleElements.hasBeginning.toString(),2);
        Predicate beginInstant = atomFactory.getAtomPredicate(QuadrupleElements.instant.toString(),1);
        ImmutableFunctionalTerm beginInstantURITemplate =
                termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(beginInstant.getName()),
                        temporalTriplesMap.getTemporalMappingInterval().getBegin());

        ngComponentsList.add(termFactory.getImmutableFunctionalTerm(hasBeginning,intervalURITemplate, beginInstantURITemplate));

        //TODO:support other types too
        //begin inXSDDateTime
        Predicate beginInXSDDateTime = atomFactory.getAtomPredicate(QuadrupleElements.inXSDTimeBegin.toString(),2);

        ngComponentsList.add(termFactory.getImmutableFunctionalTerm(beginInXSDDateTime, beginInstantURITemplate, temporalTriplesMap.getTemporalMappingInterval().getBegin()));

        //isEndInclusive
        Predicate endInclusive = atomFactory.getAtomPredicate(QuadrupleElements.isEndInclusive.toString(),2);

        ngComponentsList.add(termFactory.getImmutableFunctionalTerm(endInclusive, intervalURITemplate, termFactory.getBooleanConstant(((BooleanConstant)temporalTriplesMap.getTemporalMappingInterval().isEndInclusive()).getBooleanValue())));

        //hasEnd
        Predicate hasEnd = atomFactory.getAtomPredicate(QuadrupleElements.hasEnd.toString(),2);
        Predicate endInstant = atomFactory.getAtomPredicate(QuadrupleElements.instant.toString(),1);
        ImmutableFunctionalTerm endInstantURITemplate =
                termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(endInstant.getName()),
                        temporalTriplesMap.getTemporalMappingInterval().getEnd());

        ngComponentsList.add(termFactory.getImmutableFunctionalTerm(hasEnd,intervalURITemplate, endInstantURITemplate));

        //TODO:support other types too
        //end inXSDDateTime
        Predicate endInXSDDateTime = atomFactory.getAtomPredicate(QuadrupleElements.inXSDTimeEnd.toString(),2);

        ngComponentsList.add(termFactory.getImmutableFunctionalTerm(endInXSDDateTime, endInstantURITemplate, temporalTriplesMap.getTemporalMappingInterval().getEnd()));

        return new SQLPPTemporalTriplesMapImpl(temporalTriplesMap, ngComponentsList.stream().collect(ImmutableCollectors.toList()), targetAtom.getFunctionSymbol());
    }

    private ImmutableTerm getGraphURITemplate(TemporalMappingInterval intervalQuery){

        ImmutableTerm graphConstrantLiteral = termFactory.getConstantLiteral(QuadrupleElements.namedGraph.toString());
        ImmutableTerm beginInc = termFactory.getConstantLiteral(intervalQuery.isBeginInclusiveToString(), typeFactory.getXsdBooleanDatatype());
        ImmutableTerm endInc = termFactory.getConstantLiteral(intervalQuery.isEndInclusiveToString(), typeFactory.getXsdBooleanDatatype());

        return termFactory.getImmutableUriTemplate(graphConstrantLiteral, beginInc, intervalQuery.getBegin(),intervalQuery.getEnd(), endInc);
    }


    private ImmutableFunctionalTerm convertToQuadAtom(ImmutableFunctionalTerm targetAtom, TemporalMappingInterval intervalQuery){
        Predicate atomPred = targetAtom.getFunctionSymbol();

        ArrayList <ImmutableTerm> argList = new ArrayList();
        Iterator it = targetAtom.getArguments().iterator();
        while(it.hasNext()){
            argList.add((ImmutableTerm) it.next());
        }

        if(argList.size() == 1){
            ImmutableTerm rdfTypeTerm = termFactory.getConstantLiteral(IriConstants.RDF_TYPE);
            argList.add(rdfTypeTerm);
            ImmutableTerm predTerm = termFactory.getConstantLiteral(atomPred.getName());
            argList.add(predTerm);
        }else {
            ImmutableTerm predTerm = termFactory.getConstantLiteral(atomPred.getName());
            argList.add(1, predTerm);
        }

        ImmutableTerm graphURITemplate = getGraphURITemplate(intervalQuery);
        argList.add(graphURITemplate);

        ImmutableFunctionalTerm dataAtom = termFactory.getImmutableFunctionalTerm(atomFactory.getQuadrupleAtomPredicate(), argList.stream().collect(ImmutableCollectors.toList()));

        return dataAtom;
    }

    /**
     * TODO: describe
     * TODO: follow the advice of IntelliJ: split this method to make its workflow tractable.
     * @param reader
     * @param invalidMappingIndicators Read-write list of error indicators.
     * @return The updated mapping set of the current source
     * @throws IOException
     */
    private List<SQLPPTemporalTriplesMap> readMappingDeclaration(LineNumberReader reader,
                                                                        List<TargetQueryParser> parsers,
                                                                        List<Indicator> invalidMappingIndicators)
            throws IOException {
        List<SQLPPTemporalTriplesMap> currentSourceMappings = new ArrayList<>();

        String mappingId = "";
        String currentLabel = ""; // the reader is working on which label
        StringBuffer sourceQuery = null;
        ImmutableList<ImmutableFunctionalTerm> targetQuery = null;
        TemporalMappingInterval intevalQuery = null;
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
                                addNewMapping(mappingId, sourceQuery.toString(),  appendTemporalComponents(targetQuery, intevalQuery), intevalQuery, currentSourceMappings);
                        mappingId = "";
                        sourceQuery = null;
                        targetQuery = null;
                        intevalQuery = null;
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
            }else if(currentLabel.equals(Label.interval.name())){
                String intervalString = value;
                if (intervalString.isEmpty()) { // empty or not
                    invalidMappingIndicators.add(new Indicator(lineNumber, mappingId, INTERVAL_QUERY_IS_BLANK));
                    isMappingValid = false;
                }else{
                    //load the interval query
                    intevalQuery = loadTemporalIntervalQuery(intervalString);
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
            currentSourceMappings = addNewMapping(mappingId, sourceQuery.toString(), appendTemporalComponents(targetQuery, intevalQuery), intevalQuery,
                    currentSourceMappings);
        }

        return currentSourceMappings;
    }

    private ImmutableList<ImmutableFunctionalTerm> appendTemporalComponents(ImmutableList<ImmutableFunctionalTerm> targetQuery, TemporalMappingInterval intervalQuery ){
        List <ImmutableFunctionalTerm> newList = new ArrayList<>();
        for (ImmutableFunctionalTerm f : targetQuery){
            List<ImmutableTerm> newArglist = new ArrayList<>(f.getArguments());
            newArglist.add(intervalQuery.isBeginInclusive());
            newArglist.add(intervalQuery.getBegin());
            newArglist.add(intervalQuery.getEnd());
            newArglist.add(intervalQuery.isEndInclusive());
            AtomPredicate newAtom = atomFactory.getAtomPredicate(f.getFunctionSymbol().getName(), f.getFunctionSymbol().getArity() + 4);
            newList.add(termFactory.getImmutableFunctionalTerm(newAtom, ImmutableList.copyOf(newArglist)));
        }
        return ImmutableList.copyOf(newList);
    }
    //TODO: extend NativeQueryLanguageComponentFactory to support creating temporal mapping axioms
    //TODO: replace TemporalMappingFactory with NativeQueryLanguageComponentFactory
    private static List<SQLPPTemporalTriplesMap> addNewMapping(String mappingId, String sourceQuery, ImmutableList<ImmutableFunctionalTerm> targetQuery, TemporalMappingInterval intervalQuery,
                                                               List<SQLPPTemporalTriplesMap> currentSourceMappings) {
        SQLPPTemporalTriplesMap mapping = new SQLPPTemporalTriplesMapImpl(mappingId, SQL_MAPPING_FACTORY.getSQLQuery(sourceQuery), targetQuery, intervalQuery);
        if (!currentSourceMappings.contains(mapping)) {
            currentSourceMappings.add(mapping);
        }
        else {
            LOG.warn("Duplicate mapping %s", mappingId);
        }
        return currentSourceMappings;
    }

    public static ImmutableList<ImmutableFunctionalTerm> loadTargetQuery(String targetString,
                                                                          List<TargetQueryParser> parsers) throws UnparsableTargetQueryException {
        Map<TargetQueryParser, TargetQueryParserException> exceptions = new HashMap<>();
        for (TargetQueryParser parser : parsers) {
            try {
                ImmutableList<ImmutableFunctionalTerm> parse = parser.parse(targetString);
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

    private TemporalMappingInterval loadTemporalIntervalQuery(String intervalString){
        intervalString = intervalString.trim();
        return intervalQueryParser.parse(intervalString);
//        if(IntervalQueryParser.temporalMappingIntervalValidator(intervalString))
//            return intervalQueryParser.parse(intervalString);
//        return null;
    }
}
