package federationOptimization;

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
        System.out.println("\r\n");
    }
}
