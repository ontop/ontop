package federationOptimization;

import java.util.List;

public class MaterializedView {
    String table;
    List<String> attributes;
    String sourceSQL;

    public MaterializedView(){
        table = null;
        attributes = null;
        sourceSQL = null;

    }

    public MaterializedView(String relation, List<String> attribues, String sourceSQL){
        this.table = relation;
        this.attributes = attributes;
        this.sourceSQL = sourceSQL;

    }

    public void print(){
        System.out.println(table+"("+attributes+")");
        System.out.println("<--");
        System.out.println(sourceSQL);
        System.out.println("\r\n");
    }
}
