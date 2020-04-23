import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;


public class CollisionEvent {

	private Obj with;
	private Area area;

	public CollisionEvent(Obj with, Area area){
		this.with=with;
		this.area=area;
	}
	
	public boolean isCollision(){
		return (with!=null);
	}
	
	public Obj getObj(){
		return with;
	}
	
	public Area getArea(){
		if(area!=null) return area;
		return new Area();
	}
	
	public Point getPoint(){
		Rectangle bounds=area.getBounds();
		return new Point(bounds.x+bounds.width/2,bounds.y+bounds.height/2);
	}
	
	public Rectangle getBounds(){
		return area.getBounds();
	}
	
	public String toString(){
		if(!isCollision()) return "No collision";
		return "Collision with #"+getObj().id+" ("+getObj().getClass().getName()+") at "+getPoint();
	}
	
}
