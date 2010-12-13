package cirrus.server;

import cirrus.common.Constants;
import cirrus.server.AntiVirus;
import cirrus.server.ClamAV;
import cirrus.server.Flagger;
import cirrus.common.Time;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.ArrayList;

public class Server {

	public static String downloadFile(String fileName, DataInputStream inFromClient, 
			ArrayList<Time> io, ArrayList<Time> comm) throws Exception {
		FileOutputStream output = new FileOutputStream(Constants.OUTPUT_FOLDER + fileName);
				
		long length = inFromClient.readLong();
		
		byte[] buffer = new byte[65536];
		int read = 0;
		int offset = 0;
		
		long ioStart;
		long ioEnd;
		long commStart;
		long commEnd;
		
		while (offset < length) {
			commStart = System.currentTimeMillis();
			read = inFromClient.read(buffer, 0, 65536);
			commEnd = ioStart = System.currentTimeMillis();
			output.write(buffer, 0, read);
			ioEnd = System.currentTimeMillis();
			
			comm.add(new Time(commStart, commEnd));
			io.add(new Time(ioStart, ioEnd));
			
			offset += read;
		}
		output.close();
		return Constants.OUTPUT_FOLDER + fileName;
	}

	public static String downloadURL(String urlPath, ArrayList<Time> io, 
			ArrayList<Time> comm) throws Exception {
		URL url = new URL(urlPath);
		
		long ioStart;
		long ioEnd;
		long commStart;
		long commEnd;
		
		BufferedInputStream inUrl = new BufferedInputStream(url.openStream());
		String fileName = Constants.getFileNameFromUrl(url.getFile());
		
		FileOutputStream output = new FileOutputStream(Constants.OUTPUT_FOLDER + fileName);
		byte[] buffer = new byte[65536];
		int read = 1;
		while (read > 0) {
			commStart = System.currentTimeMillis();
			read = inUrl.read(buffer, 0, 65536);
			commEnd = ioStart = System.currentTimeMillis();
			if (read > 0)
				output.write(buffer, 0, read);
			ioEnd = System.currentTimeMillis();
			
			comm.add(new Time(commStart, commEnd));
			io.add(new Time(ioStart, ioEnd));
		}
		
		output.close();
		inUrl.close();
		
		return Constants.OUTPUT_FOLDER + fileName;
	}
	
	public static void uploadToClient(String filePath, DataOutputStream outToClient, 
			ArrayList<Time> io, ArrayList<Time> comm) throws Exception {
		File file = new File(filePath);
		long length = file.length();
		
		long ioStart;
		long ioEnd;
		long commStart;
		long commEnd;
			
		BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(file));
		
		commStart = System.currentTimeMillis();
		outToClient.writeBytes(file.getName() + "\n");
		outToClient.writeLong(length);
		commEnd = System.currentTimeMillis();
		comm.add(new Time(commStart, commEnd));
		
		byte[] buffer = new byte[65536];
		int offset = 0;
		int read = 0;
		
		while(offset < length) {
			ioStart = System.currentTimeMillis();
			read = inFile.read(buffer, 0, 65536);
			ioEnd = commStart = System.currentTimeMillis();
			outToClient.write(buffer, 0, read);
			commEnd = System.currentTimeMillis();
			
			comm.add(new Time(commStart, commEnd));
			io.add(new Time(ioStart, ioEnd));
			
			offset += read;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        Time total = new Time();
        ArrayList<Time> io = new ArrayList<Time>();
        ArrayList<Time> comm = new ArrayList<Time>();
        ArrayList<Time> scan = new ArrayList<Time>();
        
        long ioStart;
        long ioEnd;
        long commStart;
        long commEnd;
        long scanStart;
        long scanEnd;

        boolean clamAV = false;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--av")) {
                clamAV = true;
			}
        }

        AntiVirus av;
        if (clamAV)
            av = new ClamAV();
        else
            av = new Flagger();

		SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		SSLServerSocket welcomeSocket = (SSLServerSocket)factory.createServerSocket(6789);
		
		while(true) {
			SSLSocket connectionSocket = (SSLSocket)welcomeSocket.accept();
			total.start = System.currentTimeMillis();
			
			DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
			
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			int numFilesToReceive = inFromClient.readInt();
			for (int i = 0; i < numFilesToReceive; i++) {
				BufferedReader line = new BufferedReader(new InputStreamReader(inFromClient));
				commStart = System.currentTimeMillis();
				//Get Type
				String type = line.readLine();
				//Get File Name/URL
				String name = line.readLine();
				commEnd = System.currentTimeMillis();
				comm.add(new Time(commStart, commEnd));

				//Get File / URL File
				if (type.equalsIgnoreCase(Constants.FILE)) {
					System.out.println("Receiving File: " + name);
					name = downloadFile(name, inFromClient, io, comm);
					scanStart = System.currentTimeMillis();
					boolean infected = av.scan(name);
					scanEnd = System.currentTimeMillis();
					scan.add(new Time(scanStart, scanEnd));
					commStart = System.currentTimeMillis();
					outToClient.writeBytes(infected + "\n");
					commEnd = System.currentTimeMillis();
					comm.add(new Time(commStart, commEnd));
				} else if (type.equalsIgnoreCase(Constants.URL)) {
					System.out.println("Receiving URL to check: " + name);
					name = downloadURL(name, io, comm);
					scanStart = System.currentTimeMillis();
					
					boolean infected = av.scan(name);
					
					scanEnd = System.currentTimeMillis();
					scan.add(new Time(scanStart, scanEnd));
					commStart = System.currentTimeMillis();
					outToClient.writeBytes(infected + "\n");
					commEnd = System.currentTimeMillis();
					comm.add(new Time(commStart, commEnd));
					
					//Send uninfected files to Client
					if (!infected) {
						uploadToClient(name, outToClient, io, comm);
						System.out.println("Sending URL to client.");
					}
				}
			}
			
			total.end = System.currentTimeMillis();
			
			long ioTotal = 0;
			for (Time time : io) {
				ioTotal += time.getTotal();
			}
			
			long commTotal = 0;
			for (Time time : comm) {
				commTotal += time.getTotal();
			}
			
			long scanTotal = 0;
			for (Time time : scan) {
				scanTotal += time.getTotal();
			}
			
			System.out.println("\nStatistics:");
			System.out.println("Total time:\t" + (total.getTotal() / 1000.0) + " s");
			System.out.println("Computation:\t" + ((total.getTotal() - ioTotal - commTotal) / 1000.0) + " s");
			System.out.println("IO:\t\t" + (ioTotal / 1000.0) + " s");
			System.out.println("Communication:\t" + (commTotal / 1000.0) + " s");
			System.out.println("Scanning:\t" + (scanTotal / 1000.0) + " s");
		}
	}

}
