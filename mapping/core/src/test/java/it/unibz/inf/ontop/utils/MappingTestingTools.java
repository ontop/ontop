package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.datalog.UnionFlattener;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.spec.fact.FactExtractor;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;


public class MappingTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final TargetAtomFactory TARGET_ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final RDF RDF_FACTORY;
    public static final MappingVariableNameNormalizer MAPPING_NORMALIZER;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;
    public static final CoreSingletons CORE_SINGLETONS;

    public static final TargetQueryParserFactory TARGET_QUERY_PARSER_FACTORY;

    public static final FactExtractor FACT_EXTRACTOR;
    public static final FactIntoMappingConverter A_BOX_FACT_INTO_MAPPING_CONVERTER;
    public static final OntopMappingSettings ONTOP_MAPPING_SETTINGS;
    public static final MappingSameAsInverseRewriter SAME_AS_INVERSE_REWRITER;
    public static final MappingSaturator MAPPING_SATURATOR;

    public static final PrefixManager EMPTY_PREFIX_MANAGER;
    public static final UnionFlattener UNION_FLATTENER;
    public static final SpecificationFactory SPECIFICATION_FACTORY;

    public static final MappingCQCOptimizer MAPPING_CQC_OPTIMIZER;

    public static final NamedRelationDefinition TABLE1_AR2;
    public static final NamedRelationDefinition TABLE2_AR2;
    public static final NamedRelationDefinition TABLE1_AR3;
    public static final NamedRelationDefinition TABLE2_AR3;
    public static final NamedRelationDefinition TABLE3_AR3;
    public static final NamedRelationDefinition TABLE4_AR3;

    static {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_NORMALIZER = injector.getInstance(MappingVariableNameNormalizer.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        TARGET_ATOM_FACTORY = injector.getInstance(TargetAtomFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        FACT_EXTRACTOR = injector.getInstance(FactExtractor.class);
        A_BOX_FACT_INTO_MAPPING_CONVERTER = injector.getInstance(FactIntoMappingConverter.class);
        ONTOP_MAPPING_SETTINGS = injector.getInstance(OntopMappingSettings.class);
        SAME_AS_INVERSE_REWRITER = injector.getInstance(MappingSameAsInverseRewriter.class);
        MAPPING_SATURATOR = injector.getInstance(MappingSaturator.class);
        UNION_FLATTENER = injector.getInstance(UnionFlattener.class);
        SPECIFICATION_FACTORY = injector.getInstance(SpecificationFactory.class);
        RDF_FACTORY = injector.getInstance(RDF.class);
        TARGET_QUERY_PARSER_FACTORY = injector.getInstance(TargetQueryParserFactory.class);
        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
        CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);

        EMPTY_PREFIX_MANAGER = SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of());

        MAPPING_CQC_OPTIMIZER = injector.getInstance(MappingCQCOptimizer.class);

        OfflineMetadataProviderBuilder2 builder = createMetadataProviderBuilder();
        TABLE1_AR2 = builder.createRelationPredicate(1, 2);
        TABLE2_AR2 = builder.createRelationPredicate(2, 2);
        TABLE1_AR3 = builder.createRelationPredicate(4, 3);
        TABLE2_AR3 = builder.createRelationPredicate(5, 3);
        TABLE3_AR3 = builder.createRelationPredicate(6, 3);
        TABLE4_AR3 = builder.createRelationPredicate(7, 3);
    }

    public static OfflineMetadataProviderBuilder2 createMetadataProviderBuilder() {
        return new OfflineMetadataProviderBuilder2(CORE_SINGLETONS);
    }

    public static class OfflineMetadataProviderBuilder2 extends OfflineMetadataProviderBuilder {

        public OfflineMetadataProviderBuilder2(CoreSingletons coreSingletons) { super(coreSingletons); }

        private NamedRelationDefinition createRelationPredicate(int tableNumber, int arity) {
            QuotedIDFactory idFactory = getQuotedIDFactory();
            DBTermType stringDBType = getDBTypeFactory().getDBStringType();

            RelationDefinition.AttributeListBuilder builder = DatabaseTableDefinition.attributeListBuilder();
            for (int i = 1 ; i <= arity; i++) {
                builder.addAttribute(idFactory.createAttributeID("col" + i), stringDBType, false);
            }
            RelationID id = idFactory.createRelationID("TABLE" + tableNumber + "AR" + arity);
            return createDatabaseRelation(ImmutableList.of(id), builder);
        }
    }
}
