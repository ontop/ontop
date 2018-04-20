package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingVariableNameNormalizer;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Singleton
public class MappingVariableNameNormalizerImpl implements MappingVariableNameNormalizer {

    private final SpecificationFactory specificationFactory;
    private final QueryTransformerFactory transformerFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private MappingVariableNameNormalizerImpl(SpecificationFactory specificationFactory,
                                              QueryTransformerFactory transformerFactory,
                                              SubstitutionFactory substitutionFactory,
                                              TermFactory termFactory) {
        this.specificationFactory = specificationFactory;
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public Mapping normalize(Mapping mapping) {
        // TODO: generalize
        Optional<RDFAtomPredicate> triplePredicate = mapping.getRDFAtomPredicates().stream()
                .filter(p -> p instanceof TriplePredicate)
                .findAny();

        return triplePredicate
                .map(p -> normalize(mapping, p))
                .orElseGet(() -> specificationFactory.createMapping(mapping.getMetadata(),
                        ImmutableMap.of(),
                        ImmutableMap.of()));
    }

    private Mapping normalize(Mapping mapping, RDFAtomPredicate rdfAtomPredicate) {

        Stream<IQ> queryPropertiesStream = mapping.getRDFProperties(rdfAtomPredicate).stream()
                .map(i -> mapping.getRDFPropertyDefinition(rdfAtomPredicate, i))
                .filter(Optional::isPresent)
                .map(Optional::get);

        Stream<IQ> queryClassesStream = mapping.getRDFClasses(rdfAtomPredicate).stream()
                .map(i -> mapping.getRDFClassDefinition(rdfAtomPredicate, i))
                .filter(Optional::isPresent)
                .map(Optional::get);

        ImmutableMap<IRI, IQ> normalizedMappingPropertyMap = renameQueries(queryPropertiesStream)
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractPropertiesIRI,
                        q -> q
                ));

        ImmutableMap<IRI, IQ> normalizedMappingClassyMap = renameQueries(queryClassesStream)
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractClassIRI,
                        q -> q
                ));

        return specificationFactory.createMapping(mapping.getMetadata(),  normalizedMappingPropertyMap,
                normalizedMappingClassyMap);
    }


    /**
     * Appends a different suffix to each query
     */
    private Stream<IQ> renameQueries(Stream<IQ> queryStream) {
        AtomicInteger i = new AtomicInteger(0);
        return queryStream
                .map(m -> appendSuffixToVariableNames(transformerFactory, m, i.incrementAndGet()));
    }

    private IQ appendSuffixToVariableNames(QueryTransformerFactory transformerFactory,
                                           IQ query, int suffix) {
        Map<Variable, Variable> substitutionMap =
                query.getTree().getKnownVariables().stream()
                        .collect(Collectors.toMap(
                                v -> v,
                                v -> termFactory.getVariable(v.getName()+"m"+suffix)));
        QueryRenamer queryRenamer = transformerFactory.createRenamer(substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap));
        return queryRenamer.transform(query);
    }
}
