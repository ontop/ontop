package it.unibz.inf.ontop.iq.executor.leftjoin;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Does not propose to change the left data node.
 */
public interface LeftJoinRightChildNormalizationAnalyzer {

    LeftJoinRightChildNormalizationAnalysis analyze(ImmutableList<DataNode> leftDataNodes, DataNode rightDataNode,
                                                    DBMetadata dbMetadata, VariableGenerator variableGenerator);

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
