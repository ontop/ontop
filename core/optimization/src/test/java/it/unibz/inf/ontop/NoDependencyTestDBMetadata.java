package it.unibz.inf.ontop;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.FlattenNodePredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

import static it.unibz.inf.ontop.OptimizationTestingTools.TYPE_FACTORY;
import static it.unibz.inf.ontop.OptimizationTestingTools.createDummyMetadata;

public class NoDependencyTestDBMetadata {

    public static final RelationPredicate TABLE1_AR1;
    public static final RelationPredicate TABLE2_AR1;
    public static final RelationPredicate TABLE3_AR1;
    public static final RelationPredicate TABLE4_AR1;
    public static final RelationPredicate TABLE5_AR1;

    public static final RelationPredicate TABLE1_AR2;
    public static final RelationPredicate TABLE2_AR2;
    public static final RelationPredicate TABLE3_AR2;
    public static final RelationPredicate TABLE4_AR2;
    public static final RelationPredicate TABLE5_AR2;
    public static final RelationPredicate TABLE6_AR2;

    public static final RelationPredicate TABLE1_AR3;
    public static final RelationPredicate TABLE2_AR3;
    public static final RelationPredicate TABLE3_AR3;
    public static final RelationPredicate TABLE4_AR3;
    public static final RelationPredicate TABLE5_AR3;
    public static final RelationPredicate TABLE6_AR3;

    public static final RelationPredicate TABLE1_AR4;

    public static final FlattenNodePredicate FLATTEN_NODE_PRED_AR1;
    public static final FlattenNodePredicate FLATTEN_NODE_PRED_AR2;
    public static final FlattenNodePredicate FLATTEN_NODE_PRED_AR3;
    public static final FlattenNodePredicate FLATTEN_NODE_PRED_AR4;

    public static final BasicDBMetadata DB_METADATA;

    private static RelationPredicate createRelationPredicate(BasicDBMetadata dbMetadata, QuotedIDFactory idFactory,
                                                             int tableNumber, int arity) {
        DatabaseRelationDefinition tableDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                "TABLE" + tableNumber + "AR" + arity));
        for (int i=1 ; i <= arity; i++) {
            tableDef.addAttribute(idFactory.createAttributeID("col" + i), TYPE_FACTORY.getUnderspecifiedDBType(), null, false);
        }
        return tableDef.getAtomPredicate();
    }

    private static FlattenNodePredicate createFlattenNodePredicate(BasicDBMetadata dbMetadata, QuotedIDFactory idFactory, int arity){
        return dbMetadata.createFlattenNodeRelation(
                idFactory.createRelationID(
                        null,
                        String.format(
                                "flatten_node_pred_arity_%s",
                                arity
                        )),
                createAttributeIds(idFactory, arity),
                IntStream.range(0, arity)
                        .mapToObj(i -> TYPE_FACTORY.getUnderspecifiedDBType())
                        .collect(ImmutableCollectors.toList()),
                IntStream.range(0, arity)
                        .mapToObj(i -> true)
                        .collect(ImmutableCollectors.toList())
                ).getAtomPredicate();
    }

    private static ImmutableList<QuotedID> createAttributeIds(QuotedIDFactory idFac, int arity) {
        return IntStream.range(1, arity+1).boxed()
                .map(i -> idFac.createAttributeID("col"+i))
                .collect(ImmutableCollectors.toList());
    }

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        TABLE1_AR1 = createRelationPredicate(dbMetadata, idFactory, 1, 1);
        TABLE2_AR1 = createRelationPredicate(dbMetadata, idFactory, 2, 1);
        TABLE3_AR1 = createRelationPredicate(dbMetadata, idFactory, 3, 1);
        TABLE4_AR1 = createRelationPredicate(dbMetadata, idFactory, 4, 1);
        TABLE5_AR1 = createRelationPredicate(dbMetadata, idFactory, 5, 1);

        TABLE1_AR2 = createRelationPredicate(dbMetadata, idFactory, 1, 2);
        TABLE2_AR2 = createRelationPredicate(dbMetadata, idFactory, 2, 2);
        TABLE3_AR2 = createRelationPredicate(dbMetadata, idFactory, 3, 2);
        TABLE4_AR2 = createRelationPredicate(dbMetadata, idFactory, 4, 2);
        TABLE5_AR2 = createRelationPredicate(dbMetadata, idFactory, 5, 2);
        TABLE6_AR2 = createRelationPredicate(dbMetadata, idFactory, 6, 2);

        TABLE1_AR3 = createRelationPredicate(dbMetadata, idFactory, 1, 3);
        TABLE2_AR3 = createRelationPredicate(dbMetadata, idFactory, 2, 3);
        TABLE3_AR3 = createRelationPredicate(dbMetadata, idFactory, 3, 3);
        TABLE4_AR3 = createRelationPredicate(dbMetadata, idFactory, 4, 3);
        TABLE5_AR3 = createRelationPredicate(dbMetadata, idFactory, 5, 3);
        TABLE6_AR3 = createRelationPredicate(dbMetadata, idFactory, 6, 3);

        TABLE1_AR4 = createRelationPredicate(dbMetadata, idFactory, 1, 4);

        FLATTEN_NODE_PRED_AR1 = createFlattenNodePredicate(dbMetadata, idFactory, 1);
        FLATTEN_NODE_PRED_AR2 = createFlattenNodePredicate(dbMetadata, idFactory, 2);
        FLATTEN_NODE_PRED_AR3 = createFlattenNodePredicate(dbMetadata, idFactory, 3);
        FLATTEN_NODE_PRED_AR4 = createFlattenNodePredicate(dbMetadata, idFactory, 4);

        dbMetadata.freeze();
        DB_METADATA = dbMetadata;
    }

}
