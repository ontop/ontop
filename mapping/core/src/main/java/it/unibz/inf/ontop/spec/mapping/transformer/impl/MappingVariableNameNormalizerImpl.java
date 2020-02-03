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
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
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
    public MappingInTransformation normalize(MappingInTransformation mapping) {

        AtomicInteger i = new AtomicInteger(0);

        ImmutableTable<RDFAtomPredicate, IRI, IQ> newPropertyTable = normalize(mapping.getRDFPropertyQueries(), i);
        ImmutableTable<RDFAtomPredicate, IRI, IQ> newClassTable = normalize(mapping.getRDFClassQueries(), i);

        return specificationFactory.createMapping(newPropertyTable, newClassTable);
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> normalize(
            ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>> queryCells, AtomicInteger i) {
        return queryCells.stream()
                .map(c -> Tables.immutableCell(
                        c.getRowKey(),
                        c.getColumnKey(),
                        appendSuffixToVariableNames(c.getValue(), i.incrementAndGet())))
                .collect(ImmutableCollectors.toTable());
    }

    private IQ appendSuffixToVariableNames(IQ query, int suffix) {
        Map<Variable, Variable> substitutionMap =
                query.getTree().getKnownVariables().stream()
                        .collect(Collectors.toMap(
                                v -> v,
                                v -> termFactory.getVariable(v.getName()+"m"+suffix)));
        QueryRenamer queryRenamer = transformerFactory.createRenamer(substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap));
        return queryRenamer.transform(query);
    }
}
