package it.unibz.inf.ontop.dbschema;

public class RelationNotFoundException extends Exception {
    public RelationNotFoundException(RelationID id) {
        super("Relation not found: " + id);
    }
}
