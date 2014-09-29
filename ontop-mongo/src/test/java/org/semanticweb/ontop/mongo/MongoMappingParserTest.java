package org.semanticweb.ontop.mongo;


import junit.framework.Assert;
import org.junit.Test;
import org.semanticweb.ontop.model.OBDAMappingAxiom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MongoMappingParserTest {
    @Test
    public void testParse() throws IOException {
        InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/simpleMapping.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.parse();
        Assert.assertEquals(3, mappings.size());
        // TODO: test more details in the parsed mappings
    }
}
