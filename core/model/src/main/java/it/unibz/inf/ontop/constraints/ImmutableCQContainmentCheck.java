package it.unibz.inf.ontop.constraints;

import it.unibz.inf.ontop.model.atom.AtomPredicate;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface ImmutableCQContainmentCheck<P extends AtomPredicate> {

    /**
     * Returns true if the query cq1 is contained in the query cq2
     *    (in other words, the first query is more specific, it has fewer answers)
     *
     * @param cq1
     * @param cq2
     * @return true if the first query is contained in the second query
     */

    boolean isContainedIn(ImmutableCQ<P> cq1, ImmutableCQ<P> cq2);

    /***
     * Removes queries that are contained syntactically, using the method
     * isContainedIn(CQIE q1, CQIE 2).
     *
     * Removal of queries is done in two main double scans. The first scan goes
     * top-down/down-top, the second scan goes down-top/top-down
     *
     * @param queries
     */

    default void removeContainedQueries(List<ImmutableCQ<P>> queries) {

        // first pass - from the start
        {
            Iterator<ImmutableCQ<P>> iterator = queries.iterator();
            while (iterator.hasNext()) {
                ImmutableCQ<P> query = iterator.next();
                ListIterator<ImmutableCQ<P>> iterator2 = queries.listIterator(queries.size());
                while (iterator2.hasPrevious()) {
                    ImmutableCQ<P> query2 = iterator2.previous();
                    if (query2 == query)
                        break;
                    if (isContainedIn(query, query2)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        // second pass - from the end
        {
            ListIterator<ImmutableCQ<P>> iterator = queries.listIterator(queries.size());
            while (iterator.hasPrevious()) {
                ImmutableCQ<P> query = iterator.previous();
                Iterator<ImmutableCQ<P>> iterator2 = queries.iterator();
                while (iterator2.hasNext()) {
                    ImmutableCQ<P> query2 = iterator2.next();
                    if (query2 == query)
                        break;
                    if (isContainedIn(query, query2)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

    }

}
