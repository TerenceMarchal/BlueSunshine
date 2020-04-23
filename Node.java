import java.awt.Point;


public class Node {

	public int x,y;	// position dans navMap
	public Node parent;	// node par laquelle on est arriv� ici
	public int value=Integer.MAX_VALUE;	// plus la valeur est faible, meilleur est la qualit� ; exprim� en fonction de la distance par rapport � l'objectif et si on passe par la zone orange
	
	public Node(int x, int y){
		this.x=x;
		this.y=y;
	}
	
	public Node(Node parent, int x, int y, int value){
		this.parent=parent;
		this.x=x;
		this.y=y;
		this.value=value;
	}
	
	public Point getPoint(){
		return new Point(x,y);
	}
	
	public boolean equals(Object obj){	// permet de red�finir la m�thode .contains de la closedList
		if(obj instanceof Node){
			if(((Node)obj).x==x&&((Node)obj).y==y) return true;
		}
		return false;
	}
	
}
