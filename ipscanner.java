package masterslave;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ipscanner implements Runnable {
	PrintStream pstream;
	List<Integer> ip1;
	List<Integer> ip2;
	List<String> ips = new ArrayList<String>();
	int a, b, c, d, e, f, g, h;

	ipscanner(List<Integer> ip1, List<Integer> ip2, Socket ms) {
		try {
			pstream = new PrintStream(ms.getOutputStream());
			this.ip1 = ip1;
			this.ip2 = ip2;

			a = ip1.get(0);
			b = ip1.get(1);
			c = ip1.get(2);
			d = ip1.get(3);

			e = ip2.get(0);
			f = ip2.get(1);
			g = ip2.get(2);
			h = ip2.get(3);

		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		String IPstring1 = new String(a + "." + b + "." + c + "." + d);
		long iplong1 = ipToLong(IPstring1);
		String IPstring2 = new String(e + "." + f + "." + g + "." + h);
		long iplong2 = ipToLong(IPstring2);

		for (; iplong1 <= iplong2; iplong1++) {
			testIP(longToIP(iplong1));
		}

		sendToMaster();

		System.out.println("IPscanner finished.");
	}

	public void sendToMaster() {
		String msg = "From slave: available ips are\n";
		for (int i = 0; i < ips.size(); i++) {
			msg += ips.get(i);
			if (i != ips.size() - 1)
				msg += ",";
		}

		if (ips.isEmpty()) {
			msg += "None";
		}

		msg += "\n";
		pstream.print(msg);
	}

	// public String iptostring(List<Integer> ip) {
	// return new String(ip.get(0).toString() + "." + ip.get(1).toString() + "."
	// + ip.get(2).toString() + "."
	// + ip.get(3).toString());
	//
	// }

	public void testIP(String IPstring) {

		System.out.println("Test " + IPstring);
		try {
			boolean isreachable = InetAddress.getByName(IPstring).isReachable(5000);
			if (isreachable) {
				ips.add(IPstring);
			}
		} catch (IOException e) {
			// Auto-generated catch block
			System.out.println("IP " + IPstring + " is not reachable.");
			// e.printStackTrace();
		}
	}

	private long ipToLong(String ipAddress) {
		String[] ipAddressInArray = ipAddress.split("\\.");

		long result = 0;

		for (int i = 0; i < ipAddressInArray.length; i++) {
			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);
		}

		return result;
	}

	private String longToIP(long ip) {
		return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
	}

}
