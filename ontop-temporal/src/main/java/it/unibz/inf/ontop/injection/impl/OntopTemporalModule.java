package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.SameAsRewriter;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintExtractor;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.TemporalPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.StaticRuleMappingSaturator;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingTransformer;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalog2QueryMappingConverter;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalogProgram2QueryConverter;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.*;

import java.util.Properties;

/**
 * Created by elem on 08/08/2017.
 */
public class OntopTemporalModule extends OntopAbstractModule {

    private final OntopMappingSQLAllSettings settings;

    //TODO this method can be removed
    protected OntopTemporalModule(OntopTemporalSQLOWLAPIConfiguration configuration) {
        super(configuration.getSettings());
        settings = ((OntopTemporalMappingSQLAllConfiguration)configuration).getSettings();
    }

    OntopTemporalModule(OntopTemporalMappingSQLAllConfigurationImpl configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSQLAllSettings.class).toInstance(settings);

        bindFromSettings(SQLPPMappingFactory.class);
        bindFromSettings(SQLMappingParser.class);
        bindFromSettings(SQLPPMappingConverter.class);
        bindFromSettings(PreProcessedImplicitRelationalDBConstraintExtractor.class);
        bindFromSettings(MappingExtractor.class);
        bindFromSettings(TemporalMappingParser.class);
        bindFromSettings(TemporalMappingExtractor.class);
        bindFromSettings(TemporalPPMappingConverter.class);
        bindFromSettings(TemporalDatalog2QueryMappingConverter.class);
        bindFromSettings(TemporalDatalogProgram2QueryConverter.class);
        bindFromSettings(TemporalMappingTransformer.class);
        bindFromSettings(TemporalMappingSaturator.class);
        bindFromSettings(StaticRuleMappingSaturator.class);
        //bindFromSettings(TemporalMapping.class);

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
                BinaryNonCommutativeIQTree.class,
                IQ.class,
                NaryIQTree.class,
                UnaryIQTree.class,
                ExtensionalDataNode.class,
                IntensionalDataNode.class,
                EmptyNode.class,
                TrueNode.class,
                TemporalJoinNode.class,
                BoxMinusNode.class,
                BoxPlusNode.class,
                DiamondMinusNode.class,
                DiamondPlusNode.class,
                SinceNode.class,
                UntilNode.class,
                TemporalCoalesceNode.class
                //TemporalRange.class
                ),
                TemporalIntermediateQueryFactory.class);
        install(iqFactoryModule);

        Module nativeQLFactoryModule = buildFactory(
                ImmutableList.of(RDBMetadataExtractor.class),
                NativeQueryLanguageComponentFactory.class);
        install(nativeQLFactoryModule);

        /*Module translationFactoryModule = buildFactory(
                ImmutableList.of(
                        QueryUnfolder.class,
                        NativeQueryGenerator.class,
                        SameAsRewriter.class,
                        InputQueryTranslator.class),
                TranslationFactory.class);
        install(translationFactoryModule);*/

    }
}
