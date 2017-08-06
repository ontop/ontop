package it.unibz.inf.ontop.temporal.model;


import java.util.List;

public interface DatalogMTLProgram {

    List<DatalogMTLRule> getRules();

    String render();

}
