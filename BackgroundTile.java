import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;


public class BackgroundTile {

	public static int tileSize=2000;//(int)Screen.screen.getToolkit().getScreenSize().getWidth();
	private static int W=Screen.screen.getToolkit().getScreenSize().width;
	private static int H=Screen.screen.getToolkit().getScreenSize().height;
	
	private BufferedImage img=null;
	public int x,y;
	
	private static Vector<BackgroundTile> tiles=new Vector<BackgroundTile>();
	private static int nbTilesDrawed;
	
	public static void initEngine(){
		Rectangle map=Screen.screen.mapBounds;
		
		for(int i=map.x;i<map.width;i+=tileSize){
			for(int j=map.y;j<map.height;j+=tileSize){
				tiles.add(new BackgroundTile(i,j));
			}
		}
	}
	
	public BackgroundTile(int x, int y){
		this.x=x;
		this.y=y;
	}
	
	public static void drawAll(Graphics2D g){		
		Player pl=Screen.player;
		Area map=new Area(new Rectangle(W,H));
		map.transform(AffineTransform.getRotateInstance(-Math.toRadians(pl.angle+90),W/2,50));
		map.transform(AffineTransform.getTranslateInstance(pl.x-W/2,pl.y-50));
		
		nbTilesDrawed=0;
		
		for(int i=0;i<tiles.size();i++){
			BackgroundTile tile=tiles.get(i);
			Rectangle rect=new Rectangle(tile.x,tile.y,tileSize,tileSize);
			if(map.intersects(rect)){
				if(tile.img==null){
					try {
						Image img = ImageIO.read(new File("data/backgrounds/"+(tile.x/tileSize)+"_"+(tile.y/tileSize)+".png"));
						tile.img=new BufferedImage(tileSize,tileSize,BufferedImage.TYPE_INT_ARGB);
						Graphics G=tile.img.getGraphics();
						G.drawImage(img,0,0,null);
						G.dispose();
					} catch (IOException e) {e.printStackTrace();}
					
				}
				g.drawImage(tile.img,tile.x,tile.y,null);
				nbTilesDrawed++;
			}else{
				tile.img=null;
			}
		}
	}
	
	public static int getPercentageDrawed(){
		return (int)Math.round(nbTilesDrawed*100.0/tiles.size());
	}
	
	public static int getMemoryUsed(){	// en bits
		return nbTilesDrawed*tileSize*tileSize*24;
	}
	
	/*public static int TOP_LEFT=0;
	public static int TOP_RIGHT=1;
	public static int BOTTOM_LEFT=2;
	public static int BOTTOM_RIGHT=3;
	
	public static int tileSize=(int)Screen.screen.getToolkit().getScreenSize().getWidth();
	
	private int id=0;	// une des 4 constantes ci-dessus
	private Point pos=new Point(0,0);	// en multiples de largeur d'écran
	private Image tile, tempTile;
	private MediaTracker mt=new MediaTracker(Screen.screen);
	
	private static int W,H; 
	
	public BackgroundTile(int id){
		this.id=id;
		switch(id){
			case 1:pos.x++;break;
			case 2:pos.y++;break;
			case 3:pos.x++;pos.y++;break;
		}
		loadAdjacentTile(0,0);
		if(id==0){
			W=(int)Math.ceil(Screen.mapBounds.width/tileSize);
			H=(int)Math.ceil(Screen.mapBounds.height/tileSize);
		}
	}
	
	public void draw(Graphics2D g){
		if(id==TOP_LEFT){
			Player pl=Screen.player;
			int X=(int)Math.round(pl.x/tileSize)-1;
			int Y=(int)Math.round(pl.y/tileSize)-1;
			X=Math.max(0,Math.min(W-1,X));
			Y=Math.max(0,Math.min(H-1,Y));
			System.out.println(X+"/"+Y);
			
			if(X!=pos.x||Y!=pos.y){
				int diffX=X-pos.x;
				int diffY=Y-pos.y;
				for(int i=0;i<4;i++){
					Screen.screen.backgroundTiles[i].loadAdjacentTile(diffX,diffY);
				}
			}
		}
		//System.out.println(id+"\t"+pos);
		if(mt.checkAll(true)){
			System.out.println("OK");
			tile=tempTile;
			mt.removeImage(tempTile);
			tempTile.flush();
		}
		g.drawImage(tile,pos.x*tileSize,pos.y*tileSize,null);
	}
	
	protected void loadAdjacentTile(int x, int y){
		pos.translate(x,y);
		tempTile=Toolkit.getDefaultToolkit().getImage("data/backgrounds/"+pos.x+"_"+pos.y+".jpg");	// l'image est chargée en arrière plan avec Toolkit
		mt.addImage(tempTile,0);
	}*/
	
	
}
