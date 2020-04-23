import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class Screen extends JPanel implements MouseMotionListener, MouseListener, KeyListener{ // pour lancer : java -Xms2g -Xmx2g -jar jeu.jar
	
	public static int KEY_MOVE_RIGHT=KeyEvent.VK_RIGHT;
	public static int KEY_MOVE_LEFT=KeyEvent.VK_LEFT;
	public static int KEY_MOVE_UP=KeyEvent.VK_UP;
	public static int KEY_MOVE_DOWN=KeyEvent.VK_DOWN;
	public static int KEY_TURBO=KeyEvent.VK_SHIFT;
	public static int KEY_SCREENSHOT=KeyEvent.VK_F2;
	public static int KEY_SHOW_MAP=KeyEvent.VK_END;
	
	public static Color COLOR_TEAM_A=new Color(0,191,255);
	public static Color COLOR_TEAM_B=new Color(0,255,127);
	
	public static int MENU_NO=0;	// aucu menu, =jeu
	public static int MENU_LOADING=1;
	public static int MENU_PAUSE=2;
	public static int MENU_MAIN=3;
	public static int MENU_LOGO=4;	// logo WSS
	public static int MENU_SETTINGS=5;
	public static int MENU_CHOOSE_MAP=6;
	public static int MENU_WIN=7;
	public static int MENU_LOSE_DEAD=8;
	public static int MENU_LOSE_TIME=9;
	
	public static int menu=MENU_LOGO;
	private boolean initializing=false;
	
	protected int idLoading=0;
	protected static int percentLoading=-1;	// -1=inconnu
	protected static int LOAD_SOUND=0;
	protected static int LOAD_MAP=1;
	protected static int LOAD_PATTERNS=2;
	protected static int LOAD_NAV_MAP=3;
	protected static int LOAD_PLAYER=4;
	
	//private static Vector<Obj> playerSpottedBy=new Vector<Obj>();	// liste des objets (caméra, robots) ayant le joueur en visuel
	
	private static HashMap<Integer,Vector<Obj>> alarms=new HashMap<Integer,Vector<Obj>>();
	
	protected int idTip=0;
	protected String[] tips=new String[]{
		"Certains murs peuvent êtres détruits avec des explosifs.",
		"Les balles peuvent traverser certains murs.",
		"Tirez sur les portes pour les ouvrir.",
		"Utilisez une IEM sur une porte pour la bloquer quelques instants.",
		"Le Taser va empêcher votre ennemi de bouger et de tirer.",
		"Le Dazer est une arme aveuglante : votre ennemi ne verra plus rien pendant quelques secondes.",
		"L'IEM déconnecte complètement votre ennemi pendant quelques secondes : il sera aveugle et paralysé.",
		"Vous manquez souvent de munitions ? Le revolver dispose d'un stock de balles infini.",
		"Ne dévoilez pas votre position : utilisez le revolver à silencieux.",
		"Les mines et le C4 sont invisibles pour vos ennemis si un obstacle les sépare ou s'ils sont trop éloignés.",
		"Installez vos explosifs dans des coins, l'ennemi les verra au dernier moment.",
		"Appuyez sur la molette pour vous stabiliser : vos tirs seront plus précis.",
		"Vous pouvez creuser des passages à travers les murs à l'aide d'explosifs !",
		"N'oubliez pas que vos ennemis ne voient pas derrière eux, sachez en profiter.",
		"Protégez vos arrières : ne restez pas inutilement exposé.",
		"Vous pouvez définir la qualité des effets sonores dans les paramètres.",
		"Vous allez moins vite en reculant qu'en avancant : réfléchissez à votre tactique de fuite.",
		"Attention aux explosions en chaîne !",
		"Les IEM aussi peuvent se déclencher mutuellement lors de leur activation."
	};
	
	private boolean showPatterns=true;
	private boolean showDebug=false;
	private boolean loadAStar=false;
	public static boolean showImpacts=true;
	
	public static boolean showNavMap=false;
	public static boolean showPathGraph=false;
	public static boolean playMusic=false;
	public static boolean drawCaptorAngles=false;
	
	private Vector<Long> fps=new Vector<Long>();
	
	private static Vector<Obj> objs=new Vector<Obj>();
	public static Player player;
	private int staticMouseX, staticMouseY;	// position de la souris sans tenir compte de la view et de l'angle
	private BufferedImage hud,pauseBackground;
	private FontMetrics fm;
	private Font fontDefault;
	private Hashtable<Integer, Font> fonts=new Hashtable<Integer, Font>();
	public static Point topLeft,topRight,bottomLeft,bottomRight;
	public static Screen screen;
	public static int mouseX, mouseY, viewX, viewY, W, H;
	public static JFrame window;
	private BufferedImage minimap;
	public static BufferedImage background;
	public BackgroundTile[] backgroundTiles=new BackgroundTile[4];
	private PathManager pathManager;
	private static String map;
	public static int T=0;
	
	private Point mousePositionPause;	// pour remettre la souris correctement en quittant le menu pause (évite rotation brusque)
	private static Point startPoint;	// point de départ du joueur
	
	private WSSlogo wss=new WSSlogo();
	private boolean mousePressed=false;
	private boolean mouseLeft=true;	// indique quel bouton de la souris a été pressé
	
	private static Vector<Point> impacts=new Vector<Point>();
	
	private boolean alarm=false;	// indique si il y a une alerte actuellement (n'importe quel ennemi)
	private int alphaAlarm=0;
	public static Rectangle mapBounds;
	public static Sound alarmSound=new Sound(null,new File("data/sound/alarme.wav"),false);
	
	// debug :
	public static boolean showEnemiesPaths=false;
	public static boolean showAlwaysEnemies=false;
	public static boolean playerInvisible=false;
	public static boolean playerInvincible=false;
	
	/* A* :
	 * Permet aux ennemis de trouver un chemin vers une position
	 * chaque case de la map peut être "verte", "orange" ou "rouge" : 
	 *   on ne peut pas aller en zone rouge et on essaie d'éviter les zone orange (pour ne pas coller les murs)
	 */
	
	private static Area redArea=new Area();
	private static Area orangeArea=new Area();
	public static int sizeNavMap=8;	// taille des cases de la carte de navigation
	public static byte[][] navMap;
	public static byte NAV_RED=-1;
	public static byte NAV_ORANGE=1;
	public static byte NAV_GREEN=0;
	
	public static Server server;
	public static String pseudo="Térence";
	
	private Vector<String> levels=new Vector<String>();
	private String levelDescription="";
	private int weaponLeft=Weapon.ASSAULT_RIFLE, weaponRight=Weapon.ROCKET_LAUNCHER;
	private boolean selectWeapons=false;	// false pour choisir les armes, true pour choisir la map
	
	private int exitMainMenu=-1;	// pour fondu quand on quitte le menu principal ; -1 signifie qu'on reste

	public Screen(){
		screen=this;
		alarmSound.setVolume(0);
		/*setFocusable(true);
		requestFocus();*/
		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16,16,new int[16*16],0,16)),new Point(0,0),"noCursor"));
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		new SoundManager();
		loadFont();
		
		
		
		
		
		File[] dir=new File("data/map/").listFiles();
		for(int i=0;i<dir.length;i++){
			File f=dir[i];
			int pos=f.getName().indexOf(".png");
			if(pos!=-1){
				levels.add(f.getName().substring(0,pos));
			}
		}
		map=levels.get(0);
	}
	
	private void initGame(final boolean sameMap){
		menu=MENU_LOADING;
		initializing=true;
		(new Thread(){
			public void run(){
				T=0;
				idTip=(int)Math.floor(Math.random()*tips.length);
				initOpenAL();
				idLoading++;
				loadMap(sameMap);
				idLoading++;
				if(loadAStar) initAStar();
				idLoading++;
				//server=new Server();
				while(server!=null&&!server.canPlay()){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {e.printStackTrace();}
				}
				percentLoading=-1;
				player=new Player(startPoint,weaponLeft, weaponRight);
				menu=MENU_NO;
				//server.sendMessage("GET_DATA");
			}
		}).start();
	}
	
	public void paintComponent(Graphics G){
		long startTime=System.currentTimeMillis();
		Graphics2D g=(Graphics2D)G;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		W=getWidth();
		H=getHeight();
		setFont(g,64);
		fm=g.getFontMetrics();
		//g.setColor(new Color(50,50,50));
		g.setColor(Color.black);
		g.fillRect(0,0,W,H);
		if(menu==MENU_NO){
			updateScreenPos();
			
			Point p1=distDir(staticMouseX+Screen.viewX-player.x,player.angle);
			Point p2=distDir(staticMouseY+Screen.viewY-player.y,player.angle);
			mouseX=(int)(player.x-p1.x-p2.x);
			mouseY=(int)(player.y-p1.y-p2.y);
			
			for(int i=0;i<getObjsNumber();i++){
				getObj(i).step();
			}
			
			
			
			viewX=(int)(player.x-W/2);
			viewY=(int)(player.y-(H-50));
			
			
			
			g.translate(-viewX,-viewY);
			g.rotate(Math.toRadians(player.angle-90),player.x,player.y);
			
			Rectangle rect=mapBounds;
			g.drawImage(background,rect.x,rect.y,null);
			
			Goal.updateSystem();
			g.setColor(COLOR_TEAM_A);
			Stroke oldStroke=g.getStroke();
			g.setStroke(new BasicStroke(4,BasicStroke.JOIN_MITER,BasicStroke.CAP_SQUARE));
			Goal goal=Goal.getCurrentGoal();
			
			g.drawOval(goal.x-37,goal.y-37,75,75);
			
			
			int d=T%30*75/30;
			g.fillOval(goal.x-d/2,goal.y-d/2,d,d);
			
			
			g.setStroke(oldStroke);
			//BackgroundTile.drawAll(g);
			
			if(loadAStar&&showNavMap){
				int minX=(Math.min(topLeft.x,Math.min(topRight.x,Math.min(bottomRight.x,bottomLeft.x)))-rect.x)/sizeNavMap;
				int maxX=(Math.max(topLeft.x,Math.max(topRight.x,Math.max(bottomRight.x,bottomLeft.x)))-rect.x)/sizeNavMap;
				int minY=(Math.min(topLeft.y,Math.min(topRight.y,Math.min(bottomRight.y,bottomLeft.y)))-rect.y)/sizeNavMap;
				int maxY=(Math.max(topLeft.y,Math.max(topRight.y,Math.max(bottomRight.y,bottomLeft.y)))-rect.y)/sizeNavMap;
				rect=mapBounds;
				for(int i=Math.max(0,minX);i<Math.min(navMap.length-1,maxX);i++){
					for(int j=Math.max(0,minY);j<Math.min(navMap[0].length-1,maxY);j++){
						byte b=navMap[i][j];
						if(b==NAV_GREEN) continue;
						if(b==NAV_RED) g.setColor(Color.red);
						if(b==NAV_ORANGE) g.setColor(Color.orange);
						g.drawRect(i*sizeNavMap+rect.x,j*sizeNavMap+rect.y,sizeNavMap,sizeNavMap);
					}
				}
			}
			if(showPathGraph){
				g.setColor(Color.blue);
				for(int i=0;i<PathManager.getPointsNumber();i++){
					Point p=PathManager.getPoint(i);
					g.fillOval(p.x-4,p.y-4,9,9);
					Vector<Integer> vec=PathManager.getNeighbours(i);
					for(int j=0;j<vec.size();j++){
						p2=PathManager.getPoint(vec.get(j));
						g.drawLine(p.x,p.y,p2.x,p2.y);
					}
				}
			}
			
			boolean isAlerted=false;
			for(int i=0;i<getObjsNumber();i++){
				Obj obj=getObj(i);
				if(obj!=player&&!(obj instanceof Weapon)){
					obj.draw(g);
				}
				if(!isAlerted&&obj instanceof Enemy){
					if(((Enemy)obj).isAlerted()) isAlerted=true;
				}
			}
			alarm=isAlerted;
			
			if(alarm){
				alphaAlarm=(T%30)*128/30;
			}else{
				alphaAlarm=Math.max(0,alphaAlarm-2);
			}
			//alarmSound.setVolume((alarm)?0.6f:alphaAlarm/255f);
			
			
			g.setColor(new Color(50,50,50));
			for(int i=0;i<impacts.size();i++){
				Point p=impacts.get(i);
				g.fillOval(p.x-1,p.y-1,3,3);
			}
			
			//drawWarFog(g);
			
			
			
			
			player.draw(g);
			g.rotate(-Math.toRadians(player.angle-90),player.x,player.y);
			g.translate(viewX,viewY);		

			
			Color c=COLOR_TEAM_B;
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alphaAlarm));
			//g.fillRect(0,0,W,H);
			
			if(player.showMap) drawMap(g);
			
			drawHUD(g);
			
			drawEffects(g);
			
			g.setColor(COLOR_TEAM_A);
			
			g.fillOval(player.mouseScreenX-player.staticMouseX+W/2-3,player.mouseScreenY-3,6,6);
			
			if(showDebug){
				long time=System.currentTimeMillis();
				fps.add(time);
				int nb=0;
				for(int i=fps.size()-1;i>0;i--){
					if(time-fps.get(i)<1000){
						nb++;
					}else{
						fps.remove(i);
						break;
					}
				}
				
				g.setColor(COLOR_TEAM_A);
				setFont(g,24);
				g.drawString("FPS : "+nb,20,50);
				
				setFont(g,16);
				
				g.setColor(Color.gray);
				if(showPathGraph) g.setColor(COLOR_TEAM_A);
				g.drawString("4  afficher le graphe des chemins",W/2,50);
				
				g.setColor(Color.gray);
				if(showAlwaysEnemies) g.setColor(COLOR_TEAM_A);
				g.drawString("3  toujours montrer les ennemis",W/2,50+fm.getHeight());
				
				g.setColor(Color.gray);
				if(showEnemiesPaths) g.setColor(COLOR_TEAM_A);
				g.drawString("2  afficher les chemins des ennemis",W/2,50+fm.getHeight()*2);
				
				g.setColor(Color.gray);
				if(playerInvisible) g.setColor(COLOR_TEAM_A);
				g.drawString("1  joueur invisible",W/2,50+fm.getHeight()*3);
				
				g.setColor(Color.gray);
				if(playerInvincible) g.setColor(COLOR_TEAM_A);
				g.drawString("5  joueur invincible",W/2,50+fm.getHeight()*4);
			}
			
		}else{
			if(menu==MENU_LOADING) showLoading(g);
			if(menu==MENU_MAIN) showMainMenu(g);
			if(menu==MENU_LOGO) showLogoMenu(g);
			if(menu==MENU_CHOOSE_MAP) showMapMenu(g);
			if(menu==MENU_PAUSE) showPauseMenu(g);
			if(menu==MENU_SETTINGS) showSettingsMenu(g);
			if(menu==MENU_WIN) showWinMenu(g);
			if(menu==MENU_LOSE_DEAD) showLoseMenu(g,true);
			if(menu==MENU_LOSE_TIME) showLoseMenu(g,false);
			mousePressed=false;
		}
		
		
		
		
		
		T++;
		long endTime=System.currentTimeMillis();

		if(showDebug&&menu==MENU_NO) g.drawString("TIME : "+((endTime-startTime)*100/(1000/30))+"%",200,50);
		while(endTime-startTime<1000/30){
			endTime=System.currentTimeMillis();
		}
		repaint();
	}
	
	private void showWinMenu(Graphics2D g){
		g.setColor(COLOR_TEAM_A);
		g.fillRect(0,0,W,H);
		
		g.setColor(Color.white);
		
		setFont(g,64);
		String str="Mission réussie !";
		g.drawString(str,W/2-fm.stringWidth(str)/2,H/4);
		
		setFont(g,32);
		str="Félicitations !";
		g.drawString(str,W/2-fm.stringWidth(str)/2,H/4+fm.getHeight()*2);
		
		setFont(g,26);
		str="Appuyez sur une touche pour retourner au menu principal";
		g.drawString(str,W/2-fm.stringWidth(str)/2,H*2/3);
	}
	
	private void showLoseMenu(Graphics2D g, boolean dead){	// dead=true si perdu parce que mort ou =false si temps écoulé
		int size=W/100;
		for(int i=0;i<W/size+1;i++){
			for(int j=0;j<H/size+1;j++){
				int n=(int)(Math.random()*255);
				g.setColor(new Color(n,n,n));
				g.fillRect(i*size,j*size,size,size);
			}
		}
		g.setColor(new Color(0,0,0,92));
		g.fillRect(0,0,W,H);
		
		g.setColor(COLOR_TEAM_B);
		
		setFont(g,64);
		String str="Mission échouée";
		g.drawString(str,W/2-fm.stringWidth(str)/2,H/4);
		
		setFont(g,32);
		str="L'objectif n'a pas été rempli dans le temps imparti";
		if(dead) str="Votre robot a été détruit";
		g.drawString(str,W/2-fm.stringWidth(str)/2,H/4+fm.getHeight()*2);
		
		setFont(g,26);
		str="Appuyez sur une touche pour recommencer la partie";
		g.drawString(str,W/2-fm.stringWidth(str)/2,H*2/3);
	}
	
	private void showSettingsMenu(Graphics2D g){
		g.drawImage(pauseBackground,0,0,null);
		int W=getWidth();
		int H=getHeight();
		g.setColor(new Color(0,0,0,128));
		g.fillRect(0,0,W,H);
		g.setColor(Color.white);
		
		setFont(g,56);
		int Y=H/5;

		Stroke oldStroke=g.getStroke();
		
		g.setStroke(new BasicStroke(1.5f,BasicStroke.JOIN_MITER,BasicStroke.CAP_SQUARE));
		
		String str="paramètres";
		Area outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(W/2-fm.stringWidth(str)/2,Y));
		g.draw(outline);
		
		
		setFont(g,24);
		
		Y=H/3;
		int X=W/10;
		
		str="Impacts de balles :";
		outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(X-16,Y+12+fm.getAscent()/2));
		g.draw(outline);
		Y+=fm.getHeight()*2;
		
		str="Oui";
		g.drawOval(X,Y,24,24);
		g.drawString(str,X+32,Y+12+fm.getAscent()/2);
		if(showImpacts) g.fillOval(X,Y,24,24);
		if(mousePressed&&new Rectangle(X,Y,fm.stringWidth(str)+32,24).contains(staticMouseX,staticMouseY)) showImpacts=true;
		Y+=fm.getHeight();
		
		str="Non";
		g.drawOval(X,Y,24,24);
		g.drawString(str,X+32,Y+12+fm.getAscent()/2);
		if(!showImpacts) g.fillOval(X,Y,24,24);
		if(mousePressed&&new Rectangle(X,Y,fm.stringWidth(str)+32,24).contains(staticMouseX,staticMouseY)) showImpacts=false;
		Y+=fm.getHeight()*3;
		
		str="Qualité sonore :";
		outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(X-16,Y+12+fm.getAscent()/2));
		g.draw(outline);
		Y+=fm.getHeight()*2;
		
		str="Mono";
		g.drawOval(X,Y,24,24);
		g.drawString(str,X+32,Y+12+fm.getAscent()/2);
		if(SoundManager.getSoundQuality()==SoundManager.MONO) g.fillOval(X,Y,24,24);
		if(mousePressed&&new Rectangle(X,Y,fm.stringWidth(str)+32,24).contains(staticMouseX,staticMouseY)) SoundManager.setSoundQuality(SoundManager.MONO);
		Y+=fm.getHeight();
		
		str="Stéréo";
		g.drawOval(X,Y,24,24);
		g.drawString(str,X+32,Y+12+fm.getAscent()/2);
		if(SoundManager.getSoundQuality()==SoundManager.STEREO) g.fillOval(X,Y,24,24);
		if(mousePressed&&new Rectangle(X,Y,fm.stringWidth(str)+32,24).contains(staticMouseX,staticMouseY)) SoundManager.setSoundQuality(SoundManager.STEREO);
		Y+=fm.getHeight();
		
		str="Stéréo haute qualité";
		g.drawOval(X,Y,24,24);
		g.drawString(str,X+32,Y+12+fm.getAscent()/2);
		if(SoundManager.getSoundQuality()==SoundManager.STEREO_SUP) g.fillOval(X,Y,24,24);
		if(mousePressed&&new Rectangle(X,Y,fm.stringWidth(str)+32,24).contains(staticMouseX,staticMouseY)) SoundManager.setSoundQuality(SoundManager.STEREO_SUP);
		Y+=fm.getHeight();
		
		X=W/2+W/10-32;
		Y=H/3;
		
		str="Commandes clavier :";
		outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(X-16,Y+12+fm.getAscent()/2));
		g.draw(outline);
		Y+=fm.getHeight()*2;
		
		str="Avancer";
		g.drawString(str,X,Y+12+fm.getAscent()/2);
		if(mousePressed&&new Rectangle(X,Y,W-X-32,24).contains(staticMouseX,staticMouseY)&&
				KEY_MOVE_UP!=-1&&KEY_MOVE_DOWN!=-1&&KEY_MOVE_LEFT!=-1&&KEY_MOVE_RIGHT!=-1&&KEY_TURBO!=-1&&KEY_SHOW_MAP!=-1&&KEY_SCREENSHOT!=-1) KEY_MOVE_UP=-1;
		str=KeyEvent.getKeyText(KEY_MOVE_UP);
		if(KEY_MOVE_UP!=-1||T%30<15) g.drawString(str,W-32-fm.stringWidth(str),Y+12+fm.getAscent()/2);
		Y+=fm.getHeight();
		
		str="Reculer";
		g.drawString(str,X,Y+12+fm.getAscent()/2);
		if(mousePressed&&new Rectangle(X,Y,W-X-32,24).contains(staticMouseX,staticMouseY)&&
				KEY_MOVE_UP!=-1&&KEY_MOVE_DOWN!=-1&&KEY_MOVE_LEFT!=-1&&KEY_MOVE_RIGHT!=-1&&KEY_TURBO!=-1&&KEY_SHOW_MAP!=-1&&KEY_SCREENSHOT!=-1) KEY_MOVE_DOWN=-1;
		str=KeyEvent.getKeyText(KEY_MOVE_DOWN);
		if(KEY_MOVE_DOWN!=-1||T%30<15) g.drawString(str,W-32-fm.stringWidth(str),Y+12+fm.getAscent()/2);
		Y+=fm.getHeight();
		
		str="Latéral gauche";
		g.drawString(str,X,Y+12+fm.getAscent()/2);
		if(mousePressed&&new Rectangle(X,Y,W-X-32,24).contains(staticMouseX,staticMouseY)&&
				KEY_MOVE_UP!=-1&&KEY_MOVE_DOWN!=-1&&KEY_MOVE_LEFT!=-1&&KEY_MOVE_RIGHT!=-1&&KEY_TURBO!=-1&&KEY_SHOW_MAP!=-1&&KEY_SCREENSHOT!=-1) KEY_MOVE_LEFT=-1;
		str=KeyEvent.getKeyText(KEY_MOVE_LEFT);
		if(KEY_MOVE_LEFT!=-1||T%30<15) g.drawString(str,W-32-fm.stringWidth(str),Y+12+fm.getAscent()/2);
		Y+=fm.getHeight();
		
		str="Latéral droit";
		g.drawString(str,X,Y+12+fm.getAscent()/2);
		if(mousePressed&&new Rectangle(X,Y,W-X-32,24).contains(staticMouseX,staticMouseY)&&
				KEY_MOVE_UP!=-1&&KEY_MOVE_DOWN!=-1&&KEY_MOVE_LEFT!=-1&&KEY_MOVE_RIGHT!=-1&&KEY_TURBO!=-1&&KEY_SHOW_MAP!=-1&&KEY_SCREENSHOT!=-1) KEY_MOVE_RIGHT=-1;
		str=KeyEvent.getKeyText(KEY_MOVE_RIGHT);
		if(KEY_MOVE_RIGHT!=-1||T%30<15) g.drawString(str,W-32-fm.stringWidth(str),Y+12+fm.getAscent()/2);
		Y+=fm.getHeight();
		
		str="Turbo";
		g.drawString(str,X,Y+12+fm.getAscent()/2);
		if(mousePressed&&new Rectangle(X,Y,W-X-32,24).contains(staticMouseX,staticMouseY)&&
				KEY_MOVE_UP!=-1&&KEY_MOVE_DOWN!=-1&&KEY_MOVE_LEFT!=-1&&KEY_MOVE_RIGHT!=-1&&KEY_TURBO!=-1&&KEY_SHOW_MAP!=-1&&KEY_SCREENSHOT!=-1) KEY_TURBO=-1;
		str=KeyEvent.getKeyText(KEY_TURBO);
		if(KEY_TURBO!=-1||T%30<15) g.drawString(str,W-32-fm.stringWidth(str),Y+12+fm.getAscent()/2);
		Y+=fm.getHeight();
		
		str="Carte";
		g.drawString(str,X,Y+12+fm.getAscent()/2);
		if(mousePressed&&new Rectangle(X,Y,W-X-32,24).contains(staticMouseX,staticMouseY)&&
				KEY_MOVE_UP!=-1&&KEY_MOVE_DOWN!=-1&&KEY_MOVE_LEFT!=-1&&KEY_MOVE_RIGHT!=-1&&KEY_TURBO!=-1&&KEY_SHOW_MAP!=-1&&KEY_SCREENSHOT!=-1) KEY_SHOW_MAP=-1;
		str=KeyEvent.getKeyText(KEY_SHOW_MAP);
		if(KEY_SHOW_MAP!=-1||T%30<15) g.drawString(str,W-32-fm.stringWidth(str),Y+12+fm.getAscent()/2);
		Y+=fm.getHeight();
		
		str="Capture d'écran";
		g.drawString(str,X,Y+12+fm.getAscent()/2);
		if(mousePressed&&new Rectangle(X,Y,W-X-32,24).contains(staticMouseX,staticMouseY)&&
				KEY_MOVE_UP!=-1&&KEY_MOVE_DOWN!=-1&&KEY_MOVE_LEFT!=-1&&KEY_MOVE_RIGHT!=-1&&KEY_TURBO!=-1&&KEY_SHOW_MAP!=-1&&KEY_SCREENSHOT!=-1) KEY_SCREENSHOT=-1;
		str=KeyEvent.getKeyText(KEY_SCREENSHOT);
		if(KEY_SCREENSHOT!=-1||T%30<15) g.drawString(str,W-32-fm.stringWidth(str),Y+12+fm.getAscent()/2);
		Y+=fm.getHeight();
		
		
		g.setStroke(oldStroke);
		g.setColor(COLOR_TEAM_A);
		g.fillOval(staticMouseX-4,staticMouseY-4,9,9);
		g.setColor(Color.black);
		g.drawOval(staticMouseX-4,staticMouseY-4,9,9);
	}
	
	private void showPauseMenu(Graphics2D g){
		g.drawImage(pauseBackground,0,0,null);
		int W=getWidth();
		int H=getHeight();
		g.setColor(new Color(0,0,0,128));
		g.fillRect(0,0,W,H);
		g.setColor(Color.white);
		
		setFont(g,44);
		int Y=H/3;

		Stroke oldStroke=g.getStroke();
		
		g.setStroke(new BasicStroke(1.5f,BasicStroke.JOIN_MITER,BasicStroke.CAP_SQUARE));
		
		String str="reprendre";
		Area outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(W/2-fm.stringWidth(str)/2,Y));
		if((new Rectangle(W/2-fm.stringWidth(str)/2,Y-fm.getAscent(),fm.stringWidth(str),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY)))){
			g.setColor(new Color(255,255,255,192));
			g.fill(outline);
			if(mousePressed){
				setPause(false);
			}
		}
		g.setColor(COLOR_TEAM_A);
		g.draw(outline);
		Y+=fm.getHeight();
		
		str="recommencer";
		outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(W/2-fm.stringWidth(str)/2,Y));
		if((new Rectangle(W/2-fm.stringWidth(str)/2,Y-fm.getAscent(),fm.stringWidth(str),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY)))){
			g.setColor(new Color(255,255,255,192));
			g.fill(outline);
			if(mousePressed){
				initGame(true);
			}
		}
		g.setColor(COLOR_TEAM_A);
		g.draw(outline);
		Y+=fm.getHeight();
		
		str="paramètres";
		outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(W/2-fm.stringWidth(str)/2,Y));
		if((new Rectangle(W/2-fm.stringWidth(str)/2,Y-fm.getAscent(),fm.stringWidth(str),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY)))){
			g.setColor(new Color(255,255,255,192));
			g.fill(outline);
			if(mousePressed){
				menu=MENU_SETTINGS;
			}
		}
		g.setColor(COLOR_TEAM_A);
		g.draw(outline);
		Y+=fm.getHeight()*2;
		
		str="menu principal";
		outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(W/2-fm.stringWidth(str)/2,Y));
		if((new Rectangle(W/2-fm.stringWidth(str)/2,Y-fm.getAscent(),fm.stringWidth(str),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY)))){
			g.setColor(new Color(255,255,255,192));
			g.fill(outline);
			if(mousePressed){
				T=0;
				menu=MENU_MAIN;
			}
		}
		g.setColor(COLOR_TEAM_A);
		g.draw(outline);
		Y+=fm.getHeight();
		
		str="quitter";
		outline=new Area(g.getFont().createGlyphVector(fm.getFontRenderContext(),str).getOutline());
		outline.transform(AffineTransform.getTranslateInstance(W/2-fm.stringWidth(str)/2,Y));
		if((new Rectangle(W/2-fm.stringWidth(str)/2,Y-fm.getAscent(),fm.stringWidth(str),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY)))){
			g.setColor(new Color(255,255,255,192));
			g.fill(outline);
			if(mousePressed){
				System.exit(0);
			}
		}
		g.setColor(COLOR_TEAM_A);
		g.draw(outline);
		Y+=fm.getHeight();
		
		g.setStroke(oldStroke);
		
		g.setColor(COLOR_TEAM_A);
		g.fillOval(staticMouseX-4,staticMouseY-4,9,9);
		g.setColor(Color.black);
		g.drawOval(staticMouseX-4,staticMouseY-4,9,9);
		
	}
	
	private void drawMap(Graphics2D g){
		int W=getWidth();
		int H=getHeight();
		int X=W/2;
		int Y=H/2;
		int ww=1024;
		int hh=768;
		
		g.setColor(new Color(0,0,0,128));
		g.fillRect(0,0,W,H);
		
		if(mapBounds.width>mapBounds.height*4/3){
			hh=(int)(mapBounds.height*1024.0/mapBounds.width);
			X-=512;
			Y-=hh/2;
		}else{
			ww=(int)(mapBounds.width*768.0/mapBounds.height);
			X-=ww/2;
			Y-=384;
		}
		
		Composite oldComposite=g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f));
		g.drawImage(minimap,W/2-512,H/2-384,null);
		
		g.setColor(COLOR_TEAM_A);
		g.setComposite(oldComposite);
		
		int x=X+(int)((player.x-mapBounds.x)*ww/mapBounds.width);
		int y=Y+(int)((player.y-mapBounds.y)*hh/mapBounds.height);
		g.rotate(-Math.toRadians(player.angle),x,y);
		
		Polygon poly=new Polygon();
		poly.addPoint(x+7,y);
		poly.addPoint(x-5,y+5);
		poly.addPoint(x-5,y-5);
		
		g.fill(poly);
		
		g.rotate(Math.toRadians(player.angle),x,y);
		
		Goal goal=Goal.getCurrentGoal();
		
		x=X+(int)((goal.x-mapBounds.x)*ww/mapBounds.width);
		y=Y+(int)((goal.y-mapBounds.y)*hh/mapBounds.height);
		
		g.drawOval(x-7,y-7,14,14);
		
		setFont(g,18);
		
		String str=goal.getDescription();
		Vector<String> lines=new Vector<String>();
		lines.add("");
		
		for(int i=0;i<str.length();i++){
			lines.set(lines.size()-1,lines.get(lines.size()-1)+str.charAt(i));
			if(str.charAt(i)=='\n'||fm.stringWidth(lines.get(lines.size()-1))>ww){
				lines.add("");
			}
		}
		
		for(int i=lines.size()-1;i>=0;i--){
			g.drawString(lines.get(lines.size()-1-i),X,Y-i*fm.getHeight());
		}
	}
	
	private void showMapMenu(Graphics2D g){
		int W=getWidth();
		int H=getHeight();
		g.setColor(Color.black);
		g.fillRect(0,0,W,H);
		g.setColor(COLOR_TEAM_A);
		
		int Y=24;
		
		setFont(g,24);
		g.setColor(Color.gray);
		if(new Rectangle(24,Y,fm.stringWidth("Niveau :"),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY))){
			g.setColor(COLOR_TEAM_A);
			if(mousePressed){
				selectWeapons=false;
			}
		}
		if(!selectWeapons) g.setColor(COLOR_TEAM_A);
		g.drawString("Niveau :",24,Y+fm.getAscent());
		int ww=48+fm.stringWidth("Niveau :");
		Y+=2*fm.getAscent();
		setFont(g,18);
		
		for(int i=0;i<levels.size();i++){
			String lvl=levels.get(i);
			ww=Math.max(ww,60+fm.stringWidth(lvl));
			g.setColor(Color.gray);
			if(new Rectangle(32,Y,fm.stringWidth(lvl),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY))){
				g.setColor(COLOR_TEAM_A);
				if(mousePressed){
					selectWeapons=false;
					map=lvl;
					try {
						minimap=ImageIO.read(new File("data/map/"+map+".png"));
					
						BufferedReader buff=new BufferedReader(new InputStreamReader(new FileInputStream(new File("data/map/"+map+".map")),"ISO-8859-1"));
						buff.readLine();
						levelDescription=buff.readLine();
						if(levelDescription!=null) levelDescription=levelDescription.replace("<br/>","\n");
						buff.close();
					} catch (IOException e) {e.printStackTrace();}
				}
			}
			if(lvl.equals(map)) g.setColor(COLOR_TEAM_A);
			g.drawString(lvl,32,Y+fm.getAscent());
			Y+=fm.getHeight();
		}
		g.setColor(COLOR_TEAM_A);
		
		
		setFont(g,24);
		Y+=3*fm.getAscent();
		g.setColor(Color.gray);
		if(new Rectangle(24,Y,fm.stringWidth("Armes :"),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY))){
			g.setColor(COLOR_TEAM_A);
			if(mousePressed){
				selectWeapons=true;
			}
		}
		if(selectWeapons) g.setColor(COLOR_TEAM_A);
		g.drawString("Armes :",24,Y+fm.getAscent());
		Y+=2*fm.getAscent();
		setFont(g,18);
		g.setColor(Color.gray);
		
		Weapon wLeft=Weapon.getWeapon(weaponLeft);
		Weapon wRight=Weapon.getWeapon(weaponRight);
		
		for(int i=0;i<12;i++){
			Weapon weap=Weapon.getWeapon(i);
			g.setColor(Color.gray);
			if(new Rectangle(32,Y,fm.stringWidth(weap.getShortName()),fm.getAscent()).contains(new Point(staticMouseX,staticMouseY))){
				g.setColor(COLOR_TEAM_A);
				if(mousePressed){
					selectWeapons=true;
					if(mouseLeft){
						if(weaponLeft==i){
							weaponLeft=Weapon.NONE;
						}else{
							weaponLeft=i;
						}
					}else{
						if(weaponRight==i){
							weaponRight=Weapon.NONE;
						}else{
							weaponRight=i;
						}
					}
				}
			}
			if(weaponLeft==i) g.setColor(COLOR_TEAM_A);
			if(weaponRight==i) g.setColor(COLOR_TEAM_B);
			g.drawString(weap.getShortName(),24,Y+fm.getAscent());
			Y+=fm.getHeight();
		}
		
		Stroke oldStroke=g.getStroke();
		g.setStroke(new BasicStroke(4,BasicStroke.JOIN_MITER,BasicStroke.CAP_SQUARE));
		
		g.setColor(Color.gray);
		g.drawLine(ww,0,ww,H);
		g.setColor(COLOR_TEAM_A);
		
		if(!selectWeapons){
			g.setColor(Color.lightGray);
			g.drawImage(minimap,ww+(W-ww)/2-512,24,null);
			
			String str=levelDescription;
			Vector<String> lines=new Vector<String>();
			lines.add("");
			
			if(str!=null){
				for(int i=0;i<str.length();i++){
					lines.set(lines.size()-1,lines.get(lines.size()-1)+str.charAt(i));
					if(str.charAt(i)=='\n'||fm.stringWidth(lines.get(lines.size()-1))>W-ww-48){
						lines.add("");
					}
				}
			}
			
			for(int i=0;i<lines.size();i++){
				g.drawString(lines.get(i),ww+24,816+i*fm.getHeight());
			}
		}else{
			g.setStroke(oldStroke);
			for(int i=0;i<2;i++){
				int X=ww+32;
				Color c=COLOR_TEAM_A;
				Weapon weap=Weapon.getWeapon(weaponLeft);
				if(i==1){
					X+=(W-ww)/2;
					c=COLOR_TEAM_B;
					weap=Weapon.getWeapon(weaponRight);
					if(weaponRight==Weapon.NONE) continue;
				}else{
					if(weaponLeft==Weapon.NONE) continue;
				}
				g.setColor(c);
				setFont(g,24);
				Y=24+fm.getAscent();
				g.drawString(weap.getName(),X-8,Y);
				Y+=fm.getHeight()*2;
				
				setFont(g,18);
				g.setColor(Color.lightGray);
				
				int wBar=(W-ww)/2-fm.stringWidth("Cadence de rechargement :")-96;
				
				g.drawString("Capacité des chargeurs :",X,Y);
				g.setColor(c);
				g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),weap.getAmmoByClip()*wBar/Weapon.getAmmoByClipMax(),fm.getAscent());
				g.setColor(Color.lightGray);
				g.drawRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
				Y+=fm.getHeight();
				
				g.drawString("Nombre de chargeurs :",X,Y);
				g.setColor(c);
				if(weap.getClip()!=99){
					g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),weap.getClip()*wBar/Weapon.getClipMax(),fm.getAscent());
				}else{
					g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
					g.setColor(Color.white);
					Y-=fm.getAscent()/2;
					setFont(g,14);
					g.drawString("illimité",X+(W-ww)/2-wBar/2-48-fm.stringWidth("illimité")/2,Y+fm.getAscent()/2);
					setFont(g,18);
					Y+=fm.getAscent()/2;
				}
				g.setColor(Color.lightGray);
				g.drawRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
				Y+=fm.getHeight();
				
				g.drawString("Cadence de tir :",X,Y);
				g.setColor(c);
				g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar-weap.getShootingTime()*wBar/Weapon.getShootingTimeMax(),fm.getAscent());
				g.setColor(Color.lightGray);
				g.drawRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
				Y+=fm.getHeight();
				
				g.drawString("Cadence de rechargement :",X,Y);
				g.setColor(c);
				g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar-weap.getChargingTime()*wBar/Weapon.getChargingTimeMax(),fm.getAscent());
				g.setColor(Color.lightGray);
				g.drawRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
				Y+=fm.getHeight();
				
				g.drawString("Précision :",X,Y);
				g.setColor(c);
				g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),(int)(wBar-weap.getDispersion()*wBar/Weapon.getDispersionMax()),fm.getAscent());
				g.setColor(Color.lightGray);
				g.drawRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
				Y+=fm.getHeight();
				
				g.drawString("Dommages :",X,Y);
				g.setColor(c);
				g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),(int)(weap.getDamagePerStep()*wBar/Weapon.getDamagePerStepMax()),fm.getAscent());
				g.setColor(Color.lightGray);
				g.drawRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
				Y+=fm.getHeight();
				
				g.drawString("Vitesse :",X,Y);
				g.setColor(c);
				g.fillRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),(int)weap.getSpeed()*wBar/Weapon.getSpeedMax(),fm.getAscent());
				g.setColor(Color.lightGray);
				g.drawRect(X+(W-ww)/2-wBar-48,Y-fm.getAscent(),wBar,fm.getAscent());
				Y+=fm.getHeight();
				
				
			}
		}
		
		/*g.setColor(Color.gray);
		g.drawLine(ww,816,W,816);
		g.setColor(COLOR_TEAM_A);
		
		int X=ww+24;
		int Y=840;
		int w=W-ww-48;
		
		setFont(g,16);
		
		for(int i=0;i<12;i++){
			Weapon weap=Weapon.getWeapon(i);
			g.drawString(weap.getName(),X+w/2,Y+i*fm.getHeight());
		}*/
		
		g.setColor(COLOR_TEAM_A);
		
		g.fillRect(0,H-48,ww,48);
		
		g.setColor(Color.white);
		setFont(g,36);
		g.drawString("GO !",ww/2-fm.stringWidth("GO !")/2,H-24+fm.getAscent()-fm.getHeight()/2);
		
		if(mousePressed&&new Rectangle(0,H-48,ww,48).contains(new Point(staticMouseX,staticMouseY))){
			initGame(false);
		}
		
		g.setStroke(oldStroke);
		
		g.setColor(COLOR_TEAM_A);
		g.fillOval(staticMouseX-4,staticMouseY-4,9,9);
		g.setColor(Color.black);
		g.drawOval(staticMouseX-4,staticMouseY-4,9,9);
		
		if(T<15){
			int alpha=255-(int)getProduitCroixCarre(T,255,15);
			g.setColor(new Color(0,0,0,alpha));
			g.fillRect(0,0,W,H);
		}
	}
	
	private void showMainMenu(Graphics2D g){
		int W=getWidth();
		int H=getHeight();
		g.setColor(Color.black);
		g.fillRect(0,0,W,H);
		
		
		int tr=Math.min(128,T*2);;
		
		g.translate(0,-tr);
		
		Paint defaultPaint = g.getPaint();
		g.setPaint(new RadialGradientPaint(new Point2D.Double(W/2,H+W*2.5),(float)(W*2.5),new Point2D.Double(W/2,H+W*2.5), new float[]{0f,0.994f,0.995f,1f},
				new Color[]{Color.black,Color.black,COLOR_TEAM_A,Color.black},CycleMethod.NO_CYCLE));
		
		g.fillOval(-W*2,H,W*5,W*5);
		g.translate(0,tr);
		//g.setPaint(defaultPaint);
		
		/*if(T>48){
			int alpha=Math.min(255,(T-48)*3);
			Color c=COLOR_TEAM_A;
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
			setFont(g,72);
			FontMetrics fm=g.getFontMetrics();
			int shift=0;
			if(T<48+255/3) shift=(int)getProduitCroixCarre(255/3-(T-48),48,255/3);
			g.drawString("Blue Sunshine",W/2-fm.stringWidth("Blue Sunshine")/2,H*3/10-shift);
		}*/
		
		int alpha=Math.min(255,T*3);
		Color c=COLOR_TEAM_A;
		//g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
		
		setFont(g,72);
		int shift=0;
		if(T<255/3) shift=(int)getProduitCroixCarre(255/3-T,48,255/3);
		
		g.setPaint(new GradientPaint(W/2,H*3/10-shift-fm.getAscent(),c,W/2,H*3/10-shift,c.darker()));
		//g.fillRect(W/2-fm.stringWidth("Blue Sunshine")/2,H*3/10,fm.stringWidth("Blue Sunshine"),fm.getAscent());
		g.drawString("Blue Sunshine",W/2-fm.stringWidth("Blue Sunshine")/2,H*3/10-shift);
		
		
		g.setPaint(defaultPaint);
		setFont(g,24);
		
		if(T>85){
			alpha=255-(T-85)%30*255/30;
			if((T-85)%60<30) alpha=255-alpha;
			g.setColor(new Color(255,255,255,alpha));
			String str="Appuyez sur une touche pour continuer";
			g.drawString(str,W/2-fm.stringWidth(str)/2,H-fm.getHeight());
		}
		if(exitMainMenu!=-1){
			alpha=(int)getProduitCroixCarre(T-exitMainMenu,255,15);
			g.setColor(new Color(0,0,0,alpha));
			g.fillRect(0,0,W,H);
			if(T-exitMainMenu==15){
				exitMainMenu=-1;
				T=0;
				menu=MENU_CHOOSE_MAP;
			}
		}
	}
	
	private void showLogoMenu(Graphics2D g){
		int W=getWidth();
		int H=getHeight();
		g.setColor(Color.white);
		g.fillRect(0,0,W,H);
		g.translate(W/2-200,H/2-100);
		wss.showLogo(g,399,200);
		g.translate(-(W/2-200),-(H/2-100));
		
		setFont(g,24);
		FontMetrics fm=g.getFontMetrics();
		
		
		//int alpha=(T>30)?(int)getProduitCroixCarre(T-30,255,60):0;
		int alpha=(int)getProduitCroixCarre(T,255,60);
		g.setColor(new Color(0,0,0,Math.min(255,alpha)));
		
		int Y=H-fm.getHeight();;
		String str="Lycée Louis Armand";
		g.drawString(str,24,Y);
		
		str="Projet ISN 2014/2015";
		g.drawString(str,W/2-fm.stringWidth(str)/2,Y);
		
		str="MARCHAL Térence";
		g.drawString(str,W-fm.stringWidth(str)-24,Y);
		
		if(T>120){
			alpha=(int)getProduitCroixCarre(T-120,255,45);
			g.setColor(new Color(0,0,0,Math.min(255,alpha)));
			g.fillRect(0,0,W,H);
			if(alpha>255){
				T=0;
				menu=MENU_MAIN;
				return;
			}
		}
	}
	
	private void drawEffects(Graphics2D g){
		if(player.isBlinded()){
			int al=255;
			if(player.getBlindedTime()<=10) al=(int)(player.getBlindedTime()*255/10.0);
			int size=W/100;
			for(int i=0;i<W/size+1;i++){
				for(int j=0;j<H/size+1;j++){
					int n=(int)(Math.random()*255);
					g.setColor(new Color(n,n,n,al));
					g.fillRect(i*size,j*size,size,size);
				}
			}
			
			setFont(g,64);
			Color c=COLOR_TEAM_A;
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),al));
			String str="NO INPUT SIGNAL";
			if(player.isParalyzed()) str="NO INPUT & OUTPUT SIGNAL";
			g.drawString(str,W/2-fm.stringWidth(str)/2,H/2+fm.getDescent());
		}
		if(player.isParalyzed()&&!player.isBlinded()){
			int al=255;
			if(player.getParalyzedTime()<=10) al=(int)(player.getParalyzedTime()*255/10.0);
			setFont(g,64);
			Color c=COLOR_TEAM_A;
			int alpha=T%30*255/30;
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha*al/255));
			String str="NO OUTPUT SIGNAL";
			g.drawString(str,W/2-fm.stringWidth(str)/2,H/2+fm.getDescent());
			
			int hh=fm.getHeight();
			int ww=fm.stringWidth(str);
			
			setFont(g,24);
			
			g.setColor(new Color(0,0,0,64*al/255));
			int width=fm.stringWidth("0");
			int height=fm.getHeight();
			for(int i=0;i<H/height+1;i++){
				for(int j=0;j<W/width+1;j++){
					if((i*height<H/2-hh/2||i*height>H/2+hh/2)||((j+1)*width<W/2-ww/2||j*width>W/2+ww/2)){
						g.drawString(Math.round(Math.random())+"",j*width,i*height);
					}
				}
			}
		}	
		 
	}
	
	private void drawHUD(Graphics2D g){
		Goal goal=Goal.getCurrentGoal();
		int X=W-368;
		int Y=H-176;
		Color c=player.getColor();
		if(hud==null) initHUD();
		g.drawImage(hud,X-8,Y-8,null);
		
		Weapon left=player.getLeftWeapon();
		Weapon right=player.getRightWeapon();
		
		Stroke oldStroke=g.getStroke();
		g.setStroke(new BasicStroke(4,BasicStroke.JOIN_MITER,BasicStroke.CAP_SQUARE));

		int alpha=left.getTimeLeft()*255/left.getShootingTime();
		if(left.isReloading()) alpha=left.getTimeLeft()*255/left.getChargingTime();
		if(alpha<=255){
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
			
			g.drawOval(X+12,Y+12,136,136);
		}
		if(alpha==0) g.setColor(c);
		
		setFont(g,64);
		
		String str=left.getAmmo()+"";
		if(left.isReloading()) str="•••";
		if(left.getAmmo()==0) str="×";
		int h=fm.getDescent();
		g.drawString(str,X+80-fm.stringWidth(str)/2,Y+80+h);
		
		setFont(g,10);
		
		str=left.getShortName();
		g.drawString(str,X+80-fm.stringWidth(str)/2,Y+80+h+fm.getHeight());
		
		g.setColor(c);
		setFont(g,24);
		
		str="";
		if(left.getClip()<99) for(int i=0;i<left.getClip();i++) str+="×";
		g.drawString(str,X+80-fm.stringWidth(str)/2,Y+140);
		
		setFont(g,64);
		X+=192;
		
		alpha=right.getTimeLeft()*255/right.getShootingTime();
		if(right.isReloading()) alpha=right.getTimeLeft()*255/right.getChargingTime();
		if(alpha<=255){
			g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
			
			g.drawOval(X+12,Y+12,136,136);
		}
		if(alpha==0) g.setColor(c);
		
		str=right.getAmmo()+"";
		if(right.isReloading()) str="•••";
		if(right.getAmmo()==0) str="×";
		h=fm.getDescent();
		g.drawString(str,X+80-fm.stringWidth(str)/2,Y+80+h);
		
		setFont(g,10);
		
		str=right.getShortName();
		g.drawString(str,X+80-fm.stringWidth(str)/2,Y+80+h+fm.getHeight());
		
		setFont(g,24);
		
		str="";
		if(right.getClip()<99) for(int i=0;i<right.getClip();i++) str+="×";
		g.drawString(str,X+80-fm.stringWidth(str)/2,Y+140);
		
		g.setColor(c);
		setFont(g,64);
		
		X-=192;
		
		g.setStroke(new BasicStroke(8,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
		
		g.drawArc(X-16,Y-16,192,192,60,(int)(player.getLife()*160/100));
		
		X+=192;
		int val=(int)(player.getTurbo()*60/300);
		g.drawArc(X-16,Y-16,192,192,90-val,val*2);
		
		g.setStroke(oldStroke);
		
		X-=16;
		Y+=80;
		Y=(H+Y)/2;
		
		int dir=90+(int)(pointDirection(player.x,player.y,goal.x,goal.y)-player.angle);
		g.fillArc(X-16,Y-16,32,32,dir-8,16);
		
		dir=(int)(-player.angle+90);
		Point p=distDir(16,dir);
		g.fillOval(X-2,Y-2,4,4);
		setFont(g,10);
		g.drawString("N",X+p.x-fm.stringWidth("N")/2,Y+p.y+fm.getAscent()/2);
		g.drawArc(X-16,Y-16,32,32,dir+30,300);
		
		
		
		setFont(g,14);
		
		int dist=(int)Math.round(player.pointDistance(player.x,player.y,goal.x,goal.y)/10);	// en décimètres
		
		str=(dist/10)+"."+(dist%10)+"m";
		g.drawString(str,X-fm.stringWidth(str)/2,Y+16+fm.getHeight());
		
		X=W-288;
		Y=H-96;
		p=player.distDir(96,55);
		X+=p.x;
		Y+=p.y+fm.getAscent()/2;
		int t=goal.getTimeLeft()/30;
		if(goal.getTimeLeft()>=0){
			if(t<=10){
				c=COLOR_TEAM_A;
				g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),255-T%30*255/30));
			}
			int m=t/60;
			str=((m>9)?m:"0"+m)+":";
			t-=m*60;
			str+=(t>9)?t:"0"+t;
			g.rotate(Math.toRadians(50),X,Y);
			g.drawString(str,X,Y);
			g.rotate(-Math.toRadians(50),X,Y);
		}
		
		
		g.setStroke(oldStroke);
	}
	
	private void initHUD(){		
		hud=new BufferedImage(368,176,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=(Graphics2D)hud.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(player.getColor());
		g.setStroke(new BasicStroke(4,BasicStroke.JOIN_MITER,BasicStroke.CAP_SQUARE));
		
		int X=8;
		int Y=8;
		g.drawArc(X,Y,160,160,90,180);
		g.drawArc(X+192,Y,160,160,270,180);
		
		Path2D.Double path=new Path2D.Double();
		path.moveTo(X+80,Y);
		path.curveTo(X+176,Y,X+176,Y+160,X+272,Y+160);
		path.moveTo(X+80,Y+160);
		path.curveTo(X+176,Y+160,X+176,Y,X+272,Y);
		g.draw(path);
		
		g.dispose();
	}
	
	private void initAStar(){
		Rectangle map=mapBounds;
		Area orangeArea=(Area)this.orangeArea.clone();
		Area redArea=(Area)this.redArea.clone();
		navMap=new byte[(map.width-map.x)/sizeNavMap+1][(map.height-map.y)/sizeNavMap+1];
		int casesMax=16;
		int cases=casesMax;	// principe de diviser pour régner : indique le nombre de cases en hauteur qu'on peut tester en une fois au max
		for(int i=0;i<(map.width-map.x)/sizeNavMap+1;i++){
			for(int j=0;j<(map.height-map.y)/sizeNavMap+1;j++){
				Rectangle rect=new Rectangle(i*sizeNavMap+map.x,j*sizeNavMap+map.y,sizeNavMap,sizeNavMap*cases);
				
				if(orangeArea.intersects(rect)){
					if(cases==1){
						if(redArea.intersects(rect)){
							navMap[i][j]=NAV_RED;
						}else{
							navMap[i][j]=NAV_ORANGE;
						}
					}else{
						cases/=2;
						j--;
					}
				}else{
					for(int k=0;k<cases;k++){
						if(j+k<navMap[0].length){
							navMap[i][j+k]=NAV_GREEN;
						}
					}
					j+=cases-1;
					cases=casesMax;
				}
				percentLoading=((i-1)*((map.height-map.y)/sizeNavMap+1)+j)*100/(((map.width-map.x)/sizeNavMap+1)*((map.height-map.y)/sizeNavMap+1));
			}
		}
	}
	
	public static void addToRedArea(Area area){
		redArea.add(area);
	}
	
	public static void addToOrangeArea(Area area){
		orangeArea.add(area);
	}
	
	private void initOpenAL(){
		 //new SoundSource();
	}
	
	private void setFont(Graphics2D g, int size){
		Font f;
		if(fonts.containsKey(size)){
			f=fonts.get(size);
		}else{
			f=fontDefault.deriveFont((float)size);
			fonts.put(size,f);
		}
		g.setFont(f);
		fm=g.getFontMetrics();
	}
	
	private void loadFont() {
		 try {
			 GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
			 fontDefault=Font.createFont(Font.TRUETYPE_FONT, new File("data/font/xirod.ttf"));
		 } catch (IOException|FontFormatException e) {e.printStackTrace();};
	 }
	
	private void showLoading(Graphics2D g){
		g.setColor(Color.black);
		g.fillRect(0,0,W,H);
		g.setColor(Color.white);
		
		g.setColor(COLOR_TEAM_A);
		setFont(g,64);
		String str="Blue Sunshine";
		int Y=fm.getHeight()*2;
		g.drawString(str,W/2-fm.stringWidth(str)/2,Y);
		
		g.setColor(Color.white);
		setFont(g,24);
		str="Chargement en cours...";
		Y+=fm.getHeight()*2;
		g.drawString(str,W/2-fm.stringWidth(str)/2,Y);
		
		Y+=(H-Y)/2;
		
		Stroke oldStroke=g.getStroke();
		
		for(int i=0;i<5;i++){
			int r=i*45;
			int angle=(int)(T*4*(1+i));
			g.setStroke(new BasicStroke(50,BasicStroke.JOIN_MITER,BasicStroke.CAP_BUTT));
			g.drawArc(W/2-r,Y-r,r*2,r*2,angle,90);
		}
		
		g.setStroke(oldStroke);
		
		setFont(g,18);
		Y=H-fm.getHeight()*2;
		str=getTip();
		g.drawString(str,W/2-fm.stringWidth(str)/2,Y);
		
		/*if(!showPatterns){
			g.setColor(COLOR_TEAM_A);
			setFont(g,14);
			g.drawString("Motifs désactivés",50,H/4);
		}*/
		
		g.setColor(COLOR_TEAM_A);
		setFont(g,14);
		int w=fm.stringWidth(" 100% ");
		Y=H/4;
		int X=W-80;
		
		for(int i=0;i<7;i++){				// <- CHANGER VALEUR POUR D'AUTRES CHARGEMENTS !!!
			str=getTitleLoading(i);
			g.drawString(str,X-w-fm.stringWidth(str),Y);
			str="";
			if(i<idLoading) str="100%";
			if(i==idLoading){
				str=percentLoading+"%";
				if(percentLoading==-1) str="µ";
			}
			g.drawString(str,W-fm.stringWidth(str),Y);
			Y+=fm.getHeight();
		}
		
	}

	
	private String getTip(){
		return tips[idTip];
	}
	
	private String getTitleLoading(int id){
		String str="";
		switch(id){
			case 0:str="Portes";break;
			case 1:str="Murs";break;
			case 2:str="Motifs";break;
			case 3:str="Chemins";break;
			case 4:str="Ennemis";break;
			case 5:str="Capteurs";break;
			case 6:str="Objectifs";break;
		}
		return str;
	}
	
	private void updateScreenPos(){
		Point p=distDir(player.y-viewY,player.angle);
		Point P=distDir(W/2,player.angle+90);
		topLeft=new Point((int)player.x+p.x+P.x,(int)player.y+p.y+P.y);
		p=distDir(player.y-viewY,player.angle);
		P=distDir(W/2,player.angle-90);
		topRight=new Point((int)player.x+p.x+P.x,(int)player.y+p.y+P.y);
		p=distDir(player.y-viewY-H,player.angle);
		P=distDir(W/2,player.angle+90);
		bottomLeft=new Point((int)player.x+p.x+P.x,(int)player.y+p.y+P.y);
		p=distDir(player.y-viewY-H,player.angle);
		P=distDir(W/2,player.angle-90);
		bottomRight=new Point((int)player.x+p.x+P.x,(int)player.y+p.y+P.y);
	}
	
	private void drawWarFog(Graphics2D g){
		int d=(int)Math.sqrt(W*H);
		
		// coins de l'écran et équations des bords de l'écran :
		
		Point P=distDir(W/2,player.angle+90);
		Point playerLeft=new Point((int)player.x+P.x,(int)player.y+P.y);
		
		Polygon screen=new Polygon();
		screen.addPoint(topLeft.x,topLeft.y);
		screen.addPoint(topRight.x,topRight.y);
		screen.addPoint(bottomRight.x,bottomRight.y);
		screen.addPoint(bottomLeft.x,bottomLeft.y);
		
		
		double aTop=(topLeft.y-topRight.y*1.0)/(topLeft.x-topRight.x);
		double bTop=topLeft.y-aTop*topLeft.x;
		double aBottom=(bottomLeft.y-bottomRight.y*1.0)/(bottomLeft.x-bottomRight.x);
		double bBottom=bottomLeft.y-aBottom*bottomLeft.x;
		double aLeft=(topLeft.y-bottomLeft.y*1.0)/(topLeft.x-bottomLeft.x);
		double bLeft=topLeft.y-aLeft*topLeft.x;
		double aRight=(topRight.y-bottomRight.y*1.0)/(topRight.x-bottomRight.x);
		double bRight=topRight.y-aRight*topRight.x;
		
		// déterminants : vecteur pour savoir si un vecteur est à gauche d'un autre par exemple
		Point detHor=distDir(d,player.angle+90);	// rectangle de vue du héros
		Point detVert=distDir(d,player.angle);
		
		
		g.setColor(new Color(0,0,0,255));
		for(int i=0;i<objs.size();i++){	// 			<- objs.size()
			if(objs.get(i) instanceof Wall){
				Wall wall=(Wall)objs.get(i);
				Vector<Point> points=wall.getPoints();
				for(int j=0;j<points.size();j++){	//                       <-  points.size()
					Point p=points.get(j);
					Point nextP=points.get((j<points.size()-1)?j+1:0);
					/*
					 * ((detHor.x*(p.y-player.y)-detHor.y*(p.x-player.x)>0&&detHor.x*(p.y-topLeft.y)-detHor.y*(p.x-topLeft.x)<0&&
								detVert.x*(p.y-topLeft.y)-detVert.y*(p.x-topLeft.x)>0&&detVert.x*(p.y-topRight.y)-detVert.y*(p.x-topRight.x)<0))||
								((detHor.x*(nextP.y-player.y)-detHor.y*(nextP.x-player.x)>0&&detHor.x*(nextP.y-topLeft.y)-detHor.y*(nextP.x-topLeft.x)<0&&
								detVert.x*(nextP.y-topLeft.y)-detVert.y*(nextP.x-topLeft.x)>0&&detVert.x*(nextP.y-topRight.y)-detVert.y*(nextP.x-topRight.x)<0))
					 */
					if((detHor.x*(p.y-player.y)-detHor.y*(p.x-player.x)>0&&detHor.x*(p.y-topLeft.y)-detHor.y*(p.x-topLeft.x)<0||
							detHor.x*(nextP.y-player.y)-detHor.y*(nextP.x-player.x)>0&&detHor.x*(nextP.y-topLeft.y)-detHor.y*(nextP.x-topLeft.x)<0)&&
							screen.intersects(Math.min(p.x,nextP.x),Math.min(p.y,nextP.y),Math.abs(nextP.x-p.x),Math.abs(nextP.y-p.y))){
						// voir explications dans .txt
						double a=(p.y-player.y*1.0)/(p.x-player.x);
						double b=p.y-a*p.x;
						double x=(b-bTop)/(aTop-a);

						Point top1=new Point((int)x,(int)(aTop*x+bTop));
						if(Double.isInfinite(aTop)){
							top1=new Point(topLeft.x,(int)(a*topLeft.x+b));
						}
						if(Double.isInfinite(a)){
							top1=new Point(p.x,(int)(aTop*p.x+bTop));
						}
						x=(b-bBottom)/(aBottom-a);
						Point bottom1=new Point((int)x,(int)(aBottom*x+bBottom));
						if(Double.isInfinite(aBottom)){
							bottom1=new Point(bottomLeft.x,(int)(a*bottomLeft.x+b));
						}
						if(Double.isInfinite(a)){
							bottom1=new Point(p.x,(int)(aBottom*p.x+bBottom));
						}
						x=(b-bLeft)/(aLeft-a);
						Point left1=new Point((int)x,(int)(aLeft*x+bLeft));
						if(Double.isInfinite(aLeft)){
							left1=new Point(topLeft.x,(int)(a*topLeft.x+b));
						}
						if(Double.isInfinite(a)){
							left1=new Point(p.x,(int)(aLeft*p.x+bLeft));
						}
						x=(b-bRight)/(aRight-a);
						Point right1=new Point((int)x,(int)(aRight*x+bRight));
						if(Double.isInfinite(aRight)){
							right1=new Point(topRight.x,(int)(a*topRight.x+b));
						}
						if(Double.isInfinite(a)){
							right1=new Point(p.x,(int)(aRight*p.x+bRight));
						}

						a=(nextP.y-player.y)/(nextP.x-player.x);
						b=nextP.y-a*nextP.x;

						x=(b-bTop)/(aTop-a);
						Point top2=new Point((int)x,(int)(aTop*x+bTop));
						if(Double.isInfinite(aTop)){
							top2=new Point(topRight.x,(int)(a*topRight.x+b));
						}
						if(Double.isInfinite(a)){
							top2=new Point(p.x,(int)(aTop*p.x+bTop));
						}
						x=(b-bBottom)/(aBottom-a);
						Point bottom2=new Point((int)x,(int)(aBottom*x+bBottom));
						if(Double.isInfinite(aBottom)){
							bottom2=new Point(bottomRight.x,(int)(a*bottomRight.x+b));
						}
						if(Double.isInfinite(a)){
							bottom2=new Point(p.x,(int)(aBottom*p.x+bBottom));
						}
						x=(b-bLeft)/(aLeft-a);
						Point left2=new Point((int)x,(int)(aLeft*x+bLeft));
						if(Double.isInfinite(aLeft)){
							left2=new Point(topLeft.x,(int)(a*topLeft.x+b));
						}
						if(Double.isInfinite(a)){
							left2=new Point(p.x,(int)(aLeft*p.x+bRight));
						}
						x=(b-bRight)/(aRight-a);
						Point right2=new Point((int)x,(int)(aRight*x+bRight));
						if(Double.isInfinite(aRight)){
							right2=new Point(topRight.x,(int)(a*topRight.x+b));
						}
						if(Double.isInfinite(a)){
							right2=new Point(p.x,(int)(aRight*p.x+bRight));
						}

						/*g.setColor(Color.red);
							g.fillOval(top1.x-4,top1.y-4,8,8);
							g.fillOval(top2.x-4,top2.y-4,8,8);
							g.setColor(Color.green);
							g.fillOval(left1.x-4,left1.y-4,8,8);
							g.fillOval(left2.x-4,left2.y-4,8,8);
							g.setColor(Color.blue);
							g.fillOval(right1.x-4,right1.y-4,8,8);
							g.fillOval(right2.x-4,right2.y-4,8,8);


							g.setColor(Color.pink);
							g.fillOval(p.x-4,p.y-4,8,8);
							g.setColor(Color.orange);
							g.fillOval(nextP.x-4,nextP.y-4,8,8);
							g.setColor(new Color(0,0,0,255));
						 */




						Polygon shadow=new Polygon();
						shadow.addPoint(p.x,p.y);

						boolean top1Visible=detVert.x*(top1.y-topLeft.y)-detVert.y*(top1.x-topLeft.x)>0&&detVert.x*(top1.y-topRight.y)-detVert.y*(top1.x-topRight.x)<0;
						boolean top2Visible=detVert.x*(top2.y-topLeft.y)-detVert.y*(top2.x-topLeft.x)>0&&detVert.x*(top2.y-topRight.y)-detVert.y*(top2.x-topRight.x)<0;
						boolean left1Visible=detHor.x*(left1.y-playerLeft.y)-detHor.y*(left1.x-playerLeft.x)>0&&detHor.x*(left1.y-topLeft.y)-detHor.y*(left1.x-topLeft.x)<0;
						boolean left2Visible=detHor.x*(left2.y-playerLeft.y)-detHor.y*(left2.x-playerLeft.x)>0&&detHor.x*(left2.y-topLeft.y)-detHor.y*(left2.x-topLeft.x)<0;
						boolean right1Visible=detHor.x*(right1.y-playerLeft.y)-detHor.y*(right1.x-playerLeft.x)>0&&detHor.x*(right1.y-topLeft.y)-detHor.y*(right1.x-topLeft.x)<0;
						boolean right2Visible=detHor.x*(right2.y-playerLeft.y)-detHor.y*(right2.x-playerLeft.x)>0&&detHor.x*(right2.y-topLeft.y)-detHor.y*(right2.x-topLeft.x)<0;

						boolean pIsAtLeft=detVert.x*(p.y-player.y)-detVert.y*(p.x-player.x)<0;
						boolean nextPIsAtLeft=detVert.x*(nextP.y-player.y)-detVert.y*(nextP.x-player.x)<0;
						boolean playerIsAtLeft=(nextP.x-p.x)*(player.y-p.y)-(nextP.y-p.y)*(player.x-p.x)<0;
						boolean pIsBefore=detHor.x*(p.y-player.y)-detHor.y*(p.x-player.x)>0;
						boolean nextPIsBefore=detHor.x*(nextP.y-player.y)-detHor.y*(nextP.x-player.x)>0;



						if(pIsBefore){
							if(nextPIsBefore){
								if(top1Visible){
									if(top2Visible){
										shadow.addPoint(top1.x,top1.y);
										shadow.addPoint(top2.x,top2.y);
									}else{
										if(left2Visible){
											shadow.addPoint(top1.x,top1.y);
											shadow.addPoint(topLeft.x,topLeft.y);
											shadow.addPoint(left2.x,left2.y);
										}else{
											shadow.addPoint(top1.x,top1.y);
											shadow.addPoint(topRight.x,topRight.y);
											shadow.addPoint(right2.x,right2.y);
										}
									}
								}else{
									if(top2Visible){
										if(left1Visible){
											shadow.addPoint(left1.x,left1.y);
											shadow.addPoint(topLeft.x,topLeft.y);
											shadow.addPoint(top2.x,top2.y);
										}else{
											shadow.addPoint(right1.x,right1.y);
											shadow.addPoint(topRight.x,topRight.y);
											shadow.addPoint(top2.x,top2.y);
										}
									}else{	// top1 et top2 non-visibles
										if(left1Visible&&left2Visible){
											shadow.addPoint(left1.x,left1.y);
											shadow.addPoint(left2.x,left2.y);
										}else{
											if(right1Visible&&right2Visible){
												shadow.addPoint(right1.x,right1.y);
												shadow.addPoint(right2.x,right2.y);
											}else{
												if(left1Visible&&right2Visible){
													shadow.addPoint(left1.x,left1.y);
													shadow.addPoint(topLeft.x,topLeft.y);
													shadow.addPoint(topRight.x,topRight.y);
													shadow.addPoint(right2.x,right2.y);
												}else{
													if(left2Visible&&right1Visible){
														shadow.addPoint(right1.x,right1.y);
														shadow.addPoint(topRight.x,topRight.y);
														shadow.addPoint(topLeft.x,topLeft.y);
														shadow.addPoint(left2.x,left2.y);
													}
												}
											}
										}
									}
								}
							}else{	// nextP derrière le joueur
								if(playerIsAtLeft){
									Point proj;	// le plus proche entre left2 et bottom2
									if(Math.pow(left2.x-player.x,2)+Math.pow(left2.y-player.y,2)<Math.pow(bottom2.x-player.x,2)+Math.pow(bottom2.y-player.y,2)){
										proj=left2;
									}else{
										proj=bottom2;
									}
									if(left1Visible){
										shadow.addPoint(left1.x,left1.y);
										shadow.addPoint(bottomLeft.x,bottomLeft.y);
										shadow.addPoint(proj.x,proj.y);
									}else{
										if(top1Visible){
											shadow.addPoint(top1.x,top1.y);
											shadow.addPoint(topLeft.x,topLeft.y);
											shadow.addPoint(bottomLeft.x,bottomLeft.y);
											shadow.addPoint(proj.x,proj.y);
										}else{
											shadow.addPoint(right1.x,right1.y);
											shadow.addPoint(topRight.x,topRight.y);
											shadow.addPoint(topLeft.x,topLeft.y);
											shadow.addPoint(bottomLeft.x,bottomLeft.y);
											shadow.addPoint(proj.x,proj.y);
										}
									}
								}else{
									Point proj;	// le plus proche entre right2 et bottom2
									if(Math.pow(right2.x-player.x,2)+Math.pow(right2.y-player.y,2)<Math.pow(bottom2.x-player.x,2)+Math.pow(bottom2.y-player.y,2)){
										proj=right2;
									}else{
										proj=bottom2;
									}
									if(right1Visible){
										shadow.addPoint(right1.x,right1.y);
										shadow.addPoint(bottomRight.x,bottomRight.y);
										shadow.addPoint(proj.x,proj.y);
									}else{
										if(top1Visible){
											shadow.addPoint(top1.x,top1.y);
											shadow.addPoint(topRight.x,topRight.y);
											shadow.addPoint(bottomRight.x,bottomRight.y);
											shadow.addPoint(proj.x,proj.y);
										}else{
											shadow.addPoint(left1.x,left1.y);
											shadow.addPoint(topLeft.x,topLeft.y);
											shadow.addPoint(topRight.x,topRight.y);
											shadow.addPoint(bottomRight.x,bottomRight.y);
											shadow.addPoint(proj.x,proj.y);
										}
									}
								}
							}
						}else{	// p est derrière le joueur
							if(!playerIsAtLeft){
								Point proj;	// le plus proche entre left1 et bottom1
								if(Math.pow(left1.x-player.x,2)+Math.pow(left1.y-player.y,2)<Math.pow(bottom1.x-player.x,2)+Math.pow(bottom1.y-player.y,2)){
									proj=left1;
								}else{
									proj=bottom1;
								}
								if(left2Visible){
									shadow.addPoint(proj.x,proj.y);
									shadow.addPoint(bottomLeft.x,bottomLeft.y);
									shadow.addPoint(left2.x,left2.y);										
								}else{
									if(top2Visible){
										shadow.addPoint(proj.x,proj.y);
										shadow.addPoint(bottomLeft.x,bottomLeft.y);
										shadow.addPoint(topLeft.x,topLeft.y);
										shadow.addPoint(top2.x,top2.y);											
									}else{
										shadow.addPoint(proj.x,proj.y);
										shadow.addPoint(bottomLeft.x,bottomLeft.y);
										shadow.addPoint(topLeft.x,topLeft.y);
										shadow.addPoint(topRight.x,topRight.y);
										shadow.addPoint(right2.x,right2.y);											
									}
								}
							}else{
								Point proj;	// le plus proche entre right1 et bottom1
								if(Math.pow(right1.x-player.x,2)+Math.pow(right1.y-player.y,2)<Math.pow(bottom1.x-player.x,2)+Math.pow(bottom1.y-player.y,2)){
									proj=right1;
								}else{
									proj=bottom1;
								}
								if(right2Visible){
									shadow.addPoint(proj.x,proj.y);
									shadow.addPoint(bottomRight.x,bottomRight.y);
									shadow.addPoint(right2.x,right2.y);

								}else{
									if(top2Visible){
										shadow.addPoint(proj.x,proj.y);
										shadow.addPoint(bottomRight.x,bottomRight.y);
										shadow.addPoint(topRight.x,topRight.y);
										shadow.addPoint(top2.x,top2.y);
									}else{
										shadow.addPoint(proj.x,proj.y);
										shadow.addPoint(bottomRight.x,bottomRight.y);
										shadow.addPoint(topRight.x,topRight.y);
										shadow.addPoint(topLeft.x,topLeft.y);
										shadow.addPoint(left2.x,left2.y);										
									}
								}
							}
						}

						/*if(top1Visible&&top2Visible){
								if(pIsBefore){
									shadow.addPoint(top1.x,top1.y);
								}else{
									shadow.addPoint(bottom1.x,bottom1.y);
									if(nextPIsAtLeft){
										shadow.addPoint(bottomLeft.x,bottomLeft.y);
										shadow.addPoint(topLeft.x,topLeft.y);
									}else{
										shadow.addPoint(bottomRight.x,bottomRight.y);
										shadow.addPoint(topRight.x,topRight.y);
									}
								}
								if(nextPIsBefore){
									shadow.addPoint(top2.x,top2.y);
								}else{
									if(nextPIsAtLeft){
										shadow.addPoint(topLeft.x,topLeft.y);
										shadow.addPoint(bottomLeft.x,bottomLeft.y);
									}else{
										shadow.addPoint(topRight.x,topRight.y);
										shadow.addPoint(bottomRight.x,bottomRight.y);
									}
									shadow.addPoint(bottom2.x,bottom2.y);
								}
							}else{
								if(top1Visible){
									shadow.addPoint(top1.x,top1.y);
									if(nextPIsAtLeft){		
										shadow.addPoint(topLeft.x,topLeft.y);
										shadow.addPoint(left2.x,left2.y);
									}else{
										shadow.addPoint(topRight.x,topRight.y);
										shadow.addPoint(right2.x,right2.y);
									}
								}else{
									if(top2Visible){
										if(pIsAtLeft){
											shadow.addPoint(left1.x,left1.y);
											shadow.addPoint(topLeft.x,topLeft.y);
										}else{
											shadow.addPoint(right1.x,right1.y);
											shadow.addPoint(topRight.x,topRight.y);
										}
										shadow.addPoint(top2.x,top2.y);
									}else{
										/*if(left1Visible&&right2Visible&&detHor.x*(left1.y-player.y)-detHor.y*(left1.x-player.x)>0){	// et si left1 est devant le joueur
											shadow.addPoint(left1.x,left1.y);
											shadow.addPoint(topLeft.x,topLeft.y);
											shadow.addPoint(topRight.x,topRight.y);
											shadow.addPoint(right2.x,right2.y);
										}else{
											if(left2Visible&&right1Visible){													
												shadow.addPoint(right1.x,right1.y);
												shadow.addPoint(topRight.x,topRight.y);
												shadow.addPoint(topLeft.x,topLeft.y);
												shadow.addPoint(left2.x,left2.y);

											}
										}
										/*if((left1Visible||left2Visible)&&isAtLeft){
											shadow.addPoint(left1.x,left1.y);
											shadow.addPoint(left2.x,left2.y);
										}else{
											if(right1Visible||right2Visible){
												shadow.addPoint(right1.x,right1.y);
												shadow.addPoint(right2.x,right2.y);
											}
										}
									}
								}
							}*/

						/*if(detBefore.x*(p.y-player.y)-detBefore.y*(p.x-player.x)<0&&detBefore.x*(nextP.y-player.y)-detBefore.y*(nextP.x-player.x)<0&&
									detHor.x*(left1.y-topLeft.y)-detHor.y*(left1.x-topLeft.x)>0&&detHor.x*(left2.y-topLeft.y)-detHor.y*(left2.x-topLeft.x)>0){
								shadow.addPoint(left1.x,left1.y);
								shadow.addPoint(left2.x,left2.y);
							}else{
								if(detBefore.x*(p.y-player.y)-detBefore.y*(p.x-player.x)>0&&detBefore.x*(nextP.y-player.y)-detBefore.y*(nextP.x-player.x)>0&&
										detHor.x*(right1.y-topLeft.y)-detHor.y*(right1.x-topLeft.x)>0&&detHor.x*(right2.y-topLeft.y)-detHor.y*(right2.x-topLeft.x)>0){
									shadow.addPoint(right1.x,right1.y);
									shadow.addPoint(right2.x,right2.y);
								}else{
									if(detTop.x*(left1.y-topLeft.y)-detTop.y*(left1.x-topLeft.x)>0&&detTop.x*(left1.y-bottomLeft.y)-detTop.y*(left1.x-bottomLeft.x)<0&&
											detTop.x*(right2.y-topLeft.y)-detTop.y*(right2.x-topLeft.x)>0&&detTop.x*(right2.y-bottomLeft.y)-detTop.y*(right2.x-bottomLeft.x)<0){
										shadow.addPoint(left1.x,left1.y);
										shadow.addPoint(topLeft.x,topLeft.y);
										shadow.addPoint(topRight.x,topRight.y);
										shadow.addPoint(right2.x,right2.y);
									}else{
										if(detTop.x*(left2.y-topLeft.y)-detTop.y*(left2.x-topLeft.x)>0&&detTop.x*(left2.y-bottomLeft.y)-detTop.y*(left2.x-bottomLeft.x)<0&&
												detTop.x*(right1.y-topLeft.y)-detTop.y*(right1.x-topLeft.x)>0&&detTop.x*(right1.y-bottomLeft.y)-detTop.y*(right1.x-bottomLeft.x)<0){
											shadow.addPoint(right1.x,right1.y);
											shadow.addPoint(topRight.x,topRight.y);
											shadow.addPoint(topLeft.x,topLeft.y);
											shadow.addPoint(left2.x,left2.y);
										}
									}
								}
							}*/

						shadow.addPoint(nextP.x,nextP.y);
						g.fill(shadow);

						/*if(detBefore.x*(p.y-player.y)-detBefore.y*(p.x-player.x)<=0&&detBefore.x*(nextP.y-player.y)-detBefore.y*(nextP.x-player.x)<=0)
								System.out.println("left");
								pA=left1;
								pB=left2;
							}else{
								if(detBefore.x*(p.y-player.y)-detBefore.y*(p.x-player.x)>=0&&detBefore.x*(nextP.y-player.y)-detBefore.y*(nextP.x-player.x)>=0){
									System.out.println("right");
									pA=right1;
									pB=right2;
								}else{
									System.out.println("top");
										pA=top1;
										pB=top2;
								}
							}*/
						/*Polygon shadow=new Polygon();
								shadow.addPoint(p.x,p.y);
								Point trans=distDir(d,pointDirection(player.x,player.y,p.x,p.y));
								shadow.addPoint(p.x+trans.x,p.y+trans.y);
								trans=distDir(1000,pointDirection(player.x,player.y,nextP.x,nextP.y));
								shadow.addPoint(nextP.x+trans.x,nextP.y+trans.y);
								shadow.addPoint(nextP.x,nextP.y);
								g.fill(shadow);*/
					}
				}
			}
		}
		Polygon shadow=new Polygon();
		shadow.addPoint((int)player.x+detHor.x,(int)player.y+detHor.y);
		shadow.addPoint((int)player.x-detHor.x,(int)player.y-detHor.y);
		Point p=distDir(Math.sqrt(W*H),player.angle+180);
		shadow.addPoint((int)player.x-detHor.x+p.x,(int)player.y-detHor.y+p.y);
		shadow.addPoint((int)player.x+detHor.x+p.x,(int)player.y+detHor.y+p.y);
		g.fill(shadow);
	}
	
	private void loadMap(boolean sameMap){	// s'il on recharge le meme niveau, pas besoin de recharger le background par exemple
		try{
			idLoading=0;
			percentLoading=-1;
			objs.removeAllElements();
			alarms.clear();
			alphaAlarm=0;
			impacts.removeAllElements();
			String line;
			
			minimap=ImageIO.read(new File("data/map/"+map+".png"));
			BufferedReader buff=new BufferedReader(new FileReader("data/map/"+map+".doors"));
			buff.mark(100000);
			int nbLines=0;
			while((line=buff.readLine())!=null) nbLines++;
			buff.reset();
			int idLine=0;
			while((line=buff.readLine())!=null){
				String[] params=line.split(";");
				Door door=new Door(Integer.parseInt(params[0]),new Point(Integer.parseInt(params[2]),Integer.parseInt(params[3])),
						new Point(Integer.parseInt(params[4]),Integer.parseInt(params[5])),Integer.parseInt(params[1]));
				if(params.length>6) addAlarms(door,params[6]);
				idLine++;
				percentLoading=(int)(idLine*100.0/nbLines);
			}
			buff.close();
			
			idLoading++;
			percentLoading=-1;
			buff=new BufferedReader(new FileReader("data/map/"+map+".walls"));
			buff.mark(100000);
			nbLines=0;
			while((line=buff.readLine())!=null) nbLines++;
			buff.reset();
			idLine=0;
			while((line=buff.readLine())!=null){
				String[] infos=line.split("#");
				String[] params=infos[0].split(";");
				
				Vector<Point> vec=new Vector<Point>();
				String[] points=infos[1].split("/");
				for(int i=0;i<points.length;i++){
					String[] coor=points[i].split(";");
					vec.add(new Point(Integer.parseInt(coor[0]),Integer.parseInt(coor[1])));
				}
				new Wall(Boolean.parseBoolean(params[0]),Integer.parseInt(params[1]),Boolean.parseBoolean(params[2]),Boolean.parseBoolean(params[3]),
						Boolean.parseBoolean(params[4]),vec);
				idLine++;
				percentLoading=(int)(idLine*100.0/nbLines);
			}
			buff.close();
			
			buff=new BufferedReader(new FileReader("data/map/"+map+".map"));
			
			
			line=buff.readLine();
			String[] data=line.split(";");
			mapBounds=new Rectangle(Integer.parseInt(data[0]),Integer.parseInt(data[1]),Integer.parseInt(data[2])-Integer.parseInt(data[0]),Integer.parseInt(data[3])-Integer.parseInt(data[1]));
			startPoint=new Point(Integer.parseInt(data[4]),Integer.parseInt(data[5]));
			
			idLoading++;
			percentLoading=-1;
			if(showPatterns&&!sameMap){
				Vector<String> lines=new Vector<String>();
				buff=new BufferedReader(new FileReader("data/map/"+map+".patterns"));
				while((line=buff.readLine())!=null) lines.add(line);
				buff.close();
				background=new BufferedImage(mapBounds.width,mapBounds.height,BufferedImage.TYPE_INT_RGB);
				Graphics2D g=(Graphics2D)background.getGraphics();
				g.setColor(Color.black);
				g.fillRect(0,0,background.getWidth(),background.getHeight());

				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);

				for(int i=0;i<lines.size();i++){
					String[] infos=lines.get(i).split("#");
					String[] params=infos[0].split(";");
					int nbControlPoints=Integer.parseInt(params[0]);
					Path2D.Double pattern=new Path2D.Double();
					String[] pts=infos[1].split("/");

					Vector<Point> points=new Vector<Point>();
					for(int m=0;m<pts.length;m++){
						String[] coor=pts[m].split(";");
						Point p=new Point(Integer.parseInt(coor[0])-mapBounds.x,Integer.parseInt(coor[1])-mapBounds.y);
						points.add(p);

					}
					for(int j=0;j<points.size();j++){
						Point p=points.get(j);
						if(j>0)
							if(nbControlPoints==0){
								pattern.lineTo(p.x,p.y);
							}else{
								if(nbControlPoints==1){
									if(j%2==0){
										Point p1=points.get(j-1);
										pattern.quadTo(p1.x,p1.y,p.x,p.y);
									}
								}else{
									if(j%3==0){	
										Point p1=points.get(j-1);
										Point p2=points.get(j-2);
										pattern.curveTo(p2.x,p2.y,p1.x,p1.y,p.x,p.y);
									}
								}
							}
						else{
							pattern.moveTo(p.x,p.y);
						}
					}
					if(Boolean.parseBoolean(params[3])) pattern.closePath();
					g.setStroke(new BasicStroke(Integer.parseInt(params[2]),BasicStroke.JOIN_MITER,BasicStroke.CAP_BUTT));
					g.setColor(new Color(Integer.parseInt(params[4]),Integer.parseInt(params[5]),Integer.parseInt(params[6])));
					if(Boolean.parseBoolean(params[1])) g.fill(pattern); else g.draw(pattern);
					percentLoading=i*100/lines.size();
				}
				g.dispose();
			}
			
			idLoading++;
			percentLoading=-1;
			if(!sameMap){
				buff=new BufferedReader(new FileReader("data/map/"+map+".path"));
				line=buff.readLine();
				if(line!=null&&!line.equals("")){
					data=line.split(";");
					int[] aps=new int[data.length];
					for(int i=0;i<data.length;i++){
						aps[i]=Integer.parseInt(data[i]);
						percentLoading=(int)(i*50.0/data.length);
					}
					line=buff.readLine();
					data=line.split(";");
					int[] fs=new int[data.length];
					for(int i=0;i<data.length;i++){
						fs[i]=Integer.parseInt(data[i]);
					}
					line=buff.readLine();
					if(line!=null) data=line.split("/"); else data=new String[0];
					Point[] points=new Point[data.length];
					if(line!=null){
						for(int i=0;i<data.length;i++){
							String[] infos=data[i].split(";");
							points[i]=new Point(Integer.parseInt(infos[0]),Integer.parseInt(infos[1]));
							percentLoading=(int)(50+i*50.0/data.length);
						}
					}
					pathManager=new PathManager(aps,fs,points);
				}
				buff.close();
			}
			
			idLoading++;
			percentLoading=-1;
			buff=new BufferedReader(new FileReader("data/map/"+map+".bots"));
			buff.mark(100000);
			nbLines=0;
			while((line=buff.readLine())!=null) nbLines++;
			buff.reset();
			idLine=0;
			while((line=buff.readLine())!=null){
				String[] infos=line.split("#");
				String[] params=infos[0].split(";");
				
				String[] points=infos[1].split(";");
				int[] path=new int[points.length];
				for(int i=0;i<points.length;i++){
					path[i]=Integer.parseInt(points[i]);
				}
				Enemy bot=new Enemy();
				bot.setLeftWeapon(Integer.parseInt(params[0]));
				bot.setRightWeapon(Integer.parseInt(params[1]));
				bot.setLife(Integer.parseInt(params[2]));
				bot.setDistMax(Integer.parseInt(params[3]));
				bot.setWhenCallHelp(Integer.parseInt(params[4]));
				bot.setIsPathClosed(Boolean.parseBoolean(params[5]));
				bot.setPath(path);
				bot.setDefaultPath(path,Boolean.parseBoolean(params[5]));
				Point pos=PathManager.getPoint(path[0]);
				bot.x=pos.x;
				bot.y=pos.y;
				if(path.length>1){
					bot.angle=bot.pointDirection(pos,PathManager.getPoint(path[1]));
					if(bot.leftWeapon!=null) bot.leftWeapon.angle=bot.angle+80;
					if(bot.rightWeapon!=null) bot.rightWeapon.angle=bot.angle+100;
				}
				if(params.length>6) addAlarms(bot,params[6]);
				idLine++;
				percentLoading=(int)(idLine*100.0/nbLines);
			}
			buff.close();
			
			idLoading++;
			percentLoading=-1;
			buff=new BufferedReader(new FileReader("data/map/"+map+".captors"));
			buff.mark(100000);
			nbLines=0;
			while((line=buff.readLine())!=null) nbLines++;
			buff.reset();
			idLine=0;
			while((line=buff.readLine())!=null){
				String[] params=line.split(";");
				Captor captor=new Captor(Integer.parseInt(params[0]),Integer.parseInt(params[1]),Boolean.parseBoolean(params[2]),Double.parseDouble(params[3]),Integer.parseInt(params[4]),
						Integer.parseInt(params[5]),Integer.parseInt(params[6]),Integer.parseInt(params[6])+Integer.parseInt(params[7]),Integer.parseInt(params[8]));
				if(params.length>9) addAlarms(captor,params[9]);
				idLine++;
				percentLoading=(int)(idLine*100.0/nbLines);
			}
			buff.close();
			
			idLoading++;
			percentLoading=-1;
			buff=new BufferedReader(new InputStreamReader(new FileInputStream(new File("data/map/"+map+".goals")),"ISO-8859-1"));
			Goal.removeAllGoals();
			buff.mark(100000);
			nbLines=0;
			while((line=buff.readLine())!=null) nbLines++;
			buff.reset();
			idLine=0;
			while((line=buff.readLine())!=null){
				String[] params=line.split(";");
				Goal g=new Goal(Integer.parseInt(params[0]),Integer.parseInt(params[1]),Integer.parseInt(params[2])*30,(params.length>3)?params[3].replace("<br/>","\n"):"");
				idLine++;
				percentLoading=(int)(idLine*100.0/nbLines);
			}
			buff.close();
			
			
		}catch(IOException e1) {e1.printStackTrace();}
	}
	
	public static void setPercentLoading(int p){
		percentLoading=p;
	}
	
	public static boolean isVisible(Obj obj){
		Area view=new Area(new Rectangle2D.Double(topLeft.x,topLeft.y,bottomRight.x-topLeft.x,bottomRight.y-topLeft.y));
		for(int i=0;i<getObjsNumber();i++){
			Area area=new Area(getObj(i).getMask());
			area.intersect(view);
			if(!area.isEmpty()) return true;
		}
		return false;
	}
	
	/**
	 * Prend en argument la liste des alarmes non-parsée.
	 * Indique aux objets qui en ont besoin les alarmes auxquelles ils sont connectés (pas les portes par exemple mais pour les robots ou les capteurs)
	 */
	private void addAlarms(Obj obj, String str){
		Vector<Integer> listAlarms=new Vector<Integer>();
		String lastNb="";
		for(int i=0;i<str.length();i++){
			char c=str.charAt(i);
			if((int)c>=48&&(int)c<=57){
				lastNb+=c;
			}
			if(c==','||i==str.length()-1){
				listAlarms.add(Integer.parseInt(lastNb));
				lastNb="";
			}
		}
		for(int i=0;i<listAlarms.size();i++){
			int id=listAlarms.get(i);
			if(alarms.containsKey(id)){
				alarms.get(id).add(obj);
			}else{
				Vector<Obj> vect=new Vector<Obj>();
				vect.add(obj);
				alarms.put(id,vect);
			}
			if(obj instanceof Enemy){
				((Enemy)obj).addAlarm(id);
			}
			if(obj instanceof Captor){
				((Captor)obj).addAlarm(id);
			}
			// NE PAS OUBLIER D'AJOUTER AUSSI OBJET DANS triggerAlarm()
		}
	}
	
	public static void triggerAlarms(Vector<Integer> ids, Obj by){
		for(int i=0;i<ids.size();i++){
			triggerAlarm(ids.get(i),by);
		}
	}
	
	public static void triggerAlarm(int id, Obj by){	// l'objet qui déclenche l'alarme (ennemi, capteur)
		Vector<Obj> objs=alarms.get(id);
		for(int i=0;i<objs.size();i++){
			Obj obj=objs.get(i);
			if(obj instanceof Enemy){
				((Enemy)obj).triggerAlarm(id,by);
			}
			if(obj instanceof Captor){
				((Captor)obj).triggerAlarm(id,by);
			}
			if(obj instanceof Door){
				((Door)obj).triggerAlarm(id,by);
			}
		}
	}
	
	public static void triggerAlarmsOf(Vector<Enemy> objs, Obj by){	// l'objet qui déclenche l'alarme (ennemi, capteur)
		for(int i=0;i<objs.size();i++){
			Obj obj=objs.get(i);
			((Enemy)obj).triggerAlarm(-1,by);
		}
	}
	
	private Point distDir(double dist, double dir){
		dir=Math.toRadians(360-dir);
		int x=(int)(dist*Math.cos(dir));
		int y=(int)(dist*Math.sin(dir));
		return new Point(x,y);
	}
	
	private double pointDistance(double x1, double y1, double x2, double y2){
		return Math.sqrt(Math.pow(Math.abs(x1-x2),2)+Math.pow(Math.abs(y1-y2),2));
	}
	
	private double pointDirection(double x1, double y1, double x2, double y2){
		double d=Math.toDegrees(Math.asin(Math.abs(y1-y2)/pointDistance(x1,y1,x2,y2)));
		if(x2>x1){
			if(y2>y1) d=360-d;
		}else{
			if(y2>y1) d+=180; else d=180-d;
		}
		return d%360;
	}
	
	private boolean checkCollisionLine(double x1, double y1, double x2, double y2){
		Rectangle2D.Double rect=new Rectangle2D.Double(x1,y1,pointDistance(x1,y1,x2,y2),1);
		Area line=new Area(rect);
		double dir=pointDirection(x1,y1,x2,y2);
		line.transform(AffineTransform.getRotateInstance(Math.toRadians(360-dir),x1,y1));
		for(int i=0;i<getObjsNumber();i++){
			Area area=new Area(getObj(i).getMask());
			area.intersect(line);
			if(!area.isEmpty()) return true;
		}
		return false;
	}
	
	private boolean checkCollisionLine(Point a, Point b){
		return checkCollisionLine(a.x,a.y,b.x,b.y);
	}
	
	public static void exit(){
		if(server!=null) server.exit();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		window=new JFrame();
		window.setSize(1024,768);
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				Screen.exit();
			}
		});
		window.setUndecorated(true);
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = environment.getDefaultScreenDevice();
		device.setFullScreenWindow(window);
		window.setContentPane(new Screen());
		window.addMouseListener(screen);
		window.addMouseMotionListener(screen);
		window.addKeyListener(screen);
		window.setVisible(true);
	}

	public static void addObj(Obj obj){
		objs.add(obj);
	}
	
	public static void removeObj(Obj obj){
		objs.remove(obj);
	}
	
	public static Obj getObj(int i){
		return objs.get(i);
	}
	public static int getObjsNumber(){
		return objs.size();
	}
	
	public static Obj getObjById(int id){
		for(int i=0;i<objs.size();i++){
			Obj obj=objs.get(i);
			if(obj.id==id) return obj;
		}
		return null;
	}
	
	public static Vector<Obj> getObjsOfClass(String classe){
		Vector<Obj> all=new Vector<Obj>();
		for(int i=0;i<objs.size();i++){
			Obj obj=objs.get(i);
			if(obj.getClass().getName().equals(classe)) all.add(obj);
		}
		return all;
	}
	
	/**
	 * retourne une valeur entre 0 et valMax de facon quadratique en fonction de pos sur etendu
	 * ex : getProduitCroixCarre(25,100,100)=6.25
	 */
	public static double getProduitCroixCarre(int pos, int valMax, int etendu){
		return Math.pow(pos,2)*valMax/Math.pow(etendu,2);
	}
	
	public static void addImpact(Point p){
		impacts.add(p);
	}
	
	public static void removeImpact(int i){
		impacts.remove(i);
	}
	
	public static Point getImpact(int i){
		return impacts.get(i);
	}
	
	public static int getImpactsNumber(){
		return impacts.size();
	}
	/*public static boolean isPlayerSpotted(){
		return playerSpottedBy.size()>0;
	}
	public static void playerSpotted(Obj by, boolean isSpotted){
		if(isSpotted){
			if(!playerSpottedBy.contains(by)) playerSpottedBy.add(by);
		}else{
			playerSpottedBy.remove(by);
		}
	}*/
	
	public void setPause(boolean pause){
		if(pause){
			pauseBackground=player.getScreenshot();
			mousePositionPause=new Point(player.mouseScreenX-player.staticMouseX+W/2,player.mouseScreenY);
			menu=MENU_PAUSE;
		}else{
			player.mouseRobot.mouseMove(mousePositionPause.x,mousePositionPause.y);
			menu=MENU_NO;
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if(menu==MENU_MAIN){
			exitMainMenu=T;
		}
		if(menu==MENU_LOGO){
			T=0;
			menu=MENU_MAIN;
		}
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
			if(menu==MENU_CHOOSE_MAP){
				menu=MENU_MAIN;
			}
			if(menu==MENU_NO){
				setPause(true);
			}else{
				if(menu==MENU_PAUSE){
					setPause(false);
				}
			}
			if(menu==MENU_SETTINGS){
				menu=MENU_PAUSE;
			}
		}else{
			if(menu==MENU_SETTINGS){
				if(KEY_MOVE_UP==-1) KEY_MOVE_UP=e.getKeyCode();
				if(KEY_MOVE_DOWN==-1) KEY_MOVE_DOWN=e.getKeyCode();
				if(KEY_MOVE_LEFT==-1) KEY_MOVE_LEFT=e.getKeyCode();
				if(KEY_MOVE_RIGHT==-1) KEY_MOVE_RIGHT=e.getKeyCode();
				if(KEY_TURBO==-1) KEY_TURBO=e.getKeyCode();
				if(KEY_SHOW_MAP==-1) KEY_SHOW_MAP=e.getKeyCode();
				if(KEY_SCREENSHOT==-1) KEY_SCREENSHOT=e.getKeyCode();
			}
		}
		if(menu==MENU_NO){
			int key=e.getKeyCode();
			player.moveRight|=key==KEY_MOVE_RIGHT;
			player.moveUp|=key==KEY_MOVE_UP;
			player.moveLeft|=key==KEY_MOVE_LEFT;
			player.moveDown|=key==KEY_MOVE_DOWN;
			if(key==KEY_TURBO&&!player.stabilized){
				player.turbo=true;
				player.maxSpeed=player.defaultMaxSpeed*1.5;
			}
			if(key==KEY_SHOW_MAP){
				player.showMap=!player.showMap;
			}
			if(key==KEY_SCREENSHOT){
				BufferedImage img=player.getScreenshot();
				try {
					Date d=new Date();
					ImageIO.write(img,"png",new File("Blue Sunshine "+d.getDate()+"-"+d.getMonth()+"-"+(d.getYear()-100)+"   -   "+d.getHours()+"-"+d.getMinutes()+"-"+d.getSeconds()+".png"));
				} catch (IOException e1) {e1.printStackTrace();}
			}
			if(e.isAltDown()){
				if(key==KeyEvent.VK_1){
					playerInvisible=!playerInvisible;
				}
				if(key==KeyEvent.VK_2){
					showEnemiesPaths=!showEnemiesPaths;
				}
				if(key==KeyEvent.VK_3){
					showAlwaysEnemies=!showAlwaysEnemies;
				}
				if(key==KeyEvent.VK_4){
					showPathGraph=!showPathGraph;
				}
				if(key==KeyEvent.VK_0){
					showDebug=!showDebug;
				}
				if(key==KeyEvent.VK_5){
					playerInvincible=!playerInvincible;
				}
			}
		}
	}
	public void keyReleased(KeyEvent e) {
		if(menu==MENU_NO){
			int key=e.getKeyCode();
			player.moveRight&=key!=KEY_MOVE_RIGHT;
			player.moveUp&=key!=KEY_MOVE_UP;
			player.moveLeft&=key!=KEY_MOVE_LEFT;
			player.moveDown&=key!=KEY_MOVE_DOWN;
			if(key==KEY_TURBO) player.turbo=false;
		}
	}
	public void keyTyped(KeyEvent e) {
		if(menu==MENU_LOSE_DEAD||menu==MENU_LOSE_TIME){
			initGame(true);
		}
		if(menu==MENU_WIN){
			menu=MENU_MAIN;
		}
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		mousePressed=true;
		mouseLeft=SwingUtilities.isLeftMouseButton(e);
		if(menu==MENU_NO){
			if(SwingUtilities.isMiddleMouseButton(e)){
				player.stabilized=true;
				player.maxSpeed=8;
			}
			if(SwingUtilities.isLeftMouseButton(e)){
				player.leftWeapon.mousePressed=true;		
			}
			if(SwingUtilities.isRightMouseButton(e)){
				player.rightWeapon.mousePressed=true;
			}
		}
	}
	public void mouseReleased(MouseEvent e) {
		if(menu==MENU_NO){
			if(SwingUtilities.isMiddleMouseButton(e)){
				player.stabilized=false;
				player.maxSpeed=12;
				if(player.timeStabilized<5&&player.allC4.size()>0){
					player.allC4.get(0).explode();
					player.allC4.remove(0);
				}
			}
			if(SwingUtilities.isLeftMouseButton(e)){
				player.leftWeapon.mousePressed=false;		
			}
			if(SwingUtilities.isRightMouseButton(e)){
				player.rightWeapon.mousePressed=false;
			}
		}
	}
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
	public void mouseMoved(MouseEvent e) {
		staticMouseX=e.getX();
		staticMouseY=e.getY();
		
		if(player!=null){
			player.mouseScreenX=e.getXOnScreen();
			player.mouseScreenY=e.getYOnScreen();
			player.staticMouseX=e.getX();
		}
	}
	
	
}
