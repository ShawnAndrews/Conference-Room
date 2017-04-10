package whiteboardPkg;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

/* Abstract class for implementing a network server or client */
public abstract class WhiteboardNetworkAbstract {

	protected BufferedReader input;
	protected PrintWriter output;
	
	/* Start server or client */
	abstract public void start(String ip, int port) throws IOException;
	
	/* Packet requests */
	abstract protected void sendDrawPacket(int drawingTool, int x, int y, int col);
	
	abstract protected void sendAddVisitorPacket(Socket s, String username, int col);
	
	abstract protected void sendChangeNamePacket(Socket clientConnection, String oldUsername, String newUsername);
	
	abstract protected void sendChangeColorPacket(Socket clientConnection, String username, int oldCol, int newCol);
	
	abstract protected void sendClearScreen();
	
	/* Close network connection */
	abstract protected void closeConnection();
	
	/* Sleep to force GUI thread to update */
	protected void yieldToMainThread(){
		try {
			Thread.currentThread();
			Thread.sleep(10);
		} catch (InterruptedException e1) { e1.printStackTrace(); }
	}

	/* Receive serialized packet from given socket connection */
	protected WhiteboardNetworkSerializedPacket receivePacket(Socket s){
		
		// receive serialized packet
		try { return (WhiteboardNetworkSerializedPacket) new ObjectInputStream(s.getInputStream()).readObject(); } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
		return null;
		
	}
	
}