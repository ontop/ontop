package org.semanticweb.ontop.cli;

import com.google.common.base.Strings;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.r2rml.R2RMLReader;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@Command(name = "to-obda",
        description = "Convert R2RML format to ontop native mapping format (.obda)")
public class OntopR2RMLToOBDA implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-i", "--input"}, title = "mapping.ttl",
            description = "Input mapping file in R2RML format (.ttl)", required = true)
    protected String inputMappingFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "mapping.obda",
            description = "Output mapping file in Ontop native format (.obda)")
    protected String outputMappingFile;

    @Override
    public void run() {

        if(Strings.isNullOrEmpty(outputMappingFile)){
            outputMappingFile = inputMappingFile.substring(inputMappingFile.lastIndexOf(".")).concat(".obda");
        }

        File out = new File(outputMappingFile);

        URI obdaURI = new File(inputMappingFile).toURI();
        R2RMLReader reader = new R2RMLReader(inputMappingFile);

        String jdbcurl = "jdbc:h2:tcp://localhost/DBName";
        String username = "username";
        String password = "password";
        String driverclass = "com.mysql.jdbc.Driver";

        OBDADataFactory f = OBDADataFactoryImpl.getInstance();
        String sourceUrl = obdaURI.toString();
        OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
                username, password, driverclass);
        OBDAModel model = reader.readModel(dataSource);

        ModelIOManager modelIO = new ModelIOManager(model);
        try {
            modelIO.save(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
