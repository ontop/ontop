package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingVariableNameNormalizer;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
    public MappingInTransformation normalize(MappingInTransformation mapping) {
        AtomicInteger i = new AtomicInteger(0);
        return specificationFactory.createMapping(
                mapping.getAssertions().entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> appendSuffixToVariableNames(e.getValue(), i.incrementAndGet()))));
    }

    private IQ appendSuffixToVariableNames(IQ query, int suffix) {
        ImmutableMap<Variable, Variable> substitutionMap =
                query.getTree().getKnownVariables().stream()
                        .collect(ImmutableCollectors.toMap(
                                Function.identity(),
                                v -> termFactory.getVariable(v.getName() + "m" + suffix)));
        QueryRenamer queryRenamer = transformerFactory.createRenamer(
                substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap));
        return queryRenamer.transform(query);
    }
}
