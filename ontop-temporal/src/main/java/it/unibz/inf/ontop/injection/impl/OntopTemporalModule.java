package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintExtractor;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.TemporalPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingTransformer;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalog2QueryMappingConverter;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalogProgram2QueryConverter;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.*;

/**
 * Created by elem on 08/08/2017.
 */
public class OntopTemporalModule extends OntopAbstractModule{

    private final OntopMappingSQLAllSettings settings;

    protected OntopTemporalModule(OntopTemporalSQLOWLAPIConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    protected OntopTemporalModule(OntopTemporalMappingSQLAllConfigurationImpl configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSQLAllSettings.class).toInstance(settings);

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
        //bindFromPreferences(TemporalMapping.class);

        Module specFactoryModule = buildFactory(ImmutableList.of(
                PrefixManager.class,
                MappingMetadata.class,
                Mapping.class,
                OBDASpecification.class,
                TemporalMapping.class
        ), TemporalSpecificationFactory.class);
        install(specFactoryModule);

        Module iqFactoryModule = buildFactory(ImmutableList.of(
                IntermediateQueryBuilder.class,
                TemporalIntermediateQueryBuilder.class,
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
