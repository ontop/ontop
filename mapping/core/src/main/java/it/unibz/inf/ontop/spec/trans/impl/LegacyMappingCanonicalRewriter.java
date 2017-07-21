package it.unibz.inf.ontop.spec.trans.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.spec.trans.MappingCanonicalRewriter;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.CanonicalIRIRewriter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.stream.Collectors;


@Singleton
public class LegacyMappingCanonicalRewriter implements MappingCanonicalRewriter {

    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;

    @Inject
    private LegacyMappingCanonicalRewriter(Mapping2DatalogConverter mapping2DatalogConverter,
                                           Datalog2QueryMappingConverter datalog2MappingConverter) {
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
    }

    @Override
    public Mapping rewrite(Mapping mapping, DBMetadata dbMetadata) {
        ImmutableList<CQIE> inputMappingRules = mapping2DatalogConverter.convert(mapping).collect(ImmutableCollectors.toList());

        List<CQIE> canonicalRules = new CanonicalIRIRewriter().buildCanonicalIRIMappings(inputMappingRules);

        return datalog2MappingConverter.convertMappingRules(ImmutableList.copyOf(canonicalRules),
                dbMetadata, mapping.getExecutorRegistry(), mapping.getMetadata());
    }
}
