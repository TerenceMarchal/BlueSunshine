import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Vector;


public class Enemy extends Bot{
	
	public static Vector<Enemy> allEnemies=new Vector<Enemy>();
	
	// IA :
	
	private int distMax;	// le robot n'intervient que si l'arlame a été déclenchée assez proche de lui ; distMax est en px^2
	private int whenCallHelp;
	private int[] path;	// chemin que suit le robot
	private boolean isPathClosed;	// indique si le chemin est fermé, si oui, il tourne en boucle, autrement il fait demi-tour à la fin du chemin
	private int[] defaultPath;	// chemin que suit le robot	par défaut
	private boolean isDefaultPathClosed;

	private int numPointTo=0;	// numéro du point de son propre path vers lequel il se dirige
	private int numPointFrom=0;	// numéro du point de son propre path duquel il part vers le numPointTo
	private int timeBeforeAction=-1;	// temps restant (en step) avant de passer à la prochaine action
	//private int progressAction=0;	// permet de savoir par exemple si l'ennemi est bien repéré
	private int action=ACTION_FOLLOW_PATH;
	private Point actionPosition;	// position ou l'action a lieu : endroit de l'explosion, dernière position du joueur,etc...
	private int turnTo=0;	// =-1 ou 1 : on ajoute ca à l'angle pour l'action ACTION_CHECK_FOR_ENEMIS pour se tourner dans la bonne direction /!!!\ DOIT ÊTRE A 0 POUR SUIVRE UN CHEMIN
	private Point returnPoint;	// position à laquelle le robot retourne à la fin de l'alerte
	private int returnPointTo;	// valeur de pointTo avant l'alerte
	private Point lastTargetPoint;	// position où la cible a été apercue pour la dernière fois
	private Bot target=null;

	private static int ACTION_FOLLOW_PATH=0;	// suit le chemin de ronde
	private static int ACTION_CHECK_FOR_ENEMIES=1;	// si il y a son ou qu'il apercoit un ennemi, il se TOURNE vers la source pour vérifier
	private static int ACTION_KILL=2;	// essaie de tuer l'ennemi

	private static int WAY_UP=1;
	private static int WAY_DOWN=-1;
	private static int WAY_NONE=0;	// quand on ne suit plus le chemin

	private int wayPath=WAY_UP;	// sens de parcours
	
	private Vector<Integer> alarms=new Vector<Integer>();
	private boolean alerted=false;	// l'ennemi est il en état d'alerte ? (et se dirige vers la source)
	
	private int angleTurnedWhileChecking=0;	// angle tourné lorsqu'il vérifie la présence d'ennemi. On continue à suivre le chemin quand ==360
	
	// A* :
	protected Vector<Node> openList=new Vector<Node>();
	protected Vector<Node> closedList=new Vector<Node>();
	private boolean canSeePlayer;

	public static int CALL_HELP_NEVER=0;
	public static int CALL_HELP_ON_SIGHT=1;
	public static int CALL_HELP_ON_SHOT=2;
	public static int CALL_HELP_ON_ATTACK=3;

	public Enemy(){
		super();
		allEnemies.add(this);
	}
	
