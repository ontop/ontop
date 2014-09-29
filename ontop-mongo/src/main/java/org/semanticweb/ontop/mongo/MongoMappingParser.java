package org.semanticweb.ontop.mongo;

import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAMappingAxiom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class MongoMappingParser implements MappingParser {

    private final String mappingDocument;

    public MongoMappingParser(String mappingDocument){
        this.mappingDocument = mappingDocument;
    }

    public MongoMappingParser(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) { // while loop begins here
            sb.append(line).append("\n");
        }
        br.close();
        mappingDocument = sb.toString();
    }


    @Override
    public List<OBDAMappingAxiom> parse() {
        return null;
    }
}
