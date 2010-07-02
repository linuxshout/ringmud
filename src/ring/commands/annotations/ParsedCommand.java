package ring.commands.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ring.commands.WorldObjectSearch;
import ring.mobiles.Mobile;
import ring.movement.Room;
import ring.world.WorldObject;

public class ParsedCommand {
	private String formID;
	private String command;
	private List<Object> arguments;
	private Scope cascadeType;
	private Scope scope;
	
	public String getFormID() {
		return formID;
	}
	
	public void setFormID(String id) {
		formID = id;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public List<Object> getArguments() {
		return arguments;
	}
	
	public void setArguments(List<Object> args) {
		arguments = args;
	}
	
	public void setArguments(Object ... args) {
		arguments.addAll(Arrays.asList(args));
	}
	
	public Object getArgument(int index) {
		return arguments.get(index);
	}

	public void setCascadeType(Scope cascadeType) {
		this.cascadeType = cascadeType;
	}

	public Scope getCascadeType() {
		return cascadeType;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}
	
	protected void initialize(CommandSender sender, List<ParsedCommandToken> tokens) {
		if (tokens == null || tokens.size() == 0) {
			setArguments(sender.getContext().getLocation());
			return;
		}
		else {
			if (this.getCascadeType() == Scope.LTR_CASCADING) {
				initializeLTRCascade(sender, tokens);
			}
			else if (this.getCascadeType() == Scope.RTL_CASCADING) {
				initializeRTLCascade(sender, tokens);
			}
		}
	}
	
	/**
	 * Performs object translation for a command that cascades its data left-to-right. 
	 * @param sender
	 * @param tokens
	 */
	private void initializeLTRCascade(CommandSender sender, List<ParsedCommandToken> tokens) {
		List<Object> arguments = new ArrayList<Object>(tokens.size());
		
		ParsedCommandToken first = tokens.get(0);
		Scope scope = first.getMatched().getScope();
		
		//Retrieve initial WO dataset from first token based on its scope and bind types.
		//Filter that list via WorldObjectSearch and bind the first result to the argument
		//Begin looping over rest of variables:
		//	Use previous WO's produceSearchList(Class ... cs) method based on bind types for the current PT
		//	Filter WO list based on parsed token and set the argument.
		
		WorldObject rootArg = null;
		if (scope == Scope.ROOM) {
			Room location = sender.getContext().getLocation();
			rootArg = worldObjectFromToken(first, location);
		}
		else if (scope == Scope.MOBILE) {
			//Room location = sender.getContext().getLocation();
			//rootArg = worldObjectForMobScope(first, location);
			throw new IllegalArgumentException("Mobile scope is unsupported at this time.");
		}
		else if (scope == Scope.SELF) {
			throw new IllegalArgumentException("Self scope is unsupported at this time.");
		}
		
		if (rootArg == null) {
			//not here.
		}
		
		arguments.add(rootArg);
		
		WorldObject previousArg = rootArg;
		
		for (int c = 1; c < tokens.size(); c++) {
			ParsedCommandToken token = tokens.get(c);
			WorldObject arg = worldObjectFromToken(token, previousArg);
			arguments.add(arg);
			previousArg = arg;
		}
		
		setArguments(arguments);
	}
	
	/**
	 * Performs object translation for a command that cascades its data right-to-left.
	 * @param sender
	 * @param tokens
	 */
	private void initializeRTLCascade(CommandSender sender, List<ParsedCommandToken> tokens) {
		List<Object> arguments = new ArrayList<Object>(tokens.size());
		
		ParsedCommandToken last = tokens.get(tokens.size() - 1);
		Scope scope = last.getMatched().getScope();
		
		//Retrieve initial WO dataset from first token based on its scope and bind types.
		//Filter that list via WorldObjectSearch and bind the first result to the argument
		//Begin looping over rest of variables:
		//	Use previous WO's produceSearchList(Class ... cs) method based on bind types for the current PT
		//	Filter WO list based on parsed token and set the argument.
		
		WorldObject rootArg = null;
		if (scope == Scope.ROOM) {
			Room location = sender.getContext().getLocation();
			rootArg = worldObjectFromToken(last, location);
		}
		else if (scope == Scope.MOBILE) {
			//Room location = sender.getContext().getLocation();
			//rootArg = worldObjectForMobScope(first, location);
			throw new IllegalArgumentException("Mobile scope is unsupported at this time.");
		}
		else if (scope == Scope.SELF) {
			throw new IllegalArgumentException("Self scope is unsupported at this time.");
		}
		
		if (rootArg == null) {
			//not here.
		}
		
		arguments.add(rootArg);
		
		WorldObject previousArg = rootArg;
		
		for (int c = tokens.size() - 2; c >= 0; c--) {
			ParsedCommandToken token = tokens.get(c);
			WorldObject arg = worldObjectFromToken(token, previousArg);
			arguments.add(arg);
			previousArg = arg;
		}
		
		//Must be reversed since arguments are added to the list while going backwards
		//through parsed command tokens
		Collections.reverse(arguments);
		
		setArguments(arguments);
	}
	
	/**
	 * Finds a world object based on the given command token. Uses the specified WorldObject as a
	 * data source to find WorldObjects to search for.
	 * @param token
	 * @param datasource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private WorldObject worldObjectFromToken(ParsedCommandToken token, WorldObject datasource) {
		List<Class<?>> bindTypes = token.getMatched().getBindTypes();
		List<WorldObject> objs = datasource.produceSearchList(bindTypes);
		return search(token.getToken(), objs);		
	}
	
	/**
	 * Finds a world object based on the given command token. Uses the specified Room as a 
	 * data source to find the WorldObjects to search for.
	 * @param token
	 * @param datasource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private WorldObject worldObjectFromToken(ParsedCommandToken token, Room datasource) {
		List<Class<?>> bindTypes = token.getMatched().getBindTypes();
		List<WorldObject> objs = datasource.produceSearchList(bindTypes);
		return search(token.getToken(), objs);		
	}
	
	/**
	 * Unused currently. Will theoretically be for commands that operate within a "shell"
	 * that knows about a target Mobile.
	 * @param token
	 * @param datasource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private WorldObject worldObjectForMobScope(ParsedCommandToken token, Room datasource) {
		Class<?>[] bindTypes = new Class<?>[] { Mobile.class };
		List<WorldObject> objs = datasource.produceSearchList(bindTypes);
		return search(token.getToken(), objs);		
	}
	
	/**
	 * This method delegates to {@link ring.commands.WorldObjectSearch} in order
	 * to search collections of world objects from generic data sources. It returns
	 * the most relevant world object found amongst all presented collections. The
	 * text searched for is case-insensitive.
	 * @param name The name to search for.
	 * @param worldObjectLists {@link java.util.Collection}s of {@link WorldObject}s. 
	 * @return The most relevant world object, or null if nothing was found.
	 */
	private WorldObject search(String name, Collection<? extends WorldObject> ... worldObjectLists) {
		WorldObjectSearch search = new WorldObjectSearch();
		
		for (Collection<? extends WorldObject> list : worldObjectLists) {
			search.addSearchList(list);
		}
		
		List<WorldObject> results = search.search(name);
		
		if (results.size() > 0) {
			return results.get(0);
		}
		else {
			return null;
		}
	}
}
