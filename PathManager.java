import java.awt.Point;
import java.util.Vector;


public class PathManager{

	public static PathManager pm;
	private static int[] aps;
	private static int[] fs;
	private static Point[] points;
	
	public PathManager(int[] aps, int[] fs, Point[] points){
		pm=this;
		PathManager.aps=aps;
		PathManager.fs=fs;
		PathManager.points=points;
	}
	
	public static boolean areLinked(int p1, int p2){
		int p1copy=p1;
		p1=Math.min(p1, p2);
		p2=Math.max(p1copy,p2);
		int pos=aps[p1];
		while(fs[pos]!=-1){
			if(fs[pos]==p2) return true;
			pos++;
		}
		return false;
	}
	
	public static Point getPoint(int p){
		return points[p];
	}
	
	public static int getPointsNumber(){
		return points.length;
	}
	
	public static Vector<Integer> getNeighbours(int p){
		Vector<Integer> vec=new Vector<Integer>();
		int pos=aps[p];
		while(fs[pos]!=-1){
			vec.add(fs[pos]);
			pos++;
		}
		
		// puisque la liaison est notée uniquement chez le plsu petit id, on parcours les id plus petits :
		int id=0;
		for(int i=0;i<aps[p]-1;i++){
			if(fs[i]==-1){
				id++;
			}
			if(fs[i]==p){
				vec.add(id);
			}
		}
		
		
		return vec;
	}
	
	/**
	 * Calcule et attribue un chemin au bot passé en paramètre.
	 * Aucune valeur de retour puisque multithreadé
	 * from1 et from2 sont les deux points de la droite sur laquelle est situé le bot (évite de faire un aller-retour, l'algo va choisir directement le meilleur)
	 * to est le noeud où on veut aller
	 */
	public static void setPath(Enemy bot, int from1, int from2, int to){
		DijkstraNode[] nodes=new DijkstraNode[points.length];
		Vector<DijkstraNode> notVisited=new Vector<DijkstraNode>(); 
		for(int i=0;i<points.length;i++){
			DijkstraNode node = new DijkstraNode(i);
			nodes[i]=node;
			notVisited.add(node);
		}
		nodes[from1].setDistance(0);
		while(notVisited.size()>0){
			DijkstraNode n1=notVisited.get(0);
			for(int i=1;i<notVisited.size();i++){
				DijkstraNode n=notVisited.get(i);
				if(n.getDistance()<n1.getDistance()){
					n1=n;
				}
			}
			notVisited.remove(n1);
			Vector<Integer> children=getNeighbours(n1.getId());
			Point p1=getPoint(n1.getId());
			for(int i=0;i<children.size();i++){
				DijkstraNode n2=nodes[children.get(i)];
				Point p2=getPoint(n2.getId());
				
				double dist=bot.pointDistance(p1,p2);
				if(n1.getDistance()+dist<n2.getDistance()){
					n2.setDistance(n1.getDistance()+dist);
					n2.setParent(n1);
				}
			}
		}
		Vector<DijkstraNode> nodePath=new Vector<DijkstraNode>();
		DijkstraNode node=nodes[to];
		while(node.getId()!=from1){
			nodePath.insertElementAt(node,0);
			node=node.getParent();
			if(node==null) System.exit(0);
		}
		if(nodePath.size()>0&&nodePath.get(0).getId()!=from2){
			nodePath.insertElementAt(nodes[from1],0);
		}
		
		int[] path=new int[nodePath.size()];
		for(int i=0;i<nodePath.size();i++){
			path[i]=nodePath.get(i).getId();
		}
		
		if(path.length>0){
			bot.setPath(path);
			bot.setIsPathClosed(false);
		}
	}
	
	public static int getNearestPoint(double x, double y){
		int nearest=-1;
		int dist=Integer.MAX_VALUE;
		for(int i=0;i<PathManager.getPointsNumber();i++){
			Point p=PathManager.getPoint(i);
			int d=(int)(Math.pow(x-p.x,2)+Math.pow(y-p.y,2));
			if(d<dist){
				nearest=i;
				dist=d;
			}
		}
		return nearest;
	}
	
}
