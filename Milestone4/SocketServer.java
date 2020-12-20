package server;

 import java.awt.BorderLayout;


import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
// import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.io.IOException;

 
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants; 

 public class SocketServer extends JFrame{
     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int port = 3000;
     
     CardLayout card;
     JPanel textArea;
     JPanel userPanel;
     
     Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
     
     String username;
     
     List<JPanel> userArray = new ArrayList<JPanel>();
     private List<Client> clients = new ArrayList<Client>();
     
     public static boolean isRunning = false;
     List<Room> rooms = new ArrayList<Room>();
     private Room lobby;// here for convenience
     private List<Room> isolatedPrelobbies = new ArrayList<Room>();
     private final static String PRELOBBY = "PreLobby";
     protected final static String LOBBY = "Lobby";
     private final static Logger log = Logger.getLogger(SocketServer.class.getName());
     
     public static String run;
     
     void start(int port, String headless) {
 	this.port = port;
 	log.log(Level.INFO, "Waiting for client");
 	try (ServerSocket serverSocket = new ServerSocket(port);) {
 	    isRunning = true;
 	    // create a lobby on start
 	    Room.setServer(this);
 	    lobby = new Room(LOBBY);// , this);
 	    rooms.add(lobby);
 	    if (headless.equals("n")) { //initialize UI if called in CLI
 	    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 	   	windowSize.width *= .4;
			windowSize.height *= .4;
			setPreferredSize(windowSize);
			setSize(windowSize);
			setLocationRelativeTo(null);
	
			setTitle("Server");
			card = new CardLayout();
			setLayout(card);
			createPanelHome();
			createClientList();
			showUI();
 	    }  	  
 	    while (SocketServer.isRunning) {
 		try {
 		    Socket client = serverSocket.accept();
 		    log.log(Level.INFO, "Client connecting...");
 		    // Server thread is the server's representation of the client
 		    ServerThread thread = new ServerThread(client, lobby);
 		    thread.start();
 		    // create a dummy room until we get further client details
 		    // technically once a user fully joins this lobby will be destroyed
 		    // but we'll track it in an array so we can attempt to clean it up just in case
 		    Room prelobby = new Room(PRELOBBY);// , this);
 		    prelobby.addClient(thread);
 		    isolatedPrelobbies.add(prelobby);

 		    log.log(Level.INFO, "Client added to clients pool");
 		}
 		catch (IOException e) {
 		    e.printStackTrace();
 		}
 	    }

 	}
 	catch (IOException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    try {
 		isRunning = false;
 		cleanup();
 		log.log(Level.INFO, "closing server socket");
 	    }
 	    catch (Exception e) {
 		e.printStackTrace();
 	    }
 	}
     }

     protected void cleanupRoom(Room r) {
 	Iterator<Room> iter = isolatedPrelobbies.iterator();
 	while (iter.hasNext()) {
 	    Room check = iter.next();
 	    if (check.equals(r)) {
 		iter.remove();
 		log.log(Level.INFO, "Removed " + check.getName() + " from prelobbies");
 		break;
 	    }
 	}
     }

    private void cleanup() {
	 	Iterator<Room> iter = this.rooms.iterator();
	 	while (iter.hasNext()) {
	 	    Room r = iter.next();
	 	    try {
	 		r.close();
	 		iter.remove();
	 	    }
	 	    catch (Exception e) {
	 		// it's ok to ignore this one
	 	    }
	 	}
	 	Iterator<Room> pl = isolatedPrelobbies.iterator();
	 	while (pl.hasNext()) {
	 	    Room r = pl.next();
	 	    try {
	 		r.close();
	 		pl.remove();
	 	    }
	 	    catch (Exception e) {
	 		// it's ok to ignore this one
	 	    }
	 	}
	 	try {
	 	    lobby.close();
	 	    log.log(Level.WARNING, "Lobby closed");
	 	}
	 	catch (Exception e) {
	 	    // ok to ignore this too
	 	}
	     }
	
	     protected Room getLobby() {
	 	return lobby;
    }

     protected List<String> getRooms(String room) {
	 	// not the most efficient way to do it, but it works
	 	List<String> roomNames = new ArrayList<String>();
	 	Iterator<Room> iter = rooms.iterator();
	 	// part 2, limit returned rooms
	 	int i = 0;
	 	int max = 10;// lets get up to 10 rooms
	 	while (iter.hasNext()) {
	 	    Room r = iter.next();
	 	    // Part 2 added room name filter for searches
	 	    if ((r != null && r.getName() != null)
	 		    && (room == null || (room != null && r.getName().toLowerCase().contains(room.toLowerCase())))) {
	 		roomNames.add(r.getName());
	 	    }
	
	 	    i++;
	 	    if (i > max) {
	 		break;
	 	    }
	
	 	}
	 	return roomNames;
     }

     /***
      * Special helper to join the lobby and close the previous room client was in if
      * it's marked as Prelobby. Mostly used for prelobby once the server receives
      * more client details.
      * 
      * @param client
      */
    protected void joinLobby(ServerThread client) {
 	Room prelobby = client.getCurrentRoom();
 	if (joinRoom(LOBBY, client)) {
 	    if (prelobby != null) {
 		prelobby.removeClient(client);
 		log.log(Level.INFO, "Added " + client.getClientName() + " to Lobby; Prelobby should self destruct");
 	    }
 	    else {
 		log.log(Level.WARNING, "Prelobby was null for " + client.getClientName());
 	    }
 	}
 	else {
 	    log.log(Level.INFO, "Problem moving " + client.getClientName() + " to lobby");
 	}
 	client.loadMuteList();
    }

     /***
      * Helper function to check if room exists by case insensitive name
      * 
      * @param roomName The name of the room to look for
      * @return matched Room or null if not found
      */
     private Room getRoom(String roomName) {
 	Iterator<Room> iter = rooms.iterator();
 	while (iter.hasNext()) {
 	    Room r = iter.next();
 	    if (r != null && r.getName() != null && r.getName().equalsIgnoreCase(roomName)) {
 		return r;
 	    }
 	}
 	/*
 	 * for (int i = 0, l = rooms.size(); i < l; i++) { Room r = rooms.get(i); if (r
 	 * == null || r.getName() == null) { continue; } if
 	 * (r.getName().equalsIgnoreCase(roomName)) { return r; } }
 	 */
 	log.log(Level.WARNING, "Error getting room " + roomName);
 	return null;
     }

     /***
      * Attempts to join a room by name. Will remove client from old room and add
      * them to the new room.
      * 
      * @param roomName The desired room to join
      * @param client   The client moving rooms
      * @return true if reassign worked; false if new room doesn't exist
      */
     protected synchronized boolean joinRoom(String roomName, ServerThread client) {
	 	if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
	 	    log.log(Level.WARNING, "Room is either null or " + PRELOBBY);
	 	    return false;
	 	}
	 	Room newRoom = getRoom(roomName);
	 	Room oldRoom = client.getCurrentRoom();
	 	if (newRoom != null) {
	 	    if (oldRoom != null) {
	 		log.log(Level.INFO, client.getClientName() + " leaving room " + oldRoom.getName());
	 		oldRoom.removeClient(client);
	 	    }
	 	    else {
	 		log.log(Level.WARNING, "old room is null for " + client.getClientName());
	 	    }
	 	    log.log(Level.INFO, client.getClientName() + " joining room " + newRoom.getName());
	 	    newRoom.addClient(client);
	 	    return true;
	 	}
	 	return false;
     }

     /***
      * Attempts to create a room with given name if it doesn't exist already.
      * 
      * @param roomName The desired room to create
      * @return true if it was created and false if it exists
      */
     protected synchronized boolean createNewRoom(String roomName) {
	 	if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
	 	    return false;
	 	}
	 	if (getRoom(roomName) != null) {
	 	    // TODO can't create room
	 	    log.log(Level.INFO, "Room already exists");
	 	    return false;
	 	}
	 	else {
	 	    Room room = new Room(roomName);// , this);
	 	    rooms.add(room);
	 	    log.log(Level.INFO, "Created new room: " + roomName);
	 	    return true;
	 	}
     }

     public static void main(String[] args) {
	 	// let's allow port to be passed as a command line arg
	 	// in eclipse you can set this via "Run Configurations"
	 	// -> "Arguments" -> type the port in the text box -> Apply
	 	int port = -1;
	 	try {
	 	    port = Integer.parseInt(args[0]);
	 	}
	 	catch (Exception e) {
	 	    // ignore this, we know it was a parsing issue
	 	}
	 	if (port > -1) {
	 		Scanner scan = new Scanner(System.in); 
	 		
	 		// ask if you wish to run the server with UI or not
	 		System.out.println("Run headless? (y/n)");
	 		run = scan.nextLine();
	 		scan.close();
	 		
	 		log.log(Level.INFO, "Starting Server");
	 	 	SocketServer server = new SocketServer();
	 	 	log.log(Level.INFO, "Listening on port " + port);
	 	 	server.start(port, run);
	 	 	log.log(Level.INFO, "Server Stopped");
	 		
	 				
	 	}
    }

     
