package inf.unibz.it.obda.tool.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Utility class for transforming the old OBDA file to the new standard.
 *
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
public class ObdaFileCompatibiltyRepair {

	/** Constants */
	private static final String XML = "xml";
	private static final String XMLNS = "xmlns";

	private static final String BASE_PREFIX = "base";
	private static final String OWL_PREFIX = "owl";
	private static final String OWL2XML_PREFIX = "owl2xml";
	private static final String RDF_PREFIX = "rdf";
	private static final String RDFS_PREFIX = "rdfs";
	private static final String XSD_PREFIX = "xsd";
	private static final String OBDAP_PREFIX = "obdap";

	private static final String OWL_URI = "http://www.w3.org/2002/07/owl#";
	private static final String OWL2XML_URI = "http://www.w3.org/2006/12/owl2-xml#";
	private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String RDFS_URI = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String XSD_URI = "http://www.w3.org/2001/XMLSchema#";
	private static final String OBDAP_URI = "http://obda.org/mapping/predicates/";

	/** Fields */
	private static Options options = new Options();
	private static CommandLine cmd = null;
	private static String nsDeclaration = "";
	private static String doctypeDeclaration = "";
	private static Vector<String> doctypeEntities = new Vector<String>();

	public static void main(String[] args) {

		initOptions();

		try {
			CommandLineParser parser = new GnuParser();
			cmd = parser.parse(options, args);

			constructNamespaces();

			if (!useDepreciatedVersion())	// look at the option -depreciated
				constructDoctype();

			String[] targetFiles = cmd.getArgs(); // the target files
			if (targetFiles.length != 0) {
				for (int i = 0; i < targetFiles.length; i++) {
					String filename = targetFiles[i];
					recurse(new File(filename));
				}
			}
			else {
				System.err.println("The program requires at least one " +
						"target input file!");
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/** Setup the command line options */
	private static void initOptions() {

		options.addOption(
			OptionBuilder.withArgName("URI")
			.hasArg()
			.withDescription("Define the URI reference for the BASE namespace.")
			.create("base"));
		options.addOption(
			OptionBuilder.withArgName("prefix=uri,...")
			.hasArgs()
			.withValueSeparator(',')
			.withDescription("Define one or more URI reference for custom " +
					"namespace. Separate each namespace definition with " +
					"a comma.")
			.create("N"));
		options.addOption(
			OptionBuilder.withArgName("prefix=uri,...")
			.hasArgs()
			.withValueSeparator(',')
			.withDescription("Define one or more URI reference for custom " +
					"doctype. Separate each doctype definition with " +
					"a comma.")
			.create("D"));
		options.addOption("depreciated", false,
				"Convert to the depreciated OBDA file v2.");
		options.addOption("owl", false,
				"Add the URI reference for the OWL namespace.");
		options.addOption("owl2xml", false,
				"Add the URI reference for the OWL2XML namespace (only if " +
				"-depreciated is added, or otherwise it becomes a DOCTYPE " +
				"entity).");
		options.addOption("rdf", false,
				"Add the URI reference for the RDF namespace.");
		options.addOption("rdfs", false,
				"Add the URI reference for the RDFS namespace.");
		options.addOption("xsd", false,
				"Add the URI reference for the XSD namespace.");
		options.addOption("obdap", false,
				"Add the URI reference for the OBDA namespace (only if " +
				"-depreciated is added, or otherwise it becomes a DOCTYPE " +
				"entity).");
		options.addOption("keepOriginal", false,
				"Retain the copy of the original file.");

		// Help for the program script.
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				"java ObdaFileCompatibiltyRepair [OPTIONS] FILE1 [FILE2]...\n" +
				"Convert one or more old OBDA file (v1) to the newer standard. " +
				"Currently, the API accepts OBDA file v2 and v3 that support " +
				"namespace and doctype definitions. Thus, the conversion can " +
				"be to the either version.",
				options);
	}

	/* Construct the name-spaces based on the command line input arguments */
	private static void constructNamespaces() {

		nsDeclaration = "<OBDA";
		if (cmd.hasOption("base")) {
			nsDeclaration += "\n\t" + XML + ":" + BASE_PREFIX + "=\"" +
				cmd.getOptionValue("base") + "\"";
			nsDeclaration += "\n\t" + XMLNS + "=\"" +
				cmd.getOptionValue("base") + "\"";
		}

		if (cmd.hasOption("owl")) {
			nsDeclaration += "\n\t" + XMLNS + ":" + OWL_PREFIX + "=\"" +
				OWL_URI + "\"";
		}

		if (cmd.hasOption("owl2xml") && useDepreciatedVersion()) { // not in v3
			nsDeclaration += "\n\t" + XMLNS + ":" + OWL2XML_PREFIX + "=\"" +
				OWL2XML_URI + "\"";
		}

		if (cmd.hasOption("rdf")) {
			nsDeclaration += "\n\t" + XMLNS + ":" + RDF_PREFIX + "=\"" +
				RDF_URI + "\"";
		}

		if (cmd.hasOption("rdfs")) {
			nsDeclaration += "\n\t" + XMLNS + ":" + RDFS_PREFIX + "=\"" +
				RDFS_URI + "\"";
		}

		if (cmd.hasOption("xsd")) {
			nsDeclaration += "\n\t" + XMLNS + ":" + XSD_PREFIX + "=\"" +
				XSD_URI + "\"";
		}

		if (cmd.hasOption("obdap") && useDepreciatedVersion()) { // not in v3
			nsDeclaration += "\n\t" + XMLNS + ":" + OBDAP_PREFIX + "=\"" +
				OBDAP_URI + "\"";
		}

		if (cmd.hasOption("N")) {
			String[] namespaces = cmd.getOptionValues("N");
			for (int i = 0; i < namespaces.length; i++) {
				String[] tokens = namespaces[i].split("=");
				nsDeclaration += "\n\t" + XMLNS + ":" + tokens[0] + "=\"" +
					tokens[1] + "\"";
			}
		}

		nsDeclaration += " />";
	}

	private static void constructDoctype() {

		doctypeDeclaration = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> " +
				"<!DOCTYPE OBDA[";

		if (cmd.hasOption("owl2xml")) {
			doctypeDeclaration += "\n\t<!ENTITY " + OWL2XML_PREFIX + " " +
				"'" + OWL2XML_URI + "'>";
		}

		if (cmd.hasOption("obdap")) {
			doctypeDeclaration += "\n\t<!ENTITY " + OBDAP_PREFIX + " " +
				"'" + OBDAP_URI + "'>";
		}

		if (cmd.hasOption("D") && !useDepreciatedVersion()) { // only for v3
			String[] entities = cmd.getOptionValues("D");
			for (int i = 0; i < entities.length; i++) {
				String[] tokens = entities[i].split("=");
				doctypeDeclaration += "\n\t<!ENTITY " + tokens[0] + " " +
					"'" + tokens[1] + "'>";

				// Save the doctype entities for later transformation.
				doctypeEntities.add(tokens[0]);
			}
		}

		doctypeDeclaration += "\n]>";
	}

	/* Does the user want to keep the original file? */
	private static boolean keepOriginal() {
		if (cmd.hasOption("keepOriginal"))
			return true;

		return false;
	}

	/* Does the user want to convert the file to a depreciated version? */
	private static boolean useDepreciatedVersion() {
		if (cmd.hasOption("depreciated"))
			return true;

		return false;
	}

	/* Go through the directory and find the list of files */
	private static void recurse(File file) {
		if (file.isFile()) {
			repair(file);
		}
		else {
			File[] list = file.listFiles();
		    for (int i = 0; i < list.length; i++)
		    	recurse(list[i]);
		}
	}

	/* Match and replace some text to the new standard. */
	private static void repair(File file) {

		Pattern p = Pattern.compile("rdf:type\\s+'(\\w+)'");
		Matcher m = null;

		if (isObdaFile(file)) {
			try {
				File originalFile = copyFile(file);

				BufferedReader reader =
					new BufferedReader(new FileReader(originalFile));
				FileWriter writer = new FileWriter(file);

				String line = null;
				while((line = reader.readLine()) != null) {
					// For the beginning of the file
					if (line.contains("<?xml") &&
							!useDepreciatedVersion()) { // only for v3
						line = doctypeDeclaration;
					}
					// For the case adding prefixes in the <OBDA> tag.
					else if (line.contains("<OBDA")) {
						line = nsDeclaration;
					}
					// For the case changing the class name in the mapping head.
					else if (line.contains("headclass=")) {
						line = line.replace(
							"inf.unibz.it.obda.api.domain.ucq.ConjunctiveQuery",
							"org.obda.query.domain.imp.CQIEImpl");
					}
					// For the case adding doctype entity in the CQ string.
					else if (line.contains("<CQ string=") &&
							!useDepreciatedVersion()) { // only for v3
						// add the head
						line = line.replace(
								"<CQ string=\"",
								"<CQ string=\"&obdap;p(*) :- ");
						// replace the prefixes in the body
						for (int i = 0; i < doctypeEntities.size(); i++) {
							line = line.replaceAll(
									doctypeEntities.get(i) + ":",
									"&" + doctypeEntities.get(i) + ";");
						}
					}
					// For the case replacing the rdf:type string.
					else {
						m = p.matcher(line);
						while (m.find()) {
							line = m.replaceFirst("rdf:type :" + m.group(1));
							m = p.matcher(line);
						}
					}
					writer.write(line + "\n");
				}
				reader.close();
				writer.close();

				if (!keepOriginal())	// look at the option -keepOriginal
					originalFile.delete();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/* Create a backup for the original file. */
	private static File copyFile(File file) throws IOException {
		String oldFileName = file.toString();
		String newFileName = oldFileName + ".orig";
		File newFile = new File(newFileName);

		FileReader in = new FileReader(file);
	    FileWriter out = new FileWriter(newFile);

	    int c;
	    while ((c = in.read()) != -1)
	      out.write(c);

	    in.close();
	    out.close();

	    return newFile;
	}

	/* Check whether the target file is an OBDA file. */
	private static boolean isObdaFile(File file) {
		String fileName = file.toString();
		String extension =
			fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());

		return extension.equals("obda");
	}
}
