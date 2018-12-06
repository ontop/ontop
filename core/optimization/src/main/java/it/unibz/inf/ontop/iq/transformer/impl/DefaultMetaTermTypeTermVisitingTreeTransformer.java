package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.MetaTermTypeTermLiftTransformer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;


public class DefaultMetaTermTypeTermVisitingTreeTransformer
        extends DefaultRecursiveIQTreeVisitingTransformer implements MetaTermTypeTermLiftTransformer {

    private final VariableGenerator variableGenerator;
    private final TypeConstantDictionary dictionary;
    private final TermFactory termFactory;

    @Inject
    private DefaultMetaTermTypeTermVisitingTreeTransformer(@Assisted VariableGenerator variableGenerator,
                                                           TermFactory termFactory,
                                                           IntermediateQueryFactory iqFactory,
                                                           TypeConstantDictionary typeConstantDictionary) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
        this.dictionary = typeConstantDictionary;
        this.termFactory = termFactory;
    }

    @Override
    public IQTree transform(IQTree tree) {
        return tree.normalizeForOptimization(variableGenerator)
                .acceptTransformer(this)
                .normalizeForOptimization(variableGenerator);
    }

    /**
     * TODO: implement it seriously
     */
    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(rootNode, children);
    }

}
