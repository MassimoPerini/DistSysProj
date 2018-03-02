package operator.recovery;

import supervisor.Position;

/**
 * Created by higla on 24/02/2018.
 */
public class DataKey {
    private int data;
    private String progressiveKey;
    
    private transient Position senderPosition;
    
    // we assume that the key is given
    public DataKey(int data, String progressiveKey) {
        this.data = data;
        this.progressiveKey = progressiveKey;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public String getProgressiveKey() {
        return progressiveKey;
    }

    public void setProgressiveKey(String progressiveKey) {
        this.progressiveKey = progressiveKey;
    }

	public Position getSenderPosition() {
		return senderPosition;
	}

	public void setSenderPosition(Position senderPosition) {
		this.senderPosition = senderPosition;
	}
}
