package operator.recovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;
import utils.Debug;

/**
 * Created by higla on 24/02/2018.
 */
public class DataKey implements Serializable{

    private static final long serialVersionUID = -189729754032863167L;
	
	
	private float data;
	/**
	 * id+ sequence number of the node which created the message eg a sum (id 13, s.n. 4) receives 2,4. 
	 * The new data key will bear 13 as id, 5 as sequence number   
	 */
    private Key aggregator;
    private List<Key> sources;
    private String originalKey;
    

    public DataKey(String originalKey, double data,Key aggregator,@Nullable List<Key> sources) {
        this.originalKey = originalKey;
        this.data = (float)data;
        this.aggregator=aggregator;
        if(sources!=null)
        	this.sources=new ArrayList<>(sources);
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public float getData() {

        return data;
    }

    /*public float getValue() {

        return data;
    }*/
    public void setData(int data) {
        this.data = data;
    }




    /**
     * Used in OperatorType to list the not-yet-aggregated information
     * @return id+ sequence number of the node which created the message (see definition)
     */
    public Key getAggregator()
    {
    	return aggregator;
    }




    /**
     * this method checks if
     * @param value is equal to the value
     * @return true if it equal
     */
    private boolean checkEqualValue(float value){
        return (this.data == value);
    }

    /**
     * checks if two
     * @param dataKey s are the same
     * @return true if they are the same
     */
    public boolean checkSameData(DataKey dataKey)
    {
        Debug.printVerbose("Sto confrontanto "+this+dataKey);
        Debug.printVerbose("e sono..."+(dataKey.aggregator==this.aggregator&& checkEqualValue(dataKey.getData())));

        return dataKey.aggregator.equals(this.aggregator)&& checkEqualValue(dataKey.getData());
    }

    public List<Key> getSources()
    {
        return  this.sources;
    }

    @Override
    public String toString() {
        return "DataKey{" +
                "data=" + data +
                ", progressiveKey='" + aggregator.toString() + '\'' +
       ", sources='" + sources + '\'' +
       
                '}';
    }
    
    public boolean equals(DataKey key)
    {
    	if(this.aggregator!=null)
    		return this.aggregator.equals(key.aggregator);
    	else return key.aggregator==null;
    }

    public boolean otherHasSameSenderButOlderSequenceNumber(DataKey currentlyInFile)
    {
        return this.aggregator.otherHasSameSenderButOlderSequenceNumber(currentlyInFile.getAggregator());
    }


    public boolean hasOlderOrEqualSequenceNumberThanOther(Key receivedAck)
    {
        return  this.aggregator.getSequenceNumber()<=receivedAck.getSequenceNumber();
    }

    /**
     * Add a fake datakey to the sources of this datakey.
     * This method is used by recovery manager to update the list of nodes that have acknowledged a message.
     * @param ackSenderPosition
     */
    public void addSource(Position ackSenderPosition)
    {
        this.sources.add(new Key(ackSenderPosition,0));
    }

    /**
     * Change the position of the sender.
     * Useful since file reader can difficultly get his IP address, whilst subsequent receiver can easily.
     * @param positionOfTheOtherSide
     */
    public void setAggregator(Position positionOfTheOtherSide)
    {
        this.aggregator.setSender(positionOfTheOtherSide);
    }
}
