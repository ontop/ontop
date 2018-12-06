package it.unibz.inf.ontop.iq.tools.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TypeConstantDictionaryImpl implements TypeConstantDictionary {

    private final Map<RDFTermTypeConstant, DBConstant> typeToIntMap;
    private final Map<DBConstant, RDFTermTypeConstant> intToTypeMap;
    private final AtomicInteger counter;
    private final TermFactory termFactory;
    private final DBTermType dbIntegerType;

    @Inject
    private TypeConstantDictionaryImpl(TermFactory termFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.typeToIntMap = new HashMap<>();
        this.intToTypeMap = new HashMap<>();
        this.counter = new AtomicInteger(0);
        this.dbIntegerType = typeFactory.getDBTypeFactory().getDBLargeIntegerType();
    }

    @Override
    public DBConstant convert(RDFTermTypeConstant termTypeConstant) {
        if (typeToIntMap.containsKey(termTypeConstant))
            return typeToIntMap.get(termTypeConstant);

        DBConstant intConstant = termFactory.getDBConstant("" + counter.getAndIncrement(), dbIntegerType);
        typeToIntMap.put(termTypeConstant, intConstant);
        intToTypeMap.put(intConstant, termTypeConstant);
        return intConstant;
    }

    @Override
    public RDFTermTypeConstant convert(DBConstant constant) {
        return Optional.ofNullable(intToTypeMap.get(constant))
                .orElseThrow(() -> new NoIntConstantForTermTypeConstantException(constant));
    }

    private static class NoIntConstantForTermTypeConstantException extends MinorOntopInternalBugException {
        NoIntConstantForTermTypeConstantException(DBConstant dbConstant) {
            super("Internal bug: no RDFTermType constant registered for " + dbConstant);
        }
    }
}