	public void step(){
		super.step();
		if(!isParalyzed()){
			if(!isBlinded()){
				maxSpeed=defaultMaxSpeed;
				canSeePlayer=canSee(Screen.player);
				if(action==ACTION_KILL){
					maxSpeed=defaultMaxSpeed*1.5;
					if(canSeePlayer) lastTargetPoint=Screen.player.pos();
					direction=pointDirection(pos(),lastTargetPoint);
		
					double dist=pointDistance(pos(),lastTargetPoint);
		
					Point translation=distDir(Math.abs(Math.min(speed,Math.max(0,dist-200)))*1000,direction);
					x+=translation.x/1000.0;
					y+=translation.y/1000.0;
					
					if(canSeePlayer){
						if(leftWeapon.getTimeLeft()==0&&getDiffAngle(angle,direction)<5) leftWeapon.shot();
						if(rightWeapon.getTimeLeft()==0&&getDiffAngle(angle,direction)<5) rightWeapon.shot();
					}
					numPointFrom=-1;
					numPointTo=-1;
				}
				if(action==ACTION_FOLLOW_PATH){
					if(path.length>1){
						Point pointTo=PathManager.getPoint(path[numPointTo]);
						if(Math.pow(x-pointTo.x,2)+Math.pow(y-pointTo.y,2)<Math.pow(speed,2)){
							x=pointTo.x;
							y=pointTo.y;
							numPointFrom=numPointTo;
							numPointTo+=wayPath;
							if(!isPathClosed){
								if(wayPath==WAY_UP&&numPointTo==path.length){
									wayPath=WAY_DOWN;
									numPointTo-=2;
									if(alerted){
										direction=angle;
										action=ACTION_CHECK_FOR_ENEMIES;
									}
								}
								if(wayPath==WAY_DOWN&&numPointTo==-1){
									wayPath=WAY_UP;
									numPointTo+=2;
									if(alerted){
										alerted=false;
										setPath(defaultPath);
										isPathClosed=isDefaultPathClosed;
										
									}
								}
							}else{
								if(numPointTo==path.length){
									numPointTo=0;
								}
							}
							pointTo=PathManager.getPoint(path[numPointTo]);
							direction=pointDirection(pos(),pointTo);
						}
						Point translation=distDir(speed*1000,direction);
						x+=translation.x/1000.0;
						y+=translation.y/1000.0;
					}
				}
				
				if(action==ACTION_KILL){
					while(true){
						CollisionEvent ce=checkCollision(true,true);
						if(!ce.isCollision()) break;
						Obj obj=ce.getObj();
						moveOutsideObj(obj,pointDirection(ce.getPoint(),pos()),true,1);
					}
					while(true){
						CollisionEvent ce=leftWeapon.checkCollision(true,false,this);
						if(!ce.isCollision()) break;
						Obj obj=ce.getObj();
						moveOutsideObj(obj,pointDirection(ce.getPoint(),pos()),true,1);
					}
					while(true){
						CollisionEvent ce=rightWeapon.checkCollision(true,false,this);
						if(!ce.isCollision()) break;
						Obj obj=ce.getObj();
						moveOutsideObj(obj,pointDirection(ce.getPoint(),pos()),true,1);
					}
				}
				
				if(action==ACTION_CHECK_FOR_ENEMIES){
					direction=(direction+3)%360;
					angleTurnedWhileChecking+=3;
					if(angleTurnedWhileChecking>=360){
						angleTurnedWhileChecking=0;
						action=ACTION_FOLLOW_PATH;
					}
				}
				
				if(canSeePlayer&&(action==ACTION_FOLLOW_PATH||action==ACTION_CHECK_FOR_ENEMIES)){
					action=ACTION_KILL;
					target=Screen.player;
					lastTargetPoint=Screen.player.pos();
					if(whenCallHelp==CALL_HELP_ON_SIGHT){
						Screen.triggerAlarms(alarms,this);
					}
				}
				
				speed=maxSpeed;
				if(maxSpeed>defaultMaxSpeed){
					maxSpeed=maxSpeed*0.8;
					maxSpeed=Math.max(defaultMaxSpeed,maxSpeed);
				}
			}else{	// aveuglé
				direction+=4;
				leftWeapon.shot();
				rightWeapon.shot();
			}
		}
	}
	
	public void hurt(double damage){
		super.hurt(damage);
		if(life>0&&!isParalyzed()){
			if(action==ACTION_FOLLOW_PATH||(action==ACTION_KILL&&!canSeePlayer)){
				action=ACTION_CHECK_FOR_ENEMIES;
			}
			if(whenCallHelp==CALL_HELP_ON_ATTACK){
				Screen.triggerAlarms(alarms,this);
			}
		}
	}
	
	public void hearShot(Point source){	// lorsque l'ennemi entend un coup de feu
		if(!isParalyzed()&&!isBlinded()&&(action==ACTION_FOLLOW_PATH||action==ACTION_CHECK_FOR_ENEMIES)){
			triggerAlarm(-1,source);	// déclenche sa propre alarme
			if(whenCallHelp==CALL_HELP_ON_SHOT){
				Screen.triggerAlarms(alarms,this);
			}
		}
	}
	
	public void setDistMax(int distMax){
		this.distMax=(int)Math.pow(distMax,2);
	}
	
	public void setWhenCallHelp(int whenCallHelp){
		this.whenCallHelp=whenCallHelp;
	}
	
	/*public void setPath(Vector<Point> path){
		this.path=path;
		defaultPath=(Vector<Point>)path.clone();
		pointTo=0;
		if(path.size()>1) angle=pointDirection(path.get(0),path.get(1));
	}*/
	
	public void setPath(int[] path){
		this.path=path;
		direction=pointDirection(pos(),PathManager.getPoint(path[0]));
		numPointTo=0;
	}
	
