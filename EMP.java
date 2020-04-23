import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Vector;


public class EMP extends Obj{

	private int power=224;	// puissance (=rayon) de l'explosion
	protected Weapon weapon;
	protected int step=-1;
	private double direction, speed;
	private Area movementLine;
	
	public EMP(Weapon weapon, double x, double y, double direction, double speed){
		super();
		this.weapon=weapon;
		this.x=x;
		this.y=y;
		this.direction=direction;
		this.speed=speed;
		w=8;
		h=12;
		mask=new Area(new Rectangle2D.Double(0,0,w,h));
		Polygon rect=new Polygon();
		Point p=distDir(w/2,direction+90);
		rect.addPoint(p.x,p.y);
		rect.addPoint(-p.x,-p.y);
		Point tr=distDir(speed,direction);
		rect.addPoint(tr.x-p.x,tr.y-p.y);
		rect.addPoint(tr.x+p.x,tr.y+p.y);
		movementLine=new Area(rect);
	}
	
	public void step(){
		super.step();
		moveToDirection(direction,speed);
		
		Area line=(Area) movementLine.clone();
		line.transform(AffineTransform.getTranslateInstance(x,y));
		
		CollisionEvent ce=checkCollision(line,true,false);
		if(ce.isCollision()){
			if(ce.getObj()!=weapon&&ce.getObj()!=weapon.getBot()) explode();
		}
		
		if(step>0) step--;
		if(step==3){
			Rectangle2D.Double rectExplosion=new Rectangle2D.Double(x-power,y-power,power*2,power*2);
			for(int i=0;i<Screen.getObjsNumber();i++){
				Obj obj=Screen.getObj(i);
				if(obj!=this){
					if(obj instanceof Player||obj instanceof Enemy||obj instanceof Captor||obj instanceof Door){
						double d=pointDistance(pos(),obj.pos());
						if(d<power){
							if(obj instanceof Player||obj instanceof Enemy){
								((Bot)obj).paralyze((int)(power-d));
								((Bot)obj).blind((int)(power-d));
							}
							if(obj instanceof Door){
								((Door)obj).block(75);
							}
							if(obj instanceof Captor){
								((Captor)obj).blur(150);
							}
						}
					}
				}
			}
		}
		if(step==0) Screen.removeObj(this);
	}
	
	public void draw(Graphics2D g){
		if(step>0){
			g.setColor(weapon.getBot().getColor());
			double r=getProduitCroixCarre(8-step,power,8);
			Stroke oldStroke = g.getStroke();
			g.setStroke(new BasicStroke(step,BasicStroke.JOIN_MITER,BasicStroke.CAP_SQUARE));
			g.drawOval((int)(x-r),(int)(y-r),(int)(r*2),(int)(r*2));
			g.setStroke(oldStroke);
		}
		if(step==-1){
			g.setColor(Color.black);
			Area line=(Area) movementLine.clone();
			line.transform(AffineTransform.getTranslateInstance(x,y));
			g.fill(line);
		}
	}
	
	public void explode(){
		if(step==-1){
			step=8;
			speed=0;
		}
	}
		
}
