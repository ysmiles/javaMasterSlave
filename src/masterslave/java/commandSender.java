package masterslave;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class commandSender implements Runnable {
	// client socket
	Socket csocket;

	// command to be sent
	String com;

	// Constructor

	commandSender(Socket csocket, String com) {
		this.csocket = csocket;
		this.com = com;
	}

	public void run() {
		try {
			PrintStream pstream = new PrintStream(csocket.getOutputStream());
			pstream.println(com);
			// Ref: http://stackoverflow.com/questions/8890303/behavior-of-java-sockets-when-closing-output-stream
			// Closing the OutputStream will close the associated socket.
			// pstream.close();
			// Don't close socket
			// csocket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}