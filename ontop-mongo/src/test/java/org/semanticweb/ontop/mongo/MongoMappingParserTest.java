package org.semanticweb.ontop.mongo;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.Test;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.parser.TargetQueryParserException;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoMappingParserTest {
    @Test
    public void testParse() throws IOException, InvalidMongoMappingException {
        InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/simpleMapping.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.getOBDAModel();
     
        Assert.assertEquals(3, mappings.size());
     
        Set<String> ids = new HashSet<>();
        Set<String> criteria = new HashSet<>();
        for( OBDAMappingAxiom mapping: mappings ) {
        	Assert.assertEquals("students", ((MongoMappingAxiom)mapping).getSourceQuery().getCollectionName());
        	
        	ids.add(((MongoMappingAxiom)mapping).getId());
            criteria.add(((MongoMappingAxiom)mapping).getSourceQuery().getFilterCriteria().toString());

            Assert.assertNotNull(((MongoMappingAxiom)mapping).getTargetQuery());
        }

        Set<String> expectedIds = new HashSet<>();
        expectedIds.add("UndergraduateStudent");
        expectedIds.add("GraduateStudent");
        expectedIds.add("Student");

        Set<String> expectedCriteria = new HashSet<>();
        expectedCriteria.add("{\"type\":1}");
        expectedCriteria.add("{\"type\":2}");
        expectedCriteria.add("{}");

        Assert.assertEquals(expectedIds, ids);
        Assert.assertEquals(expectedCriteria, criteria);
        
        MongoSchemaExtractor.extractCollectionDefinition(mappings);
    }

    @Test
    public void testParse2() throws IOException, InvalidMongoMappingException {
        InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/simpleMappingNoPrefixes.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.getOBDAModel();
     
        Assert.assertEquals(3, mappings.size());
        
        Set<String> ids = new HashSet<>();
        Set<String> criteria = new HashSet<>();
        for( OBDAMappingAxiom mapping: mappings ) {
        	Assert.assertEquals("students", ((MongoMappingAxiom)mapping).getSourceQuery().getCollectionName());
        	
        	ids.add(((MongoMappingAxiom)mapping).getId());
            criteria.add(((MongoMappingAxiom)mapping).getSourceQuery().getFilterCriteria().toString());

            Assert.assertNotNull(((MongoMappingAxiom)mapping).getTargetQuery());
        }

        Set<String> expectedIds = new HashSet<>();
        expectedIds.add("UndergraduateStudent");
        expectedIds.add("GraduateStudent");
        expectedIds.add("Student");

        Set<String> expectedCriteria = new HashSet<>();
        expectedCriteria.add("{\"type\":1}");
        expectedCriteria.add("{\"type\":2}");
        expectedCriteria.add("{}");

        Assert.assertEquals(expectedIds, ids);
        Assert.assertEquals(expectedCriteria, criteria);
    }

    @Test(expected=JsonSyntaxException.class)
    public void testJsonSyntax() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/invalidJson.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.getOBDAModel();
    }

    @Test
    public void testParseDatabase1() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongDatabaseNoDatabase.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.MISSING_DATABASE_KEY, e.getMessage());
        }
    }

    @Test
    public void testParseDatabase2() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongDatabaseNotObject.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_DATABASE_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParseDatabase3() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongDatabaseNoURL.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.MISSING_ONE_OF_DATABASE_KEYS, e.getMessage());
        }
    }

    @Test
    public void testParseDatabase4() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongDatabaseUsernameNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_DATABASE_VALUE_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParsePrefixes1() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongPrefixesNotObject.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_PREFIXES_TYPE, e.getMessage());
        }
    }
    
    @Test
    public void testParsePrefixes2() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongPrefixNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	String expectedMessage = String.format(MongoMappingParser.INVALID_PREFIX_VALUE_TYPE, "xsd:");
        	Assert.assertEquals(expectedMessage, e.getMessage());
        }
    }
    
    @Test
    public void testParseMappings1() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingNoMappings.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.MISSING_MAPPINGS_KEY, e.getMessage());
        }
    }

    @Test
    public void testParseMappings2() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingNotArray.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_MAPPINGS_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParseMapping1() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingNotObject.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_MAPPING_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParseMapping2() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingNoMappingId.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.MISSING_ONE_OF_MAPPING_KEYS, e.getMessage());
        }
    }

    @Test
    public void testParseMapping3() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingMappingIdNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_MAPPING_ID_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParseMapping4() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingSourceNotObject.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_MAPPING_SOURCE_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParseMapping5() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingTargetNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_MAPPING_TARGET_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParseSource1() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingSourceNoCollection.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.MISSING_ONE_OF_SOURCE_QUERY_KEYS, e.getMessage());
        }
    }

    @Test
    public void testParseSource2() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingSourceCollectionNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_SOURCE_COLLECTION_TYPE, e.getMessage());
        }
    }

    @Test
    public void testParseSource3() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingSourceCriteriaNotObject.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_SOURCE_CRITERIA_TYPE, e.getMessage());
        }
    }

    @Test(expected=TargetQueryParserException.class)
    public void testParseTarget1() throws Throwable {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingTargetSyntaxError.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.getOBDAModel();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	throw e.getCause();
        }
    }

    @Test
    public void testExtractSchemaInformation() throws IOException, InvalidMongoMappingException {
        InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/conflictTypes.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.getOBDAModel();
     
        Assert.assertEquals(3, mappings.size());
             
        MongoSchemaExtractor.extractCollectionDefinition(mappings);
    }


}
