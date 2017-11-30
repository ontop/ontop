package it.unibz.inf.ontop.temporal.model;


import java.util.List;
import java.util.Map;

public interface DatalogMTLProgram {

    List<DatalogMTLRule> getRules();

    String render();

    Map<String, String> getPrefixes();

}
