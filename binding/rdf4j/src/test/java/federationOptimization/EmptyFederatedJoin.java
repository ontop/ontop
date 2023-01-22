package federationOptimization;

public class EmptyFederatedJoin {
    String relation1;
    String relation2;
    String joinCondition;
    //relation1 and relation2 are SQL queires
    //joinCondition: a=b, a and b are attributes from relation1 and relation2 respectively

    public EmptyFederatedJoin(String relation1, String relation2, String joinCondition){
        this.relation1 = relation1;
        this.relation2 = relation2;
        this.joinCondition = joinCondition;
    }

    public void print(){
        System.out.println("relation_1: "+relation1);
        System.out.println("relation_2: "+relation2);
        System.out.println("condition: "+joinCondition);
        System.out.println("\r\n");
    }
}
