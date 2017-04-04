package it.unibz.inf.ontop.executor.leftjoin;

import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.VariableGenerator;
import it.unibz.inf.ontop.pivotalrepr.DataNode;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Does not propose to change the left data node.
 */
public interface LeftJoinRightChildNormalizationAnalyzer {

    LeftJoinRightChildNormalizationAnalysis analyze(DataNode leftDataNode, DataNode rightDataNode, DBMetadata dbMetadata,
                                                    VariableGenerator variableGenerator);

    interface LeftJoinRightChildNormalizationAnalysis {
        /**
         * Returns true if its matches a unique constraint or a foreign key
         */
        boolean isMatchingAConstraint();
        /**
         * Only if the data node has changed
         */
        Optional<DataNode> getProposedRightDataNode();

        Optional<ImmutableExpression> getAdditionalExpression();
    }
}