/**
 * UI Porperties of server
 */

// gets time since server has been started
String getUptime() {
	RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
	long uptime = rb.getUptime() / 1000;
	return Long.toString(uptime) + " seconds";
}

// gets total used memory of the server
String getMemory() {
	double memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024);
	String memUsage =  memory + " MB";
	return memUsage;
}

void createPanelHome() {
	JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());
	
	
	textArea = new JPanel();
	textArea.setLayout(new BoxLayout(textArea, BoxLayout.Y_AXIS));
	textArea.setAlignmentY(Component.BOTTOM_ALIGNMENT);
	JScrollPane scroll = new JScrollPane(textArea);
	scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	Dimension d = new Dimension((int) (windowSize.width * .3), windowSize.height);
	scroll.setPreferredSize(d);
	scroll.setMinimumSize(d);
	scroll.setMaximumSize(d);
	panel.add(scroll, BorderLayout.CENTER);
	

	JPanel input = new JPanel();
	input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
	
	// get and show initial server uptime (should be near 0 seconds on server start)
	JEditorPane showUptime = new JEditorPane();
	showUptime.setEditable(false);
	showUptime.setText("Server uptime: " + getUptime());

	textArea.add(showUptime);
	
	//get and show initial server memory usage (estimated in MB)
	JEditorPane showMemUsage = new JEditorPane();
	showMemUsage.setEditable(false);
	showMemUsage.setText("Server memory usage: " + getMemory());

	textArea.add(showMemUsage);
	
	// press to refresh server uptime and memory usage
	JButton refresh = new JButton("Refresh");
	refresh.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	
	    	showUptime.setText("Server uptime: " + getUptime());
			showMemUsage.setText("Server memory usage: " + getMemory());
	    
	    }});
	
	input.add(refresh);
	
	// press to kill the server
	JButton terminate = new JButton("Close Server");
	terminate.setAlignmentY(Component.RIGHT_ALIGNMENT);

	terminate.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	cleanup();
	    	isRunning = false;
	    	System.exit(0);
	    }});
	
	input.add(terminate);
	panel.add(input, BorderLayout.SOUTH);
	this.add(panel);
}

