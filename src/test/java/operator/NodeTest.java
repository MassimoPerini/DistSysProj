package operator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class NodeTest {

	private List<Node> createGraph()
	{
		List<Node> graph=new ArrayList<Node>();
		
		
		for(int i=0;i<4;i++)
		{
			Position position=new Position("127.0.0.1",i);
			graph.add(new Node(new Sum(1, 1), position));
		}
		return graph;
	}
	@Test
	public void testNode() {
		fail("Not yet implemented");
	}

}
