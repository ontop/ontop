package it.unibz.inf.ontop.cli;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MutableConfigOverride;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataImpl;
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.JSONRelation;
import it.unibz.inf.ontop.dbschema.impl.RelationIDKeyDeserializer;
import org.junit.Test;

import javax.management.relation.Relation;
import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.*;

public class OntopLoadDBMetadataTest {

    // Test file
    File dbMetadataFile = new File("src/test/resources/output/exampleBooks-metadata.json");

    // DatabaseRelationDefinition error
    /*ImmutableMetadataImpl metadata = new ObjectMapper()
        .registerModule(new GuavaModule())
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .readerFor(ImmutableMetadataImpl[].class)
        .readValue(dbMetadataFile);*/

    /*ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new GuavaModule())
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
    CollectionType javatype = objectMapper.getTypeFactory().constructCollectionType(List.class,ImmutableMetadataImpl.class);
    List<ImmutableMetadataImpl> metadata = objectMapper.readValue(dbMetadataFile, javatype);*/

    SimpleModule simpleModule = new SimpleModule().addKeyDeserializer(RelationID.class, new RelationIDKeyDeserializer());
    ObjectMapper objectMapper = new ObjectMapper()
        //.registerModule(new GuavaModule())
        .registerModule(simpleModule)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        //.readerFor(JSONRelation.class)
        //.readValue(dbMetadataFile);;
        //.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        //.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    //CollectionType javatype = objectMapper.getTypeFactory().constructCollectionType(List.class,ImmutableMetadataImpl.class);
    //List<ImmutableMetadataImpl> metadata = objectMapper.readValue(dbMetadataFile, javatype);
    //ImmutableMetadataImpl metadata = objectMapper.readValue(dbMetadataFile, ImmutableMetadataImpl.class);

    /*return evenIndexedNames;
    for metadata.get().getName()
    for (RelationID retrievedId : retrievedRelation.getAllIDs()) {
        DatabaseRelationDefinition prev = map.put(retrievedId, retrievedRelation);
        if (prev != null)
            throw new MetadataExtractionException("Clashing relation IDs: " + retrievedId + " and " + relationId);
    }*/

    //ObjectMapper mapper = new ObjectMapper();

    //try {
       /* objectMapper.configOverride(JSONRelation.class)
            .setVisibility((JsonAutoDetect.Value.defaultVisibility())
                .withFieldVisibility(JsonAutoDetect.Visibility.NON_PRIVATE));*/
    //} catch (Exception e) {
    //        e.printStackTrace();
    //}



    //JSONRelation rel = new JSONRelation();

    //DatabaseRelationDefinition other = mapper.convertValue(rel, DatabaseRelationDefinition.class);


    //JSONRelation rel3 = objectMapper.convertValue()
    MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, RelationID.class, DatabaseRelationDefinition.class);
    MapType mapType1 = objectMapper.getTypeFactory().constructMapType(HashMap.class, RelationID.class, JSONRelation.class);
    HashMap<RelationID, JSONRelation> map = objectMapper.readValue(dbMetadataFile, mapType1);
    HashMap<RelationID, DatabaseRelationDefinition> map2 = objectMapper.convertValue(map, new TypeReference<HashMap<RelationID, DatabaseRelationDefinition>>() {
    });
    //MapType m2 = objectMapper.convertValue(mapType1, new TypeReference<DatabaseRelationDefinition>() {});

    //Map<RelationID, DatabaseRelationDefinition> map = objectMapper.convertValue(metadata, Map.class);




    public OntopLoadDBMetadataTest() throws IOException {
    }

    @Test // Check relation name
    public void TestLoadMetadataFromJSON() throws IOException {

        System.out.println("START HERE");
        //System.out.println(map);
        //System.out.println(evenIndexedNames);
        //System.out.println(metadata.size());
        System.out.println("END HERE");
        String name = map2.get(0).getPrimaryKey().get().getName();
        //String name = metadata.get(0).getName();
        //Relation rel2 = metadata.getRelations().get(0).
        //String name = rel2.ge
        assertEquals("\"tb_bk_gen\"", name);
    }

    @Test // Check primary key
    public void TestLoadMetadataFromJSON2() throws IOException {

        //Map<String, Object> temp1 = metadata.get(0).getUniqueConstraints();
        //String name = metadata.get(0).getForeignkeyname();
        //String name = metadata.get(0).getName();
        //assertTrue(name);
        //assertEquals("pk_gen", name);
    }
//
//    @Test // Check foreign key name
//    public void TestLoadMetadataFromJSON3() throws IOException {
//
//        List<Relations> rel = metadata.getRelations();
//        List<ForeignKeys> fk = rel.get(0).getForeignKeys();
//        assertEquals(fk.get(1).getName(), "fk_emerge_writes_book");
//    }

    /*@Test // Check foreign key name
    public void TestLoadMetadataFromJSON4() throws IOException {

        String drivername = metadata2.getDriverName();
        assertEquals("H2 JDBC Driver", drivername);
    }*/
}
