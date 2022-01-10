package it.unibz.inf.ontop;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import java.util.stream.IntStream;

/**
 *
 */
public class OntopModelTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final ImmutableUnificationTools UNIFICATION_TOOLS;
    public static final NoNullValueEnforcer NO_NULL_VALUE_ENFORCER;
    public static final RDF RDF_FACTORY;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;
    public static final CoreSingletons CORE_SINGLETONS;


    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder()
                .enableTestMode()
                .build();
        Injector injector = defaultConfiguration.getInjector();

        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        UNIFICATION_TOOLS = injector.getInstance(ImmutableUnificationTools.class);
        RDF_FACTORY = injector.getInstance(RDF.class);
        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
        NO_NULL_VALUE_ENFORCER = injector.getInstance(NoNullValueEnforcer.class);
        CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
    }

    public static OfflineMetadataProviderBuilder createMetadataProviderBuilder() {
        return new OfflineMetadataProviderBuilder(CORE_SINGLETONS);
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
