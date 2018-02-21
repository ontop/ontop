package it.unibz.inf.ontop.iq.tools.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.RootConstructionNodeEnforcer;

/**
 * Temporary fix for v3beta2.
 * Should disappear in future versions
 *
 * Adds a root Construction to an intermediate query if missing
 */
public class RootConstructionNodeEnforcerImpl implements RootConstructionNodeEnforcer{

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private RootConstructionNodeEnforcerImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IntermediateQuery enforceRootCn(IntermediateQuery query) {
        QueryNode root = query.getRootNode();
        if(root instanceof ConstructionNode){
            return query;
        }
        IntermediateQueryBuilder builder = iqFactory.createIQBuilder(
                query.getDBMetadata(),
                query.getExecutorRegistry()
        );
        ConstructionNode rootCn = iqFactory.createConstructionNode(query.getVariables(root));
        builder.init(query.getProjectionAtom(), rootCn);
        builder.addChild(rootCn, root);
        builder.appendSubtree(root, query);
        return builder.build();
    }

}
