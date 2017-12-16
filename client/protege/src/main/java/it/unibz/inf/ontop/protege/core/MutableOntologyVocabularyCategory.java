package it.unibz.inf.ontop.protege.core;

import java.util.Collection;

public interface MutableOntologyVocabularyCategory<T> {

    /**
     * check whether the entity has been declared and return the entity object
     *
     * @param uri
     * @return
     * @throws RuntimeException if the entity has not been declared
     */

    T get(String uri);

    /**
     * check whether the entity has been declared
     *
     * @param uri
     * @return
     */

    boolean contains(String uri);


    /**
     * return all declared entities
     *
     * @return
     */

    Collection<T> all();

    /**
     * declare an entity
     *
     * @param uri
     * @return entity object
     */

    T create(String uri);

    /**
     * remove the entity
     *
     * @param uri
     */

    void remove(String uri);
}
