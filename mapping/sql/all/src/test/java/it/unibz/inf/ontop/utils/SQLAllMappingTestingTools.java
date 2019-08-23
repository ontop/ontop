package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;

public class SQLAllMappingTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final TargetAtomFactory TARGET_ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final RDF RDF_FACTORY;
    public static final SpecificationFactory MAPPING_FACTORY;
    private static final DummyRDBMetadata DEFAULT_DUMMY_DB_METADATA;

    static {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_FACTORY = injector.getInstance(SpecificationFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        TARGET_ATOM_FACTORY = injector.getInstance(TargetAtomFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        RDF_FACTORY = injector.getInstance(RDF.class);

        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyRDBMetadata.class);

        EMPTY_METADATA = DEFAULT_DUMMY_DB_METADATA.clone();
        EMPTY_METADATA.freeze();
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
    }

    public static RDBMetadata createDummyMetadata() {
        return DEFAULT_DUMMY_DB_METADATA.clone();
    }
}
