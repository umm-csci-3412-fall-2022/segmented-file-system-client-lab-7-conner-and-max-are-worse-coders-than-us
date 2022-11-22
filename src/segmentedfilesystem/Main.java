package segmentedfilesystem;

import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;
import java.net.InetAddress;

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
	int packetCounter = 0;	
	// initialize structs to hold packets 
	HeaderPacket[] headPacks = new HeaderPacket[3];
	ArrayList<DataPacket> dataPacks = new ArrayList<DataPacket>();
	ArrayList<ArrayList<DataPacket>> finalPacks = new ArrayList<ArrayList<DataPacket>>();
	public FileRetriever(String server, int port) {
		// Save the server and port for use in `downloadFiles()`
		//...
		this.server = server;
		this.port = port;
		//System.out.println("I am doing it... I am doing the thing!");
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
	
		// connect to server
		try {
			DatagramSocket socker = new DatagramSocket();
			// receive data
			boolean dl = true;
			// write incoming bytes to Packet objects
			while (dl){
				byte[] buf = new byte[1028];
				DatagramPacket packer = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), this.port);
				socker.send(packer);
				socker.receive(packer);
				createPacket(packer.getData());
				// checks if all packets have been received, if yes, exit loop and close socket
				if (lastCount == 3 && headCount == 3)
					if (packetCounter == totalPackets){
						dl = false;
					} 
				}
			buildFiles();
			socker.close();
		} catch (Exception ex){
			System.err.println("Exception thrown: " + ex);
		}
	}

	// byte[] -> void 
	// builds Packet objects using arrays of bytes delivered to the socket 
	public void createPacket(byte[] stuff){
		for (int i = 0; i < stuff.length; i++){
		//	System.out.println(stuff[i]);
		}
		// if header packet:
		if (stuff[0]%2 == 0){
			String filename = "";
			for(int i = 2; i < stuff.length; i++) {
				if (!Byte.toString(stuff[i]).equals("")){
					filename += stuff[i];
				}
			}
			HeaderPacket hp = new HeaderPacket(filename, stuff[1]);
			System.out.println("Filename: " + filename);
			headPacks[headCount++] = hp;
		// otherwise create data packet
		} else {
			String data = "";
                                for(int i = 4; i < stuff.length; i++){
                                        data += stuff[i];
                                }
			Byte mostSig = new Byte(stuff[2]);
			Byte leastSig = new Byte(stuff[3]);
			int packNum = (256 * mostSig.intValue()) + leastSig.intValue(); // calculates packet number using formula provided in lab write up 
			// checks if it's a terminal packet 
			if (stuff[0]%4 == 3) {
				DataPacket dp = new DataPacket(data, true, stuff[1], packNum);
				System.out.println("Packet number: " + packNum + "\n" + "File ID: " + stuff[1]);
				dataPacks.add(dp);
				lastCount++;
				totalPackets += packNum;
				packetCounter++;
				
			} else { 
				DataPacket dp = new DataPacket(data, false, stuff[1], packNum);
				dataPacks.add(dp);
				packetCounter++;
			}
		}
	}
	
	// void -> void
	// re-organizes packets in the correct order, writes their contents to specified output files 
	public void buildFiles(){
		// Sorts packets into their respective array lists
		for (int i = 0; i < 3; i++){
			int ID = headPacks[i].fileID();
			for (int ii = 0; ii < dataPacks.size(); ii++){
				if (dataPacks.get(ii).fileID() == ID){
					finalPacks.get(i).add(dataPacks.get(ii));
				}
			}
		}
		// Sorts the contents of each array list using insertion sort
		for (int i = 0; i < 3; i++){
			ArrayList<DataPacket> currentList = finalPacks.get(i);
			for(int ii = 0; ii < currentList.size(); ii++) {
				DataPacket key = currentList.get(ii);
				int j = ii - 1;
				while (j >= 0 && currentList.get(j).getPacketNum() > key.getPacketNum()) {
					currentList.add(j+1, currentList.get(j));
					j -= 1;
				}
				currentList.add(j+1, key);
			}
		}
		// Writes the contents of each array list out to the specified output files
		for (int i = 0; i < 3; i++){
			File file = new File(headPacks[i].getFileName());
			System.out.println("The file name is: " + headPacks[i].getFileName());
			// Try block to check for exceptions
        		try {
				// Initialize a pointer in file
		            	// using OutputStream
            			OutputStream os = new FileOutputStream(file);
				for (int ii = 0; i < finalPacks.get(i).size(); ii++){
					byte[] data = finalPacks.get(i).get(ii).getData().getBytes();
					os.write(data);
				}
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

