package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.spec.dbschema.tools.DBMetadataExtractorAndSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Command(name = "extract-db-metadata",
        description = "Extract the DB metadata and serialize it into an output JSON file")
public class OntopExtractDBMetadata implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-p", "--properties"}, title = "properties file",
            description = "Properties file")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String propertiesFile;

    @Option(type = OptionType.COMMAND, override = true, name = {"-o", "--output"},
            title = "output", description = "output file")
    private String outputFile;

    @Override
    public void run() {
        OntopMappingSQLConfiguration configuration = OntopMappingSQLConfiguration.defaultBuilder()
                .propertyFile(propertiesFile)
                .build();

        Injector injector = configuration.getInjector();
        DBMetadataExtractorAndSerializer extractorAndSerializer = injector.getInstance(
                DBMetadataExtractorAndSerializer.class);

        try {
            String payload = extractorAndSerializer.extractAndSerialize();

            OutputStream out = outputFile == null
                    ? System.out
                    : new FileOutputStream(new File(outputFile));

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            wr.write(payload);
            wr.flush();
            wr.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
