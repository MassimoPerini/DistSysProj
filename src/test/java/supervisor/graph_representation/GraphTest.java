package supervisor.graph_representation;


import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class GraphTest {

	private Graph<Integer> getGraph()
	{
		Graph<Integer> g=new Graph<>();
		Vertex<Integer> v1=new Vertex<>(1);
		Vertex<Integer> v2=new Vertex<>(2);
		Vertex<Integer> v3=new Vertex<>(3);
		Vertex<Integer> v4=new Vertex<>(4);
		Vertex<Integer> v5=new Vertex<>(5);
		Vertex<Integer> v6=new Vertex<>(6);
		Vertex<Integer> v7=new Vertex<>(7);
		
		g.asymmConnect(v1, v2, 6);
		g.asymmConnect(v1, v3, 2);
		g.asymmConnect(v1, v4, 2);
		g.asymmConnect(v2, v5, 5);
		g.asymmConnect(v3, v6, 8);
		g.asymmConnect(v4, v6, 2);
		g.asymmConnect(v4, v7, 9);
		g.asymmConnect(v5, v7, 1);
		g.asymmConnect(v6, v7, 3);

		g.addVertex(1, v1);
		g.addVertex(2, v2);
		g.addVertex(3, v3);
		g.addVertex(4, v4);
		g.addVertex(5, v5);
		g.addVertex(6, v6);
		g.addVertex(7, v7);
		return g;
	}
	
	private Graph<Integer> getGraphCyclic()
	{
		Graph<Integer> g=new Graph<>();
		Vertex<Integer> v1=new Vertex<>(1);
		Vertex<Integer> v2=new Vertex<>(2);
		Vertex<Integer> v3=new Vertex<>(3);
		Vertex<Integer> v4=new Vertex<>(4);
		Vertex<Integer> v5=new Vertex<>(5);
		Vertex<Integer> v6=new Vertex<>(6);
		Vertex<Integer> v7=new Vertex<>(7);
		
		g.asymmConnect(v1, v2, 6);
		g.asymmConnect(v1, v3, 2);
		g.asymmConnect(v1, v4, 2);
		g.asymmConnect(v2, v5, 5);
		g.asymmConnect(v3, v6, 8);
		g.asymmConnect(v4, v6, 2);
		g.asymmConnect(v4, v7, 9);
		g.asymmConnect(v5, v7, 1);
		g.asymmConnect(v6, v7, 3);
		g.asymmConnect(v6, v1, 2);
		
		g.addVertex(1, v1);
		g.addVertex(2, v2);
		g.addVertex(3, v3);
		g.addVertex(4, v4);
		g.addVertex(5, v5);
		g.addVertex(6, v6);
		g.addVertex(7, v7);
		return g;
	}
	
	@Test
	public void test() {
		
		Graph<Integer> graph=getGraph();
		assertEquals(graph.idOfNodeWithoutIncomingEdges(),new Integer(1));
	}
	
	@Test
	public void sortTest()
	{
		List<Vertex<Integer>> list=getGraph().topologicalSort();
		for(int i=1;i<8;i++)
		{
			assertEquals(i, list.get(i-1).getId());
		}
	}
	
	@Test
	public void cyclicSortTest()
	{
		List<Vertex<Integer>> list=getGraphCyclic().topologicalSort();
		assertNull(list);
	}

}
