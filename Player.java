import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;


public class Player extends Bot{

	public int mouseScreenX,mouseScreenY,staticMouseX;	// coordonnées de la souris par rapport à l'écran et sans tenir compte de l'angle
	public Robot mouseRobot;
	
	public boolean showMap=false;
	
	public Player(Point pos, int weapLeft, int weapRight){	// AJOUTER ARME CORPS à CORPS
		super();
		x=pos.x;
		y=pos.y;
		color=Screen.COLOR_TEAM_A;
		angle=0;
		defaultMaxSpeed=18;
		maxSpeed=defaultMaxSpeed;
		try {
			mouseRobot=new Robot();
		} catch (AWTException e) {e.printStackTrace();}
		
		Screen.removeObj(leftWeapon);
		Screen.removeObj(rightWeapon);
		leftWeapon=new Weapon(this,Weapon.POS_LEFT,weapLeft);
		rightWeapon=new Weapon(this,Weapon.POS_RIGHT,weapRight);
		alpha=255;
	}
	
	public void step(){
		super.step();
		
		if(turbo&&turboTime>0&&!isParalyzed()){
			turboTime=Math.max(0,turboTime-2);
		}else{
			if(turboTime<300) turboTime++;
		}
		if(turboTime==0) turbo=false;  

		if(moveRight&&!isParalyzed()){
			speedRight=Math.min(maxSpeed/2,Math.max(speedRight*1.1,4.5));
		}else{
			speedRight=speedRight*0.8;
			if(speedRight<0.1) speedRight=0;
		}
		if(moveUp&&!isParalyzed()){
			speedUp=Math.min(maxSpeed,Math.max(speedUp*1.1,4.5));
		}else{
			speedUp=speedUp*0.8;
			if(speedUp<0.1) speedUp=0;
		}
		if(moveLeft&&!isParalyzed()){
			speedLeft=Math.min(maxSpeed/2,Math.max(speedLeft*1.1,4.5));
		}else{
			speedLeft=speedLeft*0.8;
			if(speedLeft<0.1) speedLeft=0;
		}
		if(moveDown&&!isParalyzed()){
			speedDown=Math.min(maxSpeed/2,Math.max(speedDown*1.1,4.5));
		}else{
			speedDown=speedDown*0.8;
			if(speedDown<0.1) speedDown=0;
		}
		if(!turbo&&maxSpeed>defaultMaxSpeed){
			maxSpeed=maxSpeed*0.8;
			maxSpeed=Math.max(defaultMaxSpeed,maxSpeed);
		}

		
		Point p=distDir(speedRight,angle-90);
		x+=p.x;
		y+=p.y;
		p=distDir(speedUp,angle);
		x+=p.x;
		y+=p.y;
		p=distDir(speedLeft,angle+90);
		x+=p.x;
		y+=p.y;
		p=distDir(speedDown,angle+180);
		x+=p.x;
		y+=p.y;
		
		if(!isParalyzed()) angle+=(Screen.W/2-staticMouseX)/20;
		mouseRobot.mouseMove(mouseScreenX-staticMouseX+Screen.W/2,mouseScreenY);
		
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
	
	public void draw(Graphics2D g){
		drawBot(g);
	}
	public void addC4(C4 c4){
		allC4.add(c4);
	}
	
	protected void die(){
		if(Screen.playerInvincible){
			life=100;
		}else{
			Screen.menu=Screen.MENU_LOSE_DEAD;
		}
	}
	
	public BufferedImage getScreenshot(){
		BufferedImage img=new BufferedImage(Screen.W,Screen.H,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		Screen.screen.paintAll(g);
		g.dispose();
		return img;
	}
}
