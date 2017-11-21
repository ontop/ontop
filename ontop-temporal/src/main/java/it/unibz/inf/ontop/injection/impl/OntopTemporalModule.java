package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintExtractor;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.TemporalPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingTransformer;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalog2QueryMappingConverter;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalogProgram2QueryConverter;
import it.unibz.inf.ontop.temporal.iq.node.*;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

/**
 * Created by elem on 08/08/2017.
 */
public class OntopTemporalModule extends OntopAbstractModule{

    private final OntopMappingSQLTemporalSettings settings;

    protected OntopTemporalModule(OntopMappingSQLTemporalConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSQLTemporalSettings.class).toInstance(settings);

        bindFromPreferences(SQLPPMappingFactory.class);
        bindFromPreferences(SQLMappingParser.class);
        bindFromPreferences(SQLPPMappingConverter.class);
        bindFromPreferences(PreProcessedImplicitRelationalDBConstraintExtractor.class);
        bindFromPreferences(MappingExtractor.class);
        bindFromPreferences(TemporalMappingExtractor.class);
        bindFromPreferences(TemporalMappingParser.class);
        bindFromPreferences(TemporalPPMappingConverter.class);
        bindFromPreferences(TemporalDatalog2QueryMappingConverter.class);
        bindFromPreferences(TemporalDatalogProgram2QueryConverter.class);
        bindFromPreferences(TemporalMappingTransformer.class);
        bindFromPreferences(TemporalMappingSaturator.class);

        Module iqFactoryModule = buildFactory(ImmutableList.of(
                IntermediateQueryBuilder.class,
                ConstructionNode.class,
                UnionNode.class,
                InnerJoinNode.class,
                LeftJoinNode.class,
                FilterNode.class,
                ExtensionalDataNode.class,
                IntensionalDataNode.class,
                EmptyNode.class,
                TrueNode.class,
                TemporalJoinNode.class,
                BoxMinusNode.class,
                BoxPlusNode.class,
                DiamondMinusNode.class,
                DiamondPlusNode.class,
                TemporalCoalesceNode.class
                //TemporalRange.class
                ),
                TemporalIntermediateQueryFactory.class);
        install(iqFactoryModule);

        Module nativeQLFactoryModule = buildFactory(
                ImmutableList.of(RDBMetadataExtractor.class),
                NativeQueryLanguageComponentFactory.class);
        install(nativeQLFactoryModule);
    }
}
