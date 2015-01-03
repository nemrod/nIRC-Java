package nirc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionOutputHandler {
	private DataOutputStream output;

	public ConnectionOutputHandler(Socket socket) {
		try {
			this.output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		System.out.println("server disconnect in ConnectionInputHandler.close()");
		try {
			this.output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(String message) {
		try {
			this.output.writeBytes(message + "\r\n");
			System.out.println("Output: " + message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}