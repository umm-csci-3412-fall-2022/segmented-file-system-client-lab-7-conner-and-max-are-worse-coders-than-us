package segmentedfilesystem;

import java.util.*;
import java.net.*;
import java.lang.*;


public class Main {
    
    // If there's one command line argument, it is assumed to
    // be the server. If there are two, the second is assumed
    // to be the port to use.
    public static void main(String[] args) {
        String server = "localhost";
        // CHANGE THIS DEFAULT PORT TO THE PORT NUMBER PROVIDED
        // BY THE INSTRUCTOR.
        int port = 6014;
        
        if (args.length >= 1) {
            server = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        FileRetriever fileRetriever = new FileRetriever(server, port);
        fileRetriever.downloadFiles();
    }

}

@SuppressWarnings({"deprecation","removal"})
class FileRetriever {
	String server;
	int port;
	int headCount = 0;
	int lastCount = 0;
	int totalPackets = 0;
	int packetCount = 0;
	HeaderPacket[] headPacks = new HeaderPacket[3];

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
		ArrayList<DataPacket> datPacks = new ArrayList<DataPacket>();
		ArrayList<ArrayList<Packet>> finalPacks = new ArrayList<ArrayList<Packet>>();
		// connect to server
		try {
			DatagramSocket socker = new DatagramSocket(this.port);
			// receive data
			boolean dl = true;
			while (dl){
				byte[] buf = new byte[1028];
				DatagramPacket packer = new DatagramPacket(buf, buf.length);
				socker.receive(packer);
				createPacket(packer.getData());
				if (lastCount == 3 && headCount == 3)
					if (packetCount == totalPackets){
						dl = false;
					} 
				}
			buildFiles();
		} catch (Exception ex){
			System.err.println("You're so dumb you dumb fool you got it wrong " + ex);
		}
	}


	public void createPacket(byte[] stuff){
		if (stuff[0]%2 == 0){
			String filename = "";
			for(int i = 2; i < stuff.length; i++) {
				if (Byte.toString(stuff[i]).equals("")){
					filename += stuff[i];
				}
			}
			HeaderPacket hp = new HeaderPacket(filename, stuff[1]);
			headPacks[headCount++] = hp;
		} else {
			String data = "";
                                for(int i = 4; i < stuff.length; i++){
                                        data += stuff[i];
                                }
			Byte mostSig = new Byte(stuff[2]);
			Byte leastSig = new Byte(stuff[3]);
			int packNum = (256 * mostSig.intValue()) + leastSig.intValue();
			if (stuff[0]%4 == 3) {
				DataPacket dp = new DataPacket(data, true, stuff[1], packNum);
				datPacks.add(dp);
				lastCount++;
				totalPackets += packNum;
				packetCounter++;
				
			} else { 
				DataPacket dp = new DataPacket(data, false, stuff[1], packNum);
				datPacks.add(dp);
				packetCounter++;
			}
		}
	}

	public void buildFiles(){
		for (int i = 0; i < 3; i++){
			finalPacks.get(i).add(headPacks[i]);
			int ID = headPacks[i].fileID();
			for (int ii = 0; ii < datPacks.size(); ii++){
				if (dataPacks.get(ii).fileID() == ID){
					finalPacks.get(i).add(datPacks.get(ii));
				}
			}
		}
		for (int i = 0; i < 3; i++){
			ArrayList<Packet> currentList = finalPacks.get(i);
			for(int ii = 0; ii < currentList.size(); ii++) {
				int key = currentList.get(ii).getPacketNum();
				int j = ii - 1;
				while (j >= 0 && currentList.get(j).getPacketNum() > key) {
					currentList.get(j + 1).getPacketNum() = currentList.get(j).getPacketNum();
					j -= 1;
				}
				currentList.get(j+1).getPacketNum = key;
			}
		}
		for (int i = 0; i < 3; i++){
			File file = new File(finalPacks.get(i).get(0).getFileName());
			// Try block to check for exceptions
        		try {
				// Initialize a pointer in file
		            	// using OutputStream
            			OutputStream os = new FileOutputStream(file);
 				byte[] bytes = finalPacks
			        // Starting writing the bytes in it
            			os.write(bytes);
 
            			// Display message onconsole for successful
            			// execution
            			System.out.println("Successfully byte inserted");
 
		            	// Close the file connections
            			os.close();
        		}
 
        		// Catch block to handle the exceptions
        		catch (Exception e) {
 
            			// Display exception on console
            			System.out.println("Exception: " + e);
        		}
		}
	}
}

abstract class Packet{
         int fileID; 
         public int fileID(){
                 return this.fileID;
         }
 
}

class DataPacket extends Packet{
           int fileID;
           String data;
           boolean isLastPacket;
           int packetNum;
   
          public DataPacket(String data, boolean ilp, int fileID, int pn){
                  this.data = data;
                  this.isLastPacket = ilp;
                  this.fileID = fileID;
                  this.packetNum = pn;
          }
  		
	  public int getPacketNum(){return this.packetNum;}
          public String getData(){return this.data;}
  	  public int getFileID(){return this.fileID;}
          public boolean lastPacket(){return this.isLastPacket;}
}

class HeaderPacket extends Packet{
           String fn;
           int fileID;
   
           public HeaderPacket(String filename, int fileID){
                   this.fn = filename;
                   this.fileID = fileID;
           }
  
           public String getFileName(){
		   return this.fn;
           }	
}

