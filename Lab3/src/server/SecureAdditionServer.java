
// An example class that uses the secure server socket class
package server;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;


public class SecureAdditionServer {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "/Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/server/LIUkeystore.ks";
	static final String TRUSTSTORE = "/Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/server/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
	
	/** Constructor
	 * @param port The port where the server
	 *    will listen for requests
	 */
	SecureAdditionServer( int port ) {
		this.port = port;
	}
	
	/** The method that does the work for the class */
	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket( port );
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );
			
			System.out.println("\n>>>> SecureAdditionServer: active ");
			SSLSocket incoming = (SSLSocket)sss.accept();

			DataInputStream socketIn = new DataInputStream(incoming.getInputStream());
            DataOutputStream socketOut = new DataOutputStream(incoming.getOutputStream());

            int option = socketIn.readInt();
			System.out.println("Option: " + option);

			switch (option) {
				case 1:
					fileDownload(socketIn, socketOut);
					break;
				case 2:
					System.out.println("hej");
					fileUpload(socketIn);
					break;
				case 3:
					fileDeletion(socketIn);
					break;


			}
			
			//close socket
			incoming.close();
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	
	private void fileDeletion(DataInputStream socketIn) throws IOException{
		//delete file on server
		String fileName = socketIn.readUTF();
		File f = new File("Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/server" + fileName);
		f.delete();
	}

	private void fileUpload(DataInputStream socketIn) throws IOException{
		//read filename from client and create file
		String fileName = socketIn.readUTF();
		File f = new File("Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/server" + fileName);

		FileOutputStream fileOutputStream = new FileOutputStream("Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/server" + fileName);

		//read filelength and create new empty byte[]
		int fileLength = socketIn.readInt();
		byte[] fileData = new byte[fileLength];

		//read data from client and write to file on server
		socketIn.read(fileData);
		fileOutputStream.write(fileData);

		fileOutputStream.close();
	}

	private void fileDownload(DataInputStream socketIn, DataOutputStream socketOut) throws IOException{
		//find client desired file and read data
		String fileName = socketIn.readUTF();
		FileInputStream fileInputStream = new FileInputStream("Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/server" + fileName);
		byte[] fileData = fileInputStream.readAllBytes();
		fileInputStream.close();

		//write file to client
		socketOut.writeInt(fileData.length);
		socketOut.write(fileData);
	}

	/** The test method for the class
	 * @param args[0] Optional port number in place of
	 *        the default
	 */
	public static void main( String[] args ) {
		int port = DEFAULT_PORT;
		if (args.length > 0 ) {
			port = Integer.parseInt( args[0] );
		}
		SecureAdditionServer addServe = new SecureAdditionServer( port );
		addServe.run();
	}
}