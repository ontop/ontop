package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.util.*;

public class CompatibleTreeWitnessSetIterator implements Iterator<Collection<TreeWitness>> {

    private final ImmutableCollection<TreeWitness> tws;

    private final boolean isInNext[];  // subset number - binary representation

    private ImmutableList<TreeWitness> nextSet = null; // the first subset is empty - still need to find a compatible non-empty subset

    CompatibleTreeWitnessSetIterator(ImmutableCollection<TreeWitness> tws) {
        this.tws = tws;
        this.isInNext = new boolean[tws.size()];
    }

    /**
     * Returns the next compatible subset of tree witnesses
     *
     * @return the next compatible subset of tree witnesses
     * @exception NoSuchElementException if it has no more subsets
     */

    @Override
    public ImmutableCollection<TreeWitness> next() {
        if (!hasNext())
            throw new NoSuchElementException("The next method was called when no more objects remained.");

        ImmutableList<TreeWitness> result = nextSet;
        nextSet = null;
        return result;
    }

    /**
     * @return <tt>true</tt> if there are more compatible subsets of tree witnesses
     */

    @Override
    public boolean hasNext() {
        if (nextSet != null)
            return !isLast();

        while (!isLast())
            if ((nextSet = moveToNext()) != null)
                return true;

        return false;
    }

    private boolean isLast() {
        for (int i = 0; i < isInNext.length; i++)
            if (!isInNext[i])
                return false;
        return true;
    }

    /**
     *   @return null if the next set is not compatible
     */

    private ImmutableList<TreeWitness> moveToNext() {
        // increment the number of the current subset
        boolean carry = true;
        for (int i = 0; i < isInNext.length; i++) {
            if (!carry)
                break;
            else {
                carry = isInNext[i];
                isInNext[i] = !isInNext[i];
            }
        }
        // extract the subset and check whether it's compatible
        ArrayList<TreeWitness> set = new ArrayList<>();
        int i = 0;
        for (TreeWitness tw : tws)
            if (isInNext[i++]) {
                for (TreeWitness tw0 : set)
                    if (!TreeWitness.isCompatible(tw0, tw))
                        return null;

                set.add(tw);
            }
        return ImmutableList.copyOf(set);
    }

    /**
     * @exception UnsupportedOperationException because the <tt>remove</tt>
     *		  operation is not supported by this Iterator
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("The CompatibleTreeWitnessSetIterator class does not support the remove method.");
    }
}
