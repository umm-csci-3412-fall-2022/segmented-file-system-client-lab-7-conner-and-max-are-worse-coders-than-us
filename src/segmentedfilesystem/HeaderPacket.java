package segmentedfilesystem;
public class HeaderPacket extends Packet{
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
