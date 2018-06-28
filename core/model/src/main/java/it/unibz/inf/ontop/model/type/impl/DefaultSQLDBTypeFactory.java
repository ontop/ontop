package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.SQLDBTypeFactory;
import it.unibz.inf.ontop.model.type.TermType;

public class DefaultSQLDBTypeFactory implements SQLDBTypeFactory {

    private final TermType rootTermType;
    private final DBTermType rootDBType;
    private final DBTermType stringType, integerType;

    @AssistedInject
    private DefaultSQLDBTypeFactory(@Assisted TermType rootTermType) {
        this.rootTermType = rootTermType;
        this.rootDBType = new DBTermTypeImpl("AbstractDBType", rootTermType.getAncestry(), true);
        this.stringType = new VarCharTermType(rootDBType.getAncestry());
        this.integerType = new IntegerDBTermType(rootDBType.getAncestry());
    }

    @Override
    public DBTermType getDBStringType() {
        return stringType;
    }

    @Override
    public DBTermType getDBIntegerType() {
        return integerType;
    }
}
