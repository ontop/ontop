package it.unibz.inf.ontop.answering.resultset;

import it.unibz.inf.ontop.exception.OntopConnectionException;

public interface BooleanResultSet extends OBDAResultSet {

    boolean getValue() throws OntopConnectionException;
}
