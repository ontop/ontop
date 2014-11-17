package org.semanticweb.ontop.owlrefplatform.core.dag;

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


import java.io.Serializable;

/**
 * Continues interval between 2 points
 *
 * @author Sergejs Pugacs
 */
@Deprecated
public class Interval implements Comparable<Interval>, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3982860811012207357L;
	final int start;
	final int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object other) {

        if (other == null)
            return false;
        if (other == this)
            return true;
        if (this.getClass() != other.getClass())
            return false;
        Interval otherInterval = (Interval) other;

        return (this.start == otherInterval.start && this.end == otherInterval.end);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 37 * result + start;
        result += 37 * result + end;
        return result;
    }

    @Override
    public int compareTo(Interval o) {
        return this.start - o.start;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]", start, end);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
