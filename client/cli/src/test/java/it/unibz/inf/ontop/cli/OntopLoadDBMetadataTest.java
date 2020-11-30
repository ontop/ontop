package it.unibz.inf.ontop.cli;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.*;
//import com.fasterxml.jackson.datatype.guava.GuavaModule;
import it.unibz.inf.ontop.dbschema.impl.BasicDBParametersImpl;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataImpl;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.*;

public class OntopLoadDBMetadataTest {

    // Test file
    File dbMetadataFile = new File("src/test/resources/output/exampleBooks-metadata.json");
    /*ImmutableMetadataImpl metadata = new ObjectMapper()
        .registerModule(new GuavaModule())
        .readerFor(ImmutableMetadataImpl.class)
        .readValue(dbMetadataFile);*/
//    ObjectMapper metadata = JsonMapper.builder()
//        .addModule(new GuavaModule())
//        .build();
    ImmutableMetadataImpl metadata = new ObjectMapper()
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .readerFor(ImmutableMetadataImpl.class)
        .readValue(dbMetadataFile);

    BasicDBParametersImpl metadata2 = new ObjectMapper()
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .readerFor(BasicDBParametersImpl.class)
        .readValue(dbMetadataFile);

    public OntopLoadDBMetadataTest() throws IOException {
    }

    @Test // Check relation name
    public void TestLoadMetadataFromJSON() throws IOException {

        //List<Relations> rel2 = metadata.getRelations();
        String name = metadata.getName();
        assertEquals("\"tb_emerge_authors\"", name);
    }

//    @Test // Check primary key
//    public void TestLoadMetadataFromJSON2() throws IOException {
//
//        List<Relations> rel2 = metadata.getRelations();
//        List<UniqueConstraints> uq2 = rel2.get(0).getUniqueConstraints();
//        assertEquals(uq2.get(0).getIsPrimaryKey(), true);
//    }
//
//    @Test // Check foreign key name
//    public void TestLoadMetadataFromJSON3() throws IOException {
//
//        List<Relations> rel = metadata.getRelations();
//        List<ForeignKeys> fk = rel.get(0).getForeignKeys();
//        assertEquals(fk.get(1).getName(), "fk_emerge_writes_book");
//    }

    @Test // Check foreign key name
    public void TestLoadMetadataFromJSON4() throws IOException {

        String drivername = metadata2.getDriverName();
        assertEquals("H2 JDBC Driver", drivername);
    }
}
