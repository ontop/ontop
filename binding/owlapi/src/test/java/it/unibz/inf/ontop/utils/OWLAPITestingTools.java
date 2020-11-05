package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;

import java.io.BufferedReader;
import java.io.FileReader;
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

    private static String readFromFile(String filename) throws IOException {
        FileReader reader = new FileReader(filename);
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line).append('\n');
            line = in.readLine();
        }
        in.close();
        return bf.toString();
    }

    private String readFile(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)).collect(joining());
    }

    public static void executeFromFile(Connection conn, String filename) throws SQLException, IOException {
        String s = readFromFile(filename);
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(s);
        }
        conn.commit();
    }
}
