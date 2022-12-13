package segmentedfilesystem;

import java.util.*;
import java.util.Comparator;
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
	// Various counters to keep track of download progress and file size
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
			byte[] buf = new byte[1028];
			byte[] hello = {0};
			DatagramPacket packer = new DatagramPacket(hello,hello.length, InetAddress.getLocalHost(), this.port);
			socker.send(packer);
			packer = new DatagramPacket(buf,buf.length);
			while (dl){
				socker.receive(packer);
				byte[] data = Arrays.copyOfRange(packer.getData(), 0, packer.getLength());
				createPacket(data);
				// checks if all packets have been received, if yes, exit loop and close socket
				if (lastCount == 3 && headCount == 3 && packetCounter == totalPackets)		
					dl = false;
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
		//grab fileID and status
		int status = Byte.toUnsignedInt(stuff[0]);
		int fileID = Byte.toUnsignedInt(stuff[1]);
		// if header packet:
		if (status%2 == 0){
			byte[] data = Arrays.copyOfRange(stuff, 2, stuff.length);
			HeaderPacket hp = new HeaderPacket(data, fileID);
			System.out.println("Filename: " + data);
			headPacks[headCount++] = hp;
		// otherwise create data packet
		} else {
			int packNum = 256 * Byte.toUnsignedInt(stuff[2]) + Byte.toUnsignedInt(stuff[3]);
			byte[] data = Arrays.copyOfRange(stuff, 4, stuff.length);
			// checks if it's a terminal packet 
			if (status%4 == 3) {
				DataPacket dp = new DataPacket(data, true, fileID, packNum);
				dataPacks.add(dp);
				lastCount++;
				totalPackets += packNum;
				packetCounter++;
				
			} else { 
				DataPacket dp = new DataPacket(data, false, fileID, packNum);
				//System.out.println("Packet number: " + packNum + " File ID: " + fileID);
				dataPacks.add(dp);
				packetCounter++;
			}
		}
	}
	
	// void -> void
	// re-organizes packets in the correct order, writes their contents to specified output files 
	public void buildFiles(){
		ArrayList<DataPacket> currentList = new ArrayList<DataPacket>();
		System.out.println("first loop");
		for (int i = 0; i < 3; i++){	
			finalPacks.add(new ArrayList<DataPacket>());
		}
		// Sorts packets into their respective array lists
		for (int i = 0; i < 3; i++){			
			int ID = headPacks[i].getFileID();
			System.out.println("ID: " + ID);
			for (int ii = 0; ii < dataPacks.size(); ii++){
				if (dataPacks.get(ii).getFileID() == ID){
					finalPacks.get(i).add(dataPacks.get(ii));
				}
			}
		}
		// Sorts the contents of each array list using insertion sort
		System.out.println("second loop");
		for (int i = 0; i < 3; i++){
			System.out.println("SIZE: " + finalPacks.get(i).size());
			currentList = finalPacks.get(i);
			currentList.sort(new packetComparator());
		}
		// Writes the contents of each array list out to the specified output files
		System.out.println("third loop");
		for (int i = 0; i < 3; i++){
			File file = new File(new String(headPacks[i].getData()));
			System.out.println("The file name is: " + headPacks[i].getData().toString());
			// Try block to check for exceptions
        		try {
				// Initialize a pointer in file
		            	// using OutputStream
            			OutputStream os = new FileOutputStream(file);
				for (int ii = 0; ii < finalPacks.get(i).size(); ii++){
					byte[] data = finalPacks.get(i).get(ii).getData();
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
	 byte[] data; 
         public int fileID(){
                 return this.fileID;
         }
 
}

class DataPacket extends Packet{
           int fileID;
           byte[] data;
           boolean isLastPacket;
           int packetNum;
   
          public DataPacket(byte[] data, boolean ilp, int fileID, int pn){
                  this.data = data;
                  this.isLastPacket = ilp;
                  this.fileID = fileID;
                  this.packetNum = pn;
          }
  		
	  public int getPacketNum(){return this.packetNum;}
          public byte[] getData(){return this.data;}
  	  public int getFileID(){return this.fileID;}
          public boolean lastPacket(){return this.isLastPacket;}
}

class HeaderPacket extends Packet{
           byte[] data;
           int fileID;
   
           public HeaderPacket(byte[] data, int fileID){
                   this.data = data;
                   this.fileID = fileID;
           }
  
           public byte[] getData(){
		   return this.data;
           }	
	   public int getFileID(){
		return this.fileID;	
	   }
}


class packetComparator implements Comparator<DataPacket> {
    public int compare(DataPacket p1, DataPacket p2){

        if(p1.getPacketNum() > p2.getPacketNum()) return 1;
        if(p1.getPacketNum() < p2.getPacketNum()) return -1;
        return 0;
    }
}
