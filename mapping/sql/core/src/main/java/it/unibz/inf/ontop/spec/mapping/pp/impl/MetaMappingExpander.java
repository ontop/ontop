package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.exception.MetaMappingExpansionException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

public interface MetaMappingExpander {

    ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mappings, DBParameters dbParameters)
            throws MetaMappingExpansionException;

}
