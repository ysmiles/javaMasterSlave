package masterslave;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class testsocket {
	static List<slave> targets = new ArrayList<slave>();
	
	public static void main(String argv[]) throws Exception{
		Socket targetsock = new Socket("localhost", 6000);

		String connectiondate = "2000-00-00";

		targets.add(null);
		System.out.println("null added"+targets.size());
		
		System.out.println(targets.get(0).getName());
		System.out.println(targets.get(0).getIPaddr());
	}

	
}

/*if (removelist.size() > 0) {
	System.out.println("Disconnection stated.");
	// delete in a reversed order
	for (int j = removelist.size() - 1; j >= 0; j--) {
		terminate(removelist.get(j));
	}
	System.out.println("Removing finished.");
}
*/