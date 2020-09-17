package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ABoxFactIntoMappingConverterImpl implements ABoxFactIntoMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected ABoxFactIntoMappingConverterImpl(
            TermFactory termFactory, IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public ImmutableList<MappingAssertion> convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled) {
        return null;
    }
}
