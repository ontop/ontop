package it.unibz.inf.ontop.spec.mapping.bootstrap.engines.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.BootConf;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Optional;

public class MPEngine extends AbstractBootstrappingEngine {
    protected MPEngine(OntopSQLCredentialSettings settings, SpecificationFactory specificationFactory, SQLPPMappingFactory ppMappingFactory, TypeFactory typeFactory, TermFactory termFactory, RDF rdfFactory, TargetAtomFactory targetAtomFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory, SQLPPSourceQueryFactory sourceQueryFactory, JDBCMetadataProviderFactory metadataProviderFactory) {
        super(settings, specificationFactory, ppMappingFactory, typeFactory, termFactory, rdfFactory, targetAtomFactory, dbFunctionSymbolFactory, sourceQueryFactory, metadataProviderFactory);
    }

    @Override
    public ImmutableList<SQLPPTriplesMap> bootstrapMappings(String baseIRI, ImmutableList<NamedRelationDefinition> tables, SQLPPMapping mapping, BootConf bootConf) {
        return null;
    }

    @Override
    public OWLOntology bootstrapOntology(String baseIRI, Optional<OWLOntology> inputOntology, SQLPPMapping newPPMapping, BootConf bootConf) throws MappingBootstrappingException {
        return null;
    }
}
