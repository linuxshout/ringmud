package ring.server.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import ring.persistence.DataStore;
import ring.persistence.DataStoreFactory;
import ring.players.Player;
import ring.players.PlayerCharacter;
import ring.server.MUDConnection;
import ring.server.MUDConnectionManager;
import ring.server.MUDConnectionState;
import ring.server.MUDConnectionTimeout;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;

public class PlayerLoginShell implements Shell {
	private Connection connection;
	private TelnetStreamCommunicator comms;
	
	public static Shell createShell() {
		return new PlayerLoginShell();
	}
	
	@Override
	public void run(Connection conn) {
		init(conn);
		
		//Commented out for now, as it can't erase the screen
		//properly in line mode.
		try {
			conn.getTerminalIO().eraseScreen();
			conn.getTerminalIO().homeCursor();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//First check for exisitng connection.
		//If so, forward directly to player shell.
		MUDConnection mudConnection = MUDConnectionManager.getConnection(connection.getConnectionData().getInetAddress());
		if (mudConnection != null) {
			comms.println("Restoring your session.");
			connection.setNextShell("player");
			return;
		}
		else {
			try {
				displayMotd();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			MUDConnection mc = doShell();
			MUDConnectionManager.addConnection(connection.getConnectionData().getInetAddress(), mc);
			connection.setNextShell("player");
		}
		
	}
	
	private void displayMotd() throws IOException {
		BufferedReader reader = null;
	
		try {
			InputStream motd = this.getClass().getClassLoader().getResourceAsStream("ring/server/resources/motd.txt");
			reader = new BufferedReader(new InputStreamReader(motd));
			String line = "";
			while ((line = reader.readLine()) != null) {
				comms.println(line);
			}
			
			//Two newlines after the title.
			comms.println();
			comms.println();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void init(Connection conn) {
		connection = conn;
		connection.addConnectionListener(this);
		
		//Clear out the timer if they have one
		MUDConnectionManager.deleteTimer(connection.getConnectionData().getInetAddress());
				
		//Initialize the communicator.
		comms = new TelnetStreamCommunicator(new TelnetInputStream(connection.getTerminalIO()),
				new TelnetOutputStream(connection.getTerminalIO()));
		
	}
	
	private MUDConnection doShell() {
		DataStore ds = DataStoreFactory.getDefaultStore();
		String playerID = inputUsername();
		String password = inputPassword();
		
		Player player = ds.retrievePlayer(playerID);
		PlayerCharacter pc = null;
		
		if (player != null) {
			//Load player character list
		}
		else {
			//New user creation.
			comms.println();
			comms.println("[B]Welcome!");
			
			//Go through the player creaton process.			
			PlayerCreation nuc = new PlayerCreation(comms, playerID, password);
			player = nuc.doCreatePlayer();
			
			comms.println();
			comms.println("[R][RED]Entering new character creation mode...");
						
			PlayerCharacterCreation creation = new PlayerCharacterCreation(comms);
			pc = creation.doCreateNewCharacter();
		}
				
		MUDConnection mc = new MUDConnection();
		mc.setPlayer(player);
		mc.setPlayerCharacter(pc);
		mc.setState(MUDConnectionState.LOGGING_IN);
		
		return mc;
	}
	
	private String inputUsername() {
		String line = "";
		while (line.equals("")) {
			comms.print("Username: " );
			line = comms.receiveData();
		}
		
		return line;
	}
	
	private String inputPassword() {
		String line = "";
		while (line.equals("")) {
			comms.print("Password: " );
			line = comms.receiveData();
		}
		
		return line;
	}

	@Override
	public void connectionIdle(ConnectionEvent arg0) {
		System.out.println("Idle connection for " + connection);
		InetAddress ip = connection.getConnectionData().getInetAddress();
		TimerTask task = new MUDConnectionTimeout(ip);
		Timer timer = MUDConnectionManager.createTimer(ip);
		//timer.schedule(task, 1000);
		timer.schedule(task, 300000); //5 minutes = 300,000
	}

	@Override
	public void connectionLogoutRequest(ConnectionEvent arg0) {
		System.out.println("Logout request for " + connection);;		
	}

	@Override
	public void connectionSentBreak(ConnectionEvent arg0) {
		System.out.println("Break for " + connection);
	}

	@Override
	public void connectionTimedOut(ConnectionEvent arg0) {
		System.out.println("Timeout for " + connection);
	}

}
