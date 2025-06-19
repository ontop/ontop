package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

/**
 * For DBMS such as Oracle, that return the results of Flatten calls where items are of type `String` encased in quotation marks.
 * These quotation marks have to be removed with an additional construction.
 */
@Singleton
public class UnquoteFlattenResultsNormalizer implements DialectExtraNormalizer {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;
    private final Transformer transformer;

    @Inject
    protected UnquoteFlattenResultsNormalizer(IntermediateQueryFactory iqFactory,
                                              SubstitutionFactory substitutionFactory,
                                              TermFactory termFactory,
                                              IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        Transformer() {
            super(UnquoteFlattenResultsNormalizer.this.iqFactory);
        }

        /**
         * Adds a construction node over any flatten call with the following expression:
         * `CASE WHEN STARTSWITH(<output>, '"') THEN SUBSTRING(<output>, 2, LENGTH(<output>) - 2) ELSE <output> END`
         * This removes encasing quotation marks from flatten results.
         */
        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            IQTree newChild = transformChild(child);
            DBMathBinaryOperator minus = termFactory.getDBFunctionSymbolFactory().getDBMathBinaryOperator("-", termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType());
            ImmutableTerm resultSubstitution = termFactory.getDBCase(
                    Stream.of(Maps.immutableEntry(
                            termFactory.getDBStartsWith(ImmutableList.of(rootNode.getOutputVariable(), termFactory.getDBStringConstant("\""))),
                            termFactory.getDBSubString3(rootNode.getOutputVariable(), termFactory.getDBIntegerConstant(2), termFactory.getImmutableFunctionalTerm(minus, termFactory.getDBCharLength(rootNode.getOutputVariable()), termFactory.getDBIntegerConstant(2))))),
                    rootNode.getOutputVariable(),
                    true);
            Substitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(rootNode.getOutputVariable(), resultSubstitution);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqFactory.createConstructionNode(rootNode.getVariables(child.getVariables()), newSubstitution))
                    .append(rootNode)
                    .build(newChild);
        }
    }
}
