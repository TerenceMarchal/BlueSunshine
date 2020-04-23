import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;


public class Obj {

	public int id;
	private static int nextId=0;
	public int w,h;
	public double x,y,angle;
	protected Area mask;	// masque de collision
	protected Point origin=new Point(0,0);	// origine : le masque est translaté de l'opposé des coordonées et tourne autour de ce point
	public double speed;
	protected boolean solid=false;
	
	public boolean mustSendData=false;
	protected static String SEP="&%&";
	
	public Obj(){
		Screen.addObj(this);
		id=nextId++;
	}
	
	public void step(){
		
	}
	
	public void draw(Graphics2D g){
		
	}
	
	/*public void createOnServer(){
		if(Screen.server!=null){
			while(Screen.server.waitingForId!=null) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {e.printStackTrace();}
			}
			Server.waitingForId=this;
			Screen.server.sendMessage("CREATE");
		}
	}
	
	public void sendUpdateArgs(String args){	// envoie les données de l'objet au serveur
		if(Screen.server!=null&&mustSendData){
			Screen.server.sendMessage("UPDATE "+id+SEP+getClass().getName()+SEP+args);
		}
	}
	
	public void sendCreateArgs(String args){	// envoie les données de l'objet au serveur
		if(Screen.server!=null&&mustSendData){
			Screen.server.sendMessage("SET "+id+SEP+getClass().getName()+SEP+args);
		}
	}
	
	public void create(int id, String[] args){	// si l'objet a été créé par un autre joueur
		this.id=id;
	}
	
	public void update(String[] args){	// on update l'objet avec les arguments du serveur (premier argument = id)
		
	}
	
	protected int toInt(String str){
		return Integer.parseInt(str);
	}
	
	protected double toDouble(String str){
		return Double.parseDouble(str);
	}*/
	
	public Point distDir(double dist, double dir){
		dir=Math.toRadians(360-dir);
		int x=(int)(dist*Math.cos(dir));
		int y=(int)(dist*Math.sin(dir));
		return new Point(x,y);
	}
	
	public double pointDistance(double x1, double y1, double x2, double y2){
		return Math.sqrt(Math.pow(Math.abs(x1-x2),2)+Math.pow(Math.abs(y1-y2),2));
	}
	
	public double pointDistance(Point a, Point b){
		return pointDistance(a.x,a.y,b.x,b.y);
	}
	
	public double pointDistance(Obj a, Obj b){
		return pointDistance(a.x,a.y,b.x,b.y);
	}
	
	public double pointDirection(double x1, double y1, double x2, double y2){
		double d=Math.toDegrees(Math.asin(Math.abs(y1-y2)/pointDistance(x1,y1,x2,y2)));
		if(x2>x1){
			if(y2>y1) d=360-d;
		}else{
			if(y2>y1) d+=180; else d=180-d;
		}
		return d%360;
	}
	
	public double pointDirection(Point p1, Point p2){
		return pointDirection(p1.x,p1.y,p2.x,p2.y);
	}
	
	public double pointDirection(Obj a, Obj b){
		return pointDirection(a.x,a.y,b.x,b.y);
	}
	
	public void setMask(Area area){
		mask=area;
	}
	
	/**
	 * @return <b>ATTENTION : </b> retourne un clone du masque
	 */
	public Area getMask(){
		if(mask!=null) return (Area) mask.clone();
		return null;
	}
	
	public Point getOrigin(){
		return origin;
	}
	
	public Point pos(){
		return new Point((int)x,(int)y);
	}
	
	public boolean isSolid(){
		return solid;
	}
	
	/**
	 * Fait sortir l'objet courant de l'objet spécifié dans la direction spécifiée
	 * @param obj
	 * @param direction
	 * @param precision <b>>=1</b>
	 */
	public void moveOutsideObj(Obj obj, double direction, boolean onlySolid, int precision){
		while(checkCollision(obj).isCollision()&&(obj.isSolid()||!onlySolid)) moveToDirection(direction,precision);
	}
	
