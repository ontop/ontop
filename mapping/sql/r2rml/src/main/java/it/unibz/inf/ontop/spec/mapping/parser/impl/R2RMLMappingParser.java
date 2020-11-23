package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import eu.optique.r2rml.api.model.PredicateObjectMap;
import eu.optique.r2rml.api.model.RefObjectMap;
import eu.optique.r2rml.api.model.SubjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.R2RMLSQLPPtriplesMap;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements SQLMappingParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(R2RMLMappingParser.class);

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final R2RMLParser r2rmlParser;
    private final SQLPPSourceQueryFactory sourceQueryFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private R2RMLMappingParser(SQLPPMappingFactory ppMappingFactory, SpecificationFactory specificationFactory,
                               TargetAtomFactory targetAtomFactory, R2RMLParser r2rmlParser,
                               SQLPPSourceQueryFactory sourceQueryFactory,
                               TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.r2rmlParser = r2rmlParser;
        this.sourceQueryFactory = sourceQueryFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
    }


    @Override
    public SQLPPMapping parse(File mappingFile) throws InvalidMappingException, MappingIOException {
        try {
            LinkedHashModel rdf4jGraph = new LinkedHashModel();
            RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
            try (InputStream in = new FileInputStream(mappingFile)) {
                URL documentUrl = new URL("file://" + mappingFile);
                StatementCollector collector = new StatementCollector(rdf4jGraph);
                parser.setRDFHandler(collector);
                parser.parse(in, documentUrl.toString());
                return parse(new RDF4J().asGraph(rdf4jGraph));
            }
        }
        catch (IOException e) {
            throw new MappingIOException(e);
        }
        catch (RDFParseException | RDFHandlerException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }


    @Override
    public SQLPPMapping parse(Reader reader) {
        // TODO: support this
        throw new UnsupportedOperationException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException {
        try {
            ImmutableList<SQLPPTriplesMap> sourceMappings = extractPPTriplesMaps(mappingGraph);

            //TODO: try to extract prefixes from the R2RML mappings
            PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());

            return ppMappingFactory.createSQLPreProcessedMapping(sourceMappings, prefixManager);
        }
        catch (InvalidR2RMLMappingException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }

    private ImmutableList<SQLPPTriplesMap> extractPPTriplesMaps(Graph mappingGraph) throws InvalidR2RMLMappingException {

        Collection<TriplesMap> tripleMaps = r2rmlParser.extractTripleMaps(mappingGraph);

        /*
         * Pass 1: creates "regular" PP triples maps using the original SQL queries
         */
        Map<TriplesMap, SQLPPTriplesMap> regularMap = new HashMap<>();

        for (TriplesMap tm : tripleMaps) {
            extractPPTriplesMap(tm)
                    .ifPresent(m -> regularMap.put(tm, m));
        }

        /*
         * It is important to create subject terms only once because of blank nodes.
         */
        ImmutableMap<TriplesMap, ImmutableTerm> subjectTermMap = regularMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> extractSubjectTerm(e.getValue())));

        List<SQLPPTriplesMap> ppTriplesMaps = Lists.newArrayList();
        ppTriplesMaps.addAll(regularMap.values());

        /*
         * Pass 2 - Creates new PP triples maps for object ref maps
         * NB: these triples maps are novel because the SQL queries are different
         */
        for (TriplesMap tm : tripleMaps) {
            ppTriplesMaps.addAll(extractJoinPPTriplesMaps(tm, subjectTermMap));
        }

        return ImmutableList.copyOf(ppTriplesMaps);
    }

    private ImmutableTerm extractSubjectTerm(SQLPPTriplesMap sqlppTriplesMap) {
        return sqlppTriplesMap.getTargetAtoms().stream()
                .map(a -> a.getSubstitutedTerm(0))
                .findAny()
                .orElseThrow(() -> new MinorOntopInternalBugException("" +
                        "All created SQLPPTriplesMaps must have at least one target atom"));
    }

    private Optional<SQLPPTriplesMap> extractPPTriplesMap(TriplesMap tm) throws InvalidR2RMLMappingException {
        String sourceQuery = r2rmlParser.extractSQLQuery(tm).trim();
        ImmutableList<TargetAtom> targetAtoms = extractMappingTargetAtoms(tm);

        if (targetAtoms.isEmpty()){
            LOGGER.warn("WARNING a triples map without target query will not be introduced : "+ tm);
        }
        return Optional.of(targetAtoms)
                .filter(as -> !as.isEmpty())
                .map(as -> new R2RMLSQLPPtriplesMap("mapping-" + tm.hashCode(),
                        sourceQueryFactory.createSourceQuery(sourceQuery),  as));
    }

    private ImmutableList<TargetAtom> extractMappingTargetAtoms(TriplesMap tm)  {
        ImmutableList.Builder<TargetAtom> targetAtoms = ImmutableList.builder();

        SubjectMap subjectMap = tm.getSubjectMap();
        ImmutableTerm subjectTerm = r2rmlParser.extractSubjectTerm(subjectMap);

        ImmutableList<NonVariableTerm> graphTerms = r2rmlParser.extractGraphTerms(subjectMap.getGraphMaps());
        boolean includeDefaultGraph = graphTerms.isEmpty() ||
                graphTerms.stream().anyMatch(R2RMLMappingParser::isDefaultGraph);

        ImmutableList<NonVariableTerm> namedGraphTerms = graphTerms.stream()
                .filter(t -> !isDefaultGraph(t))
                .collect(ImmutableCollectors.toList());

        r2rmlParser.extractClassIRIs(subjectMap)
                .flatMap(i -> {

                    Stream<TargetAtom> quads = namedGraphTerms.stream()
                            .map(g -> targetAtomFactory.getQuadTargetAtom(subjectTerm, i, g));

                    return includeDefaultGraph
                            ? Stream.concat(
                                Stream.of(targetAtomFactory.getTripleTargetAtom(subjectTerm, i)),
                                quads)
                            : quads;
                })
                .forEach(targetAtoms::add);

        for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {

            List<NonVariableTerm> predicateTerms = r2rmlParser.extractPredicateTerms(pom);
            for (ImmutableTerm objectTerm : r2rmlParser.extractRegularObjectTerms(pom)) {
                for (NonVariableTerm predicateTerm : predicateTerms) {
                    if (includeDefaultGraph)
                        targetAtoms.add(targetAtomFactory.getTripleTargetAtom(subjectTerm, predicateTerm, objectTerm));
                    for (NonVariableTerm graphTerm : namedGraphTerms) {
                        targetAtoms.add(targetAtomFactory.getQuadTargetAtom(subjectTerm, predicateTerm, objectTerm, graphTerm));
                    }
                }
            }
        }
        return targetAtoms.build();
    }

    private static boolean isDefaultGraph(ImmutableTerm graphTerm) {
        return (graphTerm instanceof IRIConstant)
                && ((IRIConstant) graphTerm).getIRI().equals(R2RMLVocabulary.defaultGraph);
    }

    private List<SQLPPTriplesMap> extractJoinPPTriplesMaps(TriplesMap tm,
                                                           ImmutableMap<TriplesMap, ImmutableTerm> subjectTermMap)  {

        ImmutableList.Builder<SQLPPTriplesMap> joinPPTriplesMapsBuilder = ImmutableList.builder();
        for (PredicateObjectMap pobm: tm.getPredicateObjectMaps()) {

            List<RefObjectMap> refObjectMaps = pobm.getRefObjectMaps();
            if (refObjectMaps.isEmpty())
                continue;

            List<NonVariableTerm> predicateTerms = r2rmlParser.extractPredicateTerms(pobm);

            SubjectMap subjectMap = tm.getSubjectMap();

            ImmutableList<NonVariableTerm> graphTerms = Stream.concat(
                    r2rmlParser.extractGraphTerms(pobm.getGraphMaps()).stream(),
                    r2rmlParser.extractGraphTerms(subjectMap.getGraphMaps()).stream())
                    .distinct()
                    .collect(ImmutableCollectors.toList());

            boolean includeDefaultGraph = graphTerms.isEmpty() ||
                    graphTerms.stream().anyMatch(R2RMLMappingParser::isDefaultGraph);

            ImmutableList<NonVariableTerm> namedGraphTerms = graphTerms.stream()
                    .filter(t -> !isDefaultGraph(t))
                    .collect(ImmutableCollectors.toList());

            for (RefObjectMap robm : refObjectMaps) {

                TriplesMap parent = robm.getParentMap();
                if (robm.getJoinConditions().isEmpty() &&
                        !parent.getLogicalTable().getSQLQuery().equals(tm.getLogicalTable().getSQLQuery()))
                    throw new IllegalArgumentException("No rr:joinCondition, but the two SQL queries are disitnct: " +
                            tm.getLogicalTable().getSQLQuery() + " and " + parent.getLogicalTable().getSQLQuery());

                ImmutableTerm childSubject = Optional.ofNullable(subjectTermMap.get(tm))
                        .orElseGet(() -> r2rmlParser.extractSubjectTerm(tm.getSubjectMap()));

                String childPrefix = robm.getJoinConditions().isEmpty() ? "TMP" : "CHILD";
                ImmutableMap<Variable, Variable> childMap = childSubject.getVariableStream()
                        .collect(ImmutableCollectors.toMap(Function.identity(),
                                v -> prefixAttributeName(childPrefix + "_", v)));

                ImmutableTerm childSubject2 = substitutionFactory
                        .getVar2VarSubstitution(childMap).apply(childSubject);

                /*
                 * Re-uses the already created subject term. Important when dealing with blank nodes.
                 */
                ImmutableTerm parentSubject = Optional.ofNullable(subjectTermMap.get(parent))
                        .orElseGet(() -> r2rmlParser.extractSubjectTerm(parent.getSubjectMap()));

                String parentPrefix =  robm.getJoinConditions().isEmpty() ? "TMP" : "PARENT";
                ImmutableMap<Variable, Variable> parentMap = parentSubject.getVariableStream()
                        .collect(ImmutableCollectors.toMap(Function.identity(),
                                v -> prefixAttributeName(parentPrefix + "_", v)));

                ImmutableTerm parentSubject2 = substitutionFactory
                        .getVar2VarSubstitution(parentMap).apply(parentSubject);

                ImmutableList.Builder<TargetAtom> targetAtoms = ImmutableList.builder();
                for (NonVariableTerm predicateTerm : predicateTerms) {
                    if (includeDefaultGraph)
                        targetAtoms.add(targetAtomFactory.getTripleTargetAtom(childSubject2, predicateTerm, parentSubject2));
                    for (NonVariableTerm graphTerm : namedGraphTerms) {
                        targetAtoms.add(targetAtomFactory.getQuadTargetAtom(childSubject2, predicateTerm, parentSubject2, graphTerm));
                    }
                }

                String sourceQuery =
                        "SELECT " + Stream.concat(
                                childMap.entrySet().stream()
                                    .map(e -> childPrefix + "." + e.getKey() + " AS " + e.getValue()),
                                parentMap.entrySet().stream()
                                    .map(e -> parentPrefix + "." + e.getKey() + " AS " + e.getValue()))
                                .collect(Collectors.joining(", ")) +
                        " FROM (" + tm.getLogicalTable().getSQLQuery() + ") " + childPrefix +
                        (robm.getJoinConditions().isEmpty()
                                ? ""
                                :
                        ", (" + parent.getLogicalTable().getSQLQuery() + ") " + parentPrefix +
                        " WHERE " + robm.getJoinConditions().stream()
                                .map(j -> childPrefix + "." + j.getChild() +
                                        " = " + parentPrefix + "." + j.getParent())
                                .collect(Collectors.joining(",")));

                // use referenceObjectMap robm as id, because there could be multiple joinCondition in the same triple map
                SQLPPTriplesMap ppTriplesMap = new R2RMLSQLPPtriplesMap("tm-join-" + robm.hashCode(),
                        sourceQueryFactory.createSourceQuery(sourceQuery), targetAtoms.build());
                LOGGER.info("Join \"triples map\" introduced: " + ppTriplesMap);
                joinPPTriplesMapsBuilder.add(ppTriplesMap);
            }

        }
        return joinPPTriplesMapsBuilder.build();
    }


    private Variable prefixAttributeName(String prefix, Variable var) {
        String attributeName = var.getName();

        String newAttributeName;
        if (attributeName.startsWith("\"") && attributeName.endsWith("\"")
                || attributeName.startsWith("`") && attributeName.endsWith("`"))
            newAttributeName = attributeName.substring(0,1) + prefix + attributeName.substring(1);
        else
            newAttributeName = prefix + attributeName;

        return termFactory.getVariable(newAttributeName);
    }

}
