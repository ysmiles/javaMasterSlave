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
			System.out.println("\nA thread waiting for message from the slave is created.");
			System.out.print(">");
			// read input form server
			BufferedReader in = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				System.out.print("\n");
				System.out.println(inputLine);
				System.out.print(">");
			}
			// close socket if disconnected from client (read null from client)
			csocket.close();
			
			System.out.println("\nDisconnection detected by message receiver.");

			masterbot.removeBySocket(csocket);
			
			System.out.println("Receiver closed.");
			System.out.print(">");

		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
