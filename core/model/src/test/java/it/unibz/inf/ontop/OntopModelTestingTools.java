package it.unibz.inf.ontop;

import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadataTestingTools;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

/**
 *
 */
public class OntopModelTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final TypeFactory TYPE_FACTORY;

    private static final Relation2Predicate RELATION_2_PREDICATE;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder()
                .enableTestMode()
                .build();
        Injector injector = defaultConfiguration.getInjector();

        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        RELATION_2_PREDICATE = injector.getInstance(Relation2Predicate.class);
        TERM_FACTORY = OntopModelSingletons.TERM_FACTORY;
        TYPE_FACTORY = OntopModelSingletons.TYPE_FACTORY;

        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
    }

    public static BasicDBMetadata createDummyMetadata() {
        return DBMetadataTestingTools.createDummyMetadata(ATOM_FACTORY, RELATION_2_PREDICATE);
    }

}
