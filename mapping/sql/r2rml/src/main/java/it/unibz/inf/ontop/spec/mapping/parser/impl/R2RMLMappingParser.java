package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
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
import java.util.stream.Collector;
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
    private final IRIConstant rdfType;



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
        this.rdfType = termFactory.getConstantIRI(RDF.TYPE);
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

    /*
         see https://www.w3.org/TR/r2rml/#generated-triples for the definitions of all variables
     */

    private ImmutableList<TargetAtom> extractMappingTargetAtoms(TriplesMap tm)  {
        SubjectMap subjectMap = tm.getSubjectMap();

        ImmutableTerm subject = r2rmlParser.extractSubjectTerm(subjectMap);
        ImmutableList<NonVariableTerm> subject_graphs = r2rmlParser.extractGraphTerms(subjectMap.getGraphMaps());

        return Stream.concat(
                subjectMap.getClasses().stream()
                        .map(termFactory::getConstantIRI)
                        .flatMap(iri -> getTargetAtoms(subject, rdfType, iri, subject_graphs)),
                tm.getPredicateObjectMaps().stream()
                        .flatMap(pom -> getPredicateObjectMapTargetAtoms(subject, pom, subject_graphs)))
                .collect(ImmutableCollectors.toList());
    }

    private Stream<TargetAtom> getPredicateObjectMapTargetAtoms(ImmutableTerm subject, PredicateObjectMap pom, ImmutableList<NonVariableTerm> subject_graphs) {
        List<NonVariableTerm> predicates = r2rmlParser.extractPredicateTerms(pom);
        List<NonVariableTerm> objects = r2rmlParser.extractRegularObjectTerms(pom);
        ImmutableList<NonVariableTerm> subject_graphs_and_predicate_object_graphs = Stream.concat(
                    subject_graphs.stream(),
                    r2rmlParser.extractGraphTerms(pom.getGraphMaps()).stream())
                .distinct()
                .collect(ImmutableCollectors.toList());

        return predicates.stream()
                .flatMap(p -> objects.stream().map(o -> Maps.immutableEntry(p, o)))
                .flatMap(e -> getTargetAtoms(subject, e.getKey(), e.getValue(), subject_graphs_and_predicate_object_graphs));
    }

    private Stream<TargetAtom> getTargetAtoms(ImmutableTerm subject, ImmutableTerm predicate, ImmutableTerm object, ImmutableList<? extends ImmutableTerm> graphs) {
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

    private static final String TMP_PREFIX = "TMP";
    private static final String CHILD_PREFIX = "CHILD";
    private static final String PARENT_PREFIX = "PARENT";

    private List<SQLPPTriplesMap> extractJoinPPTriplesMaps(TriplesMap tm,
                                                           ImmutableMap<TriplesMap, SQLPPTriplesMap> regularTriplesMap)  {

        ImmutableList.Builder<SQLPPTriplesMap> joinPPTriplesMapsBuilder = ImmutableList.builder();
        for (PredicateObjectMap pobm: tm.getPredicateObjectMaps()) {
            if (pobm.getRefObjectMaps().isEmpty())
                continue;

            ImmutableList<NonVariableTerm> extractedPredicates = r2rmlParser.extractPredicateTerms(pobm);

            ImmutableList<NonVariableTerm> subject_graphs_and_predicate_object_graphs = Stream.concat(
                    r2rmlParser.extractGraphTerms(tm.getSubjectMap().getGraphMaps()).stream(),
                    r2rmlParser.extractGraphTerms(pobm.getGraphMaps()).stream())
                    .distinct()
                    .collect(ImmutableCollectors.toList());

            for (RefObjectMap robm : pobm.getRefObjectMaps()) {

                TriplesMap parent = robm.getParentMap();

                ImmutableTerm extractedSubject = getSubjectTerm(tm, regularTriplesMap);
                ImmutableTerm extractedObject = getSubjectTerm(parent, regularTriplesMap);

                String sourceQuery;
                ImmutableMap<Variable, Variable>  childMap, parentMap;

                if (robm.getJoinConditions().isEmpty()) {
                    if (!parent.getLogicalTable().getSQLQuery().equals(tm.getLogicalTable().getSQLQuery()))
                        throw new IllegalArgumentException("No rr:joinCondition, but the two SQL queries are disitnct: " +
                            tm.getLogicalTable().getSQLQuery() + " and " + parent.getLogicalTable().getSQLQuery());

                     childMap = parentMap = Stream.concat(getVariableStreamOf(
                                    extractedSubject,
                                    extractedPredicates,
                                    subject_graphs_and_predicate_object_graphs),
                                extractedObject.getVariableStream())
                            .collect(toVariableRenamingMap(TMP_PREFIX));

                    sourceQuery = "SELECT " + childMap.entrySet().stream()
                            .map(e -> TMP_PREFIX + "." + e.getKey() + " AS " + e.getValue())
                                    .collect(Collectors.joining(", ")) +
                            " FROM (" + tm.getLogicalTable().getSQLQuery() + ") " + TMP_PREFIX;
                }
                else {
                    childMap = getVariableStreamOf(
                                    extractedSubject,
                                    extractedPredicates,
                                    subject_graphs_and_predicate_object_graphs)
                            .collect(toVariableRenamingMap(CHILD_PREFIX));

                    parentMap = extractedObject.getVariableStream()
                            .collect(toVariableRenamingMap(PARENT_PREFIX));

                    sourceQuery = "SELECT " + Stream.concat(
                            childMap.entrySet().stream()
                                .map(e -> CHILD_PREFIX + "." + e.getKey() + " AS " + e.getValue()),
                            parentMap.entrySet().stream()
                                .map(e -> PARENT_PREFIX + "." + e.getKey() + " AS " + e.getValue()))
                            .collect(Collectors.joining(", ")) +
                            " FROM (" + tm.getLogicalTable().getSQLQuery() + ") " + CHILD_PREFIX +
                            ", (" + parent.getLogicalTable().getSQLQuery() + ") " + PARENT_PREFIX +
                            " WHERE " + robm.getJoinConditions().stream()
                                    .map(j -> CHILD_PREFIX + "." + j.getChild() +
                                            " = " + PARENT_PREFIX + "." + j.getParent())
                                    .collect(Collectors.joining(","));
                }

                Var2VarSubstitution sub = substitutionFactory.getVar2VarSubstitution(childMap);
                ImmutableTerm subject = sub.apply(extractedSubject);
                ImmutableList<ImmutableTerm>  graphs = subject_graphs_and_predicate_object_graphs.stream()
                        .map(sub::apply)
                        .collect(ImmutableCollectors.toList());
                Var2VarSubstitution ob = substitutionFactory.getVar2VarSubstitution(parentMap);
                ImmutableTerm object = ob.apply(extractedObject);

                ImmutableList<TargetAtom> targetAtoms = extractedPredicates.stream().map(sub::apply)
                        .flatMap(p -> getTargetAtoms(subject, p, object, graphs))
                        .collect(ImmutableCollectors.toList());

                // use referenceObjectMap robm as id, because there could be multiple joinCondition in the same triple map
                SQLPPTriplesMap ppTriplesMap = new R2RMLSQLPPtriplesMap("tm-join-" + robm.hashCode(),
                        sourceQueryFactory.createSourceQuery(sourceQuery), targetAtoms);
                LOGGER.info("Join \"triples map\" introduced: " + ppTriplesMap);
                joinPPTriplesMapsBuilder.add(ppTriplesMap);
            }

        }
        return joinPPTriplesMapsBuilder.build();
    }

    private ImmutableTerm getSubjectTerm(TriplesMap tm, ImmutableMap<TriplesMap, SQLPPTriplesMap> regularTriplesMap) {
        // Re-uses the already created subject term. Important when dealing with blank nodes.
        return Optional.ofNullable(regularTriplesMap.get(tm))
                .map(R2RMLMappingParser.this::extractSubjectTerm)
                .orElseGet(() -> r2rmlParser.extractSubjectTerm(tm.getSubjectMap()));
    }

    private static Stream<Variable> getVariableStreamOf(ImmutableTerm t, ImmutableList<? extends ImmutableTerm> l1, ImmutableList<? extends ImmutableTerm> l2) {
        return Stream.concat(t.getVariableStream(), Stream.concat(
                l1.stream().flatMap(ImmutableTerm::getVariableStream),
                l2.stream().flatMap(ImmutableTerm::getVariableStream)));
    }

    private Collector<Variable, ?, ImmutableMap<Variable, Variable>> toVariableRenamingMap(String prefix) {
        return ImmutableCollectors.toMap(
                v -> v,
                v -> prefixAttributeName(prefix + "_", v));
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
