package it.unibz.inf.ontop.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.rvesse.airline.parser.errors.ParseOptionUnexpectedException;
import it.unibz.inf.ontop.cli.utils.Env;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule;

import java.lang.reflect.Field;
import java.util.Arrays;

import static it.unibz.inf.ontop.cli.Ontop.getOntopCommandCLI;
import static org.junit.Assert.*;

public class OntopEnvVarTest {

    private ListAppender<ILoggingEvent> listAppender;

    @Rule
    public EnvironmentVariablesRule rule = new EnvironmentVariablesRule();

    @Before
    public void setup() {
        Logger logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);

        // Set logging level to DEBUG to detect DEBUG error messages
        logbackLogger.setLevel(Level.DEBUG);
    }

    @After
    public void clean() {
        Logger logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);

        // Set logging level to DEBUG to detect DEBUG error messages
        logbackLogger.setLevel(Level.ERROR);
    }

    @Test
    public void testMappingEnvOverride() {
        rule.set("ONTOP_MAPPING_FILE", "example.obda");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-m", "example2.obda",
                "-t", "example.owl",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_MAPPING_FILE");
        assertEquals("example2.obda", envVal);
    }

    @Test
    public void testMappingEnvOverrideLog() {
        rule.set("ONTOP_MAPPING_FILE", "example.obda");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-m", "example2.obda",
                "-t", "example.owl",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_MAPPING_FILE");
        String lastNonErrorMessage = getLastLogMessage("WARN");
        assertEquals("example2.obda", envVal);
        assertEquals("Ignoring environment variable ONTOP_MAPPING_FILE=example.obda for explicitly specified " +
                "option -m", lastNonErrorMessage);
    }

    @Test
    public void testMappingEnvDeprecation() {
        rule.set("MAPPING_FILE", "example.obda");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_MAPPING_FILE");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        // Test that ONTOP_MAPPING_FILE is correctly set
        assertEquals(System.getenv("MAPPING_FILE"), envVal);
        // Test the correct warning message is logged
        assertEquals("Environment variable MAPPING_FILE is deprecated. Please use ONTOP_MAPPING_FILE instead",
                lastWarnMessage);
        // Test the correct debug message is logged, injected option
        assertEquals("Set option -m via environment variable MAPPING_FILE=example.obda", lastDebugMessage);
    }

    @Test
    public void testOntologyEnvDeprecation() {
        rule.set("ONTOLOGY_FILE", "example.owl");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_ONTOLOGY_FILE");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        // Test that ONTOP_MAPPING_FILE is correctly set
        assertEquals(System.getenv("ONTOLOGY_FILE"), envVal);
        // Test the correct warning message is logged
        assertEquals("Environment variable ONTOLOGY_FILE is deprecated. Please use ONTOP_ONTOLOGY_FILE instead",
                lastWarnMessage);
        // Test the correct debug message is logged, injected option
        assertEquals("Set option -t via environment variable ONTOLOGY_FILE=example.owl", lastDebugMessage);
    }

    @Test
    public void testOntologyEnvOverride() {
        rule.set("ONTOLOGY_FILE", "example2.owl");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_ONTOLOGY_FILE");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        // Test that ONTOLOGY_FILE is ignored, and that the value is retrieved from the option -t
        assertEquals("example.owl", envVal);
        // Test the correct warning message is logged
        assertEquals("Ignoring environment variable ONTOLOGY_FILE=example2.owl for explicitly specified " +
                "option -t", lastWarnMessage);
        // Test the correct debug message is logged, no message since option -t is set
        assertEquals(null, lastDebugMessage);
    }

    @Test
    public void testOntologyEnvOverrideDeprecation() {
        rule.set("ONTOLOGY_FILE", "example2.owl");
        rule.set("ONTOP_ONTOLOGY_FILE", "example.owl");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_ONTOLOGY_FILE");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        // Test that ONTOLOGY_FILE is ignored, and that the value is retrieved from ONTOP_ONTOLOGY_FILE
        assertEquals(System.getenv("ONTOP_ONTOLOGY_FILE"), envVal);
        // Test the correct warning message is logged. The non-deprecated env variable is used.
        assertEquals("Multiple environment variables ONTOP_ONTOLOGY_FILE=example.owl, ONTOLOGY_FILE=example2.owl" +
                " set for option -t. Using ONTOP_ONTOLOGY_FILE=example.owl", lastWarnMessage);
        // Test the correct debug message is logged, option -t is set via environment variable
        assertEquals("Set option -t via environment variable ONTOP_ONTOLOGY_FILE=example.owl", lastDebugMessage);
    }

    @Test
    public void testFactsFileEnv() {
        rule.set("ONTOP_FACTS_FILE", "examplefacts.ttl");
        rule.set("ONTOP_FACTS_FORMAT", "jsonld");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_FACTS_FILE");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        assertEquals(System.getenv("ONTOP_FACTS_FILE"), envVal);
        // Test the correct debug message is logged, option detected as -a
        assertEquals("Set option -a via environment variable ONTOP_FACTS_FILE=examplefacts.ttl",
                lastDebugMessage);
    }

    @Test
    public void testFactsFileFormatEnvOverride() {
        rule.set("ONTOP_FACTS_FILE", "examplefacts.ttl");
        rule.set("ONTOP_FACTS_FORMAT", "jsonld");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties",
                "--facts-format", "turtle");
        String envVal = getEnvValues(command, "ONTOP_FACTS_FORMAT");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        // Test that ONTOP_FACTS_FORMAT is ignored, and that the value is retrieved from the option --facts-format
        assertEquals("turtle", envVal);
        // Test the correct warning message is logged. The --facts-format input is used.
        assertEquals("Ignoring environment variable ONTOP_FACTS_FORMAT=jsonld for explicitly specified " +
                "option --facts-format", lastWarnMessage);
        assertEquals("Set option -a via environment variable ONTOP_FACTS_FILE=examplefacts.ttl",
                lastDebugMessage);
    }

    @Test
    public void testFactsBaseIRIEnvString() {
        rule.set("ONTOP_FACTS_FILE", "examplefacts.ttl");
        rule.set("ONTOP_FACTS_FORMAT", "jsonld");
        rule.set("ONTOP_FACTS_BASE_IRI", "http://example.com");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties",
                "--facts-format", "turtle");
        String envVal = getEnvValues(command, "ONTOP_FACTS_BASE_IRI");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        assertEquals(System.getenv("ONTOP_FACTS_BASE_IRI"), envVal);
        assertEquals("Ignoring environment variable ONTOP_FACTS_FORMAT=jsonld for explicitly specified " +
                "option --facts-format", lastWarnMessage);
        assertEquals("Set option -a via environment variable ONTOP_FACTS_FILE=examplefacts.ttl",
                lastDebugMessage);
    }

    @Test
    public void testFactsBaseIRIEnvStringOverride() {
        rule.set("ONTOP_FACTS_FILE", "examplefacts.ttl");
        rule.set("ONTOP_FACTS_FORMAT", "jsonld");
        rule.set("ONTOP_FACTS_BASE_IRI", "http://example.com");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties",
                "--facts-format", "turtle",
                "--facts-base-iri", "http://example2.com");
        String envVal = getEnvValues(command, "ONTOP_FACTS_BASE_IRI");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        // Test that ONTOP_FACTS_BASE_IRI is ignored, and that the value is retrieved from the option --facts-base-iri
        assertEquals("http://example2.com", envVal);
        assertEquals("Ignoring environment variable ONTOP_FACTS_BASE_IRI=http://example.com for explicitly " +
                "specified option --facts-base-iri", lastWarnMessage);
        assertEquals("Set option -a via environment variable ONTOP_FACTS_FILE=examplefacts.ttl",
                lastDebugMessage);
    }

    @Test
    public void testDBUrlFileEnvOverride() {
        rule.set("ONTOP_DB_URL", "jdbc:h2:tcp://localhost:19123/books");
        rule.set("ONTOP_DB_URL_FILE", "dburl.txt");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_DB_URL");
        String lastWarnMessage = getLastLogMessage("WARN");
        String lastDebugMessage = getLastLogMessage("DEBUG");
        // Test that ONTOP_DB_URL_FILE is ignored, and that the value is retrieved from the environment variable ONTOP_DB_URL
        assertEquals(System.getenv("ONTOP_DB_URL"), envVal);
        // Test the correct warning message is logged. The plain URL takes precedence over the file.
        assertEquals("Multiple environment variables ONTOP_DB_URL=jdbc:h2:tcp://localhost:19123/books, " +
                "ONTOP_DB_URL_FILE=dburl.txt set for option --db-url. " +
                "Using ONTOP_DB_URL=jdbc:h2:tcp://localhost:19123/books", lastWarnMessage);
        // Test the correct debug message is logged, --db-url injected
        assertEquals("Set option --db-url via environment variable " +
                "ONTOP_DB_URL=jdbc:h2:tcp://localhost:19123/books", lastDebugMessage);
    }

    @Test
    public void testDBUrlFileEnvDoesNotExist() {
        rule.set("ONTOP_DB_URL_FILE", "dburl.txt");
        Throwable exception = assertThrows(ParseOptionUnexpectedException.class, () -> {
            OntopCommand command = getOntopCommandCLI().parse("endpoint",
                    "-t", "example.owl",
                    "-m", "example.obda",
                    "-p", "example.properties");
            String envVal = getEnvValues(command, "ONTOP_DB_URL");
            assertEquals(System.getenv("ONTOP_DB_URL"), envVal);
        });

        assertEquals("Cannot obtain value of option --db-url from file dburl.txt pointed by " +
                        "ONTOP_DB_URL_FILE: dburl.txt",
                exception.getMessage());
    }

    @Test
    public void testDBUrlEnvFile() {
        rule.set("ONTOP_DB_URL_FILE", "src/test/resources/cli/input.txt");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_DB_URL");
        // ONTOP_DB_URL is set based on the result from the file
        assertEquals("jdbc:h2:tcp://localhost:19123/books", envVal);
    }

    @Test
    public void testPasswordFileEnvOverride() {
        rule.set("ONTOP_DB_PASSWORD_FILE", "src/test/resources/cli/input.txt");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties",
                "--db-password", "secret");
        String envVal = getEnvValues(command, "ONTOP_DB_PASSWORD");
        // ONTOP_DB_PASSWORD is set based on --db-password winning over the file
        assertEquals("secret", envVal);
    }

    @Test
    public void testPasswordEnv() {
        rule.set("ONTOP_DB_PASSWORD", "secret");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties",
                "--db-password", "src/test/resources/cli/input.txt");
        String envVal = getEnvValues(command, "ONTOP_DB_PASSWORD");
        // --db-password only accepts a variable as input, not a file path
        assertEquals(System.getenv("ONTOP_DB_PASSWORD"), "secret");
        assertNotEquals(System.getenv("ONTOP_DB_PASSWORD"), envVal);
    }

    @Test
    public void testBooleanEnvIncorrectInput() {
        rule.set("ONTOP_ENABLE_ANNOTATIONS", "yes");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_ENABLE_ANNOTATIONS");
        // "yes" is not accepted as input, only "1" or "true"
        assertEquals(System.getenv("ONTOP_ENABLE_ANNOTATIONS"), "yes");
        assertNotEquals(System.getenv("ONTOP_ENABLE_ANNOTATIONS"), envVal);
    }


    @Test
    public void testBooleanEnv() {
        rule.set("ONTOP_ENABLE_ANNOTATIONS", "1");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_ENABLE_ANNOTATIONS");
        // 1 is converted to true
        assertEquals("true", envVal);
    }

    @Test
    public void testBooleanEnvOverride() {
        rule.set("ONTOP_ENABLE_ANNOTATIONS", "0");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-t", "example.owl",
                "-m", "example.obda",
                "-p", "example.properties",
                "--enable-annotations");
        String envVal = getEnvValues(command, "ONTOP_ENABLE_ANNOTATIONS");
        // Environment variable overriden by CLI option
        assertEquals("true", envVal);
        String lastWarnMessage = getLastLogMessage("WARN");
        assertEquals("Ignoring environment variable ONTOP_ENABLE_ANNOTATIONS=0 for explicitly specified " +
                "option --enable-annotations", lastWarnMessage);
    }

    @Test
    public void testOntopMapping2R2RMLEnv() {
        rule.set("ONTOP_DB_METADATA_FILE", "example2.json");
        OntopCommand command = getOntopCommandCLI().parse("mapping", "to-r2rml",
                "-d", "example.json",
                "-t", "example.owl",
                "-i", "example.obda",
                "-p", "example.properties",
                "-o", "example.ttl");
        String envVal = getEnvValues(command, "ONTOP_DB_METADATA_FILE");
        assertEquals("example.json", envVal);
    }

    @Test
    public void testOntopMapping2R2RML() {
        OntopCommand command = getOntopCommandCLI().parse("mapping", "to-r2rml",
                "-d", "example.json",
                "-t", "example.owl",
                "--input", "example.obda",
                "-p", "example.properties",
                "-o", "example.ttl");
        String envVal = getEnvValues(command, "ONTOP_DB_METADATA_FILE");
        assertEquals("example.json", envVal);
    }

    @Test
    public void testOntopEndpointWithConstraintEnv() {
        rule.set("ONTOP_CONSTRAINT_FILE", "constraint.txt");
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-m", "example.obda",
                "-t", "example.owl",
                "-p", "example.properties");
        String envVal = getEnvValues(command, "ONTOP_CONSTRAINT_FILE");
        assertEquals("constraint.txt", envVal);
    }

    @Test
    public void testOntopEndpointWithConstraint() {
        OntopCommand command = getOntopCommandCLI().parse("endpoint",
                "-m", "example.obda",
                "-t", "example.owl",
                "-p", "example.properties",
                "--constraint", "constraint.txt");
        String envVal = getEnvValues(command, "ONTOP_CONSTRAINT_FILE");
        assertEquals("constraint.txt", envVal);
    }

    /**
     * Retrieve the value of a given environment variable
     * @param object The object to retrieve the value from
     * @param identifier The name of the environment variable
     * @return The value of the environment variable
     */
    private String getEnvValues(Object object, String identifier) {
        Class<?> currentClass = object.getClass();
        // Get the annotations for all the respective superclasses
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Env.class)) {
                    Env envAnnotation = field.getAnnotation(Env.class);
                    String[] envVariableNames = envAnnotation.value();
                    if (Arrays.asList(envVariableNames).contains(identifier)) {
                        try {
                            field.setAccessible(true);
                            return field.get(object).toString();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    /**
     * Retrieve the last log message of a given error level e.g. WARN or DEBUG
     * @param errorLevel The error level to retrieve the message from
     * @return The last log message of the given error level
     */
    private String getLastLogMessage(String errorLevel) {
        // Retrieve log events from the appender
        for (int i = listAppender.list.size() - 1; i >= 0; i--) {
            ILoggingEvent event = listAppender.list.get(i);
            if (event.getLevel().toString().equals(errorLevel)) {
                return event.getFormattedMessage();
            }
        }
        return null;
    }
}
