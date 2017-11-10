package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.google.common.base.Strings;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;

import java.io.File;
import java.util.Properties;

@Command(name = "to-obda",
        description = "Convert R2RML format to ontop native mapping format (.obda)")
public class OntopR2RMLToOBDA implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-i", "--input"}, title = "mapping.ttl",
            description = "Input mapping file in R2RML format (.ttl)")
    @Required
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
        try {
            Properties p = new Properties();
            p.put(OntopSQLCoreSettings.JDBC_NAME, "h2");
            p.put(OntopSQLCoreSettings.JDBC_URL, "jdbc:h2:tcp://localhost/DBName");
            p.put(OntopSQLCredentialSettings.JDBC_USER, "username");
            p.put(OntopSQLCredentialSettings.JDBC_PASSWORD, "password");
            p.put(OntopSQLCoreSettings.JDBC_DRIVER, "com.mysql.jdbc.Driver");

            OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .r2rmlMappingFile(inputMappingFile)
                    .properties(p)
                    .build();

            SQLPPMapping ppMapping = configuration.loadProvidedPPMapping();

            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(ppMapping);
            writer.save(out);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
