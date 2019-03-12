package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.optique.r2rml.api.model.PredicateObjectMap;
import eu.optique.r2rml.api.model.RefObjectMap;
import eu.optique.r2rml.api.model.SubjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
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

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements SQLMappingParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(R2RMLMappingParser.class);
    private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final TermFactory termFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final R2RMLParser r2rmlParser;


    @Inject
    private R2RMLMappingParser(SQLPPMappingFactory ppMappingFactory, SpecificationFactory specificationFactory,
                               TermFactory termFactory, TargetAtomFactory targetAtomFactory, R2RMLParser r2rmlParser) {
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.termFactory = termFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.r2rmlParser = r2rmlParser;
    }


    @Override
    public SQLPPMapping parse(File mappingFile) throws InvalidMappingException, MappingIOException, DuplicateMappingException {
        try {
            LinkedHashModel rdf4jGraph = new LinkedHashModel();
            RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
            InputStream in = new FileInputStream(mappingFile);
            URL documentUrl = new URL("file://" + mappingFile);
            StatementCollector collector = new StatementCollector(rdf4jGraph);
            parser.setRDFHandler(collector);
            parser.parse(in, documentUrl.toString());
            return parse(new RDF4J().asGraph(rdf4jGraph));
        } catch (IOException e) {
            throw new MappingIOException(e);
        } catch (RDFParseException | RDFHandlerException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }


    @Override
    public SQLPPMapping parse(Reader reader) throws InvalidMappingException, MappingIOException, DuplicateMappingException {
        // TODO: support this
        throw new UnsupportedOperationException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException, DuplicateMappingException {
        try {
            ImmutableList<SQLPPTriplesMap> sourceMappings = extractPPTriplesMaps(mappingGraph);

            //TODO: try to extract prefixes from the R2RML mappings
            PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());
            MappingMetadata mappingMetadata = specificationFactory.createMetadata(prefixManager);

            return ppMappingFactory.createSQLPreProcessedMapping(sourceMappings, mappingMetadata);
        } catch (InvalidR2RMLMappingException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }

    private ImmutableList<SQLPPTriplesMap> extractPPTriplesMaps(Graph mappingGraph) throws InvalidR2RMLMappingException {

        Collection<TriplesMap> tripleMaps = r2rmlParser.extractTripleMaps(mappingGraph);

        ImmutableList.Builder<SQLPPTriplesMap> ppTriplesMapsBuilder = ImmutableList.builder();

        /*
         * TODO: refactor so that ONLY ONE PPTriplesMap is created per triples map
         */
        for (TriplesMap tm : tripleMaps) {
            extractPPTriplesMap(tm)
                    .ifPresent(ppTriplesMapsBuilder::add);

            // pass 2 - check for join conditions, add to list
            ppTriplesMapsBuilder.addAll(extractJoinPPTriplesMaps(tripleMaps, tm));
        }
        return ppTriplesMapsBuilder.build();
    }

    private Optional<SQLPPTriplesMap> extractPPTriplesMap(TriplesMap tm) throws InvalidR2RMLMappingException {
        String sourceQuery = r2rmlParser.getSQLQuery(tm).trim();
        ImmutableList<TargetAtom> targetAtoms = extractMappingTargetAtoms(tm);

        if (targetAtoms.isEmpty()){
            LOGGER.warn("WARNING a triples map without target query will not be introduced : "+ tm);
        }
        return Optional.of(targetAtoms)
                .filter(as -> !as.isEmpty())
                // TODO: consider a R2RML-specific type of triples map
                .map(as -> new OntopNativeSQLPPTriplesMap("mapping-"+tm.hashCode(),
                        MAPPING_FACTORY.getSQLQuery(sourceQuery), as));
    }

    private ImmutableList<TargetAtom> extractMappingTargetAtoms(TriplesMap tm) throws InvalidR2RMLMappingException {
        ImmutableList.Builder<TargetAtom> targetAtoms = ImmutableList.builder();

        SubjectMap subjectMap = tm.getSubjectMap();
        ImmutableTerm subjectTerm = r2rmlParser.extractSubjectTerm(subjectMap);

        //TODO: avoid using forEach
        r2rmlParser.extractClassIRIs(subjectMap)
                .map(i -> targetAtomFactory.getTripleTargetAtom(subjectTerm, i))
                .forEach(targetAtoms::add);

        for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
            //for each predicate object map

            //predicates that contain a variable are separately treated TODO: WHERE?
            List<NonVariableTerm> bodyURIPredicates = r2rmlParser.extractPredicateTerms(pom);

            ImmutableTerm objectTerm = r2rmlParser.extractRegularObjectTerms(pom);

            if (objectTerm == null) {
                // skip, object is a join
                continue;
            }


            //treat predicates
            for (NonVariableTerm predFunction : bodyURIPredicates) {

                targetAtoms.add(targetAtomFactory.getTripleTargetAtom(subjectTerm, predFunction, objectTerm));   // objectAtom
            }
        }
        return targetAtoms.build();
    }

    private List<SQLPPTriplesMap> extractJoinPPTriplesMaps(Collection<TriplesMap> tripleMaps, TriplesMap tm) throws InvalidR2RMLMappingException {
        ImmutableList.Builder<SQLPPTriplesMap> joinPPTriplesMapsBuilder = ImmutableList.builder();
        for (PredicateObjectMap pobm: tm.getPredicateObjectMaps()) {

            List<RefObjectMap> refObjectMaps = pobm.getRefObjectMaps();
            if (refObjectMaps.isEmpty())
                continue;

            List<NonVariableTerm> predicateTerms = r2rmlParser.extractPredicateTerms(pobm);

            for(RefObjectMap robm : refObjectMaps) {
                // TODO: why is deprecated?
                String sourceQuery = robm.getJointQuery();
                if (sourceQuery.isEmpty()) {
                    throw new InvalidR2RMLMappingException("Could not create source query for join in " + tm);
                }

                ImmutableTerm childSubject = r2rmlParser.extractSubjectTerm(tm.getSubjectMap());

                TriplesMap parent = robm.getParentMap();
                // TODO: can we just use the parent instead? CHECK
                TriplesMap parentTriple = tripleMaps.stream()
                        .filter(m -> m.equals(parent))
                        .findAny()
                        .orElseThrow(() -> new MinorOntopInternalBugException(
                                "In the R2RML graph, the parent triples map could not be found"));

                ImmutableTerm parentSubject = r2rmlParser.extractSubjectTerm(parentTriple.getSubjectMap());

                ImmutableList<TargetAtom> targetAtoms = predicateTerms.stream()
                        .map(p -> targetAtomFactory.getTripleTargetAtom(childSubject, p, parentSubject))
                        .collect(ImmutableCollectors.toList());

                //finally, create mapping and add it to the list
                //use referenceObjectMap robm as id, because there could be multiple joinCondition in the same triple map
                // TODO: use a R2RML-specific class	instead
                SQLPPTriplesMap ppTriplesMap = new OntopNativeSQLPPTriplesMap("tm-join-"+robm.hashCode(),
                        MAPPING_FACTORY.getSQLQuery(sourceQuery), targetAtoms);
                LOGGER.info("Join \"triples map\" introduced: " + ppTriplesMap);
                joinPPTriplesMapsBuilder.add(ppTriplesMap);
            }

        }
        return joinPPTriplesMapsBuilder.build();
    }


}
