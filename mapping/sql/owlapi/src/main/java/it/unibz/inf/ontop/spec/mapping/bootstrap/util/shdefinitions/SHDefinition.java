package it.unibz.inf.ontop.spec.mapping.bootstrap.util.shdefinitions;

import java.util.Objects;

public class SHDefinition {
    private String parent;
    private String child;

    public SHDefinition(String parent, String child){
        this.parent = parent;
        this.child = child;
    }

    public String getParent() {
        return parent;
    }

    public String getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "SHDefinition{" +
                "parent='" + parent + '\'' +
                ", child='" + child + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SHDefinition that = (SHDefinition) o;
        return Objects.equals(parent, that.parent) &&
                Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child);
    }
}
