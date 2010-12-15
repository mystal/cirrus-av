package cirrus.server;

import cirrus.server.AntiVirus;
import cirrus.server.Server;

import java.lang.Runnable;

public class Scanner implements Runnable {
	
	private AntiVirus av;
	private int index;
	private String fileToScan;
	
	public Scanner(AntiVirus av, int index, String filename) {
		this.av = av;
		this.index = index;
		this.fileToScan = filename;
	}
	
	public void run() {
		long start = System.currentTimeMillis();
		boolean infected = av.scan(fileToScan);
		long end = System.currentTimeMillis();
		
		Server.addScanTime(start, end);
		Server.setResult(index, infected);
	}
}
