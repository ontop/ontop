/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dag;

import java.io.Serializable;

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
