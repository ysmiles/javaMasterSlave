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
		if (a == e && b == f) {
			while (c < g) {
				while (d < 256) {
					testIP(a, b, c, d);
					++d;
				}
				d = 0;
				++c;
			}
			// c==g
			while (d <= h) {
				testIP(a, b, c, d);
				++d;
			}
		} else {
			System.out.println("ip range too large.");
		}

		sendToMaster();
		
		System.out.println("IPscanner finished.");
	}

	public void sendToMaster() {
		String msg = "From slave: available ips are ";
		for (int i = 0; i < ips.size(); i++) {
			msg += ips.get(i);
			if (i != ips.size() - 1)
				msg += ", ";
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

	public void testIP(int a, int b, int c, int d) {
		String IPstring = new String(a + "." + b + "." + c + "." + d);
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

}
