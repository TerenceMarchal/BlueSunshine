import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Vector;


public class Captor extends Obj{

	private int distanceMax;	// portée maximale, si = 0, alors c'est un laser
	private int angleView;
	private boolean turnClockwise;	// indique si il tourne actuellement dans le sens des aiguilles d'une montre
	private double speedRotation;	// °/step
	private int angleMin, angleMax;
	private Area cone;	// cone de la caméra
	private Vector<Integer> alarms=new Vector<Integer>();
	private int nearestNode;	// id du point du chemin le plus proche
	private boolean playerSpotted=false;	// indique si le capteura repéré le joueur
	private int timeBlurred=0;	// caméra brouillée pour x step ; 0 = non-brouillée 
	
	public Captor(int distanceMax, int angleView, boolean turnClockwise, double speedRotation, int x, int y, int angleMin, int angleMax, double angleStart){
		super();
		this.distanceMax=distanceMax;
		this.angleView=angleView;
		this.turnClockwise=turnClockwise;
		this.speedRotation=speedRotation;
		this.angleMin=angleMin;
		this.angleMax=angleMax;
		this.x=x;
		this.y=y;
		angle=angleStart;
		w=48;
		h=24;
		if(distanceMax>0){
			Polygon poly=new Polygon();
			poly.addPoint(0,0);
			for(int i=0;i<10;i++){
				Point p=distDir(distanceMax,i*angleView/10);
				poly.addPoint(p.x,p.y);
			}
			cone=new Area(poly);
		}
		nearestNode=PathManager.getNearestPoint(x,y);
	}
	
	public void step(){
		if(!isBlurred()){
			if(turnClockwise){
				if(!isAngleInRange(angle-angleView/2-speedRotation,angleMin,angleMax)) turnClockwise=false;
				angle-=speedRotation;
			}else{
				if(!isAngleInRange(angle+angleView/2+speedRotation,angleMin,angleMax)) turnClockwise=true;
				angle+=speedRotation;
			}
		
			if(Screen.T%2==0){
				Player pl=Screen.player;
				boolean see=false;
				if(distanceMax==0){	// laser
					if(pointDistance(pos(),pl.pos())<150||getDiffAngle(angle,pointDirection(pos(),pl.pos()))<5){
						Vector<Point> points=pl.getExtremsPointsComparedTo(pos());
						points.add(pl.pos());
						Point pos=pos();
						Point p=distDir(w/2,angle);
						pos.translate(p.x,p.y);
						for(int i=0;i<points.size();i++){
							if(!checkCollisionLine(pos,points.get(i),false,new Obj[]{pl,pl.getLeftWeapon(),pl.getRightWeapon()}).isCollision()){
								see=true;
								break;
							}
						}
					}
				}else{	// caméra
					if(pointDistance(pos(),pl.pos())<distanceMax){
						if(pointDistance(pos(),pl.pos())<150||isAngleInRange(pointDirection(pos(),pl.pos()),angle-angleView/2,angle+angleView/2)){
							Vector<Point> points=pl.getExtremsPointsComparedTo(pos());
							points.add(pl.pos());
							Point pos=pos();
							Point p=distDir(w/2,angle);
							pos.translate(p.x,p.y);
							for(int i=0;i<points.size();i++){
								if(!checkCollisionLine(pos,points.get(i),false,new Obj[]{pl,pl.getLeftWeapon(),pl.getRightWeapon()}).isCollision()){
									see=true;
									break;
								}
							}
						}
					}
				}
				if(see){
					if(!playerSpotted){
						playerSpotted=true;
						Screen.triggerAlarms(alarms,this);
						System.out.println("alert");
					}
				}else{
					playerSpotted=false;
				}
			}
		}else{
			timeBlurred--;
		}
	}
	
	public void draw(Graphics2D g){
		g.setColor(Screen.COLOR_TEAM_B);
		if(Screen.drawCaptorAngles){
			Point p=distDir((distanceMax>0)?distanceMax:10000,angle-angleView/2);
			g.drawLine((int)x,(int)y,(int)x+p.x,(int)y+p.y);
			p=distDir((distanceMax>0)?distanceMax:10000,angle+angleView/2);
			g.drawLine((int)x,(int)y,(int)x+p.x,(int)y+p.y);
		}
		
		g.rotate(-Math.toRadians(angle),x,y);
		
		g.fillRect((int)x-w/2,(int)y-h/2,w,h);
		
		g.setColor(Color.black);
		Point p=distDir(h/2,angleView/2);
		g.drawLine((int)x,(int)y,(int)x+p.x,(int)y+p.y);
		p=distDir(h/2,-angleView/2);
		g.drawLine((int)x,(int)y,(int)x+p.x,(int)y+p.y);
		
		g.rotate(Math.toRadians(angle),x,y);	
		
	}
	
	public void triggerAlarm(int id, Obj by){
		
	}
	
	public void addAlarm(int id){
		alarms.add(id);
	}
	
	public int getNearestNode(){
		return nearestNode;
	}
	
	public void blur(int time){
		timeBlurred=time;
	}
	
	public boolean isBlurred(){
		return timeBlurred>0;
	}
}
