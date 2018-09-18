package it.unibz.inf.ontop.answering.reformulation.rewriting;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface ImmutableCQContainmentCheck {

    /**
     * Returns true if the query cq1 is contained in the query cq2
     *    (in other words, the first query is more specific, it has fewer answers)
     *
     * @param cq1
     * @param cq2
     * @return true if the first query is contained in the second query
     */

    boolean isContainedIn(ImmutableCQ cq1, ImmutableCQ cq2);

    /***
     * Removes queries that are contained syntactically, using the method
     * isContainedIn(CQIE q1, CQIE 2).
     *
     * Removal of queries is done in two main double scans. The first scan goes
     * top-down/down-top, the second scan goes down-top/top-down
     *
     * @param queries
     */

    default void removeContainedQueries(List<ImmutableCQ> queries) {

        // first pass - from the start
        {
            Iterator<ImmutableCQ> iterator = queries.iterator();
            while (iterator.hasNext()) {
                ImmutableCQ query = iterator.next();
                ListIterator<ImmutableCQ> iterator2 = queries.listIterator(queries.size());
                while (iterator2.hasPrevious()) {
                    ImmutableCQ query2 = iterator2.previous();
                    if (query2 == query)
                        break;
                    if (isContainedIn(query, query2)) {
                        System.out.println("Q " + query + " IS REMOVED BECAUSE OF " + query2);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        // second pass - from the end
        {
            ListIterator<ImmutableCQ> iterator = queries.listIterator(queries.size());
            while (iterator.hasPrevious()) {
                ImmutableCQ query = iterator.previous();
                Iterator<ImmutableCQ> iterator2 = queries.iterator();
                while (iterator2.hasNext()) {
                    ImmutableCQ query2 = iterator2.next();
                    if (query2 == query)
                        break;
                    if (isContainedIn(query, query2)) {
                        iterator.remove();
                        System.out.println("Q " + query + " IS REMOVED BECAUSE OF " + query2);
                        break;
                    }
                }
            }
        }

    }

}
