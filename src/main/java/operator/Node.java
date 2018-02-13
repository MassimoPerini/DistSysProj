package operator;

import java.util.ArrayList;
import java.util.List;

import operator.types.OperatorType;
import supervisor.Position;

/**
 * This class is the representation of nodes as seen by the supervisor
 */
public class Node {
	private Position ownPosition;
	private List<Position> forwardStar;
	private List<Position> backwardStar;
	//todo: make class consistent to NodeSocket
	//adding List of operators + 1 MainDaemon
	private OperatorType operator;

	public Node(OperatorType operator,Position position)
	{
		this.operator=operator;
		this.ownPosition=position;
		this.forwardStar=new ArrayList<Position>();
		this.backwardStar=new ArrayList<Position>();
	}
	
	/**
	 * Add the given node to the forward star of this object
	 * @param node the node to add
	 * @return 0 on ok
	 */
	public int addToForward(Node node)
	{
		this.forwardStar.add(node.ownPosition);
		node.backwardStar.add(ownPosition);
		return 0;
	}
	/**
	 * Send a message to the daemon where the node should be created in order to launch a process for it
	 */
	public void allocate()
	{
		
	}
	
	public List<Position> getBackwardStar()
	{
		return this.backwardStar;
	}
	
	public List<Position> getForwardStar()
	{
		return this.forwardStar;
	}
	
}
