package it.unibz.inf.ontop.iq.executor.merging;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.*;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.ProposalResults;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.FunctionalTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

@Singleton
public class QueryMergingExecutorImpl implements QueryMergingExecutor {


    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final TermFactory termFactory;
    private final IQConverter iqConverter;

    @Inject
    private QueryMergingExecutorImpl(IntermediateQueryFactory iqFactory,
                                     QueryTransformerFactory transformerFactory,
                                     SubstitutionFactory substitutionFactory, TermFactory termFactory,
                                     IQConverter iqConverter) {
        this.iqFactory = iqFactory;
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqConverter = iqConverter;
    }


    /**
     * Main method
     */
    @Override
    public ProposalResults apply(QueryMergingProposal proposal, IntermediateQuery mainQuery,
                                 QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        Optional<IntermediateQuery> optionalSubQuery = proposal.getSubQuery();
        if (optionalSubQuery.isPresent()) {
            mergeSubQuery(treeComponent, optionalSubQuery.get(), proposal.getIntensionalNode());
        }
        else {
            removeUnsatisfiedNode(treeComponent, proposal.getIntensionalNode());
        }
        return new ProposalResultsImpl();
    }

    private void removeUnsatisfiedNode(QueryTreeComponent treeComponent, IntensionalDataNode intensionalNode) {

        EmptyNode emptyNode = iqFactory.createEmptyNode(intensionalNode.getVariables());
        treeComponent.replaceSubTree(intensionalNode, emptyNode);
    }

    /**
     * TODO: explain
     *
     */
    protected void mergeSubQuery(QueryTreeComponent treeComponent, IntermediateQuery subQuery,
                                        IntensionalDataNode intensionalDataNode) {

        VariableGenerator variableGenerator = new VariableGenerator(treeComponent.getKnownVariables(), termFactory);
        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(variableGenerator,
                subQuery.getKnownVariables());

        IQ renamedIQ;
        if(renamingSubstitution.isEmpty()){
            renamedIQ = iqConverter.convert(subQuery);
        } else {
            QueryRenamer queryRenamer = transformerFactory.createRenamer(renamingSubstitution);
            renamedIQ = queryRenamer.transform(iqConverter.convert(subQuery));
        }

        ImmutableSubstitution<VariableOrGroundTerm> descendingSubstitution = extractSubstitution(
                renamingSubstitution.applyToDistinctVariableOnlyDataAtom(renamedIQ.getProjectionAtom()),
                intensionalDataNode.getProjectionAtom());

        IQTree unifiedSubTree = renamedIQ.getTree()
                .applyDescendingSubstitution(descendingSubstitution, Optional.empty())
                .liftBinding(renamedIQ.getVariableGenerator());

        treeComponent.replaceSubTreeByIQ(intensionalDataNode, unifiedSubTree);
    }


    private ImmutableSubstitution<VariableOrGroundTerm> extractSubstitution(DistinctVariableOnlyDataAtom sourceAtom,
                                                                                   DataAtom targetAtom) {
        if (!sourceAtom.getPredicate().equals(targetAtom.getPredicate())) {
            throw new IllegalStateException("Incompatible predicates");
        }
        else if (sourceAtom.getEffectiveArity() != targetAtom.getEffectiveArity()) {
            throw new IllegalStateException("Different arities");
        }

        ImmutableMap<Variable, VariableOrGroundTerm> newMap = FunctionalTools.zip(
                sourceAtom.getArguments(),
                (ImmutableList<VariableOrGroundTerm>) targetAtom.getArguments()).stream()
                .collect(ImmutableCollectors.toMap());

        return substitutionFactory.getSubstitution(newMap);
    }

}