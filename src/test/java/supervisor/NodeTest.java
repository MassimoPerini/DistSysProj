package supervisor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import operator.types.Sum;
import org.junit.Test;

public class NodeTest {

	/**
	 * Construct this graph
	 * 
	 * 
	 *     0
	 *    / \
	 *   1   2
	 *  /
	 * 3
	 * 
	 * 
	 * @return
	 */
	/*
	private List<Node> createGraph()
	{
		List<Node> graph=new ArrayList<Node>();
		
		
		for(int i=0;i<4;i++)
		{
			Position position=new Position("127.0.0.1",i);
			graph.add(new Node(new Sum(1, 1), position));
		}
		graph.get(0).addToForward(graph.get(1));
		graph.get(1).addToForward(graph.get(3));
		graph.get(0).addToForward(graph.get(2));
		return graph;
	}
	@Test
	public void testNode() {
		List<Node> graph=this.createGraph();
		assertEquals(2, graph.get(0).getForwardStar().size());
		assertEquals(1, graph.get(1).getBackwardStar().size());
	}
*/
}
