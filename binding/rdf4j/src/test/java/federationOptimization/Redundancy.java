package federationOptimization;

public class Redundancy {
    String sql1;
    String sql2;
    String relation;

    public Redundancy(String sql1, String sql2, String relation){
        this.sql1 = sql1;
        this.sql2 = sql2;
        this.relation = relation;
    }

    public void print(){
        System.out.println("relation1: "+sql1);
        System.out.println("relation2: "+sql2);
        System.out.println("redundant_relation: "+relation);
        System.out.println("\r\n");
    }
}
