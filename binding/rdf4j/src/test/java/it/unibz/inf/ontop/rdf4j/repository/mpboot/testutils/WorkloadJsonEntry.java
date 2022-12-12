package it.unibz.inf.ontop.rdf4j.repository.mpboot.testutils;

public class WorkloadJsonEntry {
    private String query;

    public WorkloadJsonEntry(String query){
        this.query = query;
    }

    public String getQuery(){
        return this.query;
    }
}
