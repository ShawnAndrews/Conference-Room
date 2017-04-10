package whiteboardPkg;

import java.net.URL;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/* Visitor class */
class visitorEntry{
	String username;
	Color col;
	
	visitorEntry(String username, Color col){
		this.username = username;
		this.col = col;
	}
}

@SuppressWarnings("serial")
public class WhiteboardWindow extends JFrame{

	private final String DEFAULT_USERNAME = "default_username";
	private final String DEFAULT_FONT = "Tw Cen MT";
	private final int DEFAULT_FONT_SIZE = 14;
	private final int WHITEBOARD_CORNER_RADIUS = 160;
	private final Color THEME_BACKGROUND = Color.DARK_GRAY;
	private final Color THEME_FOREGROUND = Color.WHITE;
	private final int NUM_DRAWING_TOOLS = 6;
	
	private static JTextField tfCreatePort;
	private static JTextField tfJoinIP, tfJoinPort;
	private static JTextField tfNewUsername;
	private static WhiteboardNetworkAbstract network;
	private static BufferedImage whiteboardBi;
	private static WhiteBoardPanel whiteboardPanel;
	private static DefaultListModel<String> visitorListModel;
	private static JList<String> visitorList;
	private static HashMap<String, Color> visitorListMap;
	private static JLabel lblUsername;
	private static JLabel lblColor;
	private static JLabel lblServerInfoIP, lblServerInfoPort;
	private static JLabel lblLeave, lblTrash;
	private static JLabel[] lblDrawingTool;
	private static int selectedDrawingTool;
	
	public WhiteboardWindow() { 
		initialize();
	}
	
	/**
	 * Initialize the contents of the 
	 */
	public void initialize() {
		selectedDrawingTool = 1;
		lblDrawingTool = new JLabel[NUM_DRAWING_TOOLS];
		network = null;
		visitorListMap = new HashMap<String, Color>();
		setTitle("Whiteboard conference room");
		
		// set frame options
		setResizable(false);
		setBounds(100, 30, 1024, 768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set background color
		getContentPane().setBackground(THEME_BACKGROUND);
		getContentPane().setLayout(null);
		
		// Set icons
		ImageIcon roomIcon = null;
		ImageIcon roomCreateIcon = null;
		ImageIcon roomJoinIcon = null;
		try { roomIcon = new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomIcon.png"))); } catch (IOException e1) { e1.printStackTrace(); }
		try { roomCreateIcon = new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomCreateIcon.png"))); } catch (IOException e1) { e1.printStackTrace(); }
		try { roomJoinIcon = new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomJoinIcon.png"))); } catch (IOException e1) { e1.printStackTrace(); }
		
		// Room
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 10, 250, 518);
		tabbedPane.setBackground(THEME_BACKGROUND);
		tabbedPane.setForeground(THEME_FOREGROUND);
		tabbedPane.setUI(new customTabbedPaneUI());
		
		// Room - tab 1
		JPanel roomPanel1 = new JPanel();
		roomPanel1.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel1.setLayout(null);
		roomPanel1.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		roomPanel1.setBackground(THEME_BACKGROUND);
		tabbedPane.addTab("Room", roomIcon, roomPanel1, "");
		
		JPanel roomPanel1SubPanelProfile = new JPanel();
		roomPanel1SubPanelProfile.setLayout(null);
		roomPanel1SubPanelProfile.setBounds(15, 10, 215, 60);
		roomPanel1SubPanelProfile.setBackground(THEME_BACKGROUND);
		TitledBorder titledColoredProfileBorder = new TitledBorder("Profile");
		titledColoredProfileBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredProfileBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		titledColoredProfileBorder.setTitleColor(THEME_FOREGROUND);
		roomPanel1SubPanelProfile.setBorder(titledColoredProfileBorder);
		
		lblUsername = new JLabel("Username: " + DEFAULT_USERNAME);
		lblUsername.setBounds(15, 17, 180, 15);
		lblUsername.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblUsername.setForeground(THEME_FOREGROUND);
		roomPanel1SubPanelProfile.add(lblUsername);
		
		JLabel lblColorChooser = new JLabel("Color:");
		lblColorChooser.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblColorChooser.setBounds(15, 36, 120, 15);
		lblColorChooser.setForeground(THEME_FOREGROUND);
		roomPanel1SubPanelProfile.add(lblColorChooser);
		
		lblColor = new JLabel("");
		lblColor.setBounds(53, 36, 15, 15);
		lblColor.setOpaque(true);
		lblColor.setBackground(new Color(0,0,0));
		lblColor.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel1SubPanelProfile.add(lblColor);
		
		roomPanel1.add(roomPanel1SubPanelProfile);
		
		JPanel roomPanel1SubPanelServerInfo = new JPanel();
		roomPanel1SubPanelServerInfo.setLayout(null);
		roomPanel1SubPanelServerInfo.setBounds(15, 80, 215, 60);
		roomPanel1SubPanelServerInfo.setBackground(THEME_BACKGROUND);
		TitledBorder titledColoredServerInfoBorder = new TitledBorder("Server info.");
		titledColoredServerInfoBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredServerInfoBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		titledColoredServerInfoBorder.setTitleColor(THEME_FOREGROUND);
		roomPanel1SubPanelServerInfo.setBorder(titledColoredServerInfoBorder);
		
		lblServerInfoIP = new JLabel("IP address: Not connected.");
		lblServerInfoIP.setBounds(15, 17, 175, 15);
		lblServerInfoIP.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblServerInfoIP.setForeground(THEME_FOREGROUND);
		roomPanel1SubPanelServerInfo.add(lblServerInfoIP);
		
		lblServerInfoPort = new JLabel("Port: ");
		lblServerInfoPort.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblServerInfoPort.setBounds(15, 36, 170, 15);
		lblServerInfoPort.setForeground(THEME_FOREGROUND);
		roomPanel1SubPanelServerInfo.add(lblServerInfoPort);
		
		roomPanel1.add(roomPanel1SubPanelServerInfo);
		
		JPanel roomPanel1SubPanelDrawingTools = new JPanel();
		roomPanel1SubPanelDrawingTools.setLayout(null);
		roomPanel1SubPanelDrawingTools.setBounds(15, 150, 215, 190);
		roomPanel1SubPanelDrawingTools.setBackground(THEME_BACKGROUND);
		TitledBorder titledColoredDrawingToolsBorder = new TitledBorder("Drawing tools");
		titledColoredDrawingToolsBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredDrawingToolsBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		titledColoredDrawingToolsBorder.setTitleColor(THEME_FOREGROUND);
		roomPanel1SubPanelDrawingTools.setBorder(titledColoredDrawingToolsBorder);
		
		// Drawing tool #1
		try {
			lblDrawingTool[0] = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool1.png"))));
			lblDrawingTool[0].setBackground(THEME_BACKGROUND);
			lblDrawingTool[0].setBounds(25, 20, 25, 25);
			lblDrawingTool[0].addMouseListener(new drawingTool1MouseListener());
			getContentPane().add(lblDrawingTool[0]);
		} catch (IOException e) { e.printStackTrace(); }
		
		// Drawing tool #2
		try {
			lblDrawingTool[1] = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool2Offset.png"))));
			lblDrawingTool[1].setBackground(THEME_BACKGROUND);
			lblDrawingTool[1].setBounds(60, 20, 25, 25);
			lblDrawingTool[1].addMouseListener(new drawingTool2MouseListener());
			getContentPane().add(lblDrawingTool[1]);
		} catch (IOException e) { e.printStackTrace(); }
		
		// Drawing tool #3
		try {
			lblDrawingTool[2] = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool3Offset.png"))));
			lblDrawingTool[2].setBackground(THEME_BACKGROUND);
			lblDrawingTool[2].setBounds(95, 20, 25, 25);
			lblDrawingTool[2].addMouseListener(new drawingTool3MouseListener());
			getContentPane().add(lblDrawingTool[2]);
		} catch (IOException e) { e.printStackTrace(); }
		
		// Drawing tool #4
		try {
			lblDrawingTool[3] = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool4Offset.png"))));
			lblDrawingTool[3].setBackground(THEME_BACKGROUND);
			lblDrawingTool[3].setBounds(130, 20, 25, 25);
			lblDrawingTool[3].addMouseListener(new drawingTool4MouseListener());
			getContentPane().add(lblDrawingTool[3]);
		} catch (IOException e) { e.printStackTrace(); }
		
		// Drawing tool #5
		try {
			lblDrawingTool[4] = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool5Offset.png"))));
			lblDrawingTool[4].setBackground(THEME_BACKGROUND);
			lblDrawingTool[4].setBounds(165, 20, 25, 25);
			lblDrawingTool[4].addMouseListener(new drawingTool5MouseListener());
			getContentPane().add(lblDrawingTool[4]);
		} catch (IOException e) { e.printStackTrace(); }
				
		// Drawing tool #6
		try {
			lblDrawingTool[5] = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool6Offset.png"))));
			lblDrawingTool[5].setBackground(THEME_BACKGROUND);
			lblDrawingTool[5].setBounds(25, 55, 25, 25);
			lblDrawingTool[5].addMouseListener(new drawingTool6MouseListener());
			getContentPane().add(lblDrawingTool[5]);
		} catch (IOException e) { e.printStackTrace(); }
		
		roomPanel1SubPanelDrawingTools.add(lblDrawingTool[0]);
		roomPanel1SubPanelDrawingTools.add(lblDrawingTool[1]);
		roomPanel1SubPanelDrawingTools.add(lblDrawingTool[2]);
		roomPanel1SubPanelDrawingTools.add(lblDrawingTool[3]);
		roomPanel1SubPanelDrawingTools.add(lblDrawingTool[4]);
		roomPanel1SubPanelDrawingTools.add(lblDrawingTool[5]);
		
		roomPanel1.add(roomPanel1SubPanelDrawingTools);
		
		JPanel roomPanel1SubPanelColor = new JPanel();
		roomPanel1SubPanelColor.setLayout(null);
		roomPanel1SubPanelColor.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel1SubPanelColor.setBounds(15, 345, 215, 55);
		roomPanel1SubPanelColor.setBackground(THEME_BACKGROUND);
		TitledBorder titledColoredColorBorder = new TitledBorder("Change color");
		titledColoredColorBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredColorBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		titledColoredColorBorder.setTitleColor(THEME_FOREGROUND);
		roomPanel1SubPanelColor.setBorder(titledColoredColorBorder);
		
		JButton btnChangeColor = new JButton("Choose");
		btnChangeColor.addActionListener(new changeColorBtnActionListener());
		btnChangeColor.setBounds(16, 21, 180, 20);
		btnChangeColor.setBackground(THEME_BACKGROUND);
		btnChangeColor.setForeground(THEME_FOREGROUND);
		btnChangeColor.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel1SubPanelColor.add(btnChangeColor);
		
		roomPanel1.add(roomPanel1SubPanelColor);
		
		JPanel roomPanel1SubPanelUsername = new JPanel();
		roomPanel1SubPanelUsername.setLayout(null);
		roomPanel1SubPanelUsername.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel1SubPanelUsername.setBounds(15, 400, 215, 75);
		roomPanel1SubPanelUsername.setBackground(THEME_BACKGROUND);
		TitledBorder titledColoredUsernameBorder = new TitledBorder("Change username");
		titledColoredUsernameBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredUsernameBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		titledColoredUsernameBorder.setTitleColor(THEME_FOREGROUND);
		roomPanel1SubPanelUsername.setBorder(titledColoredUsernameBorder);

		JLabel lblNewUsername = new JLabel("New username:");
		lblNewUsername.setBounds(15, 26, 100, 15);
		lblNewUsername.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblNewUsername.setForeground(THEME_FOREGROUND);
		roomPanel1SubPanelUsername.add(lblNewUsername);
		
		tfNewUsername = new JTextField();
		tfNewUsername.setBounds(110, 23, 86, 20);
		tfNewUsername.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		tfNewUsername.setBackground(THEME_BACKGROUND);
		tfNewUsername.setForeground(THEME_FOREGROUND);
		roomPanel1SubPanelUsername.add(tfNewUsername);
		
		JButton btnChangeUsername = new JButton("Confirm");
		btnChangeUsername.addActionListener(new changeUsernameBtnActionListener());
		btnChangeUsername.setBounds(15, 47, 180, 20);
		btnChangeUsername.setBackground(THEME_BACKGROUND);
		btnChangeUsername.setForeground(THEME_FOREGROUND);
		btnChangeUsername.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel1SubPanelUsername.add(btnChangeUsername);
		
		roomPanel1.add(roomPanel1SubPanelUsername);
		
		// Room - tab 2
		JPanel roomPanel2 = new JPanel();
		roomPanel2.setLayout(null);
		roomPanel2.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel2.setBackground(THEME_BACKGROUND);
		JPanel roomPanel2SubPanelCreateServer = new JPanel();
		roomPanel2SubPanelCreateServer.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel2SubPanelCreateServer.setLayout(null);
		roomPanel2SubPanelCreateServer.setBounds(15, 10, 215, 90);
		roomPanel2SubPanelCreateServer.setBackground(THEME_BACKGROUND);
		TitledBorder titledColoredCreateServerBorder = new TitledBorder("Host server");
		titledColoredCreateServerBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredCreateServerBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		titledColoredCreateServerBorder.setTitleColor(THEME_FOREGROUND);
		roomPanel2SubPanelCreateServer.setBorder(titledColoredCreateServerBorder);
		
		JLabel lblCreateIP = new JLabel("Server ip: " + getWANIP());
		lblCreateIP.setBounds(15, 17, 170, 12);
		lblCreateIP.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblCreateIP.setForeground(THEME_FOREGROUND);
		roomPanel2SubPanelCreateServer.add(lblCreateIP);
		
		JLabel lblCreatePort = new JLabel("Port number:");
		lblCreatePort.setBounds(15, 36, 120, 15);
		lblCreatePort.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblCreatePort.setForeground(THEME_FOREGROUND);
		roomPanel2SubPanelCreateServer.add(lblCreatePort);
		
		tfCreatePort = new JTextField();
		tfCreatePort.setToolTipText("Port number for server hosting");
		tfCreatePort.setBounds(93, 35, 86, 20);
		tfCreatePort.setBackground(THEME_BACKGROUND);
		tfCreatePort.setForeground(THEME_FOREGROUND);
		tfCreatePort.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel2SubPanelCreateServer.add(tfCreatePort);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new createBtnActionListener(this));
		btnCreate.setBounds(16, 60, 180, 20);
		btnCreate.setBackground(THEME_BACKGROUND);
		btnCreate.setForeground(THEME_FOREGROUND);
		btnCreate.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel2SubPanelCreateServer.add(btnCreate);
		
		roomPanel2.add(roomPanel2SubPanelCreateServer);
		tabbedPane.addTab("Create", roomCreateIcon, roomPanel2, "");
		
		// Room - tab 3
		JPanel roomPanel3 = new JPanel();
		roomPanel3.setLayout(null);
		roomPanel3.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel3.setBackground(THEME_BACKGROUND);
		JPanel roomPanel3SubPanelJoinServer = new JPanel();
		roomPanel3SubPanelJoinServer.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel3SubPanelJoinServer.setLayout(null);
		roomPanel3SubPanelJoinServer.setBounds(15, 10, 215, 100);
		roomPanel3SubPanelJoinServer.setBackground(THEME_BACKGROUND);
		TitledBorder titledColoredJoinServerBorder = new TitledBorder("Join server");
		titledColoredJoinServerBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredJoinServerBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		titledColoredJoinServerBorder.setTitleColor(THEME_FOREGROUND);
		roomPanel3SubPanelJoinServer.setBorder(titledColoredJoinServerBorder);
		
		JLabel lblJoinIP = new JLabel("Server ip:");
		lblJoinIP.setBounds(15, 19, 150, 15);
		lblJoinIP.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblJoinIP.setForeground(THEME_FOREGROUND);
		roomPanel3SubPanelJoinServer.add(lblJoinIP);
		
		tfJoinIP = new JTextField();
		tfJoinIP.setToolTipText("Server IP of host");
		tfJoinIP.setBounds(93, 15, 86, 20);
		tfJoinIP.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		tfJoinIP.setBackground(THEME_BACKGROUND);
		tfJoinIP.setForeground(THEME_FOREGROUND);
		roomPanel3SubPanelJoinServer.add(tfJoinIP);
		
		JLabel lblJoinPort = new JLabel("Port number:");
		lblJoinPort.setBounds(15, 43, 120, 15);
		lblJoinPort.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		lblJoinPort.setForeground(THEME_FOREGROUND);
		roomPanel3SubPanelJoinServer.add(lblJoinPort);
		
		tfJoinPort = new JTextField();
		tfJoinPort.setToolTipText("Port number for server hosting");
		tfJoinPort.setBounds(93, 40, 86, 20);
		tfJoinPort.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		tfJoinPort.setBackground(THEME_BACKGROUND);
		tfJoinPort.setForeground(THEME_FOREGROUND);
		roomPanel3SubPanelJoinServer.add(tfJoinPort);
		
		JButton btnJoin = new JButton("Join");
		btnJoin.addActionListener(new joinBtnActionListener(this));
		btnJoin.setBounds(16, 67, 180, 20);
		btnJoin.setBackground(THEME_BACKGROUND);
		btnJoin.setForeground(THEME_FOREGROUND);
		btnJoin.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		roomPanel3SubPanelJoinServer.add(btnJoin);
		
		roomPanel3.add(roomPanel3SubPanelJoinServer);
		tabbedPane.addTab("Join", roomJoinIcon, roomPanel3, "");
		
		getContentPane().add(tabbedPane);
		
		// Visitor list
		visitorListModel = new DefaultListModel<String>();
		
		visitorList = new JList<String>(visitorListModel);
		visitorList.setCellRenderer(new NameRenderer());
		visitorList.setBounds(20, 20, 200, 144);
		visitorList.setBackground(THEME_BACKGROUND);
		JScrollPane visitorListScrollPane = new JScrollPane();
		visitorListScrollPane.setViewportView(visitorList);
		visitorListScrollPane.setBorder(null);
		visitorListScrollPane.setBounds(20, 20, 210, 160);
		
		JPanel panelVisitorList = new JPanel();
		panelVisitorList.setOpaque(false);
		panelVisitorList.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		panelVisitorList.setLayout(null);
		panelVisitorList.setBounds(10, 534, 250, 194);
		TitledBorder titledColoredVisitorListBorder = new TitledBorder("Visitor list");
		titledColoredVisitorListBorder.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, THEME_FOREGROUND));
		titledColoredVisitorListBorder.setTitleColor(THEME_FOREGROUND);
		titledColoredVisitorListBorder.setTitleFont(new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
		panelVisitorList.setBorder(titledColoredVisitorListBorder);
		
		panelVisitorList.add(visitorListScrollPane);
		
		getContentPane().add(panelVisitorList);
		
		// Whiteboard
		whiteboardBi = new BufferedImage(720, 715, BufferedImage.TYPE_INT_RGB);
		whiteboardPanel = new WhiteBoardPanel(whiteboardBi);
		whiteboardPanel.setLayout(null);
		whiteboardPanel.setBounds(280, 13, 720, 715);
		whiteboardPanel.setBackground(THEME_BACKGROUND);
		
		// Leave button
		try {
			lblLeave = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomLeaveIcon2.png"))));
			lblLeave.setBackground(new Color(100,50,150,200));
			lblLeave.setBounds(696, 0, 24, 24);
			lblLeave.setVisible(true);
			lblLeave.addMouseListener(new leaveIconMouseListener());
			whiteboardPanel.add(lblLeave);
		} catch (IOException e) { e.printStackTrace(); }
		
		// Trash button
		try {
			lblTrash = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomTrashIconOffset.png"))));
			lblTrash.setBackground(new Color(100,50,150,200));
			lblTrash.setBounds(0, 0, 24, 24);
			lblTrash.setVisible(true);
			lblTrash.addMouseListener(new trashIconMouseListener());
			whiteboardPanel.add(lblTrash);
		} catch (IOException e) { e.printStackTrace(); }
		
		getContentPane().add(whiteboardPanel);
		
	}
	
	/* Paint with a drawing tool on the whiteboard */
	public static void paintPixelOnWhiteboard(int drawingTool, int x, int y, int col){
		
		if(drawingTool == 1){
			whiteboardBi.setRGB(x, y, col);
		} else if(drawingTool == 2){
			whiteboardBi.setRGB(x, y, col);
			whiteboardBi.setRGB(x + 1, y + 0, col);
			whiteboardBi.setRGB(x + 2, y + 0, col);
			whiteboardBi.setRGB(x + 1, y + 1, col);
			whiteboardBi.setRGB(x + 0, y + 1, col);
			whiteboardBi.setRGB(x + 0, y + 2, col);
			whiteboardBi.setRGB(x + 1, y - 1, col);
			whiteboardBi.setRGB(x + 0, y - 1, col);
			whiteboardBi.setRGB(x + 0, y - 2, col);
			whiteboardBi.setRGB(x - 1, y + 0, col);
			whiteboardBi.setRGB(x - 2, y + 0, col);
			whiteboardBi.setRGB(x - 1, y + 1, col);
			whiteboardBi.setRGB(x - 1, y + 1, col);
			whiteboardBi.setRGB(x - 1, y - 1, col);
			
		} else if(drawingTool == 3){
			whiteboardBi.setRGB(x, y, col);
			whiteboardBi.setRGB(x + 1, y - 1, col);
			whiteboardBi.setRGB(x + 2, y - 2, col);
			whiteboardBi.setRGB(x + 3, y - 3, col);
			whiteboardBi.setRGB(x + 4, y - 4, col);
			whiteboardBi.setRGB(x + 1, y - 1, col);
			whiteboardBi.setRGB(x + 2, y - 2, col);
			whiteboardBi.setRGB(x + 3, y - 3, col);
			whiteboardBi.setRGB(x + 4, y - 4, col);
		} else if(drawingTool == 4){
			whiteboardBi.setRGB(x, y, col);
			whiteboardBi.setRGB(x + 0, y + 1, col);
			whiteboardBi.setRGB(x + 0, y + 2, col);
			whiteboardBi.setRGB(x + 0, y + 3, col);
			whiteboardBi.setRGB(x + 0, y + 4, col);
			whiteboardBi.setRGB(x + 0, y - 1, col);
			whiteboardBi.setRGB(x + 0, y - 2, col);
			whiteboardBi.setRGB(x + 0, y - 3, col);
			whiteboardBi.setRGB(x + 0, y - 4, col);
		} else if(drawingTool == 5){
			whiteboardBi.setRGB(x, y, col);
			whiteboardBi.setRGB(x + 1, y + 0, col);
			whiteboardBi.setRGB(x + 2, y + 0, col);
			whiteboardBi.setRGB(x + 3, y + 0, col);
			whiteboardBi.setRGB(x + 4, y + 0, col);
			whiteboardBi.setRGB(x - 1, y + 0, col);
			whiteboardBi.setRGB(x - 2, y + 0, col);
			whiteboardBi.setRGB(x - 3, y + 0, col);
			whiteboardBi.setRGB(x - 4, y + 0, col);
		} else if(drawingTool == 6){
			whiteboardBi.setRGB(x, y, col);
			whiteboardBi.setRGB(x - 4, y + 4, col);
			whiteboardBi.setRGB(x - 3, y + 3, col);
			whiteboardBi.setRGB(x - 3, y - 2, col);
			whiteboardBi.setRGB(x - 1, y + 0, col);
			whiteboardBi.setRGB(x + 0, y + 3, col);
			whiteboardBi.setRGB(x + 0, y - 3, col);
			whiteboardBi.setRGB(x + 0, y - 4, col);
			whiteboardBi.setRGB(x + 1, y + 0, col);
			whiteboardBi.setRGB(x + 3, y - 3, col);
			whiteboardBi.setRGB(x + 3, y + 3, col);
			whiteboardBi.setRGB(x + 4, y + 2, col);
		}
		whiteboardPanel.repaint();
	}
	
	/* Custom UI for visitor list */
	public class NameRenderer extends JLabel implements ListCellRenderer<String> {
		 
		class ColorIcon implements Icon {

		    Color color;
		    int preferredSize = -1;

		    public ColorIcon(Color color, int preferredSize) {
		        this.color = color;
		        this.preferredSize = preferredSize;
		    }

		    @Override
		    public int getIconWidth() {
		        return preferredSize;
		    }

		    @Override
		    public int getIconHeight() {
		        return preferredSize;
		    }

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				g.setColor(color);
		        g.fillRect(0, 0, preferredSize, preferredSize);
			}
			
		}
		
	    public NameRenderer() {
	        setOpaque(true);
	    }
	 
	    @Override
	    public Component getListCellRendererComponent(JList<? extends String> list, String name, int index, boolean isSelected, boolean cellHasFocus) {
	    	
	        ColorIcon colorIcon = new ColorIcon(visitorListMap.get(name), 20);
	        
	        setIcon(colorIcon);
	        setText(name);
	        
	        if (isSelected) {
	            setBackground(list.getSelectionBackground());
	            setForeground(list.getSelectionForeground());
	        } else {
	            setBackground(list.getBackground());
	            setForeground(THEME_FOREGROUND);
	        }
	 
	        return this;
	    }
	    
	} 
	
