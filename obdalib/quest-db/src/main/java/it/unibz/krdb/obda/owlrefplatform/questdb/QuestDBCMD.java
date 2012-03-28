package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDB.StoreStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestDBCMD {

	QuestDB dbInstance = null;

	String currentstore = null;

	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	private static final Logger log = LoggerFactory.getLogger(QuestDBCMD.class);

	private static final String CMD_DEFINE_PREFIX = "DEFINE PREFIX prefix URI";

	private static final String CMD_DELETE_PREFIX = "DROP PREFIX prefix";

	private static final String CMD_DELETE_ALL_PREFIXES = "DROP ALL PREFIXES";

	private static final String CMD_DEFINE_BASE = "DEFINE BASE URI";

	private static final String CMD_SET = "SET key=value";

	private static final String CMD_SHOW_PARAMETERS = "SHOW PARAMETERS";

	private static final String CMD_SHOW = "SHOW";

	private static final String CMD_SHUTDOWN = "SHUTDOWN";

	private static final String RX_DROP = "[Dd][Rr][Oo][Pp]";

	private static final String RX_START = "[Ss][Tt][Aa][Rr][Tt]";

	private static final String RX_STOP = "[Ss][Tt][Oo][Pp]";

	private static final String RX_LIST = "[Ll][Ii][Ss][Tt]";

	private static final String RX_FAST = "[Ff][Aa][Ss][Tt]";

	private static final String RX_LOAD = "[Ll][Oo][Aa][Dd]";

	private static final String RX_SET = "[Ss][Ee][Tt]";

	private static final String RX_STORES = "[Ss][Tt][Oo][Rr][Ee][Ss]";

	private static final String RX_CREATE = "[Cc][Rr][Ee][Aa][Tt][Ee]";

	private static final String RX_VIRTUAL = "[Vv][Ii][Rr][Tt][Uu][Aa][Ll]";

	private static final String RX_INDEX = "[Ii][Nn][Dd][Ee][Xx]";

	private static final String RX_TBOX = "[Tt][Bb][Oo][Xx]";

	private static final String RX_WITH = "[Ww][Ii][Tt][Hh]";

	private static final String RX_PARAMETERS = "[Pp][Aa][Rr][Aa][Mm][Ss]";

	private static final String RX_MAP = "[Mm][Aa][Pp]";

	private static final String RX_STORE = "[Ss][Tt][Oo][Rr][Ee]";

	private static final String RX_WS = "[\\s]+";

	private static final String RX_QUOTED_PARAMETER = "\"([^\"\\r\\n\\t]*)\"";

	private static final String RX_NAME_PARAMETER = "([\\w]+)";

	/* CREATE STORE name WITH TBOX "tboxfile" PARAMS "paramsfile" */
	private static final String CMD_CREATE_STORE = RX_CREATE + RX_WS + RX_STORE + RX_WS + RX_NAME_PARAMETER + RX_WS + RX_WITH + RX_WS
			+ RX_TBOX + RX_WS + RX_QUOTED_PARAMETER + RX_WS + RX_PARAMETERS + RX_WS + RX_QUOTED_PARAMETER;

	/* CREATE VIRTUAL STORE name WITH TBOX "tboxfile" MAP "paramsfile" */
	private static final String CMD_CREATE_VIRTUAL_STORE = RX_CREATE + RX_WS + RX_VIRTUAL + RX_WS + RX_STORE + RX_WS + RX_NAME_PARAMETER
			+ RX_WS + RX_WITH + RX_WS + RX_TBOX + RX_WS + RX_QUOTED_PARAMETER + RX_WS + RX_MAP + RX_WS + RX_QUOTED_PARAMETER;

	private static final Pattern PTR_CREATE_STORE = Pattern.compile(CMD_CREATE_STORE);

	private static final Pattern PTR_CREATE_VIRTUAL_STORE = Pattern.compile(CMD_CREATE_VIRTUAL_STORE);

	/* DROP STORE name */
	private static final String CMD_DROP_STORE = RX_DROP + RX_WS + RX_STORE + RX_WS + RX_NAME_PARAMETER;

	private static final Pattern PTR_DROP_STORE = Pattern.compile(CMD_DROP_STORE);

	/* START STORE name */
	private static final String CMD_START_STORE = RX_START + RX_WS + RX_STORE + RX_WS + RX_NAME_PARAMETER;

	private static final Pattern PTR_START_STORE = Pattern.compile(CMD_START_STORE);

	/* STOP STORE name */
	private static final String CMD_STOP_STORE = RX_STOP + RX_WS + RX_STORE + RX_WS + RX_NAME_PARAMETER;

	private static final Pattern PTR_STOP_STORE = Pattern.compile(CMD_STOP_STORE);

	/* LIST STORES */
	private static final String CMD_LIST_STORES = RX_LIST + RX_WS + RX_STORES;

	private static final Pattern PTR_LIST_STORES = Pattern.compile(CMD_LIST_STORES);

	/* LOAD "datafile" */
	private static final String CMD_LOAD = RX_LOAD + RX_WS + RX_QUOTED_PARAMETER;

	private static final Pattern PTR_LOAD = Pattern.compile(CMD_LOAD);

	/* LOAD "datafile" FAST */
	private static final String CMD_LOAD_FAST = RX_LOAD + RX_WS + RX_QUOTED_PARAMETER + RX_WS + RX_FAST;

	private static final Pattern PTR_LOAD_FAST = Pattern.compile(CMD_LOAD_FAST);

	/* CREATE INDEX */
	private static final String CMD_CREATE_INDEX = RX_CREATE + RX_WS + RX_INDEX;

	private static final Pattern PTR_CREATE_INDEX = Pattern.compile(CMD_CREATE_INDEX);

	/* DROP INDEX */
	private static final String CMD_DROP_INDEX = RX_DROP + RX_WS + RX_INDEX;

	private static final Pattern PTR_DROP_INDEX = Pattern.compile(CMD_DROP_INDEX);

	/* FOR CLASSIC STORES */

	public static String NEWLINE = System.getProperty("line.separator");

	public QuestDBCMD() {
		initialize();
		printhelp();

		Thread cmdloop = new Thread() {
			public void run() {
				processCommands();
			}
		};
		cmdloop.start();
	}

	private void processCommands() {
		while (true) {
			printprompt1();
			String cmd = "";
			try {
				cmd = readCommand();
				processCommand(cmd);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		}
	}

	private void processCommand(String cmd) throws Exception {
		cmd = cmd.trim();
		if (cmd.charAt(cmd.length() - 1) == ';') {
			startTimer();
			processQuery(cmd.substring(0, cmd.length() - 1));
			stopTimer();
		} else if (cmd.equals("\\q")) {
			System.exit(0);
		} else if (cmd.equals("\\?") || cmd.equals("help")) {
			printhelp();
		} else if (cmd.startsWith("\\c")) {
			String[] s = cmd.split(" ");
			if (s.length < 2) {
				printInvalidCommand();
			}
			String store = s[1].trim();
			if (store == null || store.length() == 0 || !dbInstance.exists(store)) {
				System.out.println("\nCannot connect to the provided store. Does it exist?.");
			}
			connect(store);
		} else {
			printInvalidCommand();
		}
	}

	private void printInvalidCommand() {
		System.out.println("\nInvalid command. Try \\? for help.");
	}

	private void processQuery(String query) throws Exception {
		if (PTR_CREATE_STORE.matcher(query).matches()) {
			processCreateStore(query);
		} else if (PTR_CREATE_VIRTUAL_STORE.matcher(query).matches()) {
			processCreateVirtualStore(query);
		} else if (PTR_DROP_STORE.matcher(query).matches()) {
			processDropStore(query);
		} else if (PTR_START_STORE.matcher(query).matches()) {
			processStartStore(query);
		} else if (PTR_STOP_STORE.matcher(query).matches()) {
			processStartStore(query);
		} else if (PTR_LIST_STORES.matcher(query).matches()) {
			processListStores();
		} else if (PTR_LOAD_FAST.matcher(query).matches()) {
			processLoad(query, true);
		} else if (PTR_LOAD.matcher(query).matches()) {
			processLoad(query, false);
		} else if (PTR_CREATE_INDEX.matcher(query).matches()) {
			processCreateIndexes();
		} else if (PTR_DROP_INDEX.matcher(query).matches()) {
			processDropIndexes();
		} else if (query.startsWith("GET SQL ")) {
			if (!checkStoreIsSet()) {
				System.out.println("\nYou must set an active store first using the command \"\\c storename\"");
				return;
			}

			QuestDBStatement st = dbInstance.getStatement(currentstore);
			String sql = st.getSQL(query.substring("GET SQL ".length()));
			st.close();
			System.out.println("SQL:");
			System.out.println(sql);
		} else if (query.startsWith("GET REF ")) {
			if (!checkStoreIsSet()) {
				System.out.println("\nYou must set an active store first using the command \"\\c storename\"");
				return;
			}
			QuestDBStatement st = dbInstance.getStatement(currentstore);
			String rew = st.getRewriting(query.substring("GET REF".length()));
			System.out.println("Reformulation:");
			System.out.println(rew);
		} else {
			if (!checkStoreIsSet()) {
				System.out.println("\nYou must set an active store first using the command \"\\c storename\"");
				return;
			}
			try {

				QuestDBStatement st = dbInstance.getStatement(currentstore);
				OBDAResultSet result = st.execute(query);
				int count = printResultSet(result);
				System.out.println(count + " rows.");
				result.close();
				st.close();
			} catch (Exception e) {
				System.out.println("\nError executing query: " + e.getMessage());
			}

		}
	}

	private int printResultSet(OBDAResultSet result) throws OBDAException {
		int cols = result.getColumCount();
		List<String> signature = result.getSignature();
		for (int i = 0; i < signature.size(); i++) {
			if (i > 0)
				System.out.print(" | ");
			System.out.print(signature.get(i));
		}
		int count = 0;
		System.out.println("\n---------------------------------");
		while (result.nextRow()) {
			for (int i = 1; i < cols + 1; i++) {
				if (i > 1)
					System.out.print(" | ");
				System.out.print(result.getString(i));
			}
			System.out.println();
			count += 1;
		}
		return count;
	}

	private void processDropIndexes() {
		try {

			dbInstance.dropIndexes(currentstore);
			System.out.println("\nIndex droped.");
		} catch (Exception e) {
			System.out.println("\nUnable to drop indexes.");
			log.error(e.getMessage());
		}
	}

	private void processCreateIndexes() {
		try {

			dbInstance.createIndexes(currentstore);
			System.out.println("\nIndex created.");
		} catch (Exception e) {
			System.out.println("\nUnable to create indexes.");
			log.error(e.getMessage());
		}
	}

	private long start = 0;

	DecimalFormat df = new DecimalFormat("##.###");

	private void startTimer() {
		start = System.nanoTime();
	}

	private void stopTimer() {
		long stop = System.nanoTime();
		double seconds = (double) (stop - start) / 1000000000.0;

		System.out.println(String.format("(%s s)", df.format(seconds)));
	}

	private void processLoad(String cmd, boolean fast) {
		if (!checkStoreIsSet()) {
			System.out.println("\nYou must set an active store first using the command \"\\c storename\"");
			return;
		}

		String data = null;
		Matcher m = PTR_LOAD.matcher(cmd);
		if (m.find()) {
			data = m.group(1);
		} else {
			Matcher m2 = PTR_LOAD_FAST.matcher(cmd);
			data = m2.group(1);
		}
		try {

			URI dataURI = getFileURI(data);
			int tuples = dbInstance.load(currentstore, dataURI, fast);
			System.out.println(String.format("\n%s tuples inserted.", tuples));
		} catch (Exception e) {
			System.out.println("\nUnable to load data.");
			log.error(e.getMessage(), e);
			if (e instanceof SQLException) {
				SQLException ex = ((SQLException) e).getNextException();
				while (ex != null) {
					log.error(ex.getMessage());
					ex = ex.getNextException();
				}
			}
		}
	}

	private boolean checkStoreIsSet() {
		if (currentstore == null)
			return false;
		return true;
	}

	/*
	 * Tests for the formaat of the path string and find teh correct
	 * interpretation to generate a working URI. Returns null if the path is not
	 * valid or there is no accessible file at the path.
	 */
	private URI getFileURI(String path) {
		File file = new File(path);
		if (file.exists())
			return file.toURI();

		file = new File(URI.create(path));
		if (file.canRead())
			return file.toURI();

		return null;
	}

	private void processListStores() {

		List<StoreStatus> stores = dbInstance.listStores();
		String format = "%s | %s";
		System.out.println(String.format(format, "name", "online"));
		System.out.println("-------------------");
		for (StoreStatus status : stores) {
			System.out.println(String.format(format, status.name, status.isOnline));
		}

	}

	private void processStopStore(String cmd) {
		Matcher m = PTR_STOP_STORE.matcher(cmd);
		m.find();
		String name = m.group(1);
		try {

			dbInstance.stopStore(name);

		} catch (Exception e) {
			System.out.println("\nUnable to stop store.");
			log.error(e.getMessage());
		}
	}

	private void processStartStore(String cmd) {
		Matcher m = PTR_START_STORE.matcher(cmd);
		m.find();
		String name = m.group(1);
		try {

			dbInstance.startStore(name);

		} catch (Exception e) {
			System.out.println("\nUnable to start store.");
			log.error(e.getMessage());
		}

	}

	private void processDropStore(String cmd) {
		Matcher m = PTR_DROP_STORE.matcher(cmd);
		m.find();
		String name = m.group(1);
		try {

			dbInstance.dropStore(name);

		} catch (Exception e) {
			System.out.println("\nUnable to drop store.");
			log.error(e.getMessage());
		}

	}

	private void processCreateStore(String cmd) {
		Matcher m = PTR_CREATE_STORE.matcher(cmd);
		m.find();
		String name = m.group(1);
		String tboxfile = m.group(2);
		String paramfile = m.group(3);
		URI tboxURI = getFileURI(tboxfile);
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(getFileURI(paramfile))));

			dbInstance.createClassicStore(name, tboxURI, prop);
			System.out.println("\nStore has been created.");

		} catch (Exception e) {
			System.out.println("\nUnable to create store.");
			log.error(e.getMessage(), e);
		}

	}

	private void processCreateVirtualStore(String cmd) {
		Matcher m = PTR_CREATE_VIRTUAL_STORE.matcher(cmd);
		m.find();
		String name = m.group(1);
		String tboxfile = m.group(2);
		String obdaModel = m.group(3);
		URI tboxURI = getFileURI(tboxfile);
		URI obdaModelURI = getFileURI(obdaModel);
		try {

			dbInstance.createVirtualStore(name, tboxURI, obdaModelURI);
			System.out.println("\nStore has been created.");

		} catch (Exception e) {
			System.out.println("\nUnable to create store.");
			log.error(e.getMessage(), e);
		}

	}

	private void connect(String repository) {
		if (!dbInstance.exists(repository))
			System.out.println("\nThere is no repository by this name.");
		currentstore = repository;
		System.out.println("\n" + repository + " is now the active store.");
	}

	private void initialize() {
		System.out.println("Welcome to questcmd 1.0, the QuestDB interactive terminal.");

		System.out.println("questcmd is working in localdb mode");
		dbInstance = new QuestDB();

		/*
		 * Called when System.exit() is called or Control+C happens.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("\nGood bye.");

				/*
				 * This cleans all resources and saves the current state of each
				 * store
				 */
				dbInstance.shutdown();
			}
		});
	}

	private void printhelp() {
		System.out.println("");
		System.out.println("Type:  \\h for Quest commands");
		System.out.println("       ");
		System.out.println("       \\? for help with questcmd commands");
		System.out.println("       \\q to quit");
		System.out.println("Type \"help\" for help.");
		System.out.println("Terminate with semicolon to execute a query.");
		System.out.println("");
	}

	private void printprompt1() {
		System.out.print("quest");
		if (currentstore != null) {
			System.out.print(":");
			System.out.print(currentstore);
		}
		System.out.print("=# ");
	}

	private void printprompt2() {
		System.out.print("quest");
		if (currentstore != null) {
			System.out.print(":");
			System.out.print(currentstore);
		}
		System.out.print("-# ");
	}

	private String readCommand() throws Exception {
		StringBuffer bf = new StringBuffer();

		while (true) {
			String line = in.readLine();
			if (line.trim().equals(""))
				continue;
			line = line.replaceAll("\\s+$", "");
			bf.append(line);
			bf.append(NEWLINE);
			if (line.charAt(line.length() - 1) == ';') {
				/* Terminated the command */
				return bf.toString();
			} else if (line.charAt(0) == '\\') {
				return bf.toString();
			}
			printprompt2();
		}
	}

	public static void main(String args[]) throws IOException {
		QuestDBCMD cmd = new QuestDBCMD();
	}

}