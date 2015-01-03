package nirc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection implements PropertyChangeListener {
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private String pcsValue;
	
	private String hostname;
	private int port;
	
	private Socket socket;
	private ConnectionInputHandler inputHandler;
	private ConnectionOutputHandler outputHandler;
	private Thread inputHandlerThread;
	
	public Connection(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
		
		try {
			this.socket = new Socket(this.hostname, this.port);
			this.inputHandler = new ConnectionInputHandler(this.socket);
			this.outputHandler = new ConnectionOutputHandler(this.socket);
			this.inputHandler.addPropertyChangeListener(this);
			this.inputHandlerThread = new Thread(this.inputHandler);
			this.inputHandlerThread.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
    
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object obj = evt.getSource();
		String message = (String) evt.getNewValue();
		
		if(obj == this.inputHandler) {
			if(message != null) {
				this.setPcsValue(message);
				
				String command = message.split(" ")[0];
				String[] args = message.substring(message.indexOf(" ") + 1).split(" ");
				
				switch(command) {
					case "PING":
						// TODO not working?
						this.outputHandler.send("PONG " + args[0]);
						break;
					default:
						//this.setPcsValue(message);
				}
			}
		}
	}

	public void disconnect() {
		System.out.println("server disconnect in Connection.disconnect()");
		try {	
			this.socket.close();
			this.inputHandlerThread = null;
			this.inputHandler.close();
			this.outputHandler.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(String message) {
		outputHandler.send(message);
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}
}