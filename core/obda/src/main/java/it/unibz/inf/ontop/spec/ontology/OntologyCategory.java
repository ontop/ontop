package it.unibz.inf.ontop.spec.ontology;

import java.util.Collection;
import java.util.Iterator;

public interface OntologyCategory<T> {
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

    Iterator<T> iterator();
}
