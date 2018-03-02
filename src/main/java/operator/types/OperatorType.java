package operator.types;

import java.util.List;

import supervisor.Position;

/**
 * Created by massimo on 11/02/18.
 *
 * Represents the generic type of operator
 *
 */
public abstract class OperatorType{

    int size;
    int slide;
    /**
     * This attribute contains the position of all the nodes to which the output must be sent
     */
    private List<Position> forwardStar;
    
    /**
     * This is the port from which the input arrives.
     */
    private Position ownPort;
    /***
     *
     * @param inputData source of data
     * @return result of the computation
     */

    abstract double execute(List<Double> inputData);
    abstract int getSize();
    abstract int getSlide();
    
	public Position getOwnPort() {
		return ownPort;
	}
    
}
