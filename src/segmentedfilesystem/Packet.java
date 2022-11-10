public abstract class Packet{
	int fileID;

	public Packet(int fi){
		this.fileID = fi;
	}

	public int fileID(){
		return this.fileID;
	}


}
