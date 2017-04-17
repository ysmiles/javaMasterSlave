package masterslave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class msgReceiver implements Runnable {
	// client socket
	Socket csocket;

	// Constructor
	msgReceiver(Socket csocket) {
		this.csocket = csocket;
	}

	public void run() {
		try {
			System.out.println("A thread waiting for reply message from slave is created.");
			// read input form server
			BufferedReader in = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			
			// close socket if disconnected from client (read null from client)
			csocket.close();
			
			System.out.println("Message receiver has been closed.");

		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
