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

public class CompatibleTreeWitnessSetIterator implements Iterator<ImmutableCollection<TreeWitness>> {

    private final ImmutableList<TreeWitness> tws;

    private final boolean in[];  // subset number - binary representation
    private ImmutableList<TreeWitness> nextSet = null; // the first subset is empty - still need to find a compatible non-empty subset

    CompatibleTreeWitnessSetIterator(ImmutableList<TreeWitness> tws) {
        this.tws = tws;
        this.in = new boolean[tws.size()];
    }

    /**
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

        while (!isLast()) {
            // increment the number of the current subset
            boolean carry = true;
            for (int i = 0; i < in.length; i++) {
                if (carry) {
                    carry = in[i];
                    in[i] = !in[i];
                }
                else
                    break;
            }
            // extract the respective subset of tree witnesses
            ImmutableList.Builder<TreeWitness> builder = ImmutableList.builder();
            for (int i = 0; i < in.length; i++)
                if (in[i])
                    builder.add(tws.get(i));
            nextSet = builder.build();

            if (TreeWitness.isCompatible(nextSet))
                return true;

            // if not compatible, continue to the next iteration with clear nextSet
            nextSet = null;
        }
        return false;
    }

    private boolean isLast() {
        for (int i = 0; i < in.length; i++)
            if (!in[i])
                return false;
        return true;
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
