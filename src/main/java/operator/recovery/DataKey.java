package operator.recovery;

import operator.communication.message.MessageData;
import supervisor.Position;

/**
 * Created by higla on 24/02/2018.
 */
public class DataKey {

    private float data;
    private String progressiveKey;
    
    private transient Position senderPosition;
    
    // we assume that the key is given
    public DataKey(float data, String progressiveKey) {

        this.data = data;
        this.progressiveKey = progressiveKey;
    }

    
    /**
     * Create a datakey similar to the given messagedata (ie same value, same counter)
     * @param elem
     */
    public DataKey(MessageData elem) {
		this.data=(float)elem.getValue();
		this.progressiveKey=""+elem.getCounter();
	}


	public float getData() {

        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public String getProgressiveKey() {
        return progressiveKey;
    }



	public Position getSenderPosition() {
		return senderPosition;
	}


	public void setSenderPosition(Position senderPosition) {
		this.senderPosition = senderPosition;
	}

    /**
     * this method checks if
     * @param key is equal to a string
     * @return true if it equal
     */
	private boolean checkEqualKey(String key){
        if(this.progressiveKey.equals(key))
            return true;
        return false;
    }

    /**
     * this method checks if
     * @param value is equal to the value
     * @return true if it equal
     */
    private boolean checkEqualValue(float value){
        if(this.data == value)
            return true;
        return false;
    }

    /**
     * checks if two
     * @param dataKey s are the same
     * @return true if they are the same
     */
    public boolean checkSameData(DataKey dataKey)
    {
        return checkEqualKey(dataKey.getProgressiveKey()) && checkEqualValue(dataKey.getData());
    }






}
