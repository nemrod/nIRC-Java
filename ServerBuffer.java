package nirc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;

public class ServerBuffer extends Buffer implements PropertyChangeListener {
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private String pcsValue;

	private UserInfo user;
	
	private LinkedList<ChannelBuffer> channels;
	//private LinkedList<QueryBuffer> queries;

	private Connection connection;
	
	public ServerBuffer(String hostname, int port, UserInfo user) {
		super();
		
		this.user = user;
		
		this.channels = new LinkedList<ChannelBuffer>();
		//this.queries = new LinkedList<QueryBuffer>();
		
		this.connection = new Connection(hostname, port);
		this.connection.addPropertyChangeListener(this);
		
		this.changeNick(this.user.nickname);
		this.sendUserInfo();
	}
	
	public void disconnect() {	
		System.out.println("server disconnect in ServerBuffer.disconnect()");
		this.connection.disconnect();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    
    public String getPcsValue() {
    	return this.pcsValue;
    }
    
    public void setPcsValue(String newPcsValue) {
    	String oldPcsValue = this.pcsValue;
    	this.pcsValue = newPcsValue;
    	this.pcs.firePropertyChange("value", oldPcsValue, newPcsValue);
    }
    
    public String getHostname() {
    	return this.connection.getHostname();
    }
    
    public String getName() {
    	return this.connection.getHostname();
    }
    
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object obj = evt.getSource();
		String message = (String) evt.getNewValue();
		ChannelBuffer channel;
				
		if(obj == this.connection) {
			String[] args = message.substring(message.indexOf(" ") + 1).split(" ");

			if(args.length > 0 && args[0].matches("\\d+(\\.\\d+)?")) { // numeric messages
				int code = Integer.parseInt(args[0]);
				
				switch(code) {
					case 353: // nicklist for channel
						channel = this.getChannel(args[3]);
						if(channel != null) {
							channel.clearNicks();
							channel.addNick(args[4].substring(1));
							for(int i = 5; i < args.length; i++) {
								channel.addNick(args[i]);
							}
							this.setPcsValue(";channelUpdateNickList " + channel.getChannel());
						}
						break;
					case 366:
						break;
					case 332: // topic for channel
						channel = this.getChannel(args[2]);
						if(channel != null) {
							channel.setTopic(args[3].substring(1));
							this.setPcsValue(";channelUpdateTopic " + channel.getChannel());
						}
						break;
					// TODO case for successful nickchange; update this.user
					default:
						this.addMessageToBuffer(message);
				}
			} else if(args.length > 0 && (args[0].equals("JOIN") || args[0].equals("PART"))) { // someone joined or left a channel
				this.getNicks(args[1]);
			} else if(args.length > 1 && args[0].equals("PRIVMSG")) { // if it's a message
				if(args[1].startsWith("#")) { // if it's a channel message
					channel = this.getChannel(args[1]);
					if(channel != null) {
						channel.addMessageToBuffer(args[2].substring(1));
						this.setPcsValue(";channelShowMessage " + channel.getChannel() + " " + message.split("!")[0].substring(1) + " " + message.substring(message.substring(1).indexOf(":")));
					}
				} else { // it's a query
				}
			} else { // it's a server message - display in server window
				this.addMessageToBuffer(message);
			}
		} else if(obj instanceof ChannelBuffer) { // we got a message from one of our channels
			channel = (ChannelBuffer)obj;
			if(message.startsWith(";")) { // internal message
				message = message.substring(1);
				String command = message.split(" ")[0];
				String[] args = message.substring(message.indexOf(" ") + 1).split(" ");
				String payload = message.substring(message.indexOf(":") + 1);
				
				switch(command) {
					case "serverCommand":
						if(payload.startsWith("/part")) {
							this.parseInput("/part " + args[0]);
						} else {
							this.parseInput(payload);
						}
						break;
					case "channelSendMessage":
						this.sendChannelMessage(channel.getChannel(), payload);
						channel.addMessageToBuffer(payload);
						this.setPcsValue(";channelShowMessage " + channel.getChannel() + " " + this.user.getNickname() + " :" + payload);
						break;
				}
			}
		}
	}
	
