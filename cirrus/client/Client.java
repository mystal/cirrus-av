package cirrus.client;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class Client {

	/**
	 * Usage: Client [-h hostIpAddress] file1 file2 file3...
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//Parse args
		File[] file = new File[args.length];
		int numFilesToSend = 0;
		String host = "localhost";
		
		for (int i = 0; i < args.length; i++) {
			if ((args[i].equals("-h") || args[i].equals("-H")) && i + 1 < args.length) {
				host = args[i+1];
				i++;
			} else {
				file[numFilesToSend] = new File(args[i]);
				numFilesToSend++;
			}
		}
		
		if (numFilesToSend <= 0) {
			file = new File[1];
			file[0] = new File("../cirrus/Client.java");
			numFilesToSend = 1;
		}
		
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket clientSocket = (SSLSocket)factory.createSocket(host, 6789);
		
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		byte[] buffer = new byte[65536];
		int offset;
		int read;
		
		outToServer.writeInt(numFilesToSend);
		for (int i = 0; i < numFilesToSend; i++) {
			long length = file[i].length();
			
			BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(file[i]));
			
			outToServer.writeBytes(file[i].getName() + "\n");
			outToServer.writeLong(length);
			
			offset = 0;
			read = 0;
			
			while(offset < length) {
				read = inFile.read(buffer, 0, 65536);
				outToServer.write(buffer, 0, read);
				
				offset += read;
			}
			
			String transmission = inFromServer.readLine();
            String flag = inFromServer.readLine();
			
			System.out.println("FROM SERVER: " + transmission);
            System.out.println("Virus? " + flag);
		}		
		clientSocket.close();
	}

}
