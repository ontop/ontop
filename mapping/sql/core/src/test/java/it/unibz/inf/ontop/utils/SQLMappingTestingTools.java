package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.pp.impl.LegacySQLPPMappingConverter;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;

public class SQLMappingTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final DBFunctionSymbolFactory DB_FS_FACTORY;
    public static final TargetAtomFactory TARGET_ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final SpecificationFactory MAPPING_FACTORY;
    public static final RDF RDF_FACTORY;
    public static final TargetQueryParserFactory TARGET_QUERY_PARSER_FACTORY;
    public static final CoreSingletons CORE_SINGLETONS;
    public static final LegacySQLPPMappingConverter LEGACY_SQL_PP_MAPPING_CONVERTER;

    public static final BasicDBMetadata DEFAULT_DUMMY_DB_METADATA;


    static {
        OntopMappingSQLConfiguration defaultConfiguration = OntopMappingSQLConfiguration.defaultBuilder()
                .jdbcUrl("jdbc:h2:mem:something")
                .jdbcDriver("org.h2.Driver")
                .jdbcUser("user")
                .jdbcPassword("password")
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_FACTORY = injector.getInstance(SpecificationFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TARGET_ATOM_FACTORY = injector.getInstance(TargetAtomFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        DB_FS_FACTORY = injector.getInstance(DBFunctionSymbolFactory.class);

        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyBasicDBMetadata.class);
        RDF_FACTORY = injector.getInstance(RDF.class);

        TARGET_QUERY_PARSER_FACTORY = injector.getInstance(TargetQueryParserFactory.class);
        CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
        LEGACY_SQL_PP_MAPPING_CONVERTER = injector.getInstance(LegacySQLPPMappingConverter.class);
    }
}
