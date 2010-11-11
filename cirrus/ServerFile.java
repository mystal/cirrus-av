package cirrus;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

import cirrus.Flagger;

public class ServerFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        Flagger f = new Flagger();

		SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		SSLServerSocket welcomeSocket = (SSLServerSocket)factory.createServerSocket(6789);
		
		while(true) {
			SSLSocket connectionSocket = (SSLSocket)welcomeSocket.accept();
			
			DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
			
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			int numFilesToReceive = inFromClient.readInt();
			for (int i = 0; i < numFilesToReceive; i++) {
				BufferedReader line = new BufferedReader(new InputStreamReader(inFromClient));
				String name = line.readLine();

				FileOutputStream output = new FileOutputStream("./" + name);
				
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
				
				outToClient.writeBytes("Read " + offset + " bytes.\n");

                outToClient.writeBytes(f.flag(name) + "\n");
			}
			
			outToClient.writeBytes("Read " + numFilesToReceive + " files.\n");
			
		}
	}

}
