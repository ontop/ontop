package it.unibz.inf.ontop.spec.ontology;

/**
 * @author Davide Lanti
 */
public class NamedAssertion implements Assertion {
    public Assertion assertion;
    public String graph;

    private NamedAssertion(Assertion assertion, String graph){
        this.assertion = assertion;
        this.graph = graph;
    }

    public static NamedAssertion decorate(Assertion assertion, String graph){
        return new NamedAssertion(assertion, graph);
    }

    public Assertion assertion(){
        return assertion;
    }

    public String graph(){
        return graph;
    }

    @Override
    public String toString(){
        return "Assertion: " + this.assertion + ", Graph: " + this.graph;
    }

}
