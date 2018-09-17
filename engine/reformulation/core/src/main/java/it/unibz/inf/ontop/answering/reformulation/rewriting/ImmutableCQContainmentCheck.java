package it.unibz.inf.ontop.answering.reformulation.rewriting;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface ImmutableCQContainmentCheck {

    /**
     * Returns true if the first query (av1, atoms1) is contained in the second query (av2, atoms2)
     *    (in other words, the first query is more specific, it has fewer answers)
     *
     * @param atoms1
     * @param atoms2
     * @return true if the first query is contained in the second query
     */

    boolean isContainedIn(ImmutableList<Variable> av1, ImmutableList<DataAtom> atoms1, ImmutableList<Variable> av2, ImmutableList<DataAtom> atoms2);

    /***
     * Removes queries that are contained syntactically, using the method
     * isContainedIn(CQIE q1, CQIE 2).
     *
     * Removal of queries is done in two main double scans. The first scan goes
     * top-down/down-top, the second scan goes down-top/top-down
     *
     * @param queries
     */

    default void removeContainedQueries(ImmutableList<Variable> avs, List<ImmutableList<DataAtom>> queries) {

        // first pass - from the start
        {
            Iterator<ImmutableList<DataAtom>> iterator = queries.iterator();
            while (iterator.hasNext()) {
                ImmutableList<DataAtom> query = iterator.next();
                ListIterator<ImmutableList<DataAtom>> iterator2 = queries.listIterator(queries.size());
                while (iterator2.hasPrevious()) {
                    ImmutableList<DataAtom> query2 = iterator2.previous();
                    if (query2 == query)
                        break;
                    if (isContainedIn(avs, query, avs, query2)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        // second pass - from the end
        {
            ListIterator<ImmutableList<DataAtom>> iterator = queries.listIterator(queries.size());
            while (iterator.hasPrevious()) {
                ImmutableList<DataAtom> query = iterator.previous();
                Iterator<ImmutableList<DataAtom>> iterator2 = queries.iterator();
                while (iterator2.hasNext()) {
                    ImmutableList<DataAtom> query2 = iterator2.next();
                    if (query2 == query)
                        break;
                    if (isContainedIn(avs, query, avs, query2)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

    }

}
