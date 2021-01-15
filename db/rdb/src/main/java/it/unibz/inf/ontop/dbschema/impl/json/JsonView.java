package it.unibz.inf.ontop.dbschema.impl.json;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.OntopViewDefinition;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class JsonView extends JsonOpenObject {
    @Nonnull
    public final List<String> name;

    public JsonView(List<String> name) {
        this.name = name;
    }

    public abstract OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException;

    public abstract void insertIntegrityConstraints(MetadataLookup metadataLookup) throws MetadataExtractionException;


    protected static class TemporaryViewPredicate extends AtomPredicateImpl {

        protected TemporaryViewPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }

}
