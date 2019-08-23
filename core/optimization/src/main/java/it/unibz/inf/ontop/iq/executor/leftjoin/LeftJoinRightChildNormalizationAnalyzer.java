package it.unibz.inf.ontop.iq.executor.leftjoin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Does not propose to change the left data node.
 */
public interface LeftJoinRightChildNormalizationAnalyzer {

    LeftJoinRightChildNormalizationAnalysis analyze(ImmutableSet<Variable> leftVariables,
                                                    ImmutableList<ExtensionalDataNode> leftDataNodes,
                                                    ExtensionalDataNode rightDataNode,
                                                    VariableGenerator variableGenerator);

    interface LeftJoinRightChildNormalizationAnalysis {
        /**
         * Returns true if its matches a non-nullable unique constraint or a foreign key
         */
        boolean isMatchingAConstraint();
        /**
         * Only if the data node has changed
         */
        Optional<DataNode> getProposedRightDataNode();

        Optional<ImmutableExpression> getAdditionalExpression();
    }
}
