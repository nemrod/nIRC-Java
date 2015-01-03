package nirc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;

public abstract class Buffer {
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private String pcsValue;
	
	private LinkedList<String> messages;
	
	public Buffer() {
		this.messages = new LinkedList<String>();
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
	
	public void addMessageToBuffer(String message) {
		this.messages.add(message);
		//this.setPcsValue(message);
	}
	
	public LinkedList<String> getMessages() {
		return this.messages;
	}
	
	public String[] getMessagesAsArray() {
		String[] messages = new String[this.messages.size()];
		return this.messages.toArray(messages);
	}
	
	public String getMessagesAsText() {
		String messages = "";
		for(String s : this.messages) {
			messages += s + "\n";
		}
		return messages;
	}
	
	public abstract void parseInput(String input);
	
	public abstract String getName();
}