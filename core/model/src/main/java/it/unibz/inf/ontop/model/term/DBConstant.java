package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.model.type.DBTermType;

public interface DBConstant extends NonNullConstant {

    @Override
    DBTermType getType();
}
