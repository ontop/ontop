package it.unibz.inf.ontop.cli;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibz.inf.ontop.dbschema.impl.BasicDBParametersImpl;
import it.unibz.inf.ontop.spec.dbschema.tools.impl.ForeignKeys;
import it.unibz.inf.ontop.spec.dbschema.tools.impl.Metadata;
import it.unibz.inf.ontop.spec.dbschema.tools.impl.Relations;
import it.unibz.inf.ontop.spec.dbschema.tools.impl.UniqueConstraints;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.*;

public class OntopLoadDBMetadataTest {

    // Test file
    File dbMetadataFile = new File("src/test/resources/output/exampleBooks-metadata.json");
    Metadata metadata = new ObjectMapper()
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .readerFor(Metadata.class)
        .readValue(dbMetadataFile);

    BasicDBParametersImpl metadata2 = new ObjectMapper()
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .readerFor(BasicDBParametersImpl.class)
        .readValue(dbMetadataFile);

    public OntopLoadDBMetadataTest() throws IOException {
    }

    @Test // Check relation name
    public void TestLoadMetadataFromJSON() throws IOException {

        List<Relations> rel2 = metadata.getRelations();
        assertEquals("\"tb_emerge_authors\"", rel2.get(0).getName());
    }

    @Test // Check primary key
    public void TestLoadMetadataFromJSON2() throws IOException {

        List<Relations> rel2 = metadata.getRelations();
        List<UniqueConstraints> uq2 = rel2.get(0).getUniqueConstraints();
        assertEquals(uq2.get(0).getIsPrimaryKey(), true);
    }

    @Test // Check foreign key name
    public void TestLoadMetadataFromJSON3() throws IOException {

        List<Relations> rel = metadata.getRelations();
        List<ForeignKeys> fk = rel.get(0).getForeignKeys();
        assertEquals(fk.get(1).getName(), "fk_emerge_writes_book");
    }

    @Test // Check foreign key name
    public void TestLoadMetadataFromJSON4() throws IOException {

        String drivername = metadata2.getDriverName();
        //List<ForeignKeys> fk = rel.get(0).getForeignKeys();
        assertEquals(drivername, "fk_emerge_writes_book");
    }
}
