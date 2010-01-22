package ring.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.util.PythonInterpreter;

/**
 * Package-level indexer class that indexes Jython script files and turns them 
 * into RingMUD Command objects. JythonIndexer takes the following properties as parameters:<br/>
 * directory: The directory to index.
 * @author projectmoon
 *
 */
class JythonIndexer implements CommandIndexer {
	private String filePath;
	
	/**
	 * A list of already-executed files. Should speed things up a bit
	 * if we don't need to execute the same file twice (or more).
	 */
	private final ArrayList<String> fileCache = new ArrayList<String>();
	private final ArrayList<Command> cmds = new ArrayList<Command>();
	private boolean indexed = false;

	private Properties props;
	private static final PythonInterpreter INTERP = new PythonInterpreter();
	private static final Pattern JYTHON_PATTERN = Pattern.compile("^class\\s+(\\w+)\\s*\\([\\w,_\\s]*Command[\\w,_\\s]*\\):$");
	
	public JythonIndexer() {
		
	}
	
	/**
	 * Indexes commands from Jython script files.
	 */
	public void index() throws IllegalStateException {
		if (props == null) {
			throw new IllegalStateException("JythonIndexer: No properties! Can't index without them!");
		}
		
		filePath = props.getProperty("jythonIndexer.directory");
		initInterpreter();
		File dir = new File(filePath);
		if (dir.isDirectory()) {
			File[] pythonFiles = dir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().endsWith(".py");
				}
			});
			
			//Give the cache plenty of room for the files. Saves time later.
			fileCache.ensureCapacity(pythonFiles.length);
			
			//Process each file and attempt to instantiate classes out of them.
			for (File file : pythonFiles) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = "";
					while ((line = reader.readLine()) != null) {
						String className = findClassName(line);
						if (className != null) {
							instantiateCommand(file.getAbsolutePath().toString(), className);
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		indexed = true;
	}
	
	/**
	 * Initializes the Jython environment for use with the indexer.
	 */
	private void initInterpreter() {
		INTERP.exec("from ring.commands import Command");
		INTERP.exec("from ring.commands import CommandSender");
		INTERP.exec("from ring.commands import CommandParameters");
	}
	
	/**
	 * Extracts a Command object from the given Python file.
	 * @param filename The script to pull the command from.
	 * @param name The name of the class to create.
	 */
	private void instantiateCommand(String filename, String name) {
		System.out.println(this + ": Found command " + name + " (in " + filename + ")");
		
		//Load the python file if it hasn't already been.
		attemptExecute(filename);
		
		//Create a new Command object in the interpreter and extract it.
		INTERP.exec("_ring_obj = " + name + "()");
		Command cmd = (Command)INTERP.get("_ring_obj", Command.class);
		if (cmd == null) {
			System.out.println(this + ": Error: There was an error creating command " + name);
		}
		else if (cmd.getCommandName() == null) {
			System.err.println(this + ": Error: Command object " + name + " does not define getCommandName().");
		}
		else {
			cmds.add(cmd);
		}
	}
	
	/**
	 * Executes a python script file if it's not in the cache and has not already
	 * been executed. This stops the indexer from repeatedly executing one file that
	 * may have more than one command in it, and reduces overhead.
	 * @param filename
	 */
	private void attemptExecute(String filename) {
		if (fileCache.contains(filename) == false) {
			INTERP.execfile(filename);
			fileCache.add(filename);
		}
	}
	
	/**
	 * Responsible for pulling out Python class declarations that implement the Command
	 * interface.
	 * @param python The line of python script to match against.
	 * @return The name of the class, if found. Otherwise, null is returned.
	 */
	private String findClassName(String python) {
		Matcher m = JYTHON_PATTERN.matcher(python);
		if (m.matches()) {
			return m.group(1);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Gets the list of indexed commands. If index() has not already been called,
	 * this method calls it automatically.
	 */
	public List<Command> getCommands()  throws IllegalStateException {
		if (props == null) {
			throw new IllegalStateException("JythonIndexer: No properties! Can't get commands without them!");
		}
		
		if (!indexed) {
			index();
		}
		
		return cmds;
	}
	
	/**
	 * Gets this plugin's properties.
	 */
	public Properties getProperties() {
		return props;
	}

	/**
	 * Sets this plugin's properties.
	 */
	public void setProperties(Properties props) {
		this.props = props;	
	}
	
	/**
	 * Returns "[JythonIndexer]".
	 */
	public String toString() {
		return "[JythonIndexer]";
	}
}