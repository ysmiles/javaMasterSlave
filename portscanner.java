package masterslave;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class portscanner implements Runnable {
	PrintStream pstream;
	String ip;
	int port1, port2;
	List<Integer> ports = new ArrayList<Integer>();

	portscanner(String ip, int port1, int port2, Socket ms) {
		try {
			pstream = new PrintStream(ms.getOutputStream());
			this.ip = ip;
			this.port1 = port1;
			this.port2 = port2;

		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		while (port1 <= port2) {
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(ip, port1), 5000);
				// if the port is not reachable, exception will be thrown
				ports.add(port1);
				// close socket
				socket.close();
			} catch (IOException e) {
				// Auto-generated catch block
				System.out.println("Port " + port1 + " is not reachable.");
				// e.printStackTrace();
			}
			++port1;
		}

		sendToMaster();
	}

	public void sendToMaster() {
		String msg = "From slave: available ports are\n";
		for (int i = 0; i < ports.size(); i++) {
			msg += ports.get(i).toString();
			if (i != ports.size() - 1)
				msg += ",";
		}
		
		if(ports.isEmpty()){
			msg += "None";
		}
		
		msg += "\n";
		pstream.print(msg);
	}

}
