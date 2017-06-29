package it.unibz.inf.ontop.mapping.datalog;


import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.CQIE;

import java.util.stream.Stream;

public interface Mapping2DatalogConverter {

    Stream<CQIE> convert(Mapping mapping);
}
