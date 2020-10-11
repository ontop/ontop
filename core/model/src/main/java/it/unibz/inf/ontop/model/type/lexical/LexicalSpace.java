package it.unibz.inf.ontop.model.type.lexical;

import java.util.Optional;

@FunctionalInterface
public interface LexicalSpace {

    Optional<Boolean> includes(String lexicalValue);

}
