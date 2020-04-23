import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;


public class Mine extends Explosive{

	private int timeArmed=30;
	private int alpha;
	
	public Mine(Weapon weapon, double x, double y, double damage) {
		super(weapon,x,y,damage);
		w=40;
		h=40;
		mask=new Area(new Ellipse2D.Double(-w/2,-h/2,w,h));
	}
	
	public void step(){
		super.step();
		timeArmed--;
	}
	
	public void draw(Graphics2D g){
		super.draw(g);
		if(step==-1){
			g.rotate(Math.toRadians(angle),x,y);
			Color c=weapon.getBot().getColor();
			int a=(int)Math.max(0,255-pointDistance(this,Screen.player)*255/500);
			if(checkCollisionLine(pos(),Screen.player.pos(),true,new Obj[]{Screen.player,Screen.player.getLeftWeapon(),Screen.player.getRightWeapon()}).isCollision()) a=0;
			alpha+=(a-alpha)*0.2;
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
			g.drawOval((int)x-w/2,(int)y-h/2,w,h);
			g.rotate(-Math.toRadians(angle),x,y);
		}
	}
	
	public boolean isArmed(){
		return timeArmed<=0;
	}

}
