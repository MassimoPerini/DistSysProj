package supervisor.graph_representation;


import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Vertex<T> implements Comparable<Vertex<T>>{
	
	private int id;
	
	//from neighbor to cost
	private Map<Integer,Double> neighbours;
	
	public Vertex(int id)
	{
		this.id=id;
		this.neighbours=new TreeMap<>();
	}
	public Vertex(Vertex<T> vertex)
	{
		this.id=vertex.id;
		this.neighbours=new TreeMap<>(vertex.neighbours);
	}
	
	public void addNeighbour(int id,double cost)
	{
		neighbours.put(id, cost);
	}
	
	public Map<Integer, Double> getNeighbours()
	{
		return neighbours;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public int compareTo(Vertex<T> o) {
		return id-o.id;
	}
}
