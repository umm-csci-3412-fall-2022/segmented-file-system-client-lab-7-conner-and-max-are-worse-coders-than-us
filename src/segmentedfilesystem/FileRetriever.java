package segmentedfilesystem;
import java.util.*;

public class FileRetriever {
	String server;
	int port;
	int headCount = 0;

	public FileRetriever(String server, int port) {
        // Save the server and port for use in `downloadFiles()`
        //...
		this.server = server;
		this.port = port;
	}

	public void downloadFiles() {
        // Do all the heavy lifting here.
        // This should
        //   * Connect to the server
        //   * Download packets in some sort of loop
        //   * Handle the packets as they come in by, e.g.,
        //     handing them to some PacketManager class
        // Your loop will need to be able to ask someone
        // if you've received all the packets, and can thus
        // terminate. You might have a method like
        // PacketManager.allPacketsReceived() that you could
        // call for that, but there are a bunch of possible
        // ways.
	
	// initialize structs to hold packets 
	HeaderPacket[] headPacks = new HeaderPacket[3];
	ArrayList<DataPacket> datPacks = new ArrayList<DataPacket>();
	ArrayList<ArrayList<Packet>> finalPacks = new ArrayList<ArrayList<Packet>>();
	// connect to server
	DatagramSocket socker = new DatagramSocket(this.port);
	// receive data 
	boolean dl = true;
	while (dl){
		byte[] buf = new byte[256];
		DatagramPacket packer = new DatagramPacket(buf, buf.length);
		socker.receive(packer);
		createPacket(packer.getData());
		}
	}

	public void createPacket(byte[] stuff){
		if (stuff[0]%2 == 0){
			String filename = "";
			for(int i = 2; i < stuff.length; i++) {
				if (stuff[i] != null){
					filename += stuff[i];
				}
			}
			HeaderPacket hp = new HeaderPacket(filename, stuff[1]);
			headPacks[headCount++] = hp;
		}
		else {
			String data = "";
                                for(int i = 4; i < stuff.length; i++){
                                        data += stuff[i];
                                }
			String fileNumber = stuff[2] + stuff[3];
			int fn = (Integer)fileNumber;
			if (stuff[0]%4 == 3) {
				DataPacket dp = new DataPacket(data, true, stuff[1], fn);
			}
			else { 
				DataPacket dp = new DataPacket(data, false, stuff[1], fn);
			}
		}

	}

}
