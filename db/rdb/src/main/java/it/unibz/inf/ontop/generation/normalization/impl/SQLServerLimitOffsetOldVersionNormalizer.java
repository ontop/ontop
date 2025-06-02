package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

/**
 * SQL Server extra normalizer which can handle limit and offset for Microsoft SQL Server 2000 through 2008
 */
public class SQLServerLimitOffsetOldVersionNormalizer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final DatabaseInfoSupplier databaseInfoSupplier;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected SQLServerLimitOffsetOldVersionNormalizer(DatabaseInfoSupplier databaseInfoSupplier,
                                                       DBFunctionSymbolFactory dbFunctionSymbolFactory,
                                                       CoreSingletons coreSingletons) {
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.databaseInfoSupplier = databaseInfoSupplier;
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(new Transformer(variableGenerator));
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final VariableGenerator variableGenerator;

        Transformer(VariableGenerator variableGenerator) {
            super(SQLServerLimitOffsetOldVersionNormalizer.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        // Transformation necessary for versions 8,9,10 of SQL Server when a SliceNode is present
        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {

            // If no SliceNode present, 2) not an older version of SQL Server pre-2012 3) no db info; use default transformation
            if (!databaseInfoSupplier.getDatabaseVersion().isPresent() ||
                    (Stream.of("8.", "9.", "10.").noneMatch(s -> databaseInfoSupplier.getDatabaseVersion().get().startsWith(s)))
                    || !IQTreeTools.contains(tree, SliceNode.class)) {
                return super.transformSlice(tree, sliceNode, child);
            }

            // Add a fresh variable which will include the row number
            Variable freshVariable = variableGenerator.generateNewVariable();

            // CASE 1: No OrderBy, ORDER BY (SELECT NULL)
            // CASE 2: OrderBy present, ORDER BY (comparators)
            ConstructionNode newConstruction = iqTreeTools.createExtendingConstructionNode(
                    child.getVariables(),
                    substitutionFactory.getSubstitution(freshVariable, getOrderBySubTerm(child)));

            ImmutableExpression expression = getNewFilterExpression(sliceNode, freshVariable);
            FilterNode newFilter = iqFactory.createFilterNode(expression);

            // Patterns: SLICE [CONSTRUCT] [ORDER BY] -> SLICE [CONSTRUCT]
            var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
            var orderBy = UnaryIQTreeDecomposition.of(construction, OrderByNode.class);
            // Drop ORDER BY node since it will now be part of the orderBy subTerm
            IQTree newChild = iqTreeTools.unaryIQTreeBuilder()
                    .append(construction.getOptionalNode())
                    .build(orderBy.getTail());

            IQTree normalizedChild = transform(newChild);

            IQTree newTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(newFilter)
                    .append(newConstruction)
                    .build(normalizedChild);

            // Additional CONSTRUCTION necessary when subtree leaf in NaryIQTree (e.g. sub-query)
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(newTree.getVariables()),
                    newTree);
        }

        private ImmutableFunctionalTerm getOrderBySubTerm(IQTree childTree) {
            // Patterns: SLICE [CONSTRUCT] [ORDER BY]
            var construction = UnaryIQTreeDecomposition.of(childTree, ConstructionNode.class);
            var orderBy = UnaryIQTreeDecomposition.of(construction, OrderByNode.class);
            if (orderBy.isPresent()) {
                return termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRowNumberWithOrderBy(),
                        orderBy.getNode()
                                .getComparators().stream()
                                .map(OrderByNode.OrderComparator::getTerm)
                                .collect(ImmutableCollectors.toList()));
            }
            return termFactory.getDBRowNumber();
        }

        // Create new Filter Node condition based on Slice Node
        private ImmutableExpression getNewFilterExpression(SliceNode sliceNode, Variable freshVariable) {
            if (sliceNode.getLimit().isPresent() && sliceNode.getOffset() > 0) {
                // CASE 1: Limit and offset
                return termFactory.getConjunction(
                        termFactory.getDBDefaultInequality(InequalityLabel.GT,
                                freshVariable,
                                termFactory.getDBIntegerConstant((int) sliceNode.getOffset())),
                        termFactory.getDBDefaultInequality(InequalityLabel.LTE,
                                freshVariable,
                                termFactory.getDBIntegerConstant((int) sliceNode.getOffset() +
                                        sliceNode.getLimit().get().intValue())));
            }
            else if (!sliceNode.getLimit().isPresent()) {
                // CASE 2: Only limit
                return termFactory.getDBDefaultInequality(InequalityLabel.GT,
                        freshVariable,
                        termFactory.getDBIntegerConstant((int) sliceNode.getOffset()));
            }
            else {
                // CASE 3: Only offset
                return termFactory.getDBDefaultInequality(InequalityLabel.LTE,
                        freshVariable,
                        termFactory.getDBIntegerConstant(sliceNode.getLimit().get().intValue()));
            }
        }
    }
}
