package org.semanticweb.ontop.cli;

import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAFactoryWithException;
import org.semanticweb.ontop.io.OntopNativeMappingSerializer;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlapi3.bootstrapping.DirectMappingBootstrapper;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.questdb.R2RMLQuestPreferences;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.Properties;

@Command(name = "bootstrap",
        description = "Bootstrap ontology and mapping from the database")
public class OntopBootstrap extends OntopMappingOntologyRelatedCommand {

    @Option(type = OptionType.COMMAND, name = {"-b", "--base-uri"}, title = "baseURI",
            description = "base uri of the generated mapping")
    protected String baseUri;

    @Override
    public void run() {

        try {
            if (baseUri.contains("#")) {
                System.err.println("Base uri cannot contain the character '#'!");
            } else {
                if (owlFile != null) {
                    File owl = new File(owlFile);
                    File obda = new File(mappingFile);

                    QuestPreferences preferences = getPreferences();
                    Injector injector = Guice.createInjector(new OBDACoreModule(preferences));

                    DirectMappingBootstrapper dm = new DirectMappingBootstrapper(
                            baseUri, jdbcUrl, jdbcUserName, jdbcPassword, jdbcDriverClass,
                            injector.getInstance(NativeQueryLanguageComponentFactory.class),
                            injector.getInstance(OBDAFactoryWithException.class));

                    OBDAModel model = dm.getModel();
                    OWLOntology onto = dm.getOntology();
                    OntopNativeMappingSerializer mappingSerializer = new OntopNativeMappingSerializer(model);
                    mappingSerializer.save(obda);
                    onto.getOWLOntologyManager().saveOntology(onto,
                            new FileDocumentTarget(owl));
                } else {
                    System.err.println("Output file not found!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error occured during bootstrapping: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }

    }

    private QuestPreferences getPreferences() {
        if (mappingFile.endsWith(".obda")){
            return new QuestPreferences();
        }
        else {
            Properties p = new Properties();
            p.setProperty(QuestPreferences.JDBC_URL, jdbcUrl);
            p.setProperty(QuestPreferences.DB_USER, jdbcUserName);
            p.setProperty(QuestPreferences.DB_PASSWORD, jdbcPassword);
            p.setProperty(QuestPreferences.JDBC_DRIVER, jdbcDriverClass);

            return new R2RMLQuestPreferences(p);
        }
    }
}
