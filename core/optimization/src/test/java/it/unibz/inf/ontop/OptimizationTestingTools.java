package it.unibz.inf.ontop;


import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.LegacyVariableGenerator;
import org.apache.commons.rdf.api.RDF;

import java.util.Properties;
import java.util.stream.IntStream;

public class OptimizationTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final JoinLikeOptimizer JOIN_LIKE_OPTIMIZER;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final TermFactory TERM_FACTORY;
    public static final FunctionSymbolFactory FUNCTION_SYMBOL_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final QueryTransformerFactory TRANSFORMER_FACTORY;
    public static final OptimizerFactory OPTIMIZER_FACTORY;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;
    public static final BooleanExpressionPushDownTransformer PUSH_DOWN_BOOLEAN_EXPRESSION_TRANSFORMER;
    public static final DBConstant TRUE, FALSE;
    public static final Constant NULL;
    public static final UnionAndBindingLiftOptimizer UNION_AND_BINDING_LIFT_OPTIMIZER;
    public static final GeneralStructuralAndSemanticIQOptimizer GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER;
    public static final UnionBasedQueryMerger UNION_BASED_QUERY_MERGER;
    public static final RDF RDF_FACTORY;
    public static final CoreSingletons CORE_SINGLETONS;

    public static final Variable X;
    public static final Variable XF0;
    public static final Variable Y;
    public static final Variable W;
    public static final Variable Z;
    public static final Variable A;
    public static final Variable AF0;
    public static final Variable AF1;
    public static final Variable AF1F3;
    public static final Variable AF1F4;
    public static final Variable AF2;
    public static final Variable AF3;
    public static final Variable B;
    public static final Variable BF0;
    public static final Variable BF1;
    public static final Variable BF2;
    public static final Variable BF4F5;
    public static final Variable C;
    public static final Variable CF0;
    public static final Variable CF1;
    public static final Variable D;
    public static final Variable E;
    public static final Variable F;
    public static final Variable F6;
    public static final Variable F0;
    public static final Variable F1;
    public static final Variable F0F2;
    public static final Variable F0F3;
    public static final Variable FF4;
    public static final Variable G;
    public static final Variable GF1;
    public static final Variable H;
    public static final Variable HF0;
    public static final Variable I;
    public static final Variable IF7;
    public static final Variable L;
    public static final Variable M;
    public static final Variable N;
    public static final Variable NF0;
    public static final Variable PROV;
    public static final DBConstant ONE, TWO, ONE_STR, TWO_STR, THREE_STR, FOUR_STR, FIVE_STR;

    public static final AtomPredicate ANS1_AR0_PREDICATE, ANS1_AR1_PREDICATE, ANS1_AR2_PREDICATE, ANS1_AR3_PREDICATE,
            ANS1_AR4_PREDICATE, ANS1_AR5_PREDICATE;

    static {

        // TEMPORARY! TODO: remove it!
        Properties tmpProperties = new Properties();
        tmpProperties.put(VariableGenerator.class.getCanonicalName(), LegacyVariableGenerator.class.getCanonicalName());

        OntopOptimizationConfiguration defaultConfiguration = OntopOptimizationConfiguration.defaultBuilder()
                .properties(tmpProperties)
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        JOIN_LIKE_OPTIMIZER = injector.getInstance(JoinLikeOptimizer.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        FUNCTION_SYMBOL_FACTORY = injector.getInstance(FunctionSymbolFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
        UNION_AND_BINDING_LIFT_OPTIMIZER = injector.getInstance(UnionAndBindingLiftOptimizer.class);
        PUSH_DOWN_BOOLEAN_EXPRESSION_TRANSFORMER = injector.getInstance(BooleanExpressionPushDownTransformer.class);
        TRANSFORMER_FACTORY = injector.getInstance(QueryTransformerFactory.class);
        OPTIMIZER_FACTORY = injector.getInstance(OptimizerFactory.class);
        CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
        GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER = injector.getInstance(GeneralStructuralAndSemanticIQOptimizer.class);

        UNION_BASED_QUERY_MERGER = injector.getInstance(UnionBasedQueryMerger.class);

        NULL = TERM_FACTORY.getNullConstant();
        TRUE = TERM_FACTORY.getDBBooleanConstant(true);
        FALSE = TERM_FACTORY.getDBBooleanConstant(false);
        RDF_FACTORY = injector.getInstance(RDF.class);

        X = TERM_FACTORY.getVariable("x");
        XF0 = TERM_FACTORY.getVariable("xf0");
        Y = TERM_FACTORY.getVariable("y");
        W = TERM_FACTORY.getVariable("w");
        Z = TERM_FACTORY.getVariable("z");
        A = TERM_FACTORY.getVariable("a");
        AF0 = TERM_FACTORY.getVariable("af0");
        AF1 = TERM_FACTORY.getVariable("af1");
        AF1F3 = TERM_FACTORY.getVariable("af1f3");
        AF1F4 = TERM_FACTORY.getVariable("af1f4");
        AF2 = TERM_FACTORY.getVariable("af2");
        AF3 = TERM_FACTORY.getVariable("af3");
        B = TERM_FACTORY.getVariable("b");
        BF0 = TERM_FACTORY.getVariable("bf0");
        BF1 = TERM_FACTORY.getVariable("bf1");
        BF2 = TERM_FACTORY.getVariable("bf2");
        BF4F5 = TERM_FACTORY.getVariable("bf4f5");
        C = TERM_FACTORY.getVariable("c");
        CF0 = TERM_FACTORY.getVariable("cf0");
        CF1 = TERM_FACTORY.getVariable("cf1");
        D = TERM_FACTORY.getVariable("d");
        E = TERM_FACTORY.getVariable("e");
        F = TERM_FACTORY.getVariable("f");
        F6 = TERM_FACTORY.getVariable("f6");
        F0 = TERM_FACTORY.getVariable("f0");
        F1 = TERM_FACTORY.getVariable("f1");
        F0F2 = TERM_FACTORY.getVariable("f0f2");
        F0F3 = TERM_FACTORY.getVariable("f0f3");
        FF4 = TERM_FACTORY.getVariable("ff4");
        G = TERM_FACTORY.getVariable("g");
        GF1 = TERM_FACTORY.getVariable("gf1");
        H = TERM_FACTORY.getVariable("h");
        HF0 = TERM_FACTORY.getVariable("hf0");
        I = TERM_FACTORY.getVariable("i");
        IF7 = TERM_FACTORY.getVariable("if7");
        L = TERM_FACTORY.getVariable("l");
        M = TERM_FACTORY.getVariable("m");
        N = TERM_FACTORY.getVariable("n");
        NF0 = TERM_FACTORY.getVariable("nf0");
        PROV = TERM_FACTORY.getVariable("prov");
        ONE = TERM_FACTORY.getDBIntegerConstant(1);
        TWO = TERM_FACTORY.getDBIntegerConstant(2);
        ONE_STR = TERM_FACTORY.getDBStringConstant("1");
        TWO_STR = TERM_FACTORY.getDBStringConstant("2");
        THREE_STR = TERM_FACTORY.getDBStringConstant("3");
        FOUR_STR = TERM_FACTORY.getDBStringConstant("4");
        FIVE_STR = TERM_FACTORY.getDBStringConstant("5");

        ANS1_AR0_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(0);
        ANS1_AR1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
        ANS1_AR2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
        ANS1_AR3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
        ANS1_AR4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(4);
        ANS1_AR5_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(5);
    }

    public static OfflineMetadataProviderBuilder3 createMetadataProviderBuilder() {
        return new OfflineMetadataProviderBuilder3(CORE_SINGLETONS);
    }

    public static class OfflineMetadataProviderBuilder3 extends OfflineMetadataProviderBuilder {

        public OfflineMetadataProviderBuilder3(CoreSingletons coreSingletons) { super(coreSingletons); }

        public NamedRelationDefinition createRelation(String tableName, int arity, DBTermType termType, boolean canBeNull) {
            QuotedIDFactory idFactory = getQuotedIDFactory();
            RelationDefinition.AttributeListBuilder builder =  DatabaseTableDefinition.attributeListBuilder();
            for (int i = 1; i <= arity; i++) {
                builder.addAttribute(idFactory.createAttributeID("col" + i), termType, canBeNull);
            }
            RelationID id = idFactory.createRelationID(tableName);
            return createDatabaseRelation(ImmutableList.of(id), builder);
        }

        public NamedRelationDefinition createRelation(int tableNumber, int arity, DBTermType termType, String prefix, boolean canBeNull) {
            return createRelation(prefix + "TABLE" + tableNumber + "AR" + arity, arity, termType, canBeNull);
        }

        public NamedRelationDefinition createRelationWithPK(int tableNumber, int arity) {
            DBTermType stringDBType = getDBTypeFactory().getDBStringType();
            NamedRelationDefinition tableDef = createRelation(tableNumber, arity, stringDBType, "PK_", false);
            UniqueConstraint.primaryKeyOf(tableDef.getAttribute(1));
            return tableDef;
        }

        public NamedRelationDefinition createRelationWithUC(int tableNumber, int arity, boolean canNull) {
            DBTermType stringDBType = getDBTypeFactory().getDBStringType();
            NamedRelationDefinition tableDef = createRelation(tableNumber, arity, stringDBType, "UC_", canNull);
            UniqueConstraint.builder(tableDef, "uc_" + tableNumber)
                    .addDeterminant(1)
                    .build();
            return tableDef;
        }

        public RelationDefinition createRelationWithStringAttributes(int tableNumber, int arity, boolean canBeNull) {
            return createRelation(tableNumber, arity, getDBTypeFactory().getDBStringType(), "STR_", canBeNull);
        }

        public RelationDefinition createRelationWithIntAttributes(int tableNumber, int arity, boolean canBeNull) {
            return createRelation(tableNumber, arity, getDBTypeFactory().getDBLargeIntegerType(), "INT_", canBeNull);
        }

        public RelationDefinition createRelationWithUuidAttributes(int tableNumber, int arity, boolean canBeNull) {
            return createRelation(tableNumber, arity, getDBTypeFactory().getDBTermType("UUID"), "UUID_", canBeNull);
        }
    }

    public static ExtensionalDataNode createExtensionalDataNode(RelationDefinition relation, ImmutableList<VariableOrGroundTerm> arguments) {
        return IQ_FACTORY.createExtensionalDataNode(relation,
                IntStream.range(0, arguments.size())
                        .boxed()
                        .collect(ImmutableCollectors.toMap(
                                i -> i,
                                arguments::get)));
    }
}
