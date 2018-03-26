package operator.recovery;

import java.io.Serializable;

import operator.types.SocketRepr;
import supervisor.Position;

public class Key implements Serializable{
	
	private static final long serialVersionUID = -6573541126422556350L;

	private Position node;
	private int sequenceNumber;
	
	public Key(Position node, int sequenceNumber) {
		super();
		this.node = node;
		this.sequenceNumber = sequenceNumber;
	}
	
	
	public boolean equals(Key key)
	{
		if(this.sequenceNumber!=key.sequenceNumber)
			return false;
		if(this.node!=null)
			return this.node.equals(key.node);
		else
			return key.node==null;
	}
	
	public String toString()
	{
		return (node==null?"_file":node.toString())+sequenceNumber;
	}
	
	
}
