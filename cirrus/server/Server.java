package cirrus.server;

import cirrus.server.AntiVirus;
import cirrus.server.Flagger;
import cirrus.Constants;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class Server {

	public static String downloadFile(String fileName, DataInputStream inFromClient) 
			throws Exception {
		FileOutputStream output = new FileOutputStream(Constants.OUTPUT_FOLDER + fileName);
				
		long length = inFromClient.readLong();
		
		byte[] buffer = new byte[65536];
		int read = 0;
		int offset = 0;
		
		while (offset < length) {
			read = inFromClient.read(buffer, 0, 65536);
			output.write(buffer, 0, read);
			
			offset += read;
		}
		output.close();
		return Constants.OUTPUT_FOLDER + fileName;
	}

	public static String downloadURL(String urlPath) throws Exception {
		URL url = new URL(urlPath);
		
		BufferedInputStream inUrl = new BufferedInputStream(url.openStream());
		String fileName = Constants.getFileNameFromUrl(url.getFile());
		
		FileOutputStream output = new FileOutputStream(Constants.OUTPUT_FOLDER + fileName);
		byte[] buffer = new byte[65536];
		int read = 1;
		while (read > 0) {
			read = inUrl.read(buffer, 0, 65536);
			if (read > 0)
				output.write(buffer, 0, read);
		}
		
		output.close();
		inUrl.close();
		
		//return "./" + fileName;
		return Constants.OUTPUT_FOLDER + fileName;
	}
	
	public static void uploadToClient(String filePath, DataOutputStream outToClient) throws Exception {
		File file = new File(filePath);
		long length = file.length();
			
		BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(file));
		
		outToClient.writeBytes(file.getName() + "\n");
		outToClient.writeLong(length);
		
		byte[] buffer = new byte[65536];
		int offset = 0;
		int read = 0;
		
		while(offset < length) {
			read = inFile.read(buffer, 0, 65536);
			outToClient.write(buffer, 0, read);
			
			offset += read;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        //TODO process arguments
        //TODO create new Server class instance, which will create listeners...

        AntiVirus f = new Flagger();

		SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		SSLServerSocket welcomeSocket = (SSLServerSocket)factory.createServerSocket(6789);
		
		while(true) {
			SSLSocket connectionSocket = (SSLSocket)welcomeSocket.accept();
			
			DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
			
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			int numFilesToReceive = inFromClient.readInt();
			for (int i = 0; i < numFilesToReceive; i++) {
				BufferedReader line = new BufferedReader(new InputStreamReader(inFromClient));
				//Get Type
				String type = line.readLine();
				//Get File Name/URL
				String name = line.readLine();

				//Get File / URL File
				if (type.equalsIgnoreCase(Constants.FILE)) {
					downloadFile(name, inFromClient);
					outToClient.writeBytes(f.scan(name) + "\n");
				} else if (type.equalsIgnoreCase(Constants.URL)) {
					name = downloadURL(name);
					boolean infected = f.scan(name);
					outToClient.writeBytes(infected + "\n");
					
					//Send uninfected files to Client
					if (!infected)
						uploadToClient(name, outToClient);
				}
			}
		}
	}

}
