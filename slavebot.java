package masterslave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class slavebot extends Thread {

	private String targetname;
	private int targetport;

	public slavebot(String targetname, int targetport) {// , boolean connection)
		this.targetname = targetname;
		this.targetport = targetport;
		// this.connection = connection;
	}

	// Specify date format
	static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	// This list is used for storing slaves information.
	static List<slave> targets = new ArrayList<slave>();

	public static void main(String argv[]) throws Exception {
		// port number and name can come from arguments
		// portnumber = argv[];
		// name = argv[];
		if (argv.length != 4 || (!(argv[0].equals("-p") && argv[2].equals("-h"))
				&& !(argv[0].equals("-h") && argv[2].equals("-p")))) {
			System.out.println(
					"Correct way: slavebot -p masterport -h masterhost or \n" + "slave -h masterhost -p masterport");
			System.exit(1);
		}
		int portnum;
		String mastername;

		if (argv[0].equals("-p") && argv[2].equals("-h")) {
			portnum = Integer.parseInt(argv[1]);
			mastername = argv[3];
		} else {
			portnum = Integer.parseInt(argv[3]);
			mastername = argv[1];
		}

		String comM; // command from masterbot

		Socket mastersock = new Socket(mastername, portnum);

		// DataOutputStream outToServer = new
		// DataOutputStream(clientSocket.getOutputStream());
		// outToServer.writeBytes(sentence + '\n');

		// read input form server
		BufferedReader in = new BufferedReader(new InputStreamReader(mastersock.getInputStream()));

		PrintStream pstream = new PrintStream(mastersock.getOutputStream());

		while ((comM = in.readLine()) != null) {
			if (comM.equals("connection?")) {
				pstream.println("Slave is still connected and waiting for command.");
				continue;
			}

			System.out.println("Get command:\n" + comM);

			// pass comM to command some parser first
			// int n = fun(); socket rsock = ... ; remote socket

			commandParser par = new commandParser(comM);

			String targetIden = par.getTargetIdentifier();
			String type = par.getTargetIdentifierType();
			int tport = par.getTargetport();
			// note here isConnect means connect command
			boolean connect = par.isConnect();

			int n = par.getRepeattimes(); // 0 means disconnect
			
			if (connect) {
				for (int i = 0; i < n; i++) {
					slavebot child = new slavebot(targetIden, tport);
					child.start();
				}
			} else {
				int ind = getElementIndex(targetIden, type, tport);
				while (ind != -1) {
					terminate(ind);
					ind = getElementIndex(targetIden, type, tport);
				}
			}

		}
		// don't close
		// mastersock.close();
	}

	public void run() {
		try {
			// might need to use blocking queue
			// Socket() can handle IP and name
			Socket targetsock = new Socket(targetname, targetport);

			String connectiondate = dateFormat.format(new Date());

			addElement(new slave(targetsock, connectiondate));

			PrintStream pstream = new PrintStream(targetsock.getOutputStream());
			System.out.println("Try to connect " + targetname + " at port " + targetport + " and write some message.");
			pstream.println("test message");

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void addElement(slave s) {
		synchronized (targets) {
			targets.add(s);
		}
	}

	// Find the first match
	public static int getElementIndex(String identifier, String identifierType, int port) {
		synchronized (targets) {
			// return the fist found one, else return -1
			if (identifierType.equals("IP")) {
				for (int i = 0; i < targets.size(); i++) {
					if (targets.get(i).getIPaddr().equals(identifier)) {
						if (port == -1)
							return i;
						else if (targets.get(i).getSourcePortNumber() == port)
							return i;
					}
				}
			} else {
				for (int i = 0; i < targets.size(); i++) {
					if (targets.get(i).getName().equals(identifier)) {
						if (port == -1)
							return i;
						else if (targets.get(i).getSourcePortNumber() == port)
							return i;
					}
				}
			}
			return -1;
		}
	}

	public static int getSize() {
		synchronized (targets) {
			return targets.size();
		}
	}

	public static void terminate(int index) {
		synchronized (targets) {
			try {
				System.out.println(
						"Try to close the connection with index " + index + ", " + targets.get(index).getName());
				targets.get(index).getSock().close();
				System.out.println("Close succeed!");
				targets.remove(index);
			} catch (Exception e) {
				System.out.println("Failed close connection with " + targets.get(index).getName());
			}
		}
	}

}