	/* Custom UI for room tabs */
	public class customTabbedPaneUI extends BasicTabbedPaneUI {
		
		@Override
		protected void paintFocusIndicator(Graphics g,
                int tabPlacement,
                Rectangle[] rects,
                int tabIndex,
                Rectangle iconRect,
                Rectangle textRect,
                boolean isSelected) {
			
		}
		
		@Override
		protected void paintTabBackground(Graphics g,
                int tabPlacement,
                int tabIndex,
                int x,
                int y,
                int w,
                int h,
                boolean isSelected){
			
		}
		
	}
	
	/* Custom UI for whiteboard */
	public class WhiteBoardPanel extends JPanel implements MouseMotionListener {

        private BufferedImage image;

        public WhiteBoardPanel(BufferedImage image) {
        	this.image = image;
        	addMouseMotionListener(this);
        	
    		int rr = 255; // red component 0...255
    		int gg = 255; // green component 0...255
    		int bb = 255; // blue component 0...255
    		int col = (rr << 16) | (gg << 8) | bb;
    		for (int x = 0; x < image.getWidth(); x++)
    			for (int y = 0; y < image.getHeight(); y++)
    				image.setRGB(x, y, col);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(), image.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            
    		BufferedImage roundedImage = makeRoundedCorner(image, WHITEBOARD_CORNER_RADIUS);
    		
            g2d.drawImage(roundedImage, 0, 0, this);
            
        }

