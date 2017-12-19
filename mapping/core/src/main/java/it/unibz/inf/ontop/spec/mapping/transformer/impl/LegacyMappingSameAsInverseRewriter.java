package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSameAsInverseRewriter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.stream.Stream;

public class LegacyMappingSameAsInverseRewriter implements MappingSameAsInverseRewriter {

    private final boolean enabled;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;
    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;

    @Inject
    private LegacyMappingSameAsInverseRewriter(OntopMappingSettings settings, Mapping2DatalogConverter mapping2DatalogConverter,
                                               Datalog2QueryMappingConverter datalog2MappingConverter,
                                               TermFactory termFactory, DatalogFactory datalogFactory) {
        this.enabled = settings.isSameAsInMappingsEnabled();
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
    }

    @Override
    public Mapping rewrite(Mapping mapping, DBMetadata dbMetadata) {
        if(enabled){
            ImmutableList<CQIE> rules = mapping2DatalogConverter.convert(mapping)
                    .collect(ImmutableCollectors.toList());
            ImmutableList<CQIE> updatedRules = addSameAsInverse(rules);
            return datalog2MappingConverter.convertMappingRules(updatedRules, dbMetadata, mapping.getExecutorRegistry(),
                    mapping.getMetadata());
        }
        return mapping;
    }

    /**
     * add the inverse of the same as present in the mapping
     */
    private ImmutableList<CQIE> addSameAsInverse(ImmutableList<CQIE> mappingRules) {
        Stream<CQIE> newRuleStream = mappingRules.stream()
                // the targets are already split. We have only one target atom
                .filter(r -> r.getHead().getFunctionSymbol().getName().equals(IriConstants.SAME_AS))
                .map(r -> {
                    Function head = r.getHead();
                    Function inversedHead = termFactory.getFunction(head.getFunctionSymbol(),
                            head.getTerm(1),
                            head.getTerm(0));
                    return datalogFactory.getCQIE(inversedHead, new ArrayList<>(r.getBody()));
                });

        return Stream.concat(mappingRules.stream(), newRuleStream)
                .collect(ImmutableCollectors.toList());
    }
}
