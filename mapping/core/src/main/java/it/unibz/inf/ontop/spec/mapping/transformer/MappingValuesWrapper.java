package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;


/**
 * Wraps ValuesNode-s into lenses when they have unique constraints.
 * This enables self-join elimination without having to go over the entries in ValuesNode-s.
 */
public interface MappingValuesWrapper {

    ImmutableList<MappingAssertion> normalize(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters);
}
