package it.unibz.inf.ontop.model.type;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.IncompatibleTermException;

import java.util.Optional;

public interface ArgumentValidator {

    void validate(ImmutableList<Optional<TermType>> argumentTypes) throws IncompatibleTermException;

    TermType getExpectedBaseType(int index);

    ImmutableList<TermType> getExpectedBaseArgumentTypes();

//    default boolean areCompatible(ImmutableList<Optional<TermType>> argumentTypes) {
//        try {
//            validate(argumentTypes);
//            return true;
//        } catch (IncompatibleTermException e) {
//            return false;
//        }
//    }

}
