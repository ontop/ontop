package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

/**
 * For DBMS (such as Oracle) that return the results of Flatten calls where items are of type `String` encased in quotation marks.
 * These quotation marks have to be removed with an additional construction.
 */
@Singleton
public class UnquoteFlattenResultsNormalizer implements DialectExtraNormalizer {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected UnquoteFlattenResultsNormalizer(IntermediateQueryFactory iqFactory,
                                              SubstitutionFactory substitutionFactory,
                                              TermFactory termFactory,
                                              IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(new Transformer(variableGenerator));
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {
        Transformer(VariableGenerator variableGenerator) {
            super(UnquoteFlattenResultsNormalizer.this.iqFactory, variableGenerator);
        }

        /**
         * Adds a construction node over any flatten call with the following expression:
         * `CASE WHEN STARTSWITH(<output>, '"') THEN SUBSTRING(<output>, 2, LENGTH(<output>) - 2) ELSE <output> END`
         * This removes encasing quotation marks from flatten results.
         */
        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            IQTree newChild = transformChild(child);
            Variable newOutputVariable = variableGenerator.generateNewVariableFromVar(rootNode.getOutputVariable());
            ImmutableTerm resultSubstitution = termFactory.getDBCase(
                    Stream.of(Maps.immutableEntry(
                            termFactory.getDBStartsWith(ImmutableList.of(newOutputVariable, termFactory.getDBStringConstant("\""))),
                            termFactory.getDBSubString3(
                                    newOutputVariable,
                                    termFactory.getDBIntegerConstant(2),
                                    termFactory.getImmutableFunctionalTerm(
                                            termFactory.getDBFunctionSymbolFactory().getDBMathBinaryOperator("-", termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType()),
                                            termFactory.getDBCharLength(newOutputVariable),
                                            termFactory.getDBIntegerConstant(2))))),
                    newOutputVariable,
                    true);
            Substitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(rootNode.getOutputVariable(), resultSubstitution);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqTreeTools.createExtendingConstructionNode(rootNode.getVariables(child.getVariables()), newSubstitution))
                    .append(iqFactory.createFlattenNode(newOutputVariable, rootNode.getFlattenedVariable(), rootNode.getIndexVariable(), rootNode.getFlattenedType()))
                    .build(newChild);
        }
    }
}
