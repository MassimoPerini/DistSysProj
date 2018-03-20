package supervisor.graph_representation;


import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import supervisor.communication.message.OperatorDeployment;

public class Vertex<T> implements Comparable<Vertex<T>>{
	
	private int id;
	private T data;
	//from neighbor to cost
	private Map<Integer,Double> neighbours;
	
	public Vertex(int id,T data)
	{
		this.id=id;
		this.neighbours=new TreeMap<>();
		this.data = data;
	}
	public Vertex(Vertex<T> vertex)
	{
		this.id=vertex.id;
		this.neighbours=new TreeMap<>(vertex.neighbours);
		this.data = vertex.getData();
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
	
	public T getData() {
		return data;
	}
}
