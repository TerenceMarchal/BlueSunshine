import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Vector;


public class Bot extends Obj{

	protected boolean moveRight, moveLeft, moveUp, moveDown, turbo;
	protected double defaultMaxSpeed=12, maxSpeed=defaultMaxSpeed, speedLeft, speedRight, speedUp, speedDown;	// quand on sprint maxSpeed=1.5*defautlMaxSpeed
	protected boolean stabilized=false;	// pour mieux viser (avance moins vite) : bouton central de la souris
	protected int timeStabilized=0;	// si clic court on fait exploser le C4
	protected Weapon leftWeapon, rightWeapon;
	protected double life=100;
	protected int paralyzedTime=0;	// temps restant pendant lequel le joueur est paralysé
	protected int blindedTime=0;	// temps restant pendant lequel le joueur est aveuglé
	protected int turboTime=300;	// temps de turbo restant (en step*2)
	protected Area body, spiral, eye;
	protected double angleSpiral=0;	// angle de l'hélice, évolue en fonction de la vitesse
	protected Color color=Screen.COLOR_TEAM_B;
	protected Vector<C4> allC4=new Vector<C4>();
	
	protected int alpha=0;	// opacité
	
	protected double direction;
	
	public Bot(){
		super();
		w=75;
		h=75;
		angle=90;
		speed=maxSpeed;
		solid=true;
		mask=new Area(new Ellipse2D.Double(0,0,w,h));
		origin=new Point(w/2,h/2);
		leftWeapon=new Weapon(this,Weapon.POS_LEFT,Weapon.ASSAULT_RIFLE);
		rightWeapon=new Weapon(this,Weapon.POS_RIGHT,Weapon.ROCKET_LAUNCHER);
		initSkin();
	}
	
	
	public void step(){
		super.step();
		
		
		if(stabilized){
			timeStabilized++;
		}else{
			timeStabilized=0;
		}
		
		angleSpiral+=8+(speedUp+speedDown+speedLeft+speedRight)/4;
		angleSpiral%=360;
		if(paralyzedTime>0) paralyzedTime--;
		if(blindedTime>0) blindedTime--;
		
		CollisionEvent ce = checkCollision("Mine");
		if(ce.isCollision()){
			Mine mine=((Mine)ce.getObj());
			if(mine.isArmed()) mine.explode();
		}
		
	}
	
	public void draw(Graphics2D g){
		drawBot(g);
	}
	
	/*public void setPath(Point to){
		if(closedList.size()==0||pointDistance(closedList.get(closedList.size()-1).getPoint(),to)>100){
			Rectangle map=Screen.getMapRect();
			Node nodeTo=new Node((int)Math.round((to.x-map.x*1.0)/Screen.sizeNavMap),(int)Math.round((to.y-map.y*1.0)/Screen.sizeNavMap));
			Node node=new Node((int)Math.round((x-map.x*1.0)/Screen.sizeNavMap),(int)Math.round((y-map.y*1.0)/Screen.sizeNavMap));
			
			if(Screen.navMap[nodeTo.x][nodeTo.y]==Screen.NAV_RED){
				System.out.println("POSITION INATEIGNABLE (MUR)");
				return;
			}
			
			openList.clear();
			closedList.clear();
			closedList.add(node);
			
			while(node.x!=nodeTo.x||node.y!=nodeTo.y){
				for(int i=0;i<8;i++){
					Node nextNode=null;
					switch(i){
						case 0:nextNode=new Node(node.x+1,node.y);break;
						case 1:nextNode=new Node(node.x+1,node.y-1);break;
						case 2:nextNode=new Node(node.x,node.y-1);break;
						case 3:nextNode=new Node(node.x-1,node.y-1);break;
						case 4:nextNode=new Node(node.x-1,node.y);break;
						case 5:nextNode=new Node(node.x-1,node.y+1);break;
						case 6:nextNode=new Node(node.x,node.y+1);break;
						case 7:nextNode=new Node(node.x+1,node.y+1);break;
					}
					byte val=Screen.navMap[nextNode.x][nextNode.y];
					if(val!=Screen.NAV_RED&&!closedList.contains(nextNode)){
						int value=(int)(Math.pow(nodeTo.x-nextNode.x,2)+Math.pow(nodeTo.y-nextNode.y,2));
						if(val==Screen.NAV_ORANGE) value*=1.5;
						if(openList.contains(nextNode)){
							if(value<nextNode.value){
								nextNode.value=value;
								nextNode.parent=node;
							}
						}else{
							nextNode.value=value;
							nextNode.parent=node;
							openList.add(nextNode);
						}
					}
				}
				if(openList.size()==0){
					System.out.println("PAS DE CHEMIN");
					return;
				}else{
					Node bestNode=openList.get(0);
					for(int i=0;i<openList.size();i++){
						Node n=openList.get(i);
						if(n.value<bestNode.value) bestNode=n;
					}
					closedList.add(bestNode);
					openList.remove(bestNode);
					node=bestNode;
				}
			}
			Node lastNode=closedList.get(closedList.size()-1);
			for(int i=closedList.size()-2;i>=0;i--){
				node=closedList.get(i);
				if(Math.abs(node.x-lastNode.x)>1||Math.abs(node.y-lastNode.y)>1){
					closedList.remove(i);
				}else{
					lastNode=node;
				}
			};
			path.clear();
			for(int i=0;i<closedList.size();i++){
				Node n=closedList.get(i);
				path.add(new Point((n.x)*Screen.sizeNavMap+map.x,(n.y)*Screen.sizeNavMap+map.y));
			}
			pointTo=0;
			openList.clear();
			closedList.clear();
		}
	}*/
	
