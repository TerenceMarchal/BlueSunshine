import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.swing.SwingUtilities;


public class Weapon extends Obj{

	public static int GUN=0;
	public static int SILENCER_GUN=1;
	public static int ASSAULT_RIFLE=2;
	public static int SHOTGUN=3;
	public static int SNIPER_RIFLE=4;
	public static int TASER=5;
	public static int MACHINE_GUN=6;
	public static int ROCKET_LAUNCHER=7;
	public static int C4=8;
	public static int MINE=9;
	public static int EMP=10;	// =IEM
	public static int DAZER=11;	// laser aveuglant
	
	public static int NONE=12;	// aucune arme
	
	public static int POS_LEFT=0, POS_RIGHT=1;	// pour savoir de quel côté est l'arme
	
	private String name;
	private int type, pos;
	private int ammo, ammoByClip, clip;	// si clip==99 alors munitions infini
	private int shootingTime, chargingTime;	// en step
	private boolean burst;	// tir en rafale ?
	private double dispersion=0;	// degrés s'ajoutant à la duispersion courante à chaque tir
	private double currentDispersion=0;	// l'angle de la balle dévie de 0 à currentDispersion lors du tir
	private int timeLeft=0;	// temps restant avant de pouvoir tirer
	private double speed, damage;
	private String soundDir;	// répertoire des sons (small,silent,etc...)
	
	private Bot bot;
	public boolean mousePressed;
	private boolean reload;
	private String shortName;
	
	public Weapon(Bot bot, int pos, int type){
		super();
		//if(bot instanceof Player) Screen.screen.addMouseListener(this);
		this.bot=bot;
		this.type=type;
		this.pos=pos;
		if(bot!=null) angle=bot.angle;
		init();
		//createOnServer();
		//sendCreateArgs(player.id+SEP+);
	}
	

