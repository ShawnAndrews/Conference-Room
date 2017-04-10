package whiteboardPkg;

import java.awt.EventQueue;

public class WhiteboardMain {
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new WhiteboardWindow().setVisible(true);
			}
		});
		
	}
	
}
