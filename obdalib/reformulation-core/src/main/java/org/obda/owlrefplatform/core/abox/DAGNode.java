package org.obda.owlrefplatform.core.abox;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Sergejs Pugacs
 */
public class DAGNode implements Comparable<DAGNode> {


    private final String uri;
    private SemanticIndexRange range = DAG.NULL_RANGE;
    private int index = DAG.NULL_INDEX;

    private final Set<DAGNode> parents = new HashSet<DAGNode>();
    private Set<DAGNode> children = new HashSet<DAGNode>();

    public final Set<DAGNode> descendans = new HashSet<DAGNode>();

    public final LinkedList<DAGNode> equivalents = new LinkedList<DAGNode>();


    public DAGNode(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (this.getClass() != other.getClass())
            return false;

        DAGNode otherNode = (DAGNode) other;
        return this.uri.equals(otherNode.uri);
//                && this.range.equals(otherNode.range)
//                && this.index == otherNode.index;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 37 * result + this.uri.hashCode();
//        result += 37 * result + this.range.hashCode();
//        result += 37 * result + this.index;
        return result;

    }

    @Override
    public String toString() {
        return String.format("URI:%s, Range:%s, Idx: %d", uri, range, index);
    }

    @Override
    public int compareTo(DAGNode o) {
        return this.uri.compareTo(o.uri);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Set<DAGNode> getParents() {
        return parents;
    }

    public void setRange(SemanticIndexRange range) {
        this.range = range;
    }

    public SemanticIndexRange getRange() {
        return this.range;
    }

    public Set<DAGNode> getChildren() {
        return children;
    }

    public String getUri() {
        return uri;
    }

    public LinkedList<DAGNode> getEquivalents() {
        return equivalents;
    }

    public void setChildren(Set<DAGNode> children) {
        this.children = children;
    }

}
