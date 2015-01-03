package nirc;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

public class IRC implements PropertyChangeListener {
	private JFrame frame;
	private Container contentPane;
	
	private JMenuBar menuBar;
	private MenuListener menuListener;
	
	private JTabbedPane tabbedPane;
	private LinkedList<Tab> tabs;
	
	UserInfo defaultUser;
	LinkedList<ServerBuffer> servers;
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
        IRC i = new IRC();
	}

	public IRC() {
		this.initializeGUI();
		this.configureGUI();
		this.createMenu();
		this.buildTabBar();
		
		this.createDefaultUser();
		this.servers = new LinkedList<ServerBuffer>();
		
		//this.connectToServer("irc.freenode.net");
	}
	
	private void initializeGUI() {
		this.frame = new JFrame();
		this.contentPane = this.frame.getContentPane();
		
		this.menuBar = new JMenuBar();
		this.menuListener = new MenuListener();
		
		this.tabbedPane = new JTabbedPane();
		this.tabs = new LinkedList<Tab>();
	}
	
	private void configureGUI() {
		this.frame.setVisible(true);
		
		this.frame.setMinimumSize(new Dimension(600, 400));
		this.frame.setExtendedState(this.frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
		this.frame.setLocationRelativeTo(null);
		
		this.frame.setTitle("nIRC");
		
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.frame.setJMenuBar(this.menuBar);
	}
	
	private void createMenu() {
		JMenu menu;
		JMenuItem menuItem;
		
		menu = new JMenu("File");
		menuItem = new JMenuItem("Change default nick");
		menuItem.addActionListener(this.menuListener);
		menu.add(menuItem);
		menuItem = new JMenuItem("Connect to server");
		menuItem.addActionListener(this.menuListener);
		menu.add(menuItem);
		menuItem = new JMenuItem("Quit");
		menuItem.addActionListener(this.menuListener);
		menu.add(menuItem);
		menuBar.add(menu);
		
		menu = new JMenu("Help");
		menuItem = new JMenuItem("About");
		menuItem.addActionListener(this.menuListener);
		menu.add(menuItem);
		menuItem = new JMenuItem("Help");
		menuItem.addActionListener(this.menuListener);
		menu.add(menuItem);
		menuBar.add(menu);
	}
	
	private void buildTabBar() {
		this.contentPane.setLayout(new GridLayout(1, 1));
		this.contentPane.add(this.tabbedPane);
		this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	private void createTab(Buffer buffer) {
		Tab tab = new Tab(buffer);

		this.tabs.add(tab);
		this.tabbedPane.addTab(buffer.getName(), tab);
		this.tabbedPane.setSelectedIndex(this.tabbedPane.getTabCount() - 1);
	}
	
	private class MenuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()) {
				case "Change default nick":
					String newDefaultNick = JOptionPane.showInputDialog("Enter a new default nick:");
					if(newDefaultNick != null) {
						changeDefaultNick(newDefaultNick);
					}
					break;
				case "Connect to server":
					String hostname = JOptionPane.showInputDialog("Enter hostname (e.g. irc.freenode.net):");
					if(hostname != null) {
						connectToServer(hostname);
					}
					break;
				// TODO: the other menu items
			}
		}
	}
	
	private void createDefaultUser() {
		String localHostName = "";
		try {
			//localHostName = InetAddress.getLocalHost().getHostName();
			localHostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.defaultUser = new UserInfo("nircer", System.getProperty("user.name"), "nircer", localHostName, ""); 
	}

	private void changeDefaultNick(String newDefaultNick) {
		// TODO: check for forbidden characters, length, etc.
		this.defaultUser.setNickname(newDefaultNick);
	}
	
	private void connectToServer(String hostname) {
		this.servers.add(new ServerBuffer(hostname, 6667, this.defaultUser));
		this.servers.getLast().addPropertyChangeListener(this);
		
		this.createTab(this.servers.getLast());
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String message = (String)evt.getNewValue();
		Buffer buffer = (Buffer) evt.getSource();
		Tab tab;
		Buffer tBuffer;	
		
		if(message.startsWith(";")) { // internal message
			message = message.substring(1);
			String command = message.split(" ")[0];
			String[] args = message.substring(message.indexOf(" ") + 1).split(" ");
			String payload = message.substring(message.indexOf(":") + 1);
			
			switch(command) {
				case "serverShowMessage":
					tab = getTabForBuffer(buffer);
					if(tab != null) {
						tab.addMessage(payload);
					}
					break;
				case "serverDisconnect":
					System.out.println("server disconnect in IRC");
					tab = this.getTabForBuffer(buffer);
					this.tabbedPane.remove(tab);
					this.tabs.remove(tab);
					break;
				case "channelShowMessage":
					tBuffer = ((ServerBuffer)buffer).getChannelBuffer(args[0]);
					if(tBuffer != null) {
						tab = this.getTabForBuffer(tBuffer);
						if(tab != null) {
							tab.addMessage("<" + args[1] + "> " + payload);
						}
					}
					break;
				case "channelJoin":
					this.createTab(((ServerBuffer)buffer).getChannelBuffer(args[0]));
					break;
				case "channelPart":
					tBuffer = ((ServerBuffer)buffer).getChannelBuffer(args[0]);
					if(tBuffer != null) {
						tab = this.getTabForBuffer(tBuffer);
						if(tab != null) {
							this.tabbedPane.remove(tab);
							this.tabs.remove(tab);
						}
					}
					break;
				case "channelUpdateNickList":
					tBuffer = ((ServerBuffer)buffer).getChannelBuffer(args[0]);
					if(tBuffer != null) {
						tab = this.getTabForBuffer(tBuffer);
						if(tab != null) {
							tab.updateNickList();
						}
					}
					break;
				case "channelUpdateTopic":
					tBuffer = ((ServerBuffer)buffer).getChannelBuffer(args[0]);
					if(tBuffer != null) {
						tab = this.getTabForBuffer(tBuffer);
						if(tab != null) {
							tab.updateTopicLabel();
						}
					}
					break;
			}
		}
	}
	
	private Tab getTabForBuffer(Buffer buffer) {
		for(Tab tab : this.tabs) {
			if(tab.getBuffer() == buffer) {
				return tab;
			}
		}
		return null;
	}
}
