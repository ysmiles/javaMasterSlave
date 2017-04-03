package masterslave;

import java.net.Socket;

public class slave {
	private Socket sock;
	private String RegistrationDate;

	public slave(Socket s, String date) {
		this.sock = s;
		this.RegistrationDate = date;
	}

	public Socket getSock() {
		return sock;
	}

	public String getRegistrationDate() {
		return RegistrationDate;
	}

	public String getName() {
		return sock.getInetAddress().getHostName();
	}

	public String getIPaddr() {
		return sock.getInetAddress().getHostAddress();
	}

	public int getSourcePortNumber() {
		return sock.getPort();
	}
	
}
