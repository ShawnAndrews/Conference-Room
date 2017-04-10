package whiteboardPkg;

import java.net.Socket;
import java.net.SocketException;

import whiteboardPkg.WhiteboardNetworkSerializedPacket.PACKET_TYPE;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

public class WhiteboardNetworkClient extends WhiteboardNetworkAbstract {
	
	private Socket serverConnection;
	
	/* Attempt to start a connection with a server given and IP and Port. Throws exception if the server is not started. */
	@Override
	public void start(String ip, int port) throws IOException {
		
		// spawn client thread
		try {
			new networkThread(new Socket(ip, port)).start();
		} catch (IOException e1) {
			throw new IOException("Invalid server ip or port to join server.");
		}
		
	}
	
	/* Server connection thread */
	private class networkThread extends Thread {
		
		public networkThread(Socket socket) {
			serverConnection = socket;
		}
		
		@Override
		public void run() {
			
			try {
				System.out.println("Connected");
				input = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
				output = new PrintWriter(serverConnection.getOutputStream(), true);
             	
				// update GUI
	        	WhiteboardWindow.updateServerInfo(WhiteboardWindow.getServerIP(), WhiteboardWindow.getServerPort());
				
				// send user profile
				output.println(WhiteboardWindow.getUsername().substring("Username: ".length()));
				output.println(WhiteboardWindow.getColor().getRGB());
				
				while (serverConnection.isConnected()) {
				
					WhiteboardNetworkSerializedPacket p = receivePacket(serverConnection);
                	
					if(p == null){
						throw new SocketException("Server disconnected.");
					}else if(p.getPType() == PACKET_TYPE.CLIENT_DRAW){
                		WhiteboardWindow.paintPixelOnWhiteboard(p.getDrawingTool(), p.getX(), p.getY(), p.getCol());
                		System.out.println("Paint at " + p.getX() + "x" + p.getY() + " with color #" + p.getCol() + " with tool #" + p.getDrawingTool());
                	}else if(p.getPType() == PACKET_TYPE.CLIENT_ADD_VISITOR){
                		WhiteboardWindow.addVisitor(false, p.getUsername(), new Color(p.getCol()));
                	}else if(p.getPType() == PACKET_TYPE.CLIENT_CHANGE_USERNAME){
                		WhiteboardWindow.changeUsername(p.getOldUsername(), p.getNameUsername());
                	}else if(p.getPType() == PACKET_TYPE.CLIENT_CHANGE_COLOR){
                		WhiteboardWindow.changeColor(p.getUsername(), p.getOldCol(), p.getNewCol());
                	}else if(p.getPType() == PACKET_TYPE.CLIENT_DISCONNECTED){
                		WhiteboardWindow.removeVisitor(p.getUsername());
                	} else if(p.getPType() == PACKET_TYPE.CLIENT_CLEAR_SCREEN){
                		WhiteboardWindow.clearScreen();
                	}
					
				}
                
			} catch (SocketException e) {
            	 
				// server disconnected
            	try {
            		System.out.println(e.getMessage());
            		serverConnection.close(); 
            		WhiteboardWindow.reset();
            	} catch (IOException e1) { e1.printStackTrace(); }
				
			} catch (IOException e) { e.printStackTrace(); }
			
		}
		
	}

	/* Client packet send requests */
	@Override
	protected void sendDrawPacket(int drawingTool, int x, int y, int col) {
		
		// send draw packet
		try { 
			WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.SERVER_DRAW);
			packet.setDrawPacket(drawingTool, x, y, col);
			new ObjectOutputStream(serverConnection.getOutputStream()).writeObject(packet);
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	@Override
	protected void sendAddVisitorPacket(Socket s, String username, int col) {
		
		// send serialized packet
		try { 
			WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.SERVER_ADD_VISITOR);
			packet.setAddVisitorPacket(username, col);
			new ObjectOutputStream(s.getOutputStream()).writeObject(packet);
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	@Override
	protected void sendChangeNamePacket(Socket clientConnection, String oldUsername, String newUsername) {
		
		// send serialized packet
		try { 
			WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.SERVER_CHANGE_USERNAME);
			packet.setChangeNamePacket(oldUsername, newUsername);
			new ObjectOutputStream(serverConnection.getOutputStream()).writeObject(packet);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	@Override
	protected void sendChangeColorPacket(Socket clientConnection, String username, int oldCol, int newCol) {
		
		// send serialized packet
		try { 
			WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.SERVER_CHANGE_COLOR);
			packet.setChangeColorPacket(username, oldCol, newCol);
			new ObjectOutputStream(serverConnection.getOutputStream()).writeObject(packet);
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	@Override
	protected void sendClearScreen(){
		
		// send serialized packet
		try { 
			WhiteboardNetworkSerializedPacket packet = new WhiteboardNetworkSerializedPacket(WhiteboardNetworkSerializedPacket.PACKET_TYPE.SERVER_CLEAR_SCREEN);
			new ObjectOutputStream(serverConnection.getOutputStream()).writeObject(packet);
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	/* Close connection to server, in the event the client leaves the room */
	public void closeConnection(){
		
		// close server connection to all clients
		try { serverConnection.close(); } catch (IOException e) { e.printStackTrace(); }
		
		// leave room
		WhiteboardWindow.leaveRoom();
		
		
	}
	
}