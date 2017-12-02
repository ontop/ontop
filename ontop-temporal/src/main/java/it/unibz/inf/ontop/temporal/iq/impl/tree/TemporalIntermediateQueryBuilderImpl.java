package it.unibz.inf.ontop.temporal.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.impl.IntermediateQueryImpl;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.impl.tree.DefaultQueryTreeComponent;
import it.unibz.inf.ontop.iq.impl.tree.DefaultTree;
import it.unibz.inf.ontop.iq.impl.tree.QueryTree;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;

import java.util.Optional;

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
                                                   OntopModelSettings settings) {
        super(dbMetadata, executorRegistry, iqFactory, validator, settings);
        tiqFactory = iqFactory;
    }

    @Override
    public TemporalIntermediateQueryFactory getFactory(){
        return tiqFactory;
    }

}
