package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.SQLDBTypeFactory;
import it.unibz.inf.ontop.model.type.TermType;

public class SQLDBTypeFactoryImpl implements SQLDBTypeFactory {

    private final TermType rootTermType;

    @AssistedInject
    private SQLDBTypeFactoryImpl(@Assisted TermType rootTermType) {
        this.rootTermType = rootTermType;
    }

    @Override
    public DBTermType getDBStringType() {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public DBTermType getDBIntegerType() {
        throw new RuntimeException("TODO: implement it");
    }
}
