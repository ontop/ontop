package org.obda.owlrefplatform.core.abox;

public class Edge {
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

}