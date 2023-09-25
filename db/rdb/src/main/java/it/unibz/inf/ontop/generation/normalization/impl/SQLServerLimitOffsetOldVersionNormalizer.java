package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

/**
 * SQL Server extra normalizer which can handle limit and offset for Microsoft SQL Server 2000 through 2008
 */
public class SQLServerLimitOffsetOldVersionNormalizer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator>
        implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final DatabaseInfoSupplier databaseInfoSupplier;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected SQLServerLimitOffsetOldVersionNormalizer(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                                       SubstitutionFactory substitutionFactory, DatabaseInfoSupplier databaseInfoSupplier,
                                                       DBFunctionSymbolFactory dbFunctionSymbolFactory, CoreSingletons coreSingletons,
                                                       IQTreeTools iqTreeTools) {
        super(coreSingletons);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.databaseInfoSupplier = databaseInfoSupplier;
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return super.transform(tree, variableGenerator);
    }

    // Transformation necessary for versions 8,9,10 of SQL server when a Slice Node is present
    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child, VariableGenerator variableGenerator) {

        // If no SliceNode present, 2) not an older version of SQL Server pre-2012 3) no db info; use default transformation
        if (!databaseInfoSupplier.getDatabaseVersion().isPresent() ||
                (Stream.of("8.", "9.", "10.").noneMatch(s -> databaseInfoSupplier.getDatabaseVersion().get().startsWith(s)))
                || !containsSliceNode(tree)) {
            return super.transformSlice(tree, sliceNode, child, variableGenerator);
        }

        // Add fresh variable which will include row number
        Variable freshVariable = variableGenerator.generateNewVariable();

        // CASE 1: No OrderBy, ORDER BY (SELECT NULL)
        // CASE 2: OrderBy present, ORDER BY (comparators)
        ConstructionNode newConstruction = iqFactory.createConstructionNode(
                iqTreeTools.getChildrenVariables(child, freshVariable),
                substitutionFactory.getSubstitution(freshVariable, getOrderBySubTerm(child)));

        ImmutableExpression expression = getNewFilterExpression(sliceNode, freshVariable);
        FilterNode newFilter = iqFactory.createFilterNode(expression);

        // Drop ORDER BY node since it will now be part of the orderBy subTerm
        IQTree newChild;
        if (child.getRootNode() instanceof OrderByNode) {
            newChild = child.getChildren().get(0);
        } else if (!child.getChildren().isEmpty() && child.getChildren().get(0).getRootNode() instanceof OrderByNode) {
            newChild = iqFactory.createUnaryIQTree((ConstructionNode) child.getRootNode(), child.getChildren().get(0).getChildren().get(0));
        } else {
            newChild = child;
        }
        IQTree normalizedChild = this.transform(newChild, variableGenerator);

        IQTree newTree = iqFactory.createUnaryIQTree(newFilter,
                iqFactory.createUnaryIQTree(newConstruction, normalizedChild));

        // Additional CONSTRUCTION necessary when subtree leaf in NaryIQTree (e.g. sub-query)
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(newTree.getVariables()), newTree);
    }

    private boolean containsSliceNode(IQTree tree) {
        if (tree.getChildren().isEmpty()) { return false; }

        return tree.getRootNode() instanceof SliceNode ||
                tree.getChildren().stream().anyMatch(this::containsSliceNode);
    }

    private ImmutableFunctionalTerm getOrderBySubTerm(IQTree childTree) {

        // Let SLICE be the rootNode
        // Pattern 1: SLICE [ORDER BY]
        if (childTree.getRootNode() instanceof OrderByNode) {
           return termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRowNumberWithOrderBy(),
                    ((OrderByNode) childTree.getRootNode())
                            .getComparators().asList().stream()
                            .map(c -> c.getTerm())
                    .collect(ImmutableCollectors.toList()));
        }
        // Pattern 2: SLICE [CONSTRUCT] [ORDER BY]
        if (!childTree.getChildren().isEmpty() &&
                childTree.getChildren().get(0).getRootNode() instanceof OrderByNode) {
            return termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRowNumberWithOrderBy(),
                    ((OrderByNode) childTree.getChildren().get(0).getRootNode())
                            .getComparators().asList().stream()
                            .map(c -> c.getTerm())
                            .collect(ImmutableCollectors.toList()));
        }
        return termFactory.getDBRowNumber();
    }

    // Create new Filter Node condition based on Slice Node
    private ImmutableExpression getNewFilterExpression(SliceNode sliceNode, Variable freshVariable) {
        // CASE 1: Limit and offset
        if (sliceNode.getLimit().isPresent() && sliceNode.getOffset() > 0) {
            return termFactory.getConjunction(
                    termFactory.getDBDefaultInequality(InequalityLabel.GT,
                            freshVariable,
                            termFactory.getDBIntegerConstant((int) sliceNode.getOffset())),
                    termFactory.getDBDefaultInequality(InequalityLabel.LTE,
                            freshVariable,
                            termFactory.getDBIntegerConstant((int) sliceNode.getOffset() +
                                    sliceNode.getLimit().get().intValue())));
            // CASE 2: Only limit
        } else if (!sliceNode.getLimit().isPresent()) {
            return termFactory.getDBDefaultInequality(InequalityLabel.GT,
                    freshVariable,
                    termFactory.getDBIntegerConstant((int) sliceNode.getOffset()));
            // CASE 3: Only offset
        } else {
            return termFactory.getDBDefaultInequality(InequalityLabel.LTE,
                    freshVariable,
                    termFactory.getDBIntegerConstant(sliceNode.getLimit().get().intValue()));
        }
    }
}
