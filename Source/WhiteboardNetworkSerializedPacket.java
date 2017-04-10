package whiteboardPkg;

import java.io.Serializable;

/**
 *  Client -> Server          				Server -> Client
 *  ----------------           				----------------
 *  CLIENT_DRAW: Send request to server		SERVER_DRAW: Send request to client
 *  to draw on room's whiteboard.			to draw on client's whiteboard.
 *  CLIENT_ADD_VISITOR: Send request to		SERVER_ADD_VISITOR: Send request to
 *  server to add this client to room.		client to add a client to room list.
 *  CLIENT_CHANGE_USERNAME: Send request	SERVER_CHANGE_USERNAME: Send request
 *  to server to change this client's 		to client to change a user's name in
 *  name in room.							list.
 *  CLIENT_CHANGE_COLOR: Send request		SERVER_CHANGE_COLOR: Send request to
 *  to server to change this client's 		client to change a user's color in 
 *  color in room.							list.
 *  CLIENT_CLEAR_SCREEN: Send request		SERVER_CLEAR_SCREEN: Send request to
 *  to server to clear room's whiteboard.	client to erase whiteboard.
 *  CLIENT_DISCONNECTED: Send request
 *  to server to remove client from room.
 *  
 */
public class WhiteboardNetworkSerializedPacket implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public enum PACKET_TYPE{SERVER_DRAW, SERVER_ADD_VISITOR, SERVER_CHANGE_USERNAME, SERVER_CHANGE_COLOR, SERVER_CLEAR_SCREEN,
							CLIENT_DRAW, CLIENT_ADD_VISITOR, CLIENT_CHANGE_USERNAME, CLIENT_CHANGE_COLOR, CLIENT_CLEAR_SCREEN, CLIENT_DISCONNECTED}
	private PACKET_TYPE pType;
	private int x;
	private int y;
	private int col, oldCol, newCol;
	private int drawingTool;
	private String username, oldUsername, newUsername;
	
	public WhiteboardNetworkSerializedPacket(PACKET_TYPE pType){
		this.pType = pType;
	}
	
	public void setDrawPacket(int drawingTool, int x, int y, int col){
		this.x = x;
		this.y = y;
		this.col = col;
		this.drawingTool = drawingTool;
	}
	
	public void setAddVisitorPacket(String username, int col){
		this.username = username;
		this.col = col;
	}
	
	public void setChangeNamePacket(String oldUsername, String newUsername){
		this.oldUsername = oldUsername;
		this.newUsername = newUsername;
	}
	
	public void setChangeColorPacket(String username, int oldCol, int newCol){
		this.username = username;
		this.oldCol = oldCol;
		this.newCol = newCol;
	}
	
	public void setClientDisconnectedPacket(String username){
		this.username = username;
	}
	
	public PACKET_TYPE getPType() {return pType;}
	
	public int getX() {return x;}
	
	public int getY() {return y;}
	
	public int getCol() {return col;}
	
	public String getUsername() {return username;}
	
	public String getOldUsername() {return oldUsername;}
	
	public String getNameUsername() {return newUsername;}
	
	public int getOldCol() {return oldCol;}
	
	public int getNewCol() {return newCol;}
	
	public int getDrawingTool() {return drawingTool;}
	
}
