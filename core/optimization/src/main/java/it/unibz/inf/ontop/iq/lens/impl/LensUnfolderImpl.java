package it.unibz.inf.ontop.iq.lens.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.lens.LensUnfolder;
import it.unibz.inf.ontop.dbschema.Lens;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class LensUnfolderImpl extends AbstractIQOptimizer implements LensUnfolder {

    protected final CoreSingletons coreSingletons;

    @Inject
    protected LensUnfolderImpl(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory(), NORMALIZE_FOR_OPTIMIZATION);
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQTree transformTree(IQ query) {
        IQTree tree = query.getTree();
        int maxLevel = Lens.getMaxLevel(tree);
        if (maxLevel < 1)
            return tree;

        IQVisitor<IQTree> transformer = new MaxLevelLensUnfoldingTransformer(maxLevel, query.getVariableGenerator());
        return tree.acceptVisitor(transformer);
    }

    protected class MaxLevelLensUnfoldingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final int maxLevel;
        protected final VariableGenerator variableGenerator;

        protected MaxLevelLensUnfoldingTransformer(int maxLevel, VariableGenerator variableGenerator) {
            super(LensUnfolderImpl.this.iqFactory);
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
