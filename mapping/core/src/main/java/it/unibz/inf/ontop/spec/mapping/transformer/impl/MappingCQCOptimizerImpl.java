package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.tools.impl.IQ2CQ;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.LazyRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private static final Logger log = LoggerFactory.getLogger(MappingCQCOptimizerImpl.class);
    
    private final IntermediateQueryFactory iqFactory;
    private final CoreSingletons coreSingletons;

    @Inject
    public MappingCQCOptimizerImpl(IntermediateQueryFactory iqFactory,
                                   CoreSingletons coreSingletons) {
        this.iqFactory = iqFactory;
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(ImmutableCQContainmentCheck<RelationPredicate> cqContainmentCheck, IQ query) {

        IQTree tree = query.getTree();
        ConstructionNode constructionNode = (ConstructionNode) tree.getRootNode();

        return iqFactory.createIQ(query.getProjectionAtom(), tree.acceptTransformer(new LazyRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children0) {

                Optional<ImmutableList<ExtensionalDataNode>> c = IQ2CQ.getExtensionalDataNodes(tree, coreSingletons);

                ImmutableList<Variable> answerVariables = Stream.concat(
                        constructionNode.getSubstitution().getImmutableMap().values().stream()
                                .flatMap(ImmutableTerm::getVariableStream),
                        rootNode.getOptionalFilterCondition()
                                .map(ImmutableTerm::getVariableStream).orElse(Stream.of()))
                        .distinct()
                        .collect(ImmutableCollectors.toList());

                ImmutableList<ExtensionalDataNode> children = c.get();
                int currentIndex = 0;
                while (currentIndex < children.size()) {
                    ImmutableList.Builder<ExtensionalDataNode> builder = ImmutableList.builder();
                    for (int i = 0; i < children.size(); i++)
                        if (i != currentIndex)
                            builder.add(children.get(i));
                    ImmutableList<ExtensionalDataNode> subChildren = builder.build();

                    if (subChildren.stream()
                            .flatMap(a -> a.getVariables().stream())
                            .collect(ImmutableCollectors.toSet())
                            .containsAll(answerVariables)) {

                        if (cqContainmentCheck.isContainedIn(new ImmutableCQ<>(answerVariables,
                                        IQ2CQ.toDataAtoms(subChildren, coreSingletons)),
                                new ImmutableCQ<>(answerVariables, IQ2CQ.toDataAtoms(children, coreSingletons)))) {
                            //System.out.println("CQC-REMOVED: " + children.get(currentIndex) + " FROM " + children);
                            log.debug("CQC-REMOVED: " + children.get(currentIndex) + " FROM " + children);
                            children = subChildren;
                            if (children.size() < 2)
                                break;
                            currentIndex = 0; // reset
                        }
                        else
                            currentIndex++;
                    }
                    else
                        currentIndex++;
                }

                return IQ2CQ.toIQTree(children, rootNode.getOptionalFilterCondition(), coreSingletons);
            }
        }));

    }
}
