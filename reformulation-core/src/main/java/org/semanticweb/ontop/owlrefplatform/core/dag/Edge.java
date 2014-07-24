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

@Deprecated
public class Edge implements Serializable  {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1960603133619468795L;
	private final DAGNode left;
    private final DAGNode right;

    public Edge(DAGNode left, DAGNode right) {
        this.left = left;
        this.right = right;
    }

    public DAGNode getLeft() {
        return left;
    }

    public DAGNode getRight() {
        return right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }

    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (this.getClass() != other.getClass())
            return false;
        Edge otherEdge = (Edge) other;
        return this.left.equals(otherEdge.left) && this.right.equals(otherEdge.right);
    }
    
    public String toString() {
    	StringBuilder bf = new StringBuilder();
    	bf.append(left.toString());
    	bf.append(" is-a ");
    	bf.append(right.toString());
    	return bf.toString();
    }

}
