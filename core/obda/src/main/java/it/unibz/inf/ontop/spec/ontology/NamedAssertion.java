package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.model.term.ObjectConstant;

/**
 * @author Davide Lanti
 */
public class NamedAssertion implements Assertion {
    public Assertion assertion;
    public ObjectConstant graph;

    private NamedAssertion(Assertion assertion, ObjectConstant graph){
        this.assertion = assertion;
        this.graph = graph;
    }

    public static NamedAssertion of(Assertion assertion, ObjectConstant graph){
        return new NamedAssertion(assertion, graph);
    }

    public Assertion assertion(){
        return assertion;
    }

    public ObjectConstant getGraph(){
        return graph;
    }

    @Override
    public String toString(){
        return this.assertion + " [" + this.graph + "]";
    }

}
