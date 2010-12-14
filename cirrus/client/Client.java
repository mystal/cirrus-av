package cirrus.client;

import cirrus.common.Constants;
import cirrus.common.Time;

import java.util.ArrayList;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class Client {

	public static void getFilesFromDirectory(File dir, ArrayList<File> files) {
		File[] dirFiles = dir.listFiles();
		
		for (File file : dirFiles) {
			if (!file.isDirectory()) {
				files.add(file);
			} else {
				getFilesFromDirectory(file, files);
			}
		}
	}
	
	public static void downloadUrlFromServer(DataInputStream downloadStream, BufferedReader inFromServer, 
			byte[] buffer, ArrayList<Time> io, ArrayList<Time> comm) throws Exception {
		
		long commStart;
		long commEnd;
		long ioStart;
		long ioEnd;
	    
	    commStart = System.currentTimeMillis();
      	String fileName = inFromServer.readLine();
       	long length = downloadStream.readLong();
   		commEnd = System.currentTimeMillis();
     		
   		comm.add(new Time(commStart, commEnd));
           	
    	FileOutputStream output = new FileOutputStream(Constants.DOWNLOADS_FOLDER + fileName);
				
		int read = 0;
		int offset = 0;
				
		while (offset < length) {
			commStart = System.currentTimeMillis();
			read = downloadStream.read(buffer, 0, 65536);
			commEnd = ioStart = System.currentTimeMillis();
			output.write(buffer, 0, read);
			ioEnd = System.currentTimeMillis();
			
			io.add(new Time(ioStart, ioEnd));
			comm.add(new Time(commStart, commEnd));

			offset += read;
		}
		output.close();
	}

	/**
	 * Usage: Client [-h hostIpAddress] file1 file2 file3...
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Time total = new Time();
		ArrayList<Time> io = new ArrayList<Time>();
		ArrayList<Time> comm = new ArrayList<Time>();
		
		long ioStart;
		long ioEnd;
		long commStart;
		long commEnd;

		total.start = System.currentTimeMillis();

		//Parse args
		ArrayList<File> files = new ArrayList<File>(args.length);
		ArrayList<File> infected = new ArrayList<File>();
		ArrayList<String> urls = new ArrayList<String>(args.length);
		String host = "";
		
		for (int i = 0; i < args.length; i++) {
			if ((args[i].equals("-h") || args[i].equals("-H")) && i + 1 < args.length) {
				if (!host.equals("")) {
					System.out.println("ERROR: Usage: can only specify 1 host");
					System.exit(1);
				}
				host = args[i+1];
				i++;
			} else if ((args[i].equals("-u") || args[i].equals("-U")) && i + 1 < args.length) {
				URL test = new URL(args[i+1]);
				urls.add(args[i+1]);
				i++;
			} else if ((args[i].equals("-d") || args[i].equals("-D")) && i + 1 < args.length) {
				File dir = new File(args[i+1]);
				if (!dir.isDirectory()) {
					System.out.println("ERROR: " + args[i+1] + " is not a directory.");
					System.exit(1);
				}
				
				getFilesFromDirectory(dir, files);
				
				i++;
			} else {
				try {
					files.add(new File(args[i]));
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error opening file: " + args[i]);
					System.exit(1);
				}
			}
		}
		
		if (host.equals(""))
			host = "localhost";
		
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket clientSocket = (SSLSocket)factory.createSocket(host, 6789);
		
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		byte[] buffer = new byte[65536];
		int offset;
		int read;
		
		outToServer.writeInt(files.size() + urls.size());
		//Send files
		for (int i = 0; i < files.size(); i++) {
			System.out.println("Sending file: " + files.get(i).getName());
			long length = files.get(i).length();
			
			BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(files.get(i)));
			
			commStart = System.currentTimeMillis();
			outToServer.writeBytes(Constants.FILE + "\n");
			outToServer.writeBytes(files.get(i).getName() + "\n");
			outToServer.writeLong(length);
			commEnd = System.currentTimeMillis();
			comm.add(new Time(commStart, commEnd));
			
			offset = 0;
			read = 0;
			
			while(offset < length) {
				ioStart = System.currentTimeMillis();
				read = inFile.read(buffer, 0, 65536);
				ioEnd = commStart = System.currentTimeMillis();
				outToServer.write(buffer, 0, read);
				commEnd = System.currentTimeMillis();
				
				io.add(new Time(ioStart, ioEnd));
				comm.add(new Time(commStart, commEnd));

				offset += read;
			}
		}
		
		//Send urls
		for (int i = 0; i < urls.size(); i++) {
			System.out.println("Sending URL: " + urls.get(i));
			
			commStart = System.currentTimeMillis();
			outToServer.writeBytes(Constants.URL + "\n");
			outToServer.writeBytes(urls.get(i) + "\n");
            commEnd = System.currentTimeMillis();
            comm.add(new Time(commStart, commEnd));
		}
		
		//Get Results from Server
		for (int i = 0; i < files.size(); i++) {
			String flag = inFromServer.readLine();
			if (Boolean.parseBoolean(flag))
				infected.add(files.get(i));
		}
		
		int urlsToDownload = 0;
		for (int i = 0; i < urls.size(); i++) {
			String flag = inFromServer.readLine();
			if (!Boolean.parseBoolean(flag))
				urlsToDownload++;
		}

		//Download clean URLs		
		DataInputStream downloadStream = new DataInputStream(clientSocket.getInputStream());
		
		for (int i = 0; i < urlsToDownload; i++) {
			downloadUrlFromServer(downloadStream, inFromServer, buffer, io, comm);
		}
		
		clientSocket.close();
		
		total.end = System.currentTimeMillis();
		
		if (infected.isEmpty())
			System.out.println("\n\nScan complete. No infections found.\n");
		else {
			String infections = " infections found.";
			if (infected.size() == 1)
				infections = " infection found.";
				
			System.out.println("\n\nScan complete. " + infected.size() + infections);
			for (int i = 0; i < infected.size(); i++) {
				System.out.println("\t" + infected.get(i).getName());
			}
			System.out.print("Delete infected files (y/n)?  ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input = reader.readLine();
			
			if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
				System.out.println("Deleting Files...");

				//Delete Files
				for (int i = 0; i < infected.size(); i++) {
					if (!infected.get(i).delete())
						System.out.println("\tUnable to delete " + infected.get(i).getName());
				}
				
				System.out.println("Done!\n");
			} else if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
				System.out.println("Leaving infected files.\n");
			} else {
				System.out.println("Error reading input. Leaving infected files.\n");
			}
		}
		
		long ioTotal = 0;
		for (Time time : io) {
			ioTotal += time.getTotal();
		}
		
		long commTotal = 0;
		for (Time time : comm) {
			commTotal += time.getTotal();
		}
		
		System.out.println("\nStatistics:");
		System.out.println("Total time:\t" + (total.getTotal() / 1000.0) + " s");
		System.out.println("Computation:\t" +  ((total.getTotal() - ioTotal - commTotal) / 1000.0) + " s");
		System.out.println("IO:\t\t" + (ioTotal / 1000.0) + " s");
		System.out.println("Communication:\t" + (commTotal / 1000.0) + " s");
		
	}

}
