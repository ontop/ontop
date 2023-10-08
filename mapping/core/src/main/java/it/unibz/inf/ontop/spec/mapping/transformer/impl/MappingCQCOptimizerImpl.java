package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.constraints.HomomorphismFactory;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.constraints.impl.HomomorphismImpl;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
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
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private static final Logger log = LoggerFactory.getLogger(MappingCQCOptimizerImpl.class);
    
    private final IntermediateQueryFactory iqFactory;
    private final CoreSingletons coreSingletons;

    @Inject
    public MappingCQCOptimizerImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(ExtensionalDataNodeListContainmentCheck cqContainmentCheck, IQ query) {

        IQTree tree = query.getTree();
        ConstructionNode constructionNode = (ConstructionNode) tree.getRootNode();

        return iqFactory.createIQ(query.getProjectionAtom(), tree.acceptTransformer(new LazyRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children0) {

                ImmutableList<ExtensionalDataNode> children = IQ2CQ.getExtensionalDataNodes(tree, coreSingletons);

                ImmutableList<Variable> answerVariables = Stream.concat(
                                constructionNode.getSubstitution().getRangeVariables().stream(),
                                rootNode.getOptionalFilterCondition().stream()
                                        .flatMap(ImmutableTerm::getVariableStream))
                        .distinct()
                        .collect(ImmutableCollectors.toList());

                int currentIndex = 0;
                while (currentIndex < children.size()) {
                    ExtensionalDataNode current = children.get(currentIndex);
                    ImmutableList<ExtensionalDataNode> subChildren = children.stream()
                            .filter(child -> child != current)
                            .collect(ImmutableCollectors.toList());

                    if (subChildren.stream()
                            .flatMap(a -> a.getVariables().stream())
                            .collect(ImmutableCollectors.toSet())
                            .containsAll(answerVariables)) {

                        if (cqContainmentCheck.isContainedIn(
                                answerVariables, subChildren, answerVariables, children)) {
                            System.out.println("CQC-REMOVED: " + children.get(currentIndex) + " FROM " + children);
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
