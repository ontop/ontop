package it.unibz.inf.ontop.si.repository.impl;

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


import java.util.Objects;

/**
 * Contiguous interval
 *
 * @author Sergejs Pugacs
 */
public class Interval {

	private final int start, end;

    public Interval(int start, int end) {
        if (start > end)
            throw new IllegalArgumentException("Invalid interval [" + start + ", " + end + "]");

        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]", start, end);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Interval) {
            Interval other = (Interval) obj;
        	return this.start == other.start && this.end == other.end;
        }
        return false;
    }
}