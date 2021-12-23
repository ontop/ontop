package it.unibz.inf.ontop.iq.impl.tree;

import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.impl.IntermediateQueryImpl;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

import java.util.Optional;

/**
 * TODO: explain
 */
public class DefaultIntermediateQueryBuilder implements IntermediateQueryBuilder {

    private final IQConverter iqConverter;
    private final IntermediateQueryFactory iqFactory;
    private final IntermediateQueryValidator validator;
    private final CoreUtilsFactory coreUtilsFactory;
    private final OntopModelSettings settings;
    private DistinctVariableOnlyDataAtom projectionAtom;
    private QueryTree tree;
    private boolean canEdit;

    @AssistedInject
    protected DefaultIntermediateQueryBuilder(IQConverter iqConverter,
                                              IntermediateQueryFactory iqFactory,
                                              IntermediateQueryValidator validator,
                                              CoreUtilsFactory coreUtilsFactory,
                                              OntopModelSettings settings) {
        this.iqConverter = iqConverter;
        this.iqFactory = iqFactory;
        this.validator = validator;
        this.coreUtilsFactory = coreUtilsFactory;
        this.settings = settings;
        tree = null;
        canEdit = true;
    }


    @Override
    public void init(DistinctVariableOnlyDataAtom projectionAtom, QueryNode rootNode){
        if (tree != null)
            throw new IllegalArgumentException("Already initialized IntermediateQueryBuilder.");

        if ((rootNode instanceof ExplicitVariableProjectionNode)
            && !projectionAtom.getVariables().equals(((ExplicitVariableProjectionNode)rootNode).getVariables())) {
            throw new IllegalArgumentException("The root node " + rootNode
                    + " is not consistent with the projection atom " + projectionAtom);
        }


        // TODO: use Guice to construct this tree
        tree = new DefaultTree(rootNode);
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
    public IntermediateQuery build() throws IntermediateQueryBuilderException {
        checkInitialization();

        IntermediateQuery query = buildQuery(projectionAtom, new DefaultQueryTreeComponent(tree, coreUtilsFactory));
        canEdit = false;
        return query;
    }

    @Override
    public IQ buildIQ() throws IntermediateQueryBuilderException {
        IntermediateQuery query = build();
        return iqConverter.convert(query);
    }

    /**
     * Can be overwritten to use another constructor
     */
    protected IntermediateQuery buildQuery(DistinctVariableOnlyDataAtom projectionAtom,
                                           QueryTreeComponent treeComponent) {

        return new IntermediateQueryImpl(projectionAtom, treeComponent, validator,
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
    public boolean contains(QueryNode node) {
        return tree.contains(node);
    }

    @Override
    public IntermediateQueryFactory getFactory() {
        return iqFactory;
    }
}
