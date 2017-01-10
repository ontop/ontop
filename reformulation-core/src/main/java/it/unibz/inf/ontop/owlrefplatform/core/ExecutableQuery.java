package it.unibz.inf.ontop.owlrefplatform.core;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;

/**
 * TODO: explain
 */
public interface ExecutableQuery {

    ImmutableList<String> getSignature();

    Optional<SesameConstructTemplate> getOptionalConstructTemplate();

    boolean isEmpty();
}
