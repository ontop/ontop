package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.reformulation.rewriting.LinearInclusionDependencyTools;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.impl.CQCUtilities;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DummyBasicDBMetadata;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;

public class ReformulationTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final DatalogFactory DATALOG_FACTORY;
    public static final Relation2Predicate RELATION_2_PREDICATE;
    public static final SpecificationFactory MAPPING_FACTORY;
    public static final LinearInclusionDependencyTools INCLUSION_DEPENDENCY_TOOLS;
    private static final DummyBasicDBMetadata DEFAULT_DUMMY_DB_METADATA;

    public static final SubstitutionUtilities SUBSTITUTION_UTILITIES;
    public static final UnifierUtilities UNIFIER_UTILITIES;
    public static final CQCUtilities CQC_UTILITIES;

    static {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_FACTORY = injector.getInstance(SpecificationFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        DATALOG_FACTORY = injector.getInstance(DatalogFactory.class);
        RELATION_2_PREDICATE = injector.getInstance(Relation2Predicate.class);
        INCLUSION_DEPENDENCY_TOOLS = injector.getInstance(LinearInclusionDependencyTools.class);

        SUBSTITUTION_UTILITIES = injector.getInstance(SubstitutionUtilities.class);
        UNIFIER_UTILITIES = injector.getInstance(UnifierUtilities.class);
        CQC_UTILITIES = injector.getInstance(CQCUtilities.class);

        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyBasicDBMetadata.class);
        EMPTY_METADATA = DEFAULT_DUMMY_DB_METADATA.clone();
        EMPTY_METADATA.freeze();
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
    }
}