void showUI() {
	pack();
	Dimension lock = textArea.getSize();
	textArea.setMaximumSize(lock);
	lock = userPanel.getSize();
	userPanel.setMaximumSize(lock);
	setVisible(true);
}

// this JPanel holds all connected clients
void createClientList() {
	userPanel = new JPanel();
	
	userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
	userPanel.setAlignmentY(Component.TOP_ALIGNMENT);
	
	JEditorPane clients = new JEditorPane();
	clients.setText("Connected Clients");
	clients.setEditable(false);
	
	Dimension p = new Dimension(123, 30);
	clients.setPreferredSize(p);
	clients.setMinimumSize(p);
	clients.setMaximumSize(p);
	
	userPanel.add(clients);

	JScrollPane scroll = new JScrollPane(userPanel);
	scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

	Dimension d = new Dimension((int) (windowSize.width * .4), windowSize.height);
	scroll.setPreferredSize(d);
	scroll.setMinimumSize(d);
	scroll.setMaximumSize(d);

	
	textArea.getParent().getParent().getParent().add(scroll, BorderLayout.EAST);

}

// add client to userPanel on connect
void addClient(ServerThread client) {
	//System.out.println(client.getClientName() + " joined.");
 	JPanel addToList = new JPanel();

 	addToList.setAlignmentY(Component.TOP_ALIGNMENT);

	Dimension d = new Dimension((int) (windowSize.width * .4), (int) (windowSize.height * .2));
	addToList.setPreferredSize(d);
	addToList.setMinimumSize(d);
	addToList.setMaximumSize(d);
	
		Client c = new Client(client.getClientName());
		Dimension p = new Dimension(userPanel.getSize().width, 30);
		c.setPreferredSize(p);
		c.setMinimumSize(p);
		c.setMaximumSize(p);
		
		clients.add(c);
		
		// disconnect client from server when pressed
		//every client has a button
		JButton disconnect = new JButton("Disconnect");
		addToList.add(c);
		addToList.add(disconnect);
		
		
		disconnect.addActionListener(new ActionListener() {
	
		    @Override
		    public void actionPerformed(ActionEvent e) {
				removeClient(client, c, addToList);

		    }
		}
		);
		userPanel.add(addToList);
		userArray.add(c);
		pack();
	}

//remove client on button press
 void removeClient(ServerThread client, Client c, JPanel addToList) {
 		userPanel.remove(addToList);
 		c.removeAll();
 		userPanel.revalidate();
 		userPanel.repaint();
 		if (client.isRunning == true) {
 			client.disconnect();
 		}
 		pack();
 }
 
 // this is used in Room class, removes client from userPanel when they close their UI
 void removeClient(ServerThread client) {
	Iterator<Client> iter = clients.iterator();
 	while (iter.hasNext()) {
 	    Client c = iter.next();
 	    if (c.getName().equals(client.getClientName())) {
 	    	iter.remove();
 	    	c.removeAll();
 	    	if (client.isRunning == true) {
	  			client.disconnect();
	  		}
 	    }
 	    Iterator<JPanel> names = userArray.iterator();
 	    while (names.hasNext()) {
 	    	JPanel p = names.next();
 	    	for (int i = 0; i < p.getComponentCount(); ++i) {
     	        if (p.getComponent(i).equals(c)) {
     	        	userPanel.remove(p);
     	    		userPanel.revalidate();
     	    		userPanel.repaint();
     	    		break;
     	        }
 	        }
 	    }
 	}
 	pack();
 }




 }
  