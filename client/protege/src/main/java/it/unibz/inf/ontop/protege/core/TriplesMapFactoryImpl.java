package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.protege.mapping.TriplesMapFactory;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.TargetQueryRenderer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

public class TriplesMapFactoryImpl implements TriplesMapFactory {
    private SQLPPMappingFactory ppMappingFactory;
    private TargetQueryParserFactory targetQueryParserFactory;
    private TargetAtomFactory targetAtomFactory;
    private SubstitutionFactory substitutionFactory;
    private SQLPPSourceQueryFactory sourceQueryFactory;
    private TermFactory termFactory;

    private final OntologyPrefixManager prefixManager;

    TriplesMapFactoryImpl(OntologyPrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    void reset(OntopMappingSQLAllConfiguration configuration) {
        termFactory = configuration.getTermFactory();

        Injector injector = configuration.getInjector();
        ppMappingFactory = injector.getInstance(SQLPPMappingFactory.class);
        targetAtomFactory = injector.getInstance(TargetAtomFactory.class);
        substitutionFactory = injector.getInstance(SubstitutionFactory.class);
        targetQueryParserFactory = injector.getInstance(TargetQueryParserFactory.class);
        sourceQueryFactory = injector.getInstance(SQLPPSourceQueryFactory.class);
    }

    @Override
    public TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableMap<Variable, ImmutableTerm> map) {
        return targetAtomFactory.getTargetAtom(projectionAtom, substitutionFactory.getSubstitution(map));
    }

    @Override
    public IRIConstant getConstantIRI(org.apache.commons.rdf.api.IRI iri) {
        return termFactory.getConstantIRI(iri);
    }

    @Override
    public SQLPPSourceQuery getSourceQuery(String query) {
        return sourceQueryFactory.createSourceQuery(query);
    }

    @Override
    public ImmutableList<TargetAtom> getTargetQuery(String target) throws TargetQueryParserException {
        return targetQueryParserFactory.createParser(prefixManager).parse(target);
    }

    @Override
    public String getTargetRendering(ImmutableList<TargetAtom> targetAtoms) {
        TargetQueryRenderer targetQueryRenderer = new TargetQueryRenderer(prefixManager);
        return targetQueryRenderer.encode(targetAtoms);
    }

    @Override
    public SQLPPMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTriplesMap> triplesMaps) {
        // TODO: put an immutable copy of prefixManager
        return ppMappingFactory.createSQLPreProcessedMapping(triplesMaps, prefixManager);
    }
}