	protected void moveToDirection(double direction, double speed){
		direction%=360;
		double X=Math.cos(Math.toRadians(360-direction))*speed;
		double Y=Math.sin(Math.toRadians(360-direction))*speed;
		x+=X;
		y+=Y;
		/*double Y=Math.sqrt(Math.pow(speed,2)-Math.pow(X,2));
		x+=X;
		if(direction<180) y-=Y; else y+=Y;*/
	}
	
	/**
	 * Vérifie si il y a collision entre this et tous les autres objets 
	 * halfHeightToo indique si ton tient aussi compte des murs à mi-hauteur
	 */
	protected CollisionEvent checkCollision(boolean onlySolid, boolean halfHeightToo){
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			if(obj!=this&&(obj.isSolid()||!onlySolid)&&(halfHeightToo||!(obj instanceof Wall)||!((Wall)obj).isHalfHeight())){
				CollisionEvent ce=checkCollision(obj);
				if(ce.isCollision()) return ce;
			}
		}
		return new CollisionEvent(null,null);
	}
	
	/**
	 * Vérifie si il y a collision entre this et un objet de la classe spécifiée
	 */
	protected CollisionEvent checkCollision(String classe){
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			if(obj!=this&&obj.getClass().getName().equals(classe)){
				CollisionEvent ce=checkCollision(obj);
				if(ce.isCollision()) return ce;
			}
		}
		return new CollisionEvent(null,null);
	}
	
	/**
	 * Vérifie si il y a collision entre this et tous les autres objets exepté l'objet spécifié
	 */
	protected CollisionEvent checkCollision(boolean onlySolid, boolean halfHeightToo, Obj except){
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			if(obj!=this&&obj!=except&&(obj.isSolid()||!onlySolid)&&(halfHeightToo||!(obj instanceof Wall)||!((Wall)obj).isHalfHeight())){
				CollisionEvent ce=checkCollision(obj);
				if(ce.isCollision()) return ce;
			}
		}
		return new CollisionEvent(null,null);
	}
	
	/**
	 * retourne une valeur entre 0 et valMax de facon quadratique en fonction de pos sur etendu
	 * ex : getProduitCroixCarre(25,100,100)=6.25
	 */
	public static double getProduitCroixCarre(int pos, int valMax, int etendu){
		return Math.pow(pos,2)*valMax/Math.pow(etendu,2);
	}
	
	public Obj instanceNearest(double x, double y, String name){
		return instanceNearest(x,y,new String[]{name});
	}
	
	public Obj instanceNearest(double x, double y, String[] names){
		double dist=Double.MAX_VALUE;
		Obj nearest=null;
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			boolean nameOk=false;
			for(int j=0;j<names.length;j++){
				if(obj.getClass().getName().equals(names[j])){
					nameOk=true;
					break;
				}
			}
			if(nameOk){
				double d=Math.pow(x-obj.x,2)+Math.pow(y-obj.y,2);
				if(d<dist){
					dist=d;
					nearest=obj;
				}
			}
		}
		return nearest;
	}
	
	/**
	 * Vérfie si il y a collision entre this et l'objet spécifié
	 */
	protected CollisionEvent checkCollision(Obj with){
		Area me=getMask();
		AffineTransform meAt=new AffineTransform();
		Point p=getOrigin();
		meAt.translate(x-p.x,y-p.y);
		meAt.rotate(Math.toRadians(360-angle),p.x,p.y);
		me.transform(meAt);
		
		Area other=with.getMask();
		AffineTransform otherAt=new AffineTransform();
		p=with.getOrigin();
		otherAt.translate(with.x-p.x,with.y-p.y);
		otherAt.rotate(Math.toRadians(360-with.angle),p.x,p.y);
		other.transform(otherAt);
		
		me.intersect(other);
		if(!me.isEmpty()) return new CollisionEvent(with,me);
		return new CollisionEvent(null,me);
	}
	
	/**
	 * Vérifie si l'Area spécifié entre en collision avec un objet (solide ou non)
	 */
	protected CollisionEvent checkCollision(Area area, boolean onlySolid, boolean halfHeightToo){
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			if(obj!=this&&(obj.isSolid()||!onlySolid)&&(halfHeightToo||!(obj instanceof Wall)||!((Wall)obj).isHalfHeight())){
				CollisionEvent ce=checkCollision(obj, area);
				if(ce.isCollision()) return ce;
			}
		}
		return new CollisionEvent(null,null);
	}
	
	/**
	 * Vérifie si il y a collision entre l'objet spécifié et l'Area spécifiée
	 */
	protected CollisionEvent checkCollision(Obj with, Area area){		
		Area other=with.getMask();
		AffineTransform otherAt=new AffineTransform();
		Point p=with.getOrigin();
		otherAt.translate(with.x-p.x,with.y-p.y);
		otherAt.rotate(Math.toRadians(360-with.angle),p.x,p.y);
		other.transform(otherAt);
		
		other.intersect(area);
		if(!other.isEmpty()) return new CollisionEvent(with,other);
		return new CollisionEvent(null,area);
	}
	
	protected CollisionEvent checkCollisionLine(double x1, double y1, double x2, double y2, boolean halfHeightToo){
		return checkCollisionLine(x1,y1,x2,y2,halfHeightToo,new Obj[]{});
	}
	
	protected CollisionEvent checkCollisionLine(double x1, double y1, double x2, double y2, boolean halfHeightToo, Obj[] except){
		Rectangle2D.Double rect=new Rectangle2D.Double(x1,y1,pointDistance(x1,y1,x2,y2),1);
		Area line=new Area(rect);
		double dir=pointDirection(x1,y1,x2,y2);
		line.transform(AffineTransform.getRotateInstance(Math.toRadians(360-dir),x1,y1));
		for(int i=0;i<Screen.getObjsNumber();i++){
			Obj obj=Screen.getObj(i);
			boolean isOk=true;
			for(int j=0;j<except.length;j++){
				if(obj.equals(except[j])){
					isOk=false;
					break;
				}
			}
			if(obj!=this&&isOk&&obj.getMask()!=null&&(halfHeightToo||!(obj instanceof Wall)||!((Wall)obj).isHalfHeight())){
				Area area=obj.getMask();
				AffineTransform objAt=new AffineTransform();
				Point p=obj.getOrigin();
				objAt.translate(obj.x-p.x,obj.y-p.y);
				objAt.rotate(Math.toRadians(360-obj.angle),p.x,p.y);
				area.transform(objAt);
				area.intersect(line);
				if(!area.isEmpty()) return new CollisionEvent(obj,area);
			}
		}
		return new CollisionEvent(null,null);
	}
	
	protected CollisionEvent checkCollisionLine(Point a, Point b, boolean halfHeightToo){
		return checkCollisionLine(a.x,a.y,b.x,b.y,halfHeightToo,new Obj[]{});
	}
	
	protected CollisionEvent checkCollisionLine(Point a, Point b, boolean halfHeightToo, Obj[] except){
		return checkCollisionLine(a.x,a.y,b.x,b.y,halfHeightToo,except);
	}
	
	protected double getDiffAngle(double angle1, double angle2){
		return Math.abs(((((angle1-angle2)%360)+540)%360)-180);
	}
	
	protected boolean isAngleInRange(double angle, double angleMin, double angleMax){
		boolean in=false;
		Point p=distDir(1000,angle);
		Point pMin=distDir(1000,angleMin);
		Point pMax=distDir(1000,angleMax);
		if(angleMax-angleMin<180){
			in=pMin.x*p.y-pMin.y*p.x<0&&pMax.x*p.y-pMax.y*p.x>0;
		}else{
			in=!(pMin.x*p.y-pMin.y*p.x>0&&pMax.x*p.y-pMax.y*p.x<0);
		}
		return in;
	}
}
