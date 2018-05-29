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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + sequenceNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (sequenceNumber != other.sequenceNumber)
			return false;
		return true;
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
