package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

public class AthenaDBTypeFactory extends TrinoDBTypeFactory {
    @AssistedInject
    protected AthenaDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(rootTermType, typeFactory, createGenericAbstractTypeMap(rootTermType, typeFactory, "<>"));
    }

}
