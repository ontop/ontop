package it.unibz.inf.ontop.temporal.iq.impl.tree;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.impl.tree.QueryTree;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;

public class TemporalIntermediateQueryBuilderImpl extends DefaultIntermediateQueryBuilder implements TemporalIntermediateQueryBuilder {

    private DistinctVariableOnlyDataAtom projectionAtom;
    private QueryTree tree;
    private boolean canEdit;
    private TemporalIntermediateQueryFactory tiqFactory;

    @AssistedInject
    protected TemporalIntermediateQueryBuilderImpl(@Assisted DBMetadata dbMetadata,
                                              @Assisted ExecutorRegistry executorRegistry,
                                                   TemporalIntermediateQueryFactory iqFactory,
                                                   IntermediateQueryValidator validator,
                                                   TermFactory termFactory,
                                                   OntopModelSettings settings) {
        super(dbMetadata, executorRegistry, iqFactory, validator, termFactory, settings);
        tiqFactory = iqFactory;
    }

    @Override
    public TemporalIntermediateQueryFactory getFactory(){
        return tiqFactory;
    }

}
