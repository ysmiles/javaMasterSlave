package masterslave;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ipscanner implements Runnable {
	PrintStream pstream;
	List<Integer> ip1;
	List<Integer> ip2;
	List<String> results = new ArrayList<String>();
	List<String> geoinfo = new ArrayList<String>();
	int flag;

	ipscanner(List<Integer> ip1, List<Integer> ip2, Socket ms, int flag) {
		try {
			pstream = new PrintStream(ms.getOutputStream());
			this.ip1 = ip1;
			this.ip2 = ip2;
			this.flag = flag;

		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		String IPstring1 = listToIPstring(ip1);
		long iplong1 = ipToLong(IPstring1);
		String IPstring2 = listToIPstring(ip2);
		long iplong2 = ipToLong(IPstring2);

		for (; iplong1 <= iplong2; iplong1++) {
			testIP(longToIP(iplong1));
		}
		
		if (flag == 1){
			for (int i = 0; i < results.size(); i++){
				// TODO
				try {
					geoinfo.add(testgeo(results.get(i)));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		sendToMaster();

		System.out.println("(geo)IPscanner finished.");
	}

	public void sendToMaster() {
		String msg = "From slave: available ips are\n";
		if (flag == 0) {
			for (int i = 0; i < results.size(); i++) {
				msg += results.get(i);
				if (i != results.size() - 1)
					msg += ",";
			}

		} else if (flag == 1) {
			for (int i = 0; i < geoinfo.size(); i++) {
				msg += geoinfo.get(i);
				if (i != geoinfo.size() - 1)
					msg += "\n";
			}
		}

		if (results.isEmpty()) {
			msg += "None";
		}

		msg += "\n";

		pstream.print(msg);
	}

	public void testIP(String IPstring) {

		System.out.println("Test " + IPstring);
		try {
			boolean isreachable = InetAddress.getByName(IPstring).isReachable(5000);
			if (isreachable) {
				results.add(IPstring);
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
			int powernum = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, powernum);
		}

		return result;
	}

	private String longToIP(long ip) {
		return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
	}

	private String listToIPstring(List<Integer> ip) {
		return (new String(ip.get(0) + "." + ip.get(1) + "." + ip.get(2) + "." + ip.get(3)));
	}
	
	public String testgeo(String IPstring) throws InterruptedException {
		//http://ip-api.com/csv/208.80.152.201
		System.out.println("Test geoinfo of " + IPstring);
		try {
			String geostring;
			//TODO do something with geostring
			
			// URL object creation
			String myurlstring = "http://ip-api.com/csv/" + IPstring;
            URL myurl = new URL(myurlstring);
            URLConnection yc = myurl.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;

            // sleep 0.5 s
            Thread.sleep(500);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
                 
            // Discard all stuffs returned from server
            while ((inputLine = in.readLine()) != null)
				ps.print(inputLine);
            
            String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            
            // String[] contentArray = content.split("\\,");
               
            content  = IPstring + " " + content.substring(8);
            
			return content;
		} catch (IOException e) {
			// Auto-generated catch block
			System.out.println("IP " + IPstring + " is not reachable.");
			// e.printStackTrace();
			return "NONE";
		}
	}

}
