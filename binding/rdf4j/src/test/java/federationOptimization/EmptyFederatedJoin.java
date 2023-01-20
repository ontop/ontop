package federationOptimization;

public class EmptyFederatedJoin {
    String sql1;
    String sql2;
    String condition;

    public EmptyFederatedJoin(String sql1, String sql2, String condition){
        this.sql1 = sql1;
        this.sql2 = sql2;
        this.condition = condition;
    }

    public void print(){
        System.out.println("relation_1: "+sql1);
        System.out.println("relation_2: "+sql2);
        System.out.println("condition: "+condition);
        System.out.println("\r\n");
    }
}
