package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.QuadrupleDefinition;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;

public interface TemporalSpecificationFactory  extends SpecificationFactory{

    TemporalMapping createTemporalMapping(MappingMetadata metadata, ImmutableMap<AtomPredicate, QuadrupleDefinition> mappingMap, ExecutorRegistry executorRegistry);
}
