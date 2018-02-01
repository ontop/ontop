package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.TemporalOBDASpecification;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.impl.IntervalAndIntermediateQuery;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

public interface TemporalSpecificationFactory extends SpecificationFactory{

    TemporalMapping createTemporalMapping(MappingMetadata metadata, ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> mappingMap, ExecutorRegistry executorRegistry);

    TemporalOBDASpecification createTemporalSpecification(Mapping saturatedMapping, @Assisted("dbMetadata") DBMetadata dbMetadata, TemporalMapping saturatedTemporalMapping, @Assisted("temporalDBMetadata") DBMetadata temporalDBMetadata, ClassifiedTBox tBox);
}
