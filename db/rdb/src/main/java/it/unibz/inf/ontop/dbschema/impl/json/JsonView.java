package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.OntopViewDefinition;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

@JsonDeserialize(using = JsonView.JSONViewDeSerializer.class)
public abstract class JsonView extends JsonOpenObject {
    @Nonnull
    public final List<String> name;

    public JsonView(List<String> name) {
        this.name = name;
    }

    public abstract OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException;

    public abstract void insertIntegrityConstraints(NamedRelationDefinition relation,
                                                    ImmutableList<NamedRelationDefinition> baseRelations,
                                                    MetadataLookup metadataLookup) throws MetadataExtractionException;


    protected static class TemporaryViewPredicate extends AtomPredicateImpl {

        protected TemporaryViewPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }

    public static class JSONViewDeSerializer extends JsonDeserializer<JsonView> {

        @Override
        public JsonView deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonNode node = mapper.readTree(jp);
            String type = node.get("type").asText();

            Class<? extends JsonView> instanceClass;
            switch (type) {
                case "BasicViewDefinition":
                    instanceClass = JsonBasicView.class;
                    break;
                default:
                    // TODO: throw proper exception
                    throw new RuntimeException("Unsupported type of Ontop views: " + type);
            }
            return mapper.treeToValue(node, instanceClass);
        }
    }

}
