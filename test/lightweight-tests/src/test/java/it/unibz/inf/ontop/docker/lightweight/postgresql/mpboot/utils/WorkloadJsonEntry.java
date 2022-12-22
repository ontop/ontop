package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils;

public class WorkloadJsonEntry {
    private String query;

    public WorkloadJsonEntry(String query){
        this.query = query;
    }

    public String getQuery(){
        return this.query;
    }
}
