package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;

public class SQLAllMappingTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TargetAtomFactory TARGET_ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final RDF RDF_FACTORY;
    public static final SpecificationFactory MAPPING_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final CoreSingletons CORE_SINGLETONS;

    static {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_FACTORY = injector.getInstance(SpecificationFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TARGET_ATOM_FACTORY = injector.getInstance(TargetAtomFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        RDF_FACTORY = injector.getInstance(RDF.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
    }

    public static OfflineMetadataProviderBuilder createMetadataProviderBuilder() {
        return new OfflineMetadataProviderBuilder(CORE_SINGLETONS);
    }

}
