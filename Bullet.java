import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;


public class Bullet extends Obj{

	private double dir, damage;
	private double lastX, lastY;
	private Obj lastCollision;	// obj de la dernuère collision, pour pouvoir traverser les murs
	private Weapon weapon;
	
	public Bullet(Weapon weapon, double x, double y, double dir, double speed, double damage){
		super();
		this.x=x;
		this.y=y;
		lastX=x;
		lastY=y;
		this.weapon=weapon;
		dir+=-weapon.getCurrentDispersion()+weapon.getCurrentDispersion()*Math.random()*2;
		this.dir=dir;
		this.speed=speed;
		this.damage=damage*0.8+Math.random()*damage*0.4;
		if(weapon.getType()!=Weapon.SHOTGUN){
			File[] sounds=new File("data/sound/weapons/"+weapon.getType()+"/").listFiles();
			if(sounds.length>0){
				File f=sounds[(int)Math.floor(Math.random()*sounds.length)];
				new Sound(this,f,false);
			}
		}
		//new Sound(null,new File("data/sound/weapons/douille.wav"),false);
	}
	
	public void draw(Graphics2D g){
		if(weapon.getBot() instanceof Player) g.setColor(Screen.COLOR_TEAM_A); else g.setColor(Screen.COLOR_TEAM_B);
		g.drawLine((int)((lastX+x)/2),(int)((lastY+y)/2),(int)x,(int)y);
	}
	
	public void step(){
		super.step();
		lastX=x;
		lastY=y;
		moveToDirection(dir,speed);
		CollisionEvent ce=checkCollisionLine(lastX,lastY,x,y,false);
		if(ce.isCollision()){
			Obj obj=ce.getObj();
			if(obj!=lastCollision){
				if(obj instanceof Wall){
					Wall wall=((Wall)obj);
					if(wall.isDestructible()){
						double val=wall.getThickness();
						damage-=val;
						speed-=val/2;
						if(damage<=0||speed<=0){
							addImpact(ce);
							Screen.removeObj(this);
						}
					}else{
						addImpact(ce);
						Screen.removeObj(this);
					}
				}
				if(obj instanceof Door){
					((Door)obj).open();
					Screen.removeObj(this);
				}
				if(obj instanceof Bot&&!obj.equals(weapon.getBot())){
					Bot bot=(Bot)obj;
					if(damage>0) bot.hurt(damage);
					
					if(weapon.getType()==Weapon.TASER){
						bot.paralyze(150);
					}
					
					if(weapon.getType()==Weapon.DAZER){
						bot.blind(150);
					}
					
					Screen.removeObj(this);
				}
			}
			lastCollision=obj;
		}
	}
	
	private void addImpact(CollisionEvent ce){
		if(Screen.showImpacts){
			Point p=ce.getPoint();
			x=p.x;
			y=p.y;
			while(ce.getObj().getMask().contains(new Point2D.Double(x,y))){
				p=distDir(1000,dir+180);
				x+=p.x/1000.0;
				y+=p.y/1000.0;
			}
			x-=p.x/1000.0;
			y-=p.y/1000.0;
			Screen.addImpact(pos());
		}
	}
	
}
