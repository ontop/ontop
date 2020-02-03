package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import org.apache.commons.rdf.api.IRI;

/**
 * To be built by Guice (Assisted inject pattern)
 *
 * Accessible through Guice (recommended) or through MappingCoreSingletons.
 *
 */
public interface SpecificationFactory {

    PrefixManager createPrefixManager(ImmutableMap<String, String> prefixToURIMap);

    MappingInTransformation createMapping(@Assisted("propertyTable") ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable,
                                          @Assisted("classTable") ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable);

    OBDASpecification createSpecification(Mapping saturatedMapping, DBMetadata dbMetadata, ClassifiedTBox tBox);
}
