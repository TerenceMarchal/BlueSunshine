import java.awt.Point;
import java.util.Vector;


public class Goal {

	private static Vector<Goal> goals=new Vector<Goal>();
	
	private int timeMax;
	private int timeLeft=-1;	// temps restant en steps ; -1=infini
	public int x,y;
	private String description;
	
	public Goal(int x, int y, int timeMax, String description){
		this.x=x;
		this.y=y;
		this.timeMax=(timeMax==0)?-1:timeMax;
		timeLeft=this.timeMax;
		this.description=description;
		goals.add(this);
	}
	
	protected void update(){
		if(goals.size()==0){
			Screen.menu=Screen.MENU_WIN;
			return;
		}
		if(timeLeft>0) timeLeft--;
		if(timeLeft==0){
			Screen.menu=Screen.MENU_LOSE_TIME;
		}
		if(Math.abs(Screen.player.x-x)<25&&Math.abs(Screen.player.y-y)<25){
			goals.remove(0);
		}
	}
	
	public int getTimeLeft(){
		return timeLeft;
	}
	
	public String getDescription(){
		return description;
	}
	
	public static void updateSystem(){
		goals.get(0).update();
	}
	
	public static Goal getCurrentGoal(){
		if(goals.size()==0){
			Screen.menu=Screen.MENU_WIN;
			return new Goal(0,0,0,"");
		}
		return goals.get(0);
	}
	
	public static void removeAllGoals(){
		goals.removeAllElements();
	}
}
