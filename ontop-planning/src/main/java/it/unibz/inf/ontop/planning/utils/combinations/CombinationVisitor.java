package it.unibz.inf.ontop.planning.utils.combinations;

import java.util.List;

public interface CombinationVisitor<T> {
    
    public void visit(List<T> combination);
    
}
