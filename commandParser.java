package masterslave;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class commandParser {
	private boolean valid;

	private String slaveIdentifier; // can be hostname, IP, or all
	private String slaveIdentifierType;

	// private boolean connect; // connect or not
	// In version 3.0. change it to command type.
	/*
	 * 1. list 2. connect 3. disconnect 4. ipscan 5. tcpportscan
	 */
	private int commandType;
	private static String[] types = { "list", "connect", "disconnect", "ipscan", "tcpportscan" };

	private String targetIdentifier; // can be hostname or IP
	private String targetIdentifierType;

	private int slaveport; // -1 means don't have port number in slaveIdentifier
	private int targetport;
	private int repeattimes; // set 0 at disconnect

	// keepalive or url=/#q=
	private String option;

	// scan feature
	private int targetport2; // used for tcpportscan

	List<Integer> ip1;
	List<Integer> ip2;

	// private static final String regex =
	// "(dis)?connect\\((.+)\\)\\((.+)\\)(\\d+)?\\[(\\d+)?\\]";
	private static final String regex = "(dis)?connect\\s([^\\s]+)\\s([^\\s]+)\\s?(\\d+)?\\s?(\\d+)?\\s?(keepalive|url=[^\\s]*)?";
	private static final String IPregex = "((\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)):?(\\d+)?";
	private static final String scanregex = "(ip|tcpport)?scan\\s([^\\s]+)\\s([^\\s]+)\\s?([^\\s]+)?";
	private static final String rangeregex = "([^\\s]+)-([^\\s]+)?";

	commandParser(String comm) {
		Parser(comm);
	}

	public void Parser(String com) {
		if (com.equals("list")) {
			commandType = 1;
			valid = true;
			return;
		}

		Pattern patt = Pattern.compile(regex);
		Matcher match = patt.matcher(com);
		valid = match.matches();

		if (!valid) {
			patt = Pattern.compile(scanregex);
			match = patt.matcher(com);
			valid = match.matches();
			if (!valid) {
				System.out.println("Not valid command format.");
				return;
			}
		}

		if (match.group(1) == null) {
			commandType = 2;
		} else if (match.group(1).equals("dis")) {
			commandType = 3;
		} else if (match.group(1).equals("ip")) {
			commandType = 4;
		} else if (match.group(1).equals("tcpport")) {
			commandType = 5;
		}

		slaveIdentifier = match.group(2);
		targetIdentifier = match.group(3);

		if (commandType < 4) {
			option = match.group(6);
		}

		if (commandType == 2) {
			if (match.group(4) != null) {
				targetport = Integer.parseInt(match.group(4));
			} else {
				valid = false;
				System.out.println("Not valid target port.");
				return;
			}

			if (match.group(5) != null) {
				repeattimes = Integer.parseInt(match.group(5));
			} else {
				repeattimes = 1; // default for connection case
			}

		} else if (commandType == 3) {
			if (match.group(5) != null) {
				valid = false;
				System.out.println("Not valid. Too much arguments for disconnect.");
				return;
			}
			if (match.group(4) != null) {
				targetport = Integer.parseInt(match.group(4));
			} else {
				// means all, default for disconnection case
				targetport = -1;
			}
			repeattimes = 0; // disconnect default
		} else if (commandType == 4) {
			Pattern pat = Pattern.compile(rangeregex);
			Matcher mat = pat.matcher(targetIdentifier);
			valid = mat.matches();
			if (valid) {
				//System.out.println(mat.group(1));
				//System.out.println(mat.group(2));
				ip1 = getIPints(mat.group(1));
				ip2 = getIPints(mat.group(2));
			} else {
				System.out.println("Not valid ip range.");
				return;
			}
		} else if (commandType == 5) {
			Pattern pat = Pattern.compile(rangeregex);
			Matcher mat = pat.matcher(match.group(4));
			valid = mat.matches();
			if (valid) {
				targetport = Integer.parseInt(mat.group(1));
				targetport2 = Integer.parseInt(mat.group(2));

			} else {
				System.out.println("Not valid target port range.");
				return;
			}
		}

		// Output command
		System.out.println("Command valid.");
		System.out.println("Command type: " + commandType + " " + types[commandType - 1]);
		System.out.println("slaveIdentifier: " + slaveIdentifier);
		System.out.println("targetIdentifier: " + targetIdentifier);
		if (commandType < 4) {
			System.out.println("targetport (-1 means all ports (disconnection)): " + targetport);
			System.out.println("repeattimes (0 means disconnection): " + repeattimes);
			System.out.println("Additional option: " + option);
		}
		// else if (commandType == 4) {
		// same as before
		// System.out.println("targetIdentifier: " + targetIdentifier);
		// }
		else if (commandType == 5) {
			System.out.println("targetport range: " + targetport + "-" + targetport2);
		}

		// for connect and disconnect
		if (commandType < 4) {
			if (isValidIP(slaveIdentifier)) {
				slaveIdentifierType = "IP";
				slaveport = ifHavePort(slaveIdentifier);
				slaveIdentifier = getPureIP(slaveIdentifier);
			} else {
				slaveIdentifierType = "hostname";
				slaveport = -1;
			}

			if (isValidIP(targetIdentifier)) {
				targetIdentifierType = "IP";
				if (ifHavePort(targetIdentifier) != -1) {
					System.out.println("Target will use the port specified in parenthesis.");
					targetport = ifHavePort(targetIdentifier);
				}
				// for safe handling something like "1.2.3.4:"
				targetIdentifier = getPureIP(targetIdentifier);
			} else {
				targetIdentifierType = "hostname";
			}
		}
	}

	public int getSlaveport() {
		return slaveport;
	}

	public boolean isValidIP(String identifier) {
		Pattern pat = Pattern.compile(IPregex);
		Matcher mat = pat.matcher(identifier);
		return mat.matches() && Integer.parseInt(mat.group(2)) < 255 && Integer.parseInt(mat.group(3)) < 255
				&& Integer.parseInt(mat.group(4)) < 255 && Integer.parseInt(mat.group(5)) < 255;
	}

	public List<Integer> getIPints(String identifier) {
		List<Integer> ipv4 = new ArrayList<Integer>();
		if (isValidIP(identifier)) {
			Pattern pat = Pattern.compile(IPregex);
			Matcher mat = pat.matcher(identifier);
			mat.matches();
			ipv4.add(Integer.parseInt(mat.group(2)));
			ipv4.add(Integer.parseInt(mat.group(3)));
			ipv4.add(Integer.parseInt(mat.group(4)));
			ipv4.add(Integer.parseInt(mat.group(5)));
		} else {
			valid = false;
			System.out.println("IP not valid");
		}
		return ipv4;
	}

	public int ifHavePort(String identifier) {
		Pattern pat = Pattern.compile(IPregex);
		Matcher mat = pat.matcher(identifier);
		mat.matches();
		if (mat.group(6) != null) {
			return Integer.parseInt(mat.group(6));
		}
		return -1; // not valid port number
	}

	public String getPureIP(String identifier) {
		Pattern pat = Pattern.compile(IPregex);
		Matcher mat = pat.matcher(identifier);
		mat.matches();
		if (mat.group(6) != null) {
			return mat.group(1);
		}
		return identifier; // not valid port number
	}

	public boolean isValid() {
		return valid;
	}

	public String getSlaveIdentifier() {
		return slaveIdentifier;
	}

	public String getSlaveIdentifierType() {
		return slaveIdentifierType;
	}

	public String getTargetIdentifier() {
		return targetIdentifier;
	}

	public String getTargetIdentifierType() {
		return targetIdentifierType;
	}

	public int getCommandType() {
		return commandType;
	}

	public int getTargetport() {
		return targetport;
	}

	public int getRepeattimes() {
		return repeattimes;
	}

	public String getOption() {
		return option;
	}

	public int getTargetport2() {
		return targetport2;
	}

	public List<Integer> getIp1() {
		return ip1;
	}

	public List<Integer> getIp2() {
		return ip2;
	}

	public void correctUse() {
		System.out.println("Correct use:");
		System.out.println("1. list");
		System.out.println("2. connect(IPAddressOrHostNameOfYourSlave|all)"
				+ "(TargetHostName|IPAddress)TargetPortNumber[NumberOfConnections: 1 if not specified]"
				+ "(keepalive|url=)");
		System.out.println("3. disconnect(IPAddressOrHostNameOfYourSlave|all)"
				+ "(TargetHostName|IPAddress)[TargetPort:all if no port specified]");
		System.out.println("4. ipscan(IPAddressOrHostNameOfYourSlave|all)(IPAddressRage)");
		System.out.println("5. tcpportscan(IPAddressOrHostNameOfYourSlave|all)(TargetHostName|IPAddress)"
				+ "TargetPortNumberRage");
	}

}