	public void setDefaultPath(int[] path, boolean isClosed){
		defaultPath=path;
		isDefaultPathClosed=isClosed;
		if(path.length==1){
			numPointFrom=-1;
			numPointTo=-1;
		}
	}
	
	public void setIsPathClosed(boolean isPathClosed){
		this.isPathClosed=isPathClosed;
	}

	public void draw(Graphics2D g){
		if(Screen.showAlwaysEnemies&&Screen.showEnemiesPaths){
			g.setColor(Color.red);
			for(int i=0;i<path.length-1;i++){
				Point p1=PathManager.getPoint(path[i]);
				Point p2=PathManager.getPoint(path[i+1]);
				g.drawLine(p1.x,p1.y,p2.x,p2.y);
			}
			if(isPathClosed){
				Point p1=PathManager.getPoint(path[path.length-1]);
				Point p2=PathManager.getPoint(path[0]);
				g.drawLine(p1.x,p1.y,p2.x,p2.y);
			}
		}

		if(!isParalyzed()){
			if(turnTo==0){
				angle=(angle+360)%360;
				int diff=(int)Math.round(getDiffAngle(angle,direction));
				if(diff>0){
					if((Math.round(angle)+diff)%360==Math.round(direction)){
						angle+=Math.min(diff,4);
					}else{
						angle-=Math.min(diff,4);
					}
				}
			}
			
			if(leftWeapon!=null){
				double dir=(angle+80)%360;
				if(target!=null&&canSeePlayer) dir=(pointDirection(leftWeapon.pos(),target.pos())+90)%360;
				int diff=(int)Math.round(getDiffAngle(leftWeapon.angle,dir));
				if(diff>0){
					if((Math.round((leftWeapon.angle%360+360)%360)+diff)%360==Math.round(dir)){
						leftWeapon.angle+=Math.min(diff,4);
					}else{
						leftWeapon.angle-=Math.min(diff,4);
					}
				}
			}
			if(rightWeapon!=null){
				double dir=(angle+100)%360;
				if(target!=null&&canSeePlayer) dir=(pointDirection(rightWeapon.pos(),target.pos())+90)%360;
				int diff=(int)Math.round(getDiffAngle(rightWeapon.angle,dir));
				if(diff>0){
					if((Math.round((rightWeapon.angle%360+360)%360)+diff)%360==Math.round(dir)){
						rightWeapon.angle+=Math.min(diff,4);
					}else{
						rightWeapon.angle-=Math.min(diff,4);
					}
				}
			}
		}
		drawBot(g);
	}
	
	public void triggerAlarm(int id, Object by){
		int X=0, Y=0;
		if(by instanceof Point){
			X=((Point)by).x;
			Y=((Point)by).y;
		}
		if(by instanceof Obj){
			X=(int)((Obj)by).x;
			Y=(int)((Obj)by).y;
		}
		if(Math.pow(X-x,2)+Math.pow(Y-y,2)<distMax){
			if(!alerted){
				if(by instanceof Captor){
					if(numPointFrom!=-1&&numPointTo!=-1){
						PathManager.setPath(this,path[numPointFrom],path[numPointTo],((Captor)by).getNearestNode());
					}else{
						int p=PathManager.getNearestPoint(x,y);
						PathManager.setPath(this,p,p,((Captor)by).getNearestNode());
					}
				}
				if(by instanceof Enemy){
					int to=PathManager.getNearestPoint(X,Y);
					if(numPointFrom!=-1&&numPointTo!=-1){
						PathManager.setPath(this,path[numPointFrom],path[numPointTo],to);
					}else{
						int p=PathManager.getNearestPoint(x,y);
						if(p!=to){
							PathManager.setPath(this,p,p,to);
						}
					}
				}
				if(by instanceof Point){	// on a entendu un son
					int to=PathManager.getNearestPoint(X,Y);
					if(numPointFrom!=-1&&numPointTo!=-1){
						PathManager.setPath(this,path[numPointFrom],path[numPointTo],to);
					}else{
						int p=PathManager.getNearestPoint(x,y);
						if(p!=to){
							PathManager.setPath(this,p,p,to);
						}
					}
				}
			}
			alerted=true;
		}
	}
	
	protected void die(){
		Screen.removeObj(this);
		new C4(leftWeapon,x,y,0,256).explode();
	}

	public void addAlarm(int id){
		alarms.add(id);
	}
	
	public boolean isAlerted(){
		return alerted;
	}
}
