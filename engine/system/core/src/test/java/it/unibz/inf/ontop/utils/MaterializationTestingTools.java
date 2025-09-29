package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

public class MaterializationTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final RDF RDF_FACTORY;
    public static final QueryTransformerFactory QUERY_TRANSFORMER_FACTORY;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;
    public static final CoreSingletons CORE_SINGLETONS;

    public static final OntopModelConfiguration defaultConfiguration;

    static {

         defaultConfiguration = OntopModelConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        RDF_FACTORY = injector.getInstance(RDF.class);
        QUERY_TRANSFORMER_FACTORY = injector.getInstance(QueryTransformerFactory.class);
        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
        CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);

    }

    public static OfflineMetadataProviderBuilder createMetadataProviderBuilder() {
        return new OfflineMetadataProviderBuilder(CORE_SINGLETONS);
    }

    public static IRI getIRI(String prefix, String suffix) {
        return RDF_FACTORY.createIRI(prefix + suffix);
    }

    public static ImmutableFunctionalTerm generatePersonURI(ImmutableList<Template.Component> template, VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(template, ImmutableList.of(argument));
    }

    public static IRIConstant getConstantIRI(IRI iri) {
        return TERM_FACTORY.getConstantIRI(iri);
    }

    public static ImmutableFunctionalTerm getRDFLiteral(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, RDF_FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#string"));
    }


}
