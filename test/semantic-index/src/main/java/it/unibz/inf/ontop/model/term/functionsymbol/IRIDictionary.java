package it.unibz.inf.ontop.model.term.functionsymbol;


public interface IRIDictionary {

    /***
     * We look for the ID in the list of IDs, if its not there, we return -2, which we know will never appear
     * on the DB. This is correct because if a constant appears in a query, and that constant was never inserted
     * in the DB, the query must be empty (that atom), by putting -2 as id, we will enforce that.
     * @param uri
     * @return
     */
    int getId(String uri);

    String getURI(Integer id);
}
