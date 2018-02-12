package supervisor.graph_representation;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.omg.CORBA.PRIVATE_MEMBER;

public class Graph<T> {
	public static final int DISCOVERED=20;
	public static final int UNKNOWN=10;
	public static final int FINISHED=30;
	
	//vertices
	private Map<Integer, Vertex<T>> vertices;
	
	public Graph()
	{
		this.vertices=new TreeMap<>();
	}
	
	/**
	 * Deep copy constructor
	 * @param graph blueprint
	 */
	public Graph(Graph<T> graph)
	{
		this.vertices=new TreeMap<>();
		for(int vertex:graph.vertices.keySet())
		{
			this.vertices.put(vertex, new Vertex<>(graph.vertices.get(vertex)));
		}
	}
	
	public void addVertex(int id,Vertex<T> vertex)
	{
		vertices.put(id, vertex);
	}

	
	public  void connect(Vertex<T> v1,Vertex<T> v2,int cost)
	{
		v1.addNeighbour(v2.getId(), cost);
		v2.addNeighbour(v1.getId(), cost);
	}
	
	/**
	 * Add an edge from first to second vertex
	 * @param from first node, source of arc
	 * @param to	destination of arc
	 * @param cost	cost of the arc
	 */
	public  void asymmConnect(Vertex<T> from,Vertex<T> to,int cost)
	{
		from.addNeighbour(to.getId(), cost);
	}
	
	/**
	 * Returns the id of any node without incoming edges, or null if not existing
	 * @return
	 */
	public Integer idOfNodeWithoutIncomingEdges()
	{
		return vertices.keySet().stream().filter(
				id->
				vertices.values().stream().map(
						ve->ve.getNeighbours()).noneMatch(nei->nei.containsKey(id))
				).findFirst().orElse(null);
		
	}
	
	/**
	 * Returns a topological sort of the vertices or null if cyclic
	 * @return
	 */
	public List<Vertex<T>> topologicalSort()
	{
		Graph<T> copy=new Graph<T>(this);
		Integer next;
		List<Vertex<T>> sort=new ArrayList<>();
		do{
			next=copy.idOfNodeWithoutIncomingEdges();
			if(next==null)
			{
				break;
			}
			sort.add(copy.vertices.get(next));
			copy.vertices.remove(next);
		}while(true);
		if(copy.vertices.isEmpty())
			return sort;
		return null;
	}
	public static void main(String[] args)
	{
		Graph<Integer> g=new Graph<>();
		Vertex<Integer> v1=new Vertex<>(1);
		Vertex<Integer> v2=new Vertex<>(2);
		Vertex<Integer> v3=new Vertex<>(3);
		Vertex<Integer> v4=new Vertex<>(4);
		Vertex<Integer> v5=new Vertex<>(5);
		Vertex<Integer> v6=new Vertex<>(6);
		Vertex<Integer> v7=new Vertex<>(7);
		
		g.connect(v1, v2, 6);
		g.connect(v1, v3, 2);
		g.connect(v1, v4, 2);
		g.connect(v2, v5, 5);
		g.connect(v3, v6, 8);
		g.connect(v4, v6, 2);
		g.connect(v4, v7, 9);
		g.connect(v5, v7, 1);
		g.connect(v6, v7, 3);
		
		
		
		
		
		
		
		g.addVertex(1, v1);
		g.addVertex(2, v2);
		g.addVertex(3, v3);
		g.addVertex(4, v4);
		g.addVertex(5, v5);
		g.addVertex(6, v6);
		g.addVertex(7, v7);
	//	g.dijkstra(3);
		
	}
}
