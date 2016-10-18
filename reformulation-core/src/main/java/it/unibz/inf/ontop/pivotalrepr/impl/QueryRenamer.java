package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.pivotalrepr.*;

public class QueryRenamer extends QueryTransformer {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private QueryNodeRenamer nodeRenamer;

    public QueryRenamer(QueryNodeRenamer nodeRenamer) {
        this.nodeRenamer = nodeRenamer;
    }


    public IntermediateQuery transform(IntermediateQuery originalQuery)
            throws IntermediateQueryBuilderException, NotNeededNodeException {
        return transform(originalQuery, this.nodeRenamer);
    }

    public IntermediateQuery transform(IntermediateQuery originalQuery, QueryNodeRenamer nodeRenamer)
            throws IntermediateQueryBuilderException, NotNeededNodeException {
        DistinctVariableOnlyDataAtom renamedProjectionDataAtom =
                renameProjectionAtomVariables(originalQuery.getProjectionAtom());
        IntermediateQueryBuilder builder = super.convertToBuilderAndTransform(originalQuery, nodeRenamer, renamedProjectionDataAtom);
        return builder.build();
    }


    private DistinctVariableOnlyDataAtom renameProjectionAtomVariables(DistinctVariableOnlyDataAtom atom) {
        ImmutableList.Builder<Variable> argListBuilder = ImmutableList.builder();
        for (Variable var : atom.getVariables()) {
            argListBuilder.add(nodeRenamer.getRenamingSubstitution().applyToVariable(var));
        }
        return DATA_FACTORY.getDistinctVariableOnlyDataAtom(atom.getPredicate(), argListBuilder.build());
    }
}
