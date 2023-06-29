package it.unibz.inf.ontop.iq.optimizer.splitter;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface ProjectionSplitter {

    ProjectionSplit split(IQ iq);

    ProjectionSplit split(IQTree tree, VariableGenerator variableGenerator);

    interface ProjectionSplit {
        ConstructionNode getConstructionNode();
        IQTree getSubTree();
        VariableGenerator getVariableGenerator();
        ImmutableSet<Variable> getPushedVariables();
        ImmutableSet<ImmutableTerm> getPushedTerms();
    }
}
