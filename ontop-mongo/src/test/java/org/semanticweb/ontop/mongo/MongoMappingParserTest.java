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
import java.util.List;

public class MongoMappingParserTest {
    @Test
    public void testParse() throws IOException, InvalidMongoMappingException {
        InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/simpleMapping.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.parse();
     
        Assert.assertEquals(3, mappings.size());
        
        StringBuilder idsBuilder = new StringBuilder();
        StringBuilder criteriaBuilder = new StringBuilder();
        for( OBDAMappingAxiom mapping: mappings ) {
        	Assert.assertEquals("students", ((MongoMappingAxiom)mapping).getSourceQuery().getCollectionName());
        	idsBuilder.append(((MongoMappingAxiom)mapping).getId()).append(" ");
            criteriaBuilder.append(((MongoMappingAxiom)mapping).getSourceQuery().getFilterCriteria().toString());
            Assert.assertNotNull(((MongoMappingAxiom)mapping).getTargetQuery());
        }
        
        Assert.assertEquals("UndergraduateStudent GraduateStudent Student ", idsBuilder.toString());
        Assert.assertEquals("{\"type\":1}{\"type\":2}{}", criteriaBuilder.toString());
    }

    @Test
    public void testParse2() throws IOException, InvalidMongoMappingException {
        InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/simpleMappingNoPrefixes.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.parse();
     
        Assert.assertEquals(3, mappings.size());
        
        StringBuilder idsBuilder = new StringBuilder();
        StringBuilder criteriaBuilder = new StringBuilder();
        for( OBDAMappingAxiom mapping: mappings ) {
        	Assert.assertEquals("students", ((MongoMappingAxiom)mapping).getSourceQuery().getCollectionName());
        	idsBuilder.append(((MongoMappingAxiom)mapping).getId()).append(" ");
            criteriaBuilder.append(((MongoMappingAxiom)mapping).getSourceQuery().getFilterCriteria().toString());
            Assert.assertNotNull(((MongoMappingAxiom)mapping).getTargetQuery());
        }
        
        Assert.assertEquals("UndergraduateStudent GraduateStudent Student ", idsBuilder.toString());
        Assert.assertEquals("{\"type\":1}{\"type\":2}{}", criteriaBuilder.toString());
    }

    @Test(expected=JsonSyntaxException.class)
    public void testJsonSyntax() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/invalidJson.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        List<OBDAMappingAxiom> mappings =  parser.parse();
    }

    @Test
    public void testParsePrefixes1() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingPrefixesNotObject.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        	fail();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals(MongoMappingParser.INVALID_PREFIXES_TYPE, e.getMessage());
        }
    }
    
    @Test
    public void testParsePrefixes2() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingPrefixNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
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
        	parser.parse();
         	fail();
        } catch (InvalidMongoMappingException e) {
        	throw e.getCause();
        }
    }

}
