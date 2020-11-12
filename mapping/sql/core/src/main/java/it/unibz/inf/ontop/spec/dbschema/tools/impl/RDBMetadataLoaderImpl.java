package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;


public class RDBMetadataLoaderImpl implements RDBMetadataLoader {

    @Override
    public Metadata loadAndDeserialize() throws IOException {

        //try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();
            // convert JSON file to dbschema
            File viewsFile = new File("c://user//book.json");
            //String viewPath = "c://user//book.json";
            Metadata metadata = mapper.readValue(viewsFile, Metadata.class);
            return metadata;

        //}/* catch (JsonProcessingException e) {
        //    e.printStackTrace();

        //}*/ catch (IOException e) {
        //    e.printStackTrace();
        //}
    }
}