	public void setLeftWeapon(int weapon){
		leftWeapon=new Weapon(this,Weapon.POS_LEFT,weapon);
	}
	
	public void setRightWeapon(int weapon){
		rightWeapon=new Weapon(this,Weapon.POS_RIGHT,weapon);
	}
	
	public void setLife(int life){
		this.life=life;
	}
	
	
	
	protected void initSkin(){
		body=new Area(new Ellipse2D.Double(0,0,w,h));
		body.subtract(new Area(new Ellipse2D.Double(w/8,h/8,w*3/4,h*3/4)));
		body.add(new Area(new Arc2D.Double(0,0,w,h,-20,220,Arc2D.PIE)));
		
		spiral=new Area(new Arc2D.Double(w/8,h/8,w*3/4,h*3/4,0,40,Arc2D.PIE));
		spiral.add(new Area(new Arc2D.Double(w/8,h/8,w*3/4,h*3/4,120,40,Arc2D.PIE)));
		spiral.add(new Area(new Arc2D.Double(w/8,h/8,w*3/4,h*3/4,240,40,Arc2D.PIE)));
		
		eye=new Area(new Arc2D.Double(w/8,h/8,w*3/4,h*3/4,20,140,Arc2D.PIE));
		eye.subtract(new Area(new Arc2D.Double(w/8+4,h/8+4,w*3/4-8,h*3/4-8,20,140,Arc2D.PIE)));
	}
	
	public void paralyze(int time){
		paralyzedTime=time;
	}
	
	public boolean isParalyzed(){
		return paralyzedTime>0;
	}
	
	public int getParalyzedTime(){
		return paralyzedTime;
	}
	
	public void blind(int time){
		blindedTime=time;
	}
	
	public boolean isBlinded(){
		return blindedTime>0;
	}
	
	public int getBlindedTime(){
		return blindedTime;
	}
	
	
	
	protected void drawBot(Graphics2D g){
		if(!(this instanceof Player)){
			Area borders=new Area(new Rectangle(Screen.W,Screen.H));
			borders.transform(AffineTransform.getRotateInstance(-Math.toRadians(Screen.player.angle+90),Screen.W/2,50));
			borders.transform(AffineTransform.getTranslateInstance(Screen.player.x-Screen.W/2,Screen.player.y-50));
			Rectangle bounds=new Rectangle((int)x,(int)y,w,h);
			if(!borders.intersects(bounds)&&!borders.contains(bounds)) return;
		}
		if(leftWeapon!=null) leftWeapon.drawWeapon(g);
		if(rightWeapon!=null) rightWeapon.drawWeapon(g);
		
		Color color=getColor();
		
		if(!(this instanceof Player)){
			int a=255;
			if(!Screen.player.canSee(this)&&!Screen.playerInvisible) a=0;
			alpha+=(a-alpha)*0.2;
			color=new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha);
		}
		if(Screen.showAlwaysEnemies) alpha=255;
		if(alpha>0){
			g.setColor(color);
			
			g.rotate(Math.toRadians(-angle-angleSpiral),x,y);
			g.translate(x-w/2,y-h/2);
			g.fill(spiral);
			g.translate(-x+w/2,-y+h/2);
			g.rotate(Math.toRadians(angle+angleSpiral),x,y);
			
			g.setColor(new Color(0,0,0,alpha));
			g.rotate(Math.toRadians(-angle+90),x,y);
			g.translate(x-w/2,y-h/2);
			g.fill(body);
			double al=1;	// entre 0 et 1
			if(isParalyzed()||isBlinded()){
				al=Screen.T%30/30.0;
				if(Screen.T%60<30) al=1-al;
			}
			color=new Color(color.getRed(),color.getGreen(),color.getBlue(),(int)(alpha*al));
			g.setColor(color);
			g.fill(eye);
			g.translate(-x+w/2,-y+h/2);
			
			g.rotate(Math.toRadians(angle-90),x,y);
		}
	}
	
	public boolean isStabilized(){
		return stabilized;
	}
	
	public void moveOutsideObj(Obj obj, double direction, boolean onlySolid, int precision){
		while((obj.isSolid()||!onlySolid)&&(checkCollision(obj).isCollision()||
				(leftWeapon.checkCollision(obj).isCollision()&&(!(obj instanceof Wall)||!((Wall)obj).isHalfHeight()))||
				(rightWeapon.checkCollision(obj).isCollision()&&(!(obj instanceof Wall)||!((Wall)obj).isHalfHeight())))){
			moveToDirection(direction,precision);
			leftWeapon.updatePos();
			rightWeapon.updatePos();
		}
	}
	
	public void setColor(Color c){
		color=c;
	}
	
	public int getAlpha(){
		return alpha;
	}
	
	public double getLife(){
		return life;
	}
	
	public void hurt(double damage){
		life=Math.max(0,life-damage);
		if(life==0){
			die();
		}
		
	}
	
	protected void die(){
		Screen.removeObj(this);
	}
	
	public int getTurbo(){
		return turboTime;
	}
	
	public Color getColor(){
		return color;
		//return new Color(0,255,127);
		//return new Color(55,245,46);
	}
	
	public Weapon getLeftWeapon(){
		return leftWeapon;
	}

	public Weapon getRightWeapon(){
		return rightWeapon;
	}
	public void addC4(C4 c4){
		allC4.add(c4);
	}
	
	/**
	 * Retourne un chiffre négatif si other est à gauche de l'objet courant, 0 s'ils sont sur le même axe, positif si other est à droite de l'objet courant
	 */
	public double getRelativePos(Obj other){
		Point vecThis=distDir(1000,angle);
		return vecThis.x*(other.y-y)-vecThis.y*(other.x-x);
	}
	
	/**
	 * Indique si ce robot peut voir le robot spécifié
	 */
	public boolean canSee(Bot bot){
		if(Screen.playerInvisible)return false; 
		int minX=(Math.min(Screen.topLeft.x,Math.min(Screen.topRight.x,Math.min(Screen.bottomRight.x,Screen.bottomLeft.x))))-w/2;
		int maxX=(Math.max(Screen.topLeft.x,Math.max(Screen.topRight.x,Math.max(Screen.bottomRight.x,Screen.bottomLeft.x))))+w/2;
		int minY=(Math.min(Screen.topLeft.y,Math.min(Screen.topRight.y,Math.min(Screen.bottomRight.y,Screen.bottomLeft.y))))-h/2;
		int maxY=(Math.max(Screen.topLeft.y,Math.max(Screen.topRight.y,Math.max(Screen.bottomRight.y,Screen.bottomLeft.y))))+h/2;
		if(!new Rectangle(minX,minY,maxX-minX,maxY-minY).contains(bot.pos())) return false;
		if(this instanceof Player||(getDiffAngle(angle,pointDirection(pos(),bot.pos()))<90)){
			Obj[] objs=new Obj[]{this,leftWeapon,rightWeapon,bot,bot.getLeftWeapon(),bot.getRightWeapon()};
			
			return !checkCollisionLine(pos(),bot.pos(),false,objs).isCollision();
		}
		return false;
	}
	
	/**
	 * Calcule les deux points sur le bord du bot par rapport à la tangeante à la normale passant par le bot et le point passé en argument
	 */
	public Vector<Point> getExtremsPointsComparedTo(Point ref){
		Vector<Point> points=new Vector<Point>();
		
		Point p=distDir(w,pointDirection(ref,pos())+90);
		p.translate((int)x,(int)y);
		points.add(p);
		p=distDir(w,pointDirection(ref,pos())-90);
		p.translate((int)x,(int)y);
		points.add(p);
		
		return points;
	}
}