	@Override
	public void addMessageToBuffer(String message) {
		super.addMessageToBuffer(message);
		
		// send message upwards that we want to add to this tabs messagelist
		this.setPcsValue(";serverShowMessage :" + message);
	}
	
	public void parseInput(String input) {
		System.out.println("Keyboard: " + input);
		
		if(input.startsWith("/")) {
			this.parseCommand(input);
		}
	}

	private void parseCommand(String message) {
		String command = message.split(" ")[0].substring(1);
		String[] args = (message.split(" ").length > 1 ? message.substring(message.indexOf(" ") + 1).split(" ") : new String[0]);
		
		switch(command) {
			case "nick":
				if(args.length > 0) {
					this.changeNick(args[0]);
				} else {
					this.addMessageToBuffer("You need to specify a username.");
				}
				break;
			case "join":
				if(args.length > 0) {
					this.joinChannel(args[0]);
				} else {
					this.addMessageToBuffer("You need to specify a channel.");
				}
				break;
			case "part":
				if(args.length > 0) {
					this.partChannel(args[0]);
				} else {
					this.addMessageToBuffer("You need to specify a channel.");
				}
				break;
			case "disconnect":
				System.out.println("server disconnect in ServerBuffer.parseCommand()");
				for(ChannelBuffer cb : this.channels) {
					this.partChannel(cb.getChannel());
				}
				this.disconnect();
				this.setPcsValue(";serverDisconnect");
				break;
			/*case "msg":
			case "query":
				if(args.length > 2) {
					String privMsg = message.substring(message.indexOf(args[1]));
					this.msgUser(args[0], privMsg);
				}
				break;*/
		}
	}
	
	private void changeNick(String newNick) {
		this.connection.send("NICK " + newNick);
	}
	
	private void sendUserInfo() {
		this.connection.send("USER " + this.user.getUsername() + " " + this.user.getHostname() + " " + this.connection.getHostname() + " :" + this.user.getRealname());
	}
	
	private void joinChannel(String channel) {
		if(!channel.startsWith("#")) {
			channel = "#" + channel;
		}
		connection.send("JOIN " + channel);
		
		this.channels.add(new ChannelBuffer(channel));
		this.channels.getLast().addPropertyChangeListener(this);
		
		this.setPcsValue(";channelJoin " + channel);
	}
	
	private void partChannel(String channel) {
		connection.send("PART " + channel);
		this.setPcsValue(";channelPart " + channel);
		this.channels.remove(this.getIndexOfChannel(channel));
	}
	
	public ChannelBuffer getChannelBuffer(String channel) {
		for(ChannelBuffer c : this.channels) {
			if(c.getChannel().equals(channel)) {
				return c;
			}
		}
		return null;
	}
	
	public LinkedList<ChannelBuffer> getChannelBuffers() {
		return this.channels;
	}
	
	private void getNicks(String channel) {
		connection.send("NAMES " + channel);
	}
	
	public void sendChannelMessage(String channel, String message) {
		connection.send("PRIVMSG " + channel + " :" + message);
	}
	
	/*public void sendPrivateMessage(String username, String message) {
		connection.send("PRIVMSG " + username + " :" + message);
	}*/
	
	private ChannelBuffer getChannel(String channel) {
		for(ChannelBuffer c : this.channels) {
			if(c.getName().equals(channel)) {
				return c;
			}
		}
		return null;
	}
	
	private int getIndexOfChannel(String channel) {
		for(int i = 0; i < this.channels.size(); i++) {
			if(this.channels.get(i).getChannel().equals(channel)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean hasChannel(ChannelBuffer buffer) {
		for(ChannelBuffer cb : this.channels) {
			if(cb == buffer) {
				return true;
			}
		}
		return false;
	}
}