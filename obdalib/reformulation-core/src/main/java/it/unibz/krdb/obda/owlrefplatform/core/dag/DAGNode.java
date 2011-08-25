package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Sergejs Pugacs
 */
public class DAGNode {


    private final Description description;

    private SemanticIndexRange range = DAG.NULL_RANGE;
    private int index = DAG.NULL_INDEX;

    private Set<DAGNode> parents = new LinkedHashSet<DAGNode>();
    private Set<DAGNode> children = new LinkedHashSet<DAGNode>();

    public Set<DAGNode> descendans = new LinkedHashSet<DAGNode>();

    public LinkedList<DAGNode> equivalents = new LinkedList<DAGNode>();


    public DAGNode(Description description) {
        this.description = description;
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
        return this.description.equals(otherNode.description)
                && this.range.equals(otherNode.range)
                && this.index == otherNode.index;
    }

    @Override
    public String toString() {
        return "DAGNode{" +
                "description=" + description +
                ", range=" + range +
                ", index=" + index +
                '}';
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (range != null ? range.hashCode() : 0);
        result = 31 * result + index;
        return result;
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

    public LinkedList<DAGNode> getEquivalents() {
        return equivalents;
    }

    public void setChildren(Set<DAGNode> children) {
        this.children = children;
    }

    public void setParents(Set<DAGNode> parents) {
        this.parents = parents;
    }

    public Description getDescription() {
        return description;
    }
}
