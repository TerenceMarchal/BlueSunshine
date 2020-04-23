import java.awt.Point;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public class Sound{

	// facteur de multiplication pour les volumes (via paramètres), entre 0 et 1 
	public static float GLOBAL_VOLUME_MUSIC=0.5f;
	public static float GLOBAL_VOLUME_SOUND=1f;
	
	private Player pl=Screen.player;	// pour avoir accès aux fonctions de bases (distDir,etc...)
	
	private Obj source;	// si null, c'est une musique de fond
	public double x,y;
	private File file;
	private AudioInputStream inputStream;
	private SourceDataLine line;
	private float volume=GLOBAL_VOLUME_MUSIC;	// entre 0 et 1
	private float realVolume;	// utilisé par le line
	private float pan=0;	// entre -1 et 1 (gauche ou droite)
	private double dir;	// direction du joueur vers la source
	
	public Sound(Obj source, File file, boolean rightSound){	// pour le stéréo supérieur, permet d'utiliser deux sons pour les deux côtés
		if(source!=null){
			this.source=source;
			this.x=source.x;
			this.y=source.y;
		}
		this.file=file;
		try{
			inputStream=AudioSystem.getAudioInputStream(file);
	
			AudioFormat audioFormat=inputStream.getFormat();
			DataLine.Info info=new DataLine.Info(SourceDataLine.class,audioFormat);
	
			line=(SourceDataLine)AudioSystem.getLine(info);
			line.open(audioFormat);
			line.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(source!=null){
			double dist=pl.pointDistance(pl.x,pl.y,x,y);
			volume=(float) (1-dist*1/2000f);
			CollisionEvent coll=pl.checkCollisionLine(source.pos(),pl.pos(),false,new Obj[]{source,pl,pl.getLeftWeapon(),pl.getRightWeapon()});
			if(coll.isCollision()){
				if(coll.getObj() instanceof Wall){
					volume*=1-(((Wall)coll.getObj()).getThickness()/75.0);
				}
				if(coll.getObj() instanceof Door){
					volume*=1-(((Door)coll.getObj()).getThickness()/75.0);
				}
			}
			if(source instanceof Bullet){
				int type=Integer.parseInt(file.getParentFile().getName());
				if(type!=Weapon.SILENCER_GUN&&type!=Weapon.TASER&&type!=Weapon.DAZER){
					for(int i=0;i<Enemy.allEnemies.size();i++){
						Enemy en=Enemy.allEnemies.get(i);
						if(Math.pow(en.x-source.x,2)+Math.pow(en.y-source.y,2)<Math.pow(700,2)){
							en.hearShot(source.pos());
						}
					}
				}
			}
			realVolume=calculateVolume((float)(volume)*GLOBAL_VOLUME_SOUND);
			((FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN)).setValue(realVolume);
			
			if(SoundManager.getSoundQuality()!=SoundManager.MONO){
				if(SoundManager.getSoundQuality()==SoundManager.STEREO){
					dir=pl.pointDirection(pl.x,pl.y,x,y);
					Point vecPlayer=pl.distDir(1000,pl.angle);
					Point vecSource=pl.distDir(1000,dir);
					double d=vecPlayer.x*vecSource.y-vecPlayer.y*vecSource.x;
					if(d==0){
						pan=0;
					}else{
						double diff=pl.getDiffAngle(pl.angle,dir);
						if(diff>90) diff=180-diff;
						pan=(float)(diff*1/90f);
						if(d<0) pan=-pan;
					}
					((FloatControl)line.getControl(FloatControl.Type.PAN)).setValue(pan);
				}else{
					Point p=null;
					if(rightSound) p=pl.distDir(75/2,(int)pl.angle+90); else p=pl.distDir(75/2,(int)pl.angle-90);
					dir=pl.pointDirection(pl.x+p.x,pl.y+p.y,x,y);
					Point vecPlayer=pl.distDir(1000,pl.angle);
					Point vecSource=pl.distDir(1000,dir);
					double d=vecPlayer.x*vecSource.y-vecPlayer.y*vecSource.x;
					if(d==0){
						pan=0;
					}else{
						double diff=pl.getDiffAngle(pl.angle,dir);
						if(diff>90) diff=180-diff;
						pan=(float)(diff*1/90f);
						if(d<0) pan=-pan;
					}
					((FloatControl)line.getControl(FloatControl.Type.PAN)).setValue(pan);
					if(!rightSound){
						new Sound(source,file,true);
					}
				}
			}
			if(dist<2000) SoundManager.addSound(this);
		}else{
			SoundManager.addSound(this);
		}
		
	}
	
	public float calculateVolume(double vol){
		return (float)(Math.log(vol)/Math.log(10.0)*20.0);
	}
	
	public boolean isBackgroundMusic(){
		return source==null&&this!=Screen.alarmSound;
	}
	
	public float getPan(){
		return pan;
	}
	
	public float getVolume(){
		return volume;
	}
	
	public File getFile(){
		return file;
	}
	
	public float getRealVolume(){
		return realVolume;
	}
	
	public void setVolume(float vol){
		volume=vol;
		realVolume=calculateVolume((float)(volume)*GLOBAL_VOLUME_SOUND);
	}
	
	public String getTitle(){
		if(isBackgroundMusic()){
			return file.getName().split("°")[0];
		}
		return null;
	}
	
	public String getArtist(){
		if(isBackgroundMusic()){
			String str=file.getName().split("°")[0];
			return str.substring(0,str.length()-4);
		}
		return null;
	}
	
	public AudioInputStream getInputStream(){
		return inputStream;
	}
	
	public SourceDataLine getLine(){
		return line;
	}
}
