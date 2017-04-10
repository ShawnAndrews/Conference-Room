package whiteboardPkg;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import whiteboardPkg.WhiteboardNetworkSerializedPacket.PACKET_TYPE;

public class WhiteboardNetworkServer extends WhiteboardNetworkAbstract {

	private ArrayList<Socket> clientConnections;
	private ServerSocket listener;
	
	public WhiteboardNetworkServer(){
		super();
		clientConnections = new ArrayList<Socket>();
	}
	
	/* Attempt to start a server given a port. Throws exception if a server cannot be started. */
	@Override
	public void start(String ip, int port) throws IOException {
		
		// start server
		try {
			listener = new ServerSocket(port);
		} catch (IOException e1) {
			throw new IOException("Port is currently in use.");
		}
		
		Thread t = new Thread(new Runnable() {
		   @Override
		   public void run() {
		        System.out.println("Running");
		        	
	        	// add self to visitor list
	        	WhiteboardWindow.addVisitor(true, null, null);
	        	
	        	// update GUI
	        	WhiteboardWindow.updateServerInfo(WhiteboardWindow.getWANIP(), port);
	        	
	        	// accept visitors
	        	try { while (true) { new networkThread(listener.accept()).start(); } } catch(IOException e){ e.printStackTrace(); }
	        	
		   }
		});
		t.start();
		
	}
	
	/* Client connection thread */
	private class networkThread extends Thread {
		
		private Socket clientConnection;
		
		public networkThread(Socket socket) {
			clientConnection = socket;
			clientConnections.add(socket);
		}
		
		@Override
		public void run() {
			
			String username = null;
			Color color = null;
			
			try {
				input = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
           		output = new PrintWriter(clientConnection.getOutputStream(), true);
           		
           		// read client profile
           		username = input.readLine();
           		color = new Color(Integer.parseInt(input.readLine()));
           		
           		// add client to visitor list
                WhiteboardWindow.addVisitor(false, username, color);
           	  	
                // yield
                yieldToMainThread();
                
                // add all visitors to client's list
                for(visitorEntry e : WhiteboardWindow.getVisitorList())
                	sendAddVisitorPacket(clientConnection, e.username, e.col.getRGB());
                
                // add this client to other clients' list
                for(Socket otherClient : clientConnections)
                	if(otherClient != clientConnection)
                		sendAddVisitorPacket(otherClient, username, color.getRGB());
                	
                while (clientConnection.isConnected()) {
              	
                	WhiteboardNetworkSerializedPacket p = receivePacket(clientConnection);
                	
                	if(p == null){
						throw new SocketException("Client disconnected.");
					}else if(p.getPType() == PACKET_TYPE.SERVER_DRAW){
						WhiteboardWindow.paintPixelOnWhiteboard(p.getDrawingTool(), p.getX(), p.getY(), p.getCol());
                		sendDrawPacket(p.getDrawingTool(), p.getX(), p.getY(), p.getCol());
                	}else if(p.getPType() == PACKET_TYPE.SERVER_ADD_VISITOR){
                		WhiteboardWindow.addVisitor(false, p.getUsername(), new Color(p.getCol()));
                	}else if(p.getPType() == PACKET_TYPE.SERVER_CHANGE_USERNAME){
                		sendChangeNamePacket(clientConnection, p.getOldUsername(), p.getNameUsername());
                	}else if(p.getPType() == PACKET_TYPE.SERVER_CHANGE_COLOR){
                		sendChangeColorPacket(clientConnection, p.getUsername(), p.getOldCol(), p.getNewCol());
                	} else if(p.getPType() == PACKET_TYPE.SERVER_CLEAR_SCREEN){
                		sendClearScreen();
                	}
                	
                }
            
			} catch (SocketException e) {
          	 
        		// client disconnected
				try {
					sendClientDisconnectedPacket(clientConnection, username);
					clientConnection.close(); 
				} catch (IOException e1) {
					e1.printStackTrace(); 
				}
          	
				// remove client from visitor list
				WhiteboardWindow.removeVisitor(username);
				
			} catch (IOException e) { e.printStackTrace(); }
			
		}
		
	}
	
	/* Server packet send requests */
	private void sendClientDisconnectedPacket(Socket clientConnection, String username){
		
		// remove visitor from list
		WhiteboardWindow.removeVisitor(username);
		
		// send draw packets to all clients
		for(Socket s : clientConnections){
			if(s != clientConnection){
				try {
					WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.CLIENT_DISCONNECTED);
					packet.setClientDisconnectedPacket(username);
					new ObjectOutputStream(s.getOutputStream()).writeObject(packet);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		
	}
	
	@Override
	protected void sendDrawPacket(int drawingTool, int x, int y, int col) {
		
		// draw on server whiteboard
		WhiteboardWindow.paintPixelOnWhiteboard(drawingTool, x, y, col);
		
		// send draw packets to all clients
		for(Socket s : clientConnections){
			try {
				WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.CLIENT_DRAW);
				packet.setDrawPacket(drawingTool, x, y, col);
				new ObjectOutputStream(s.getOutputStream()).writeObject(packet);
			} catch (IOException e) { e.printStackTrace(); }
		}
		
	}
	
	@Override
	protected void sendAddVisitorPacket(Socket s, String username, int col) {
		
		// send serialized packet
		try { 
			WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.CLIENT_ADD_VISITOR);
			packet.setAddVisitorPacket(username, col);
			new ObjectOutputStream(s.getOutputStream()).writeObject(packet);
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	@Override
	protected void sendChangeNamePacket(Socket clientConnection, String oldUsername, String newUsername) {
		
		// change name on server
		WhiteboardWindow.changeUsername(oldUsername, newUsername);
		
		// send serialized packet
		for(Socket s : clientConnections){
			if(s != clientConnection){
				try {
					WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.CLIENT_CHANGE_USERNAME);
					packet.setChangeNamePacket(oldUsername, newUsername);
					new ObjectOutputStream(s.getOutputStream()).writeObject(packet);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}
	
	@Override
	protected void sendChangeColorPacket(Socket clientConnection, String username, int oldCol, int newCol) {
		
		// change color on server
		WhiteboardWindow.changeColor(username, oldCol, newCol);
		
		// send serialized packet
		for(Socket s : clientConnections){
			if(s != clientConnection){
				try { 
					WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.CLIENT_CHANGE_COLOR);
					packet.setChangeColorPacket(username, oldCol, newCol);
					new ObjectOutputStream(s.getOutputStream()).writeObject(packet);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		
	}
	
	@Override
	protected void sendClearScreen(){
		
		// clear screen on server
		WhiteboardWindow.clearScreen();
		
		// send serialized packet
		for(Socket s : clientConnections){
			try { 
				WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.CLIENT_CLEAR_SCREEN);
				new ObjectOutputStream(s.getOutputStream()).writeObject(packet);
			} catch (IOException e) { e.printStackTrace(); }
		}
		
	}
	
	/* Close connection to all client, in the event the server leaves the room */
	public void closeConnection(){
		
		// close server connection to all clients
		for(Socket s : clientConnections)
			try { s.close(); } catch (IOException e) { e.printStackTrace(); }
		
		// close server listener
		try { listener.close(); } catch (IOException e) { e.printStackTrace(); }
		
		// leave room
		WhiteboardWindow.leaveRoom();
		
	}
	
}