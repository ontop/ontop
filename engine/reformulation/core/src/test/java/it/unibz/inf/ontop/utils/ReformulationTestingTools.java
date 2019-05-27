package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.reformulation.rewriting.ImmutableLinearInclusionDependenciesTools;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.ImmutableHomomorphismUtilities;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DummyBasicDBMetadata;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.apache.commons.rdf.api.RDF;

public class ReformulationTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final FunctionSymbolFactory FS_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final DatalogFactory DATALOG_FACTORY;
    public static final SpecificationFactory MAPPING_FACTORY;
    private static final DummyBasicDBMetadata DEFAULT_DUMMY_DB_METADATA;
    public static final ImmutabilityTools IMMUTABILITY_TOOLS;
    public static final ImmutableLinearInclusionDependenciesTools IMMUTABLE_LINEAR_INCLUSION_DEPENDENCIES_TOOLS;

    public static final SubstitutionUtilities SUBSTITUTION_UTILITIES;
    public static final UnifierUtilities UNIFIER_UTILITIES;

    public static final RDF RDF_FACTORY;

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
        FS_FACTORY = injector.getInstance(FunctionSymbolFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        DATALOG_FACTORY = injector.getInstance(DatalogFactory.class);
        IMMUTABLE_LINEAR_INCLUSION_DEPENDENCIES_TOOLS = injector.getInstance(ImmutableLinearInclusionDependenciesTools.class);

        SUBSTITUTION_UTILITIES = injector.getInstance(SubstitutionUtilities.class);
        UNIFIER_UTILITIES = injector.getInstance(UnifierUtilities.class);
        IMMUTABILITY_TOOLS = injector.getInstance(ImmutabilityTools.class);

        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyBasicDBMetadata.class);
        EMPTY_METADATA = DEFAULT_DUMMY_DB_METADATA.clone();
        EMPTY_METADATA.freeze();

        RDF_FACTORY = injector.getInstance(RDF.class);
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
    }
}