		@Override
		public void mouseDragged(MouseEvent e) {
			if(network != null)
				network.sendDrawPacket(getDrawingTool(), e.getX(), e.getY(), lblColor.getBackground().getRGB());
		}

		@Override
		public void mouseMoved(MouseEvent e) { }

    }
	
	/* Returns a buffered image of a rounded rectangle */
	public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
	    int w = image.getWidth();
	    int h = image.getHeight();
	    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2 = output.createGraphics();
	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
	    g2.setComposite(AlphaComposite.SrcIn);
	    g2.drawImage(image, 0, 0, null);
	    g2.dispose();

	    return output;
	}
	
	/* Create a server on mouse click */
	class createBtnActionListener implements ActionListener {
		WhiteboardWindow frame;
		
		public createBtnActionListener(WhiteboardWindow frame){
			this.frame = frame;
		}
		
		public void actionPerformed(ActionEvent e) {
			try{
				
				// attempt to start network server
				network = new WhiteboardNetworkServer();
				network.start("", Integer.parseInt(tfCreatePort.getText()));
				
				// enable leave room
				lblLeave.setVisible(true);
				
			}catch(IOException e1){
				JOptionPane.showMessageDialog(frame,
					    e1.getMessage(),
					    "Server error",
					    JOptionPane.ERROR_MESSAGE);
			}catch(NumberFormatException e2){ e2.printStackTrace(); }
	    }
		
	}
	
	/* Join a server on mouse click */
	class joinBtnActionListener implements ActionListener {
		WhiteboardWindow frame;
		
		public joinBtnActionListener(WhiteboardWindow frame){
			this.frame = frame;
		}
		
		public void actionPerformed(ActionEvent e) {
			try{
				
				// attempt to join network server
				network = new WhiteboardNetworkClient();
				network.start(tfJoinIP.getText(), Integer.parseInt(tfJoinPort.getText()));
				
				// enable leave room
				lblLeave.setVisible(true);
				
			}catch(IOException e1){
				JOptionPane.showMessageDialog(frame,
					    e1.getMessage(),
					    "Server error",
					    JOptionPane.ERROR_MESSAGE);
			}catch(NumberFormatException e2){ e2.printStackTrace(); }
			
	    }
		
	}
	
	/* Leave room on mouse click */
	class leaveIconMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// leave room
			network.closeConnection();
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// set animated image
			try { lblLeave.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomLeaveIcon.png")))); } catch (IOException e) { e.printStackTrace(); }
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// set original image
			try { lblLeave.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomLeaveIcon2.png")))); } catch (IOException e) { e.printStackTrace(); }
		}

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	/* Erase whiteboard on mouse click */
	class trashIconMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// send clear screen
			network.sendClearScreen();
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// set animated image
			try { lblTrash.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomTrashIcon.png")))); } catch (IOException e) { e.printStackTrace(); }
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// set original image
			try { lblTrash.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomTrashIconOffset.png")))); } catch (IOException e) { e.printStackTrace(); }
		}

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	/* Select drawing tool on mouse click */
	class drawingTool1MouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(selectedDrawingTool != 1){
				try { lblDrawingTool[selectedDrawingTool - 1].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool" + selectedDrawingTool + "Offset.png")))); } catch (IOException e) { e.printStackTrace(); }
				try { lblDrawingTool[0].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool1.png")))); } catch (IOException e) { e.printStackTrace(); }
				selectedDrawingTool = 1;
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) { }

		@Override
		public void mouseExited(MouseEvent arg0) { }

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	class drawingTool2MouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(selectedDrawingTool != 2){
				try { lblDrawingTool[selectedDrawingTool - 1].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool" + selectedDrawingTool + "Offset.png")))); } catch (IOException e) { e.printStackTrace(); }
				try { lblDrawingTool[1].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool2.png")))); } catch (IOException e) { e.printStackTrace(); }
				selectedDrawingTool = 2;
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) { }

		@Override
		public void mouseExited(MouseEvent arg0) { }

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	class drawingTool3MouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(selectedDrawingTool != 3){
				try { lblDrawingTool[selectedDrawingTool - 1].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool" + selectedDrawingTool + "Offset.png")))); } catch (IOException e) { e.printStackTrace(); }
				try { lblDrawingTool[2].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool3.png")))); } catch (IOException e) { e.printStackTrace(); }
				selectedDrawingTool = 3;
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) { }

		@Override
		public void mouseExited(MouseEvent arg0) { }

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	class drawingTool4MouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(selectedDrawingTool != 4){
				try { lblDrawingTool[selectedDrawingTool - 1].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool" + selectedDrawingTool + "Offset.png")))); } catch (IOException e) { e.printStackTrace(); }
				try { lblDrawingTool[3].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool4.png")))); } catch (IOException e) { e.printStackTrace(); }
				selectedDrawingTool = 4;
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) { }

		@Override
		public void mouseExited(MouseEvent arg0) { }

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	class drawingTool5MouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(selectedDrawingTool != 5){
				try { lblDrawingTool[selectedDrawingTool - 1].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool" + selectedDrawingTool + "Offset.png")))); } catch (IOException e) { e.printStackTrace(); }
				try { lblDrawingTool[4].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool5.png")))); } catch (IOException e) { e.printStackTrace(); }
				selectedDrawingTool = 5;
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) { }

		@Override
		public void mouseExited(MouseEvent arg0) { }

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	class drawingTool6MouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(selectedDrawingTool != 6){
				try { lblDrawingTool[selectedDrawingTool - 1].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool" + selectedDrawingTool + "Offset.png")))); } catch (IOException e) { e.printStackTrace(); }
				try { lblDrawingTool[5].setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/roomDrawingTool6.png")))); } catch (IOException e) { e.printStackTrace(); }
				selectedDrawingTool = 6;
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) { }

		@Override
		public void mouseExited(MouseEvent arg0) { }

		@Override
		public void mousePressed(MouseEvent arg0) { }

		@Override
		public void mouseReleased(MouseEvent arg0) { }
		
	}
	
	/* Change username on mouse click */
	class changeUsernameBtnActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			
			String oldUsername = lblUsername.getText().substring("Username: ".length());
			String newUsername = tfNewUsername.getText();
					
			// change username in list
			changeUsername(oldUsername, newUsername);
			
			// change username in profile
			lblUsername.setText("Username: " + newUsername);
			
			// send change name request to server
			network.sendChangeNamePacket(null, oldUsername, newUsername);
			
	    }
		
	}
	
	/* Change color on mouse click */
	class changeColorBtnActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			String username = lblUsername.getText().substring("Username: ".length());
			int oldCol = lblColor.getBackground().getRGB();
			int newCol;
			
			// change color in profile
			lblColor.setBackground(JColorChooser.showDialog(null, "Choose a color", new Color(0,0,0)));
			
			// set new color
			newCol = lblColor.getBackground().getRGB();
			
			// change color in list
			changeColor(username, oldCol, newCol);
			
			// send change color request to server
			network.sendChangeColorPacket(null, username, oldCol, newCol);
			
	    }
		
	}
	
	/* Get WAN IP address of this computer */
	public static String getWANIP() {
        URL whatismyip = null;
        BufferedReader in = null;
        try {
        	whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } catch (IOException e) {
        	return "No internet.";
		}
    }
	
	/* Get this client's username */
	public static String getUsername() { return lblUsername.getText(); }
	
	/* Get this client's color */
	public static Color getColor() { return lblColor.getBackground(); }
	
	/* Get currently selected drawing tool */
	public static int getDrawingTool(){ return selectedDrawingTool; }
	
	/* Get a list of all users in room */
	public static ArrayList<visitorEntry> getVisitorList() {
		
		ArrayList<visitorEntry> list = new ArrayList<visitorEntry>();
		for(int i = 0; i < visitorListModel.getSize(); i++)
			list.add(new visitorEntry(visitorListModel.getElementAt(i), visitorListMap.get(visitorListModel.getElementAt(i))));
		return list;
		
	}
	
	/* Get IP address of server */
	public static String getServerIP() { return tfJoinIP.getText(); }
	
	/* Get port number of server */
	public static int getServerPort() { return Integer.parseInt(tfJoinPort.getText()); }
	
	/* Add visitor to list upon joining room */
	public static void addVisitor(boolean self, String name, Color color){
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if(self){
					visitorListMap.put(lblUsername.getText().substring("Username: ".length()), lblColor.getBackground());
					visitorListModel.addElement(lblUsername.getText().substring("Username: ".length()));
				}else{
					visitorListMap.put(name, color);
					visitorListModel.addElement(name);
				}
			}
		});
		
	}
	
	/* Remove visitor to list upon leaving room */
	public static void removeVisitor(String name){
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				visitorListMap.remove(name);
				visitorListModel.removeElement(name);
			}
		});
		
	}
	
	/* Change username of user in list */
	public static void changeUsername(String _oldUsername, String _newUsername){
		
		String oldUsername = _oldUsername;
		String newUsername = _newUsername;
		
		// change name in hashmap
		visitorListMap.put(newUsername,visitorListMap.remove(oldUsername));
		
		// change name in visitor list if found
		for(int i = 0; i < visitorListModel.getSize(); i++)
			if (visitorListModel.get(i).equals(oldUsername))
				visitorListModel.set(i, newUsername);
		
	}
	
	/* Change color of user in list */
	public static void changeColor(String username, int oldCol, int newCol){
		
		// change name in hashmap
		visitorListMap.put(username, new Color(newCol));
		
		// trigger list model update
		visitorList.repaint();
	}
	
	/* Reset room's visitor list and GUI */
	public static void reset(){
		
		// clear hashmap
		visitorListMap.clear();
		
		// clear visitor list model
		visitorListModel.clear();
		
		// clear server info
		lblServerInfoIP.setText("IP address: Not connected.");
		lblServerInfoPort.setText("Port: ");
		
		// disable leave room
		lblLeave.setVisible(false);
		
	}
	
	/* Update server information in GUI */
	public static void updateServerInfo(String IP, int port){
		lblServerInfoIP.setText("IP address: " + IP);
		lblServerInfoPort.setText("Port: " + Integer.toString(port));
	}
	
	/* Erase whiteboard */
	public static void clearScreen() {
		int rr = 255; // red component 0...255
		int gg = 255; // green component 0...255
		int bb = 255; // blue component 0...255
		int col = (rr << 16) | (gg << 8) | bb;
		for (int x = 0; x < whiteboardBi.getWidth(); x++)
			for (int y = 0; y < whiteboardBi.getHeight(); y++)
				whiteboardBi.setRGB(x, y, col);
		whiteboardPanel.repaint();
	}
	
	/* Leave room */
	public static void leaveRoom() {
		
		// reset network
		network = null;
		
		// reset server GUI
		reset();
				
	}

}
