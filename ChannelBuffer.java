package nirc;

import java.util.LinkedList;

public class ChannelBuffer extends Buffer {
	private String channel;
	private String topic;
	private LinkedList<String> nicks;
	
	public ChannelBuffer(String channel) {
		this.channel = channel;
		this.topic = "<no topic>";
		this.nicks = new LinkedList<String>();
	}

	public void parseInput(String input) {
		System.out.println("Keyboard: " + input);
		
		if(input.startsWith("/")) {
			this.setPcsValue(";serverCommand " + this.getChannel() + " :" + input);
		} else {
			this.setPcsValue(";channelSendMessage " + this.getChannel() + " :" + input);
		}
	}
	
	public String getName() {
		return this.channel;
	}
	
	public String getChannel() {
		return this.channel;
	}
	
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getTopic() {
		return this.topic;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public void clearNicks() {
		this.nicks.clear();
	}
	
	public void addNick(String nick) {
		this.nicks.add(nick);
	}
	
	public void removeNick(String nick) {
		this.nicks.remove(nick);
	}
	
	public LinkedList<String> getNicks() {
		return this.nicks;
	}
	
	public String[] getNicksAsArray() {
		String[] nicks = new String[this.nicks.size()];
		return this.nicks.toArray(nicks);
	}
}