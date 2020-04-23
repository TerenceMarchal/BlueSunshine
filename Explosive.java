import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;


public class Explosive extends Obj{

	private double power;	// puissance (=rayon) de l'explosion
	private Vector<Point> intersections=new Vector<Point>();
	private Vector<Point> points=new Vector<Point>();
	private Polygon explosion;
	protected Weapon weapon;
	protected int step=-1;
	
	public Explosive(Weapon weapon, double x, double y, double power){
		this.weapon=weapon;
		this.x=x;
		this.y=y;
		this.power=power;
	}
	
	public void step(){
		super.step();
		if(step>0) step--;
		if(step==3){	// explosion :
			Rectangle2D.Double rectExplosion=new Rectangle2D.Double(x-power/2,y-power/2,power,power);
			for(int i=0;i<Screen.getObjsNumber();i++){
				Obj obj=Screen.getObj(i);
				if(obj!=this){
					double dist=pointDistance(pos(),obj.pos());
					if(obj instanceof Wall&&((Wall)obj).isDestructible()&&obj.getMask().intersects(rectExplosion)){
						Area area=obj.getMask();
						area.subtract(new Area(explosion));
						obj.setMask(area);
					}
					if(obj instanceof Explosive&&dist<power/2){
						((Explosive)obj).explode();
					}
					if(obj instanceof Bot){
						Area mask=obj.getMask();
						mask.transform(AffineTransform.getTranslateInstance(obj.x-obj.origin.x,obj.y-obj.origin.y));
						if(mask.intersects(rectExplosion)&&checkCollision(obj,new Area(explosion)).isCollision()){
							((Bot)obj).hurt(Math.max(0,(power/2-dist)*power/(power/2)));
						}
					}
					if(obj instanceof Door){
						Area circle=new Area(new Ellipse2D.Double(x-power/2,y-power/2,power,power));
						if(checkCollision(obj,circle).isCollision()) ((Door)obj).open();
					}
				}
			}
			for(int i=Screen.getImpactsNumber()-1;i>0;i--){
				Point p=Screen.getImpact(i);
				if(rectExplosion.contains(p)&&explosion.contains(p)){
					Screen.removeImpact(i);
				}
			}
			for(int i=0;i<Enemy.allEnemies.size();i++){
				Enemy en=Enemy.allEnemies.get(i);
				if(Math.pow(en.x-x,2)+Math.pow(en.y-y,2)<Math.pow(1000,2)){
					en.hearShot(pos());
				}
			}
		}
		if(step==0) Screen.removeObj(this);
	}
	
	public void draw(Graphics2D g){
		if(step>0){
			g.setColor(weapon.getBot().getColor());
			Polygon poly=new Polygon();
			for(int i=0;i<explosion.npoints;i++){
				Point p=new Point(explosion.xpoints[i],explosion.ypoints[i]);
				int xx=(int)(explosion.xpoints[i]-x);
				int yy=(int)(explosion.ypoints[i]-y);
				int pos=5-(step-3);
				if(step<3) pos=step;
				poly.addPoint((int)(x+getProduitCroixCarre(pos,xx,5)),(int)(y+getProduitCroixCarre(pos,yy,5)));//+yy*(5-step)/5
			}
			g.fill(poly);
		}
		/*if(step>0){
			g.setColor(Color.red);
			Area exp=(Area)explosion.clone();
			double val=1-step/30.0;
			double lastVal=1-(step+1)/30.0;
			
			
			AffineTransform trans=new AffineTransform();
			//trans.setToScale(val,val);
			trans.setToScale(val,val);
			trans.translate(trans.getScaleX(),trans.getScaleY());
			
			double X=trans.getScaleX();
			double Y=trans.getScaleY();
			g.drawString((int)X+";"+(int)Y,(int)x-100,(int)y-100);
			
			exp.transform(trans);
			//exp.transform(AffineTransform.getTranslateInstance(-x+X,-y+Y));
			g.fill(exp);
		}*/
	}
	
	public void explode(){
		if(explosion==null){
			step=8;
			speed=0;
			Polygon poly=new Polygon();
			for(int i=0;i<360;i+=8){
				Point p=distDir(power/2-Math.random()*power/8,i-4+Math.random()*8);//power*3/8+Math.random()*power/4
				poly.addPoint((int)x+p.x,(int)y+p.y);
			}
			explosion=poly;
			File[] sounds=new File("data/sound/weapons/explosion/").listFiles();
			File f=sounds[(int)Math.floor(Math.random()*sounds.length)];
			new Sound(this,f,false);
		}
		/*Rectangle2D.Double rectExplosion=new Rectangle2D.Double(x-power/4,y-power/4,power/2,power/2);
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			if(obj!=this){
				double dist=pointDistance(pos(),obj.pos());
				if(obj instanceof Wall&&((Wall)obj).isDestructible()&&obj.getMask().intersects(rectExplosion)){
					Area area=obj.getMask();
					area.subtract(explosion);
					obj.setMask(area);
				}
				if(obj instanceof Explosive&&dist<power){
					((Explosive)obj).explode();
				}
				if(obj instanceof Player){
					((Player)obj).hurt(Math.max(0,power-dist));
				}
			}
		}
		Screen.removeObj(this);*/
	}
	
	public void oldExplode(){
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			if(obj instanceof Wall){
				Wall wall=(Wall)obj;
				if(wall.isDestructible()){
					intersections.removeAllElements();
					
					// on calcule toutes les intersections :
					Vector<Point> points=wall.getPoints();
					for(int j=0;j<points.size();j++){
						Point p=points.get(j);
						Point nextP=points.get((j<points.size()-1)?j+1:0);
						
						// on calcule l'équation du type ax+by=c du mur :
						
						double a=-(nextP.y-p.y);
						double b=nextP.x-p.x;
						double c=a*p.x+b*p.y;
						
						double[] equWall = new double[]{a,b,c};
						
						// on calcule l'équation du type ax+by=c de la perpendiculaire au mur passant par le centre du cercle :
						
						double[] equPerp=new double[]{-b,a,-b*x+a*y};
						
						// valeur de y en fonction de x (y=mx+n) :
						
						double m=(nextP.y-p.y*1.0)/(nextP.x-p.x);
						double n=p.y-m*p.x;
						
						
						
						// equation à résoudre (ax+by=c) :
						
						a=equWall[0]+equPerp[0];
						b=equWall[1]+equPerp[1];
						c=equWall[2]+equPerp[2];
						
						a+=b*m;
						c-=b*n;
						
						double xx=c/a;
						double yy=(equWall[2]-equWall[0]*xx)/equWall[1];
						
						double d=pointDistance(x,y,xx,yy);
						
						if(d<power){	// in y a bien une intersection entre le segment de mur et le cercle de l'explosion
							double dist=Math.sqrt(Math.pow(power,2)-Math.pow(pointDistance(xx,yy,x,y),2));
							double dir=pointDirection(p,nextP);
							
													
							Point pp=distDir(dist,dir+180);
							pp.translate((int)xx,(int)yy);
							if((p.x-pp.x)*(nextP.x-pp.x)+(p.y-pp.y)*(nextP.y-pp.y)<=0) intersections.add(pp);	// produit scalaire pour vérifier que le point est bien sur le segment de mur
							
							pp=distDir(dist,dir);
							pp.translate((int)xx,(int)yy);
							if((p.x-pp.x)*(nextP.x-pp.x)+(p.y-pp.y)*(nextP.y-pp.y)<=0) intersections.add(pp);
						}
					}
					
					// on tri toutes les intersections par angle croissant par rapport au centre de l'exlosion (tri à bulle) : 
					boolean terminated;
					do{
						terminated=true;
						for(int j=0;j<intersections.size()-1;j++){
							Point p=intersections.get(j);
							Point nextP=intersections.get(j+1);
							if(pointDirection(x,y,p.x,p.y)>pointDirection(x,y,nextP.x,nextP.y)){
								intersections.set(j,nextP);
								intersections.set(j+1,p);
								terminated=false;
							}
						}
					}while(!terminated);
					
					// on rajoute les points :
					
					Point p0=intersections.get(0);
					Point p1=intersections.get(1);
					
					int precision=6;
					int mod=(wall.getMask().contains(new Rectangle2D.Double((p0.x+p1.x)/2-2,(p0.y+p1.y)/2-2,5,5))?0:1);
					
					for(int j=0;j<intersections.size();j++){
						if(j%2==mod){
							Point p=intersections.get(j);
							Point nextP=intersections.get((j<intersections.size()-1)?j+1:0);
							
							double min=pointDirection(pos(),p);
							double max=pointDirection(pos(),nextP);
							System.out.println(max);
							max=min+getDiffAngle(min,max);
							System.out.println(min+"/"+max);
							
							for(double k=min;k<max;k+=precision){
								Point pp=distDir(power,k);
								pp.translate((int)x,(int)y);
								this.points.add(pp);
							}
							
						}
					}
				}
			}
		}
	}
		
}
