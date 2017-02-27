package it.unibz.inf.ontop.pivotalrepr.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import it.unibz.inf.ontop.pivotalrepr.impl.IntermediateQueryImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.pivotalrepr.validation.IntermediateQueryValidator;

import java.util.Optional;

/**
 * TODO: explain
 */
public class DefaultIntermediateQueryBuilder implements IntermediateQueryBuilder {

    private final DBMetadata dbMetadata;
    private final ExecutorRegistry executorRegistry;
    private final IntermediateQueryFactory iqFactory;
    private final IntermediateQueryValidator validator;
    private final OntopModelSettings settings;
    private DistinctVariableOnlyDataAtom projectionAtom;
    private QueryTree tree;
    private boolean canEdit;

    @AssistedInject
    protected DefaultIntermediateQueryBuilder(@Assisted DBMetadata dbMetadata,
                                            @Assisted ExecutorRegistry executorRegistry,
                                            IntermediateQueryFactory iqFactory,
                                            IntermediateQueryValidator validator,
                                            OntopModelSettings settings) {
        this.dbMetadata = dbMetadata;
        this.executorRegistry = executorRegistry;
        this.iqFactory = iqFactory;
        this.validator = validator;
        this.settings = settings;
        tree = null;
        canEdit = true;
    }


    @Override
    public void init(DistinctVariableOnlyDataAtom projectionAtom, ConstructionNode rootConstructionNode){
        if (tree != null)
            throw new IllegalArgumentException("Already initialized IntermediateQueryBuilder.");

        if (!projectionAtom.getVariables().equals(rootConstructionNode.getVariables())) {
            throw new IllegalArgumentException("The root construction node " + rootConstructionNode
                    + " is not consistent with the projection atom " + projectionAtom);
        }

        // TODO: use Guice to construct this tree
        tree = new DefaultTree(rootConstructionNode);
        this.projectionAtom = projectionAtom;
        canEdit = true;
    }

    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode) throws IntermediateQueryBuilderException {
        checkEditMode();
        try {
            tree.addChild(parentNode, childNode, Optional.<ArgumentPosition>empty(), true, false);
        } catch (IllegalTreeUpdateException e) {
            throw new IntermediateQueryBuilderException(e.getMessage());
        }
    }

    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode,
                         ArgumentPosition position)
            throws IntermediateQueryBuilderException {
        checkEditMode();
        try {
            tree.addChild(parentNode, childNode, Optional.of(position), true, false);
        } catch (IllegalTreeUpdateException e) {
            throw new IntermediateQueryBuilderException(e.getMessage());
        }
    }

    @Override
    public void addChild(QueryNode parentNode, QueryNode child,
                         Optional<ArgumentPosition> optionalPosition)
            throws IntermediateQueryBuilderException {
        if (optionalPosition.isPresent()) {
            addChild(parentNode, child, optionalPosition.get());
        }
        else {
            addChild(parentNode, child);
        }
    }

    @Override
    public IntermediateQuery build() throws IntermediateQueryBuilderException{
        checkInitialization();

        IntermediateQuery query = buildQuery(dbMetadata, projectionAtom, new DefaultQueryTreeComponent(tree));
        canEdit = false;
        return query;
    }

    /**
     * Can be overwritten to use another constructor
     */
    protected IntermediateQuery buildQuery(DBMetadata metadata,
                                           DistinctVariableOnlyDataAtom projectionAtom,
                                           QueryTreeComponent treeComponent) {

        return new IntermediateQueryImpl(metadata, projectionAtom, treeComponent, executorRegistry, validator,
                settings, iqFactory);
    }

    private void checkInitialization() throws IntermediateQueryBuilderException {
        if (tree == null)
            throw new IntermediateQueryBuilderException("Not initialized!");
    }

    private void checkEditMode() throws IntermediateQueryBuilderException {
        checkInitialization();

        if (!canEdit)
            throw new IllegalArgumentException("Cannot be edited anymore (the query has already been built).");
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws IntermediateQueryBuilderException {
        checkInitialization();
        return tree.getRootNode();
    }

    @Override
    public ImmutableList<QueryNode> getSubNodesOf(QueryNode node)
            throws IntermediateQueryBuilderException {
        checkInitialization();
        return tree.getChildren(node);
    }

    @Override
    public IntermediateQueryFactory getFactory() {
        return iqFactory;
    }

    protected ExecutorRegistry getExecutorRegistry() {
        return executorRegistry;
    }

    protected IntermediateQueryValidator getValidator() {
        return validator;
    }

    protected OntopModelSettings getSettings() {
        return settings;
    }
}
