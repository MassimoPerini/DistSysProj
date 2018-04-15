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
		return (node==null?"_file":node.toString())+":sequenceNumber="+ sequenceNumber;
	}

	public boolean sameSource(Key other)
	{
		if (node==null)
		{
			return other.node==null;
		}
		return node.equals(other.node);
	}

	public boolean otherHasSameSenderButOlderSequenceNumber(Key other)
	{
		return sameSource(other) && this.sequenceNumber>other.sequenceNumber;
	}

    public Position getNode() {
		return  node;
    }

    public int getSequenceNumber()
	{
		return  sequenceNumber;
	}

    public void setSender(Position positionOfTheOtherSide)
	{
		this.node=positionOfTheOtherSide;
    }
}
