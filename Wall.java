import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Path2D;
import java.util.Vector;


public class Wall extends Obj{

	private boolean isFilled, isDestructible, isClosed, isHalfHeight;	// isHalfHeight : les balles et les amres du joueur passent au dessus mais le joueur est bloqué
	private int thickness;
	private Vector<Point> points;
	private Path2D.Double path;
	private Vector<double[]> functions;

	public Wall(boolean isFilled, int thickness, boolean isDestructible, boolean isClosed, boolean isHalfHeight, Vector<Point> points){
		super();
		this.isFilled=isFilled;
		this.thickness=thickness;
		this.isDestructible=isDestructible;
		this.isClosed=isClosed||isFilled;
		this.isHalfHeight=isHalfHeight;
		solid=true;
		init((Vector<Point>)points.clone(),0);
		init((Vector<Point>)points.clone(),1);
		init((Vector<Point>)points.clone(),2);
	}
	
	public Vector<Point> getPoints(){
		return points;
	}
	
	public void draw(Graphics2D g){
		g.setColor(Color.black);
		if(isHalfHeight)g.setColor(new Color(15,15,15)); 
		g.fill(mask);
	}
	
	public boolean isFilled(){
		return isFilled;
	}
	
	public int getThickness(){
		return thickness;
	}
	
	public boolean isDestructible(){
		return isDestructible;
	}
	
	public boolean isHalfHeight(){
		return isHalfHeight;
	}
	
	private void init(Vector<Point> points, int step){
		int thick=thickness/2;
		if(step==1) thick+=45;
		if(step==2) thick+=70;
		path=new Path2D.Double();
		this.points=new Vector<Point>();

		if(isFilled&&step==0){
			for(int i=0;i<points.size();i++){
				Point p=points.get(i);
				this.points.add(p);
				try{
					path.lineTo(p.x,p.y);
				}catch(IllegalPathStateException e){
					path.moveTo(p.x,p.y);
				}
			}
			mask=new Area(path);
			return;
		}
		//this.points.removeAllElements();
		if(isClosed){
			points.add(points.get(0));
			points.add(points.get(1));
		}else{	// si il n'est pas fermé (et donc pas non plsu rempli) :
			if(step==1){
				/*int th=(step==1)?thickRed:thickOrange-thickRed;
				Point firstP=points.get(0);
				Point p=distDir(th,pointDirection(points.get(1),firstP));
				firstP.translate(p.x,p.y);
				Point lastP=points.get(points.size()-1);
				p=distDir(th,pointDirection(points.get(points.size()-2),lastP));
				lastP.translate(p.x,p.y);*/
				Point p=points.get(0);
				Screen.addToRedArea(new Area(new Ellipse2D.Double(p.x-thick,p.y-thick,thick*2,thick*2)));
				p=points.get(points.size()-1);
				Screen.addToRedArea(new Area(new Ellipse2D.Double(p.x-thick,p.y-thick,thick*2,thick*2)));
			}
			if(step==2){
				Point p=points.get(0);
				Screen.addToOrangeArea(new Area(new Ellipse2D.Double(p.x-thick,p.y-thick,thick*2,thick*2)));
				p=points.get(points.size()-1);
				Screen.addToOrangeArea(new Area(new Ellipse2D.Double(p.x-thick,p.y-thick,thick*2,thick*2)));
			}
		}

		Vector<Point> translated=new Vector<Point>();
		for(int i=0;i<points.size();i++){
			Point p=points.get(i);

			if(i>0){
				Point t=distDir(thick,pointDirection(p,points.get(i-1))-90);
				translated.add(new Point(p.x+t.x,p.y+t.y));
			}
			if(i<points.size()-1){
				Point t=distDir(thick,pointDirection(p,points.get(i+1))+90);
				translated.add(new Point(p.x+t.x,p.y+t.y));
			}
		}
		functions=new Vector<double[]>();	// fonctions des droites  (=> ax+b)
		for(int i=0;i<translated.size();i+=2){
			Point p1=translated.get(i);
			Point p2=translated.get(i+1);
			double a=(p2.y-p1.y*1.0)/(p2.x-p1.x);
			double b=p1.y-a*p1.x;
			if(p1.x==p2.x){	// mur vertical (pas de fonction ax+b)
				functions.add(new double[]{a,b,p2.x});
			}else{
				functions.add(new double[]{a,b});
			}
		}
		Point p=translated.get(0);
		path.moveTo(p.x,p.y);
		this.points.add(p);
		for(int i=0;i<functions.size()-1;i++){
			double[] f1=functions.get(i);
			double[] f2=functions.get(i+1);
			double x=(f2[1]-f1[1]*1.0)/(f1[0]-f2[0]);
			double y=f1[0]*x+f1[1];
			if(f1.length>2){	// mur vertical
				x=f1[2];
				y=f2[0]*x+f2[1];
			}
			if(f2.length>2){	// mur vertical
				x=f2[2];
				y=f1[0]*x+f1[1];
			}
			path.lineTo(x,y);
			this.points.add(new Point((int)x,(int)y));
		}
		p=translated.get(translated.size()-1);
		path.lineTo(p.x,p.y);
		this.points.add(p);

		translated=new Vector<Point>();
		for(int i=0;i<points.size();i++){
			p=points.get(i);

			if(i>0){
				Point t=distDir(thick,pointDirection(p,points.get(i-1))+90);
				translated.add(new Point(p.x+t.x,p.y+t.y));
			}
			if(i<points.size()-1){
				Point t=distDir(thick,pointDirection(p,points.get(i+1))-90);
				translated.add(new Point(p.x+t.x,p.y+t.y));
			}
		}
		functions=new Vector<double[]>();	// fonctions des droites ; x=a, y=b  (=> ax+b)
		for(int i=0;i<translated.size();i+=2){
			Point p1=translated.get(i);
			Point p2=translated.get(i+1);
			double a=(p2.y-p1.y*1.0)/(p2.x-p1.x);
			double b=p1.y-a*p1.x;
			if(p1.x==p2.x){	// mur vertical (pas de fonction ax+b)
				functions.add(new double[]{a,b,p2.x});
			}else{
				functions.add(new double[]{a,b});
			}
			functions.add(new double[]{a,b});
		}
		p=translated.get(translated.size()-1);
		path.lineTo(p.x,p.y);
		this.points.add(p);
		for(int i=functions.size()-1;i>0;i--){
			double[] f1=functions.get(i);
			double[] f2=functions.get(i-1);
			double x=(f2[1]-f1[1]*1.0)/(f1[0]-f2[0]);
			double y=f1[0]*x+f1[1];
			if(f1.length>2){	// mur vertical
				x=f1[2];
				y=f2[0]*x+f2[1];
			}
			if(f2.length>2){	// mur vertical
				if(i<functions.size()-1) f1=functions.get(i+1);
				x=f2[2];
				y=f1[0]*x+f1[1];
			}
			if(!Double.isNaN(x)&&!Double.isNaN(y)){
				path.lineTo(x,y);
				this.points.add(new Point((int)x,(int)y));
			}
		}
		p=translated.get(0);
		path.lineTo(p.x,p.y);
		this.points.add(p);
		p=this.points.get(0);
		path.lineTo(p.x,p.y);
		this.points.add(p);
		if(step==0) mask=new Area(path);
		if(step==1) Screen.addToRedArea(new Area(path));
		if(step==2) Screen.addToOrangeArea(new Area(path));
	}
}