	public void drawWeapon(Graphics2D g){
		updatePos();
		Color c=bot.getColor();
		g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),bot.getAlpha()));
		
		if(!bot.isParalyzed()){
			if(bot instanceof Player){
				angle=pointDirection(x,y,Screen.mouseX,Screen.mouseY)+90;
			}
		}
		
		g.translate(x,y);
		g.rotate(Math.toRadians(-angle));
		g.translate(-origin.x,-origin.y);
		g.fill(mask);
		g.translate(origin.x,origin.y);
		g.rotate(Math.toRadians(angle));
		g.translate(-x,-y);
	}
	
	public void step(){
		super.step();
		if(mousePressed){
			shot();
			if(!burst) mousePressed=false;
		}
		if(timeLeft>0) timeLeft--; else reload=false;
		currentDispersion=Math.max(0,currentDispersion-0.15);
	}
	
	public void updatePos(){
		Point p;
		if(pos==POS_LEFT) p=distDir(bot.w/2,bot.angle+90); else p=distDir(bot.w/2,bot.angle-90);
		x=bot.x+p.x;
		y=bot.y+p.y;
	}
	
	public void shot(){
		if(!bot.isParalyzed()){
			if(ammo>0){
				if(timeLeft==0){
					timeLeft=shootingTime;
					double val=currentDispersion+dispersion;
					if(bot.isStabilized()) val/=2;
					currentDispersion=Math.min(5,val);
					if(!isExplosiveWeapon()&&type!=EMP){
						double dir=angle-90;
						new Bullet(this,x,y,dir,speed,damage);
						if(type==SHOTGUN){
							File[] sounds=new File("data/sound/weapons/"+SHOTGUN+"/").listFiles();
							File f=sounds[(int)Math.floor(Math.random()*sounds.length)];//
							new Sound(this,f,false);
							new Bullet(this,x,y,dir-8,speed,damage);
							new Bullet(this,x,y,dir-4,speed,damage);
							new Bullet(this,x,y,dir+4,speed,damage);
							new Bullet(this,x,y,dir+8,speed,damage);
						}
					}else{
						if(type==ROCKET_LAUNCHER){
							new Rocket(this,x,y,angle-90,speed,damage);
						}
						if(type==C4){
							new C4(this,x,y,angle,damage);
						}
						if(type==MINE){
							new Mine(this,x,y,damage);
						}
						if(type==EMP){
							new EMP(this,x,y,angle-90,speed);
						}
						if(type==DAZER){
							
						}
					}
					reload=false;
					ammo--;
					if(ammo==0) reload();
				}
			}else{
				if(clip>0){
					reload();
				}else{
					// jouer son plus de balle
				}
			}
		}
	}
	
	public Bot getBot(){
		return bot;
	}
	
	private void reload(){
		if(clip>0){
			if(clip<99) clip--;
			ammo=ammoByClip;
			timeLeft=chargingTime;
			reload=true;
		}
	}
	
	public String getName(){
		return name;
	}
	
	public int getType(){
		return type;
	}
	
	public String getShortName(){
		return shortName;
	}
	
	public boolean isReloading(){
		return reload;
	}
	
	public int getAmmo(){
		return ammo;
	}
	public int getAmmoByClip(){
		return ammoByClip;
	}
	public int getClip(){
		return clip;
	}
	public int getShootingTime(){
		return shootingTime;
	}
	public int getChargingTime(){
		return chargingTime;
	}
	public int getTimeLeft(){
		return timeLeft;
	}
	public double getDispersion(){
		return dispersion;
	}
	public double getCurrentDispersion(){
		return currentDispersion;
	}
	
	private boolean isExplosiveWeapon(){
		return type==ROCKET_LAUNCHER||type==C4||type==MINE;
	}
	
	private void init(){
		switch(type){
			case 0:	// pistolet
				name="REVOLVER";
				shortName="REV";
				ammo=8;
				clip=99;
				shootingTime=15;
				chargingTime=60;
				dispersion=1;
				speed=70;
				damage=10;
				burst=false;
				w=8;
				h=40;
				soundDir="small";
				break;
			case 1:	// pistolet silencieux
				name="REVOLVER SILENCIEUX";
				shortName="REV SIL";
				ammo=8;
				clip=4;
				shootingTime=10;
				chargingTime=60;
				dispersion=1;
				speed=70;
				damage=15;
				burst=false;
				w=8;
				h=45;
				soundDir="silent";
				break;
			case 2:	// fusil d'assaut
				name="FUSIL D'ASSAUT";
				shortName="FUS ASST";
				ammo=30;
				clip=4;
				shootingTime=3;
				chargingTime=60;
				dispersion=0.3;
				speed=100;
				damage=20;
				burst=true;
				w=10;
				h=60;
				soundDir="small";
				break;
			case 3:	// fusil de chasse
				name="FUSIL DE CHASSE";
				shortName="FUS";
				ammo=4;
				clip=4;
				shootingTime=30;
				chargingTime=90;
				dispersion=0.5;
				speed=80;
				damage=12;
				burst=false;
				w=10;
				h=65;
				soundDir="small";
				break;
			case 4:	// fusil de sniper
				name="FUSIL DE SNIPER";
				shortName="SNP";
				ammo=4;
				clip=4;
				shootingTime=40;
				chargingTime=60;
				dispersion=0;
				speed=160;
				damage=80;
				burst=false;
				w=6;
				h=110;
				soundDir="large";
				break;
			case 5:	// taser
				name="TASER";
				shortName="TSR";
				ammo=1;
				clip=4;
				shootingTime=40;
				chargingTime=80;
				dispersion=1;
				speed=80;
				damage=0;
				burst=false;
				w=8;
				h=40;
				soundDir="taser";
				break;
			case 6:	// mitrailleuse
				name="MITRAILLEUSE";
				shortName="MIT";
				ammo=90;
				clip=2;
				shootingTime=3;
				chargingTime=120;
				dispersion=1.25;
				speed=90;
				damage=15;
				burst=true;
				w=12;
				h=60;
				soundDir="medium";
				break;
			case 7:	// lance-roquette
				name="LANCE-ROQUETTE";
				shortName="L-ROQ";
				ammo=1;
				clip=4;
				shootingTime=90;
				chargingTime=90;
				dispersion=2;
				speed=60;
				damage=256;
				burst=false;
				w=16;
				h=60;
				break;
			case 8:	// C4
				name="C4";
				shortName="C4";
				ammo=1;
				clip=3;
				shootingTime=30;
				chargingTime=30;
				dispersion=0;
				speed=0;
				damage=256;
				burst=false;
				w=16;
				h=16;
				break;
			case 9:	// mine
				name="MINE";
				shortName="MN";
				ammo=1;
				clip=3;
				shootingTime=30;
				chargingTime=30;
				dispersion=0;
				speed=0;
				damage=256;
				burst=false;
				w=16;
				h=16;
				break;
			case 10: // IEM
				name="IEM";
				shortName="IEM";
				ammo=1;
				clip=2;
				shootingTime=30;
				chargingTime=70;
				dispersion=1;
				speed=60;
				damage=150;
				burst=false;
				w=16;
				h=35;
				break;
			case 11: // dazer
				name="DAZER";
				shortName="DZR";
				ammo=2;
				clip=3;
				shootingTime=40;
				chargingTime=80;
				dispersion=0;
				speed=130;
				damage=0;
				burst=false;
				w=8;
				h=40;
				soundDir="taser";
				break;
		}
		mask=new Area(new Rectangle2D.Double(0,0,w,h));
		ammoByClip=ammo;
		origin=new Point(w/2,h/3);
	}

	public String getSoundDir(){
		return soundDir;
	}
	
	public double getDamage(){
		return damage;
	}
	
	public double getSpeed(){
		return speed;
	}

	public static Weapon getWeapon(int type){	// uniquement pour récupérer les statistiques d'une arme
		return new Weapon(null,0,type);
	}
	
	public double getDamagePerStep(){
		double d=ammoByClip*damage;
		if(type==SHOTGUN) d*=5;
		d/=ammoByClip*shootingTime+chargingTime;
		return d;
	}
	
	public static int getAmmoByClipMax(){ return 90; }
	public static int getClipMax(){ return 4; }
	public static int getShootingTimeMax(){ return 90; }
	public static int getChargingTimeMax(){ return 120; }
	public static int getDispersionMax(){ return 2; }
	public static int getSpeedMax(){ return 160; }
	public static double getDamagePerStepMax(){ return 4.266666666666667; }
}
