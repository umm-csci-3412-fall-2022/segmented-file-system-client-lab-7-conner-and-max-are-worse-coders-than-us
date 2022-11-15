package segmentedfilesystem;
public class DataPacket extends Packet{
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

	public String getData(){return this.data;}

	public boolean lastPacket(){return this.isLastPacket;}
}


