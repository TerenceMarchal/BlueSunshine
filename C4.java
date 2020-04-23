import java.awt.Color;
import java.awt.Graphics2D;


public class C4 extends Explosive{

	private int alpha;
	
	public C4(Weapon weapon, double x, double y, double angle, double damage) {
		super(weapon,x,y,damage);
		this.x=x;
		this.y=y;
		this.weapon=weapon;
		this.angle=angle;
		weapon.getBot().addC4(this);
		w=40;
		h=25;
	}

	public void draw(Graphics2D g){
		super.draw(g);
		if(step==-1){
			g.rotate(Math.toRadians(angle),x,y);
			Color c=weapon.getBot().getColor();
			int a=(int)Math.max(0,255-pointDistance(this,Screen.player)*255/500);
			if(checkCollisionLine(pos(),Screen.player.pos(),true, new Obj[]{Screen.player,Screen.player.getLeftWeapon(),Screen.player.getRightWeapon()}).isCollision()) a=0;
			alpha+=(a-alpha)*0.2;
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
			g.drawRect((int)x-w/2,(int)y-h/2,w,h);
			g.rotate(-Math.toRadians(angle),x,y);
		}
	}
	
}
