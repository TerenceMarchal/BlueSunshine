import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Vector;


public class Door extends Obj{

	public static int WAY_LEFT=0;
	public static int WAY_RIGHT=1;
	public static int WAY_BOTH=2;
	
	private static int STATE_NOTHING=0;
	private static int STATE_OPENING=1;
	private static int STATE_CLOSING=2;
	
	private int way, thickness;
	protected Door otherDoor=null;	// la deuxième porte de la double porte (=null si simple) 
	private Point p1, p2;
	private Vector<Point> points=new Vector<Point>();
	private double opening=0;	// pourcentage d'ouverture
	private int blockedTime=0;	// temps pendant lequel la porte est bloquée (IEM)
	private boolean locked=false;	// pour les alarmes
	private int timeLocked=0;	// temps restant avant que la porte soit déverouillée
	
	private int state=0;	// si la porte est en train de s'ouvrir, se fermer ou rien
	private int nextCheck=90;	// nombre de step avant la prochaine vérification de l'état
	
	private Polygon area;	// si le joueur est dans la zone, la porte s'ouvre
	
	public Door(int way, Point p1, Point p2, int thickness){
		super();
		this.way=way;
		this.p1=p1;
		this.p2=p2;
		this.thickness=thickness;
		solid=true;
		
		x=(p1.x+p2.x)/2;
		y=(p1.y+p2.y)/2;
		origin=new Point((int)x,(int)y);
		
		
		if(way==WAY_BOTH){
			Door door=new Door(WAY_RIGHT,new Point((int)x,(int)y),p2,thickness);
			door.setCenter(x,y);
			door.otherDoor=this;
			door.init();
			this.way=WAY_LEFT;
			this.p2=new Point((int)x,(int)y);
			otherDoor=door;
		}
		init();
		
	}
	
	public void open(){	// ouverture lors d'une collision avec une balle ou lors d'une explosion
		if(!locked){
			state=STATE_OPENING;
			nextCheck=90;
			if(otherDoor!=null){
				otherDoor.state=STATE_OPENING;
				otherDoor.nextCheck=90;
			}
		}
	}
	
	protected void init(){
		double dir=pointDirection(p1,p2);
		points.removeAllElements();
		Point p=distDir(thickness/2,dir+90);
		points.add(new Point(p1.x+p.x,p1.y+p.y));
		p=distDir(thickness/2,dir-90);
		points.add(new Point(p1.x+p.x,p1.y+p.y));
		p=distDir(thickness/2,dir-90);
		points.add(new Point(p2.x+p.x,p2.y+p.y));
		p=distDir(thickness/2,dir-270);
		points.add(new Point(p2.x+p.x,p2.y+p.y));
		
		Polygon poly=new Polygon();
		for(int i=0;i<points.size();i++){
			p=points.get(i);
			poly.addPoint(p.x,p.y);
		}
		mask=new Area(poly);
		
		double d=pointDistance(p1,p2);
		
		area=new Polygon();
		p=distDir(d,dir+90);
		area.addPoint(p1.x+p.x,p1.y+p.y);
		p=distDir(d,dir-90);
		area.addPoint(p1.x+p.x,p1.y+p.y);
		if(otherDoor==null){
			p=distDir(d,dir-90);
			area.addPoint(p2.x+p.x,p2.y+p.y);
			p=distDir(d,dir+90);
			area.addPoint(p2.x+p.x,p2.y+p.y);
		}else{
			if(way==WAY_LEFT){
				p=distDir(d,dir-90);
				Point tr=distDir(d,dir);
				p.translate(tr.x,tr.y);
				area.addPoint(p2.x+p.x,p2.y+p.y);
				p=distDir(d,dir+90);
				tr=distDir(d,dir);
				p.translate(tr.x,tr.y);
				area.addPoint(p2.x+p.x,p2.y+p.y);
			}else{
				area=new Polygon();
				p=distDir(d,dir+90);
				Point tr=distDir(d,dir+180);
				p.translate(tr.x,tr.y);
				area.addPoint(p1.x+p.x,p1.y+p.y);
				p=distDir(d,dir-90);
				tr=distDir(d,dir+180);
				p.translate(tr.x,tr.y);
				area.addPoint(p1.x+p.x,p1.y+p.y);
				p=distDir(d,dir-90);
				area.addPoint(p2.x+p.x,p2.y+p.y);
				p=distDir(d,dir+90);
				area.addPoint(p2.x+p.x,p2.y+p.y);
			}
		}
	}
	
	public void step(){
		super.step();
		Obj bot=instanceNearest(x,y,new String[]{"Player","Enemy"});
		if((bot instanceof Enemy||!locked)&&area.intersects(bot.x-bot.w/2,bot.y-bot.h/2,bot.w,bot.h)){
			state=STATE_OPENING;
			nextCheck=90;
		}else{
			nextCheck--;
		}
		if(nextCheck==0){
			state=STATE_CLOSING;
		}
		
		if(blockedTime==0){
			if(state==STATE_OPENING&&opening<100){
				opening=Math.min(100,opening+3);
			}
			if(state==STATE_CLOSING&&opening>0){
				opening--;
			}
		}else{
			blockedTime--;
		}
		if(timeLocked==0){
			locked=false;
		}else{
			timeLocked--;
		}
		
		if(state!=STATE_NOTHING){
			Polygon poly=new Polygon();
			
			Point pA=points.get(0);
			Point pB=points.get(1);
			
			if(way==WAY_RIGHT){
				pA=points.get(2);
				pB=points.get(3);
			}
			
			poly.addPoint(pA.x,pA.y);
			poly.addPoint(pB.x,pB.y);
			
			double xx=(p2.x-p1.x*1.0)/100.0;
			double yy=(p2.y-p1.y*1.0)/100.0;
			
			double val=100-opening;
			
			if(way==WAY_RIGHT) val=-val;
			
			poly.addPoint((int)(pB.x+val*xx),(int)(pB.y+val*yy));
			poly.addPoint((int)(pA.x+val*xx),(int)(pA.y+val*yy));
			mask=new Area(poly);
		}
	}
	
	public void triggerAlarm(int id, Obj by){
		locked=true;
		timeLocked=300;
		state=STATE_CLOSING;
		if(otherDoor!=null&&!otherDoor.isLocked()) otherDoor.triggerAlarm(id,by);
	}
	
	public boolean isLocked(){
		return locked;
	}
	
	public void block(int time){
		blockedTime=time;
	}
	
	public void draw(Graphics2D g){
		g.setColor(Color.darkGray);
		g.fill(mask);
	}
	
	protected void setCenter(double x, double y){
		this.x=x;
		this.y=y;
		origin=new Point((int)x,(int)y);
	}
	
	public int getThickness(){
		return thickness;
	}
}
