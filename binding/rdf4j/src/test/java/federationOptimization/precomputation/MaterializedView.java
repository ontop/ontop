package federationOptimization.precomputation;

import java.util.List;

public class MaterializedView {
    String table;
    List<String> attributes;
    String relation1;
    String relation2;
    String joinCondition;
    //table is the name of the relation for the materialized view
    //table(attributes) <- select * from (relation1 as V1), (relation2 as V2) where V1.attribute1 = V2.attribute2;

    public MaterializedView(){
        table = null;
        attributes = null;
        relation1 = null;
        relation2 = null;
        joinCondition = null;

    }

    public MaterializedView(String relation, List<String> attribues, String relation1, String relation2, String joinCondition){
        this.table = relation;
        this.attributes = attributes;
        this.relation1 = relation1;
        this.relation2 = relation2;
        this.joinCondition = joinCondition;

    }

    public void print(){
        System.out.println(table+"("+attributes+")");
        System.out.println("<--");
        System.out.println(relation1);
        System.out.println(relation2);
        System.out.println(joinCondition);
        System.out.println("");
    }
}
