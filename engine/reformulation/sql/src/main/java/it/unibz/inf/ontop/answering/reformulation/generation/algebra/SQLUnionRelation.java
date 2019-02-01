package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import com.google.common.collect.ImmutableList;

public interface SQLUnionRelation extends SQLRelation {

    ImmutableList<? extends SQLRelation> getSubRelations();
}
