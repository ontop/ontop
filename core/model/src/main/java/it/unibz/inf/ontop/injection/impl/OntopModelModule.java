package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.evaluator.ExpressionNormalizer;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.*;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.RDF;


public class OntopModelModule extends OntopAbstractModule {

    protected OntopModelModule(OntopModelConfiguration configuration) {
        this(configuration.getSettings());
    }

    protected OntopModelModule(OntopModelSettings settings) {
        super(settings);
    }

    @Override
    protected void configure() {
        configureCoreConfiguration();

        // Core factories: Too central to be overloaded from the properties
        bindFromSettings(TypeFactory.class);
        bindFromSettings(FunctionSymbolFactory.class);
        bindFromSettings(TermFactory.class);
        bindFromSettings(AtomFactory.class);
        bindFromSettings(SubstitutionFactory.class);

        bindFromSettings(TermNullabilityEvaluator.class);
        bindFromSettings(NoNullValueEnforcer.class);
        bindFromSettings(ExpressionNormalizer.class);
        bindFromSettings(ConditionSimplifier.class);
        bindFromSettings(ConstructionSubstitutionNormalizer.class);
        bindFromSettings(FilterNormalizer.class);
        bindFromSettings(InnerJoinNormalizer.class);
        bindFromSettings(LeftJoinNormalizer.class);
        bindFromSettings(OrderByNormalizer.class);
        bindFromSettings(DistinctNormalizer.class);
        bindFromSettings(AggregationNormalizer.class);
        bindFromSettings(NotRequiredVariableRemover.class);
        bindFromSettings(NotYetTypedEqualityTransformer.class);
        bindFromSettings(RDF.class);
        bindFromSettings(SingleTermTypeExtractor.class);
        bindFromSettings(DBFunctionSymbolFactory.class);
        bindFromSettings(TypeConstantDictionary.class);
        bindFromSettings(IQTreeCache.class);

        bind(CoreSingletons.class).to(CoreSingletonsImpl.class);

        Module utilsModule = buildFactory(
                ImmutableList.of(
                        VariableGenerator.class,
                        VariableNullability.class,
                        ProjectionDecomposer.class
                ),
                CoreUtilsFactory.class);
        install(utilsModule);

        Module dbTypeFactoryModule = buildFactory(ImmutableList.of(DBTypeFactory.class), DBTypeFactory.Factory.class);
        install(dbTypeFactoryModule);

        Module iqFactoryModule = buildFactory(ImmutableList.of(
                ConstructionNode.class,
                UnionNode.class,
                InnerJoinNode.class,
                LeftJoinNode.class,
                FilterNode.class,
                ExtensionalDataNode.class,
                IntensionalDataNode.class,
                NativeNode.class,
                ValuesNode.class,
                EmptyNode.class,
                TrueNode.class,
                DistinctNode.class,
                SliceNode.class,
                OrderByNode.class,
                OrderByNode.OrderComparator.class,
                AggregationNode.class,
                UnaryIQTree.class,
                BinaryNonCommutativeIQTree.class,
                NaryIQTree.class,
                IQ.class,
                IQTreeCache.class
                ),
                IntermediateQueryFactory.class);
        install(iqFactoryModule);

        Module queryTransformerModule = buildFactory(ImmutableList.of(
                QueryRenamer.class),
                QueryTransformerFactory.class);
        install(queryTransformerModule);
    }
}
