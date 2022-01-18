package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.stream.Collectors.joining;

public class OWLAPITestingTools {

    public static final OWLAPITranslatorOWL2QL OWLAPI_TRANSLATOR;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
        Injector injector = defaultConfiguration.getInjector();

        OWLAPI_TRANSLATOR = injector.getInstance(OWLAPITranslatorOWL2QL.class);
    }

    public static void executeFromFile(Connection conn, String filename) throws SQLException, IOException {
        String s = Files
                .lines(Paths.get(filename))
                .collect(joining("\n"));
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(s);
        }
        conn.commit();
    }
}
