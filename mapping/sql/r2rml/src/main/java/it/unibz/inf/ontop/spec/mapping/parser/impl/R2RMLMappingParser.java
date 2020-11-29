package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.PredicateObjectMap;
import eu.optique.r2rml.api.model.RefObjectMap;
import eu.optique.r2rml.api.model.SubjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
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
    private final R2RMLToSQLPPTriplesMapConverter r2rmlParser;
    private final SQLPPSourceQueryFactory sourceQueryFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final RDF4JR2RMLMappingManager manager;

    @Inject
    private R2RMLMappingParser(SQLPPMappingFactory ppMappingFactory, SpecificationFactory specificationFactory,
                               TargetAtomFactory targetAtomFactory, R2RMLToSQLPPTriplesMapConverter r2rmlParser,
                               SQLPPSourceQueryFactory sourceQueryFactory,
                               TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.r2rmlParser = r2rmlParser;
        this.sourceQueryFactory = sourceQueryFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.manager = RDF4JR2RMLMappingManager.getInstance();
    }


    @Override
    public SQLPPMapping parse(File mappingFile) throws InvalidMappingException, MappingIOException {
        try {
            LinkedHashModel rdf4jGraph = new LinkedHashModel();
            RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
            InputStream in = new FileInputStream(mappingFile);
            URL documentUrl = new URL("file://" + mappingFile);
            StatementCollector collector = new StatementCollector(rdf4jGraph);
            parser.setRDFHandler(collector);
            parser.parse(in, documentUrl.toString());
            return parse(new RDF4J().asGraph(rdf4jGraph));
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

        Collection<TriplesMap> tripleMaps = manager.importMappings(mappingGraph);


        // Pass 1: creates "regular" PP triples maps using the original SQL queries
        ImmutableMap<TriplesMap, SQLPPTriplesMap> regularMap = tripleMaps.stream()
                .map(tm -> extractPPTriplesMap(tm)
                        .map(pp -> Maps.immutableEntry(tm, pp)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toMap());

        return Stream.concat(
                regularMap.values().stream(),
                // Pass 2 - Creates new PP triples maps for object ref maps
                // NB: these triples maps are novel because the SQL queries are different
                tripleMaps.stream()
                    // It is important to create subject terms only once because of blank nodes.
                    .flatMap(tm -> extractJoinPPTriplesMaps(tm, regularMap).stream()))
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableTerm extractSubjectTerm(SQLPPTriplesMap sqlppTriplesMap) {
        return sqlppTriplesMap.getTargetAtoms().stream()
                .map(a -> a.getSubstitutedTerm(0))
                .findAny()
                .orElseThrow(() -> new MinorOntopInternalBugException("All created SQLPPTriplesMaps must have at least one target atom"));
    }

    private Optional<SQLPPTriplesMap> extractPPTriplesMap(TriplesMap tm)  {
        ImmutableList<TargetAtom> targetAtoms = extractMappingTargetAtoms(tm);
        if (targetAtoms.isEmpty()) {
            LOGGER.warn("WARNING a triples map without target query will not be introduced : "+ tm);
        }
        String sourceQuery = tm.getLogicalTable().getSQLQuery().trim();
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

        subjectMap.getClasses().stream()
                .flatMap(i -> getTargetAtoms(subjectTerm, termFactory.getConstantIRI(RDF.TYPE), termFactory.getConstantIRI(i), graphTerms))
                .forEach(targetAtoms::add);

        for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
            List<NonVariableTerm> predicateTerms = r2rmlParser.extractPredicateTerms(pom);
            for (ImmutableTerm objectTerm : r2rmlParser.extractRegularObjectTerms(pom)) {
                predicateTerms.stream()
                        .flatMap(p -> getTargetAtoms(subjectTerm, p, objectTerm, graphTerms))
                        .forEach(targetAtoms::add);
            }
        }
        return targetAtoms.build();
    }

    private Stream<TargetAtom> getTargetAtoms(ImmutableTerm subject, ImmutableTerm predicate, ImmutableTerm object, ImmutableList<NonVariableTerm> graphs) {
        return (graphs.isEmpty())
            ? Stream.of(targetAtomFactory.getTripleTargetAtom(subject, predicate, object))
            : graphs.stream()
                .map(g -> isDefaultGraph(g)
                    ? targetAtomFactory.getTripleTargetAtom(subject, predicate, object)
                    : targetAtomFactory.getQuadTargetAtom(subject, predicate, object, g));
    }

    private static boolean isDefaultGraph(ImmutableTerm graphTerm) {
        return (graphTerm instanceof IRIConstant)
                && ((IRIConstant) graphTerm).getIRI().equals(R2RMLVocabulary.defaultGraph);
    }

    private List<SQLPPTriplesMap> extractJoinPPTriplesMaps(TriplesMap tm,
                                                           ImmutableMap<TriplesMap, SQLPPTriplesMap> regularTriplesMap)  {

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

            for (RefObjectMap robm : refObjectMaps) {

                TriplesMap parent = robm.getParentMap();
                if (robm.getJoinConditions().isEmpty() &&
                        !parent.getLogicalTable().getSQLQuery().equals(tm.getLogicalTable().getSQLQuery()))
                    throw new IllegalArgumentException("No rr:joinCondition, but the two SQL queries are disitnct: " +
                            tm.getLogicalTable().getSQLQuery() + " and " + parent.getLogicalTable().getSQLQuery());

                ImmutableTerm childSubject = Optional.ofNullable(regularTriplesMap.get(tm))
                        .map(this::extractSubjectTerm)
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
                ImmutableTerm parentSubject = Optional.ofNullable(regularTriplesMap.get(parent))
                        .map(this::extractSubjectTerm)
                        .orElseGet(() -> r2rmlParser.extractSubjectTerm(parent.getSubjectMap()));

                String parentPrefix =  robm.getJoinConditions().isEmpty() ? "TMP" : "PARENT";
                ImmutableMap<Variable, Variable> parentMap = parentSubject.getVariableStream()
                        .collect(ImmutableCollectors.toMap(Function.identity(),
                                v -> prefixAttributeName(parentPrefix + "_", v)));

                ImmutableTerm parentSubject2 = substitutionFactory
                        .getVar2VarSubstitution(parentMap).apply(parentSubject);

                ImmutableList<TargetAtom> targetAtoms = predicateTerms.stream()
                        .flatMap(p -> getTargetAtoms(childSubject2, p, parentSubject2, graphTerms))
                        .collect(ImmutableCollectors.toList());

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
                        sourceQueryFactory.createSourceQuery(sourceQuery), targetAtoms);
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
