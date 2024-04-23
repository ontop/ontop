package federationOptimization.precomputation;

import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.List;

public class Redundancy {
    String relation1;
    String relation2;
    String redundancyRelation;
    //relation1 and relation2 are SQL queries
    //redundancyRelation is STRICT_CONTAINMENT or EQUIVALENCE

    public Redundancy(String relation1, String relation2, String redundancyRelation){
        this.relation1 = relation1;
        this.relation2 = relation2;
        this.redundancyRelation = redundancyRelation;
    }

    public void print(){
        System.out.println("relation1: "+relation1);
        System.out.println("relation2: "+relation2);
        System.out.println("redundant_relation: "+redundancyRelation);
        System.out.println("");
    }

    static final Comparator<Redundancy> COMPARATOR = Comparator
                .comparing((Redundancy h) -> h.relation1)
                .thenComparing(h -> h.relation2)
                .thenComparing(h -> h.redundancyRelation);

}
