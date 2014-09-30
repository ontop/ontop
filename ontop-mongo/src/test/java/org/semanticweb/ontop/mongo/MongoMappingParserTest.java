package org.semanticweb.ontop.mongo;


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
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals("The prefixes element is not a Json Object", e.getMessage());
        }
    }
    
    @Test
    public void testParsePrefixes2() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingPrefixNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals("The value of xsd: key is not a String", e.getMessage());
        }
    }
    
    @Test
    public void testParse4() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingNotArray.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals("The mappings element is not a Json Array", e.getMessage());
        }
    }

    @Test
    public void testParse5() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingNoMappingId.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals("A mapping object should contain mappingId, source and target keys", e.getMessage());
        }
    }

    @Test
    public void testParse6() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingSourceNoCollection.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals("A source query object should contain collection and criteria keys", e.getMessage());
        }
    }

    @Test
    public void testParse7() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingSourceCollectionNotString.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals("The value of collection is not a String", e.getMessage());
        }
    }

    @Test
    public void testParse8() throws IOException, InvalidMongoMappingException {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingSourceCriteriaNotObject.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        } catch (InvalidMongoMappingException e) {
        	Assert.assertEquals("The value of criteria is not a Json Object", e.getMessage());
        }
    }

    @Test(expected=TargetQueryParserException.class)
    public void testParse9() throws Throwable {
    	InputStream stream = MongoMappingParserTest.class.getResourceAsStream("/wrongMappingTargetSyntaxError.json");
        MongoMappingParser parser = new MongoMappingParser(new InputStreamReader(stream));
        try {
        	parser.parse();
        } catch (InvalidMongoMappingException e) {
        	throw e.getCause();
        }
    }

}
