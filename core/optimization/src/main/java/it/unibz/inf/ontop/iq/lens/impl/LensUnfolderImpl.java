package it.unibz.inf.ontop.iq.lens.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.lens.LensUnfolder;
import it.unibz.inf.ontop.dbschema.Lens;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.impl.RelationExtractor;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class LensUnfolderImpl implements LensUnfolder {

    protected final CoreSingletons coreSingletons;
    protected final IntermediateQueryFactory iqFactory;

    @Inject
    protected LensUnfolderImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        int maxLevel = extractMaxLevel(initialTree);
        if (maxLevel < 1)
            return query;

        IQTree newTree = new MaxLevelLensUnfoldingTransformer(maxLevel, query.getVariableGenerator())
                .transform(initialTree);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }

    private int extractMaxLevel(IQTree tree) {
        return tree.acceptVisitor(new RelationExtractor())
                .map(ExtensionalDataNode::getRelationDefinition)
                .filter(r -> r instanceof Lens)
                .map(r -> (Lens) r)
                .mapToInt(Lens::getLevel)
                .max()
                .orElse(0);
    }

    protected class MaxLevelLensUnfoldingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final int maxLevel;
        protected final VariableGenerator variableGenerator;

        protected MaxLevelLensUnfoldingTransformer(int maxLevel, VariableGenerator variableGenerator) {
            super(coreSingletons);
            this.maxLevel = maxLevel;
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            RelationDefinition relationDefinition = dataNode.getRelationDefinition();
            if (relationDefinition instanceof Lens) {
                Lens lens = (Lens) relationDefinition;
                return lens.getLevel() < maxLevel
                        ? dataNode
                        : merge(dataNode, lens.getIQ());
            }
            else
                return dataNode;
        }

        protected IQTree merge(ExtensionalDataNode dataNode, IQ definition) {
            return ExtensionalDataNodeImpl.merge(dataNode, definition, variableGenerator,
                    coreSingletons.getSubstitutionFactory(),
                    coreSingletons.getQueryTransformerFactory(),
                    iqFactory);
        }
    }
}
