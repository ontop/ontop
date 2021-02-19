package it.unibz.inf.ontop.protege.query;

public interface QueryManagerEventListener {

    void inserted(QueryManager.Item entity, int indexInParent);

    void removed(QueryManager.Item entity, int indexInParent);

    void changed(QueryManager.Item query, int indexInParent);
}
