import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;


public class Rocket extends Explosive{

	private double direction;
	private Area movementLine;	// rectangle de la collision entre chaque mouvement (comme un collisionLine mais avec une épaisseur)
	
	public Rocket(Weapon weapon, double x, double y, double direction, double speed, double damage) {
		super(weapon,x,y,damage);
		direction+=-weapon.getCurrentDispersion()+weapon.getCurrentDispersion()*Math.random()*2;
		this.direction=direction;
		this.speed=speed;
		w=8;
		h=16;
		mask=new Area(new Rectangle2D.Double(0,0,w,h));
		Polygon rect=new Polygon();
		Point p=distDir(w/2,direction+90);
		rect.addPoint(p.x,p.y);
		rect.addPoint(-p.x,-p.y);
		Point tr=distDir(speed,direction);
		rect.addPoint(tr.x-p.x,tr.y-p.y);
		rect.addPoint(tr.x+p.x,tr.y+p.y);
		movementLine=new Area(rect);
		//speed/=4;
	}

	public void step(){
		super.step();
		double lastX=x, lastY=y;
		moveToDirection(direction,speed);
		
		Area line=(Area) movementLine.clone();
		line.transform(AffineTransform.getTranslateInstance(x,y));
		
		CollisionEvent ce=checkCollision(line,true,false);
		if(ce.isCollision()){
			if(ce.getObj()!=weapon&&ce.getObj()!=weapon.getBot()) explode();
		}
	}
	
	public void draw(Graphics2D g){
		super.draw(g);
		if(step==-1){
			g.setColor(Color.black);
			/*g.translate(x-4,y-4);
			g.fill(mask);
			g.translate(-x+4,-y+4);*/
			Area line=(Area) movementLine.clone();
			line.transform(AffineTransform.getTranslateInstance(x,y));
			g.fill(line);
		}
	}
	
}
