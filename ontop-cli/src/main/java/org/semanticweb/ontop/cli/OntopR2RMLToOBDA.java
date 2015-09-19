package org.semanticweb.ontop.cli;

import com.google.common.base.Strings;
import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.io.OntopNativeMappingSerializer;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.questdb.R2RMLQuestPreferences;

import java.io.File;
import java.util.Properties;

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
        try {
            String jdbcurl = "jdbc:h2:tcp://localhost/DBName";
            String username = "username";
            String password = "password";
            String driverclass = "com.mysql.jdbc.Driver";

            Properties p = new Properties();
            p.setProperty(QuestPreferences.JDBC_URL, jdbcurl);
            p.setProperty(QuestPreferences.DB_USER, username);
            p.setProperty(QuestPreferences.DB_PASSWORD, password);
            p.setProperty(QuestPreferences.JDBC_DRIVER, driverclass);

            QuestPreferences preferences = new R2RMLQuestPreferences(p);

            Injector injector = Guice.createInjector(new OBDACoreModule(preferences));

            NativeQueryLanguageComponentFactory factory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
            MappingParser mappingParser = factory.create(new File(inputMappingFile));

            OBDAModel model = mappingParser.getOBDAModel();

            OntopNativeMappingSerializer mappingSerializer = new OntopNativeMappingSerializer(model);
            mappingSerializer.save(out);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
