package nirc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionInputHandler implements Runnable {
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private String pcsValue;
	
	private Socket socket;
	private BufferedReader input;
	
	public ConnectionInputHandler(Socket socket) {
		this.socket = socket;
		
		try {	
			this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
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
	public void run() {
		String line;
		
		while(!this.socket.isClosed()) {
			try {
				line = input.readLine();
				if(line != null) {
					System.out.println("Input: " + line);
					this.setPcsValue(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		try {
			this.input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
