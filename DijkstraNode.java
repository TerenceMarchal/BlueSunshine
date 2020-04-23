
public class DijkstraNode {

	private int id;
	private DijkstraNode parent;	// id du noeud d'où on on vient
	private double distance=Double.MAX_VALUE;	// distance totale du point d'origine jusqu'à celui-ci
	
	public DijkstraNode(int id){
		this.id=id;
	}
	
	public int getId(){
		return id;
	}
	
	public DijkstraNode getParent(){
		return parent;
	}
	
	public double getDistance(){
		return distance;
	}
	
	public void setDistance(double distance){
		this.distance=distance;
	}
	
	public void setParent(DijkstraNode parent){
		this.parent=parent;
	}
}
