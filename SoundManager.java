import java.io.File;
import java.util.Vector;

import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public class SoundManager extends Thread{

	private static Vector<Sound> sounds=new Vector<Sound>();	
	private static int soundQuality=2;
	
	public static int MONO=0;
	public static int STEREO=1;
	public static int STEREO_SUP=2;
	

	public SoundManager(){
		start();
		changebackgroundMusic();
	}

	public void run(){
		try{
			while(true){
				for(int i=0;i<sounds.size();i++){
					Sound sound=sounds.get(i);
					byte bytes[]=new byte[1024];
					int bytesRead=0;
					if((bytesRead=sound.getInputStream().read(bytes,0,bytes.length))!=-1){
						SourceDataLine line=sound.getLine();
						((FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN)).setValue(sound.getRealVolume());
						line.write(bytes,0,bytesRead);
					}else{
						sound.getLine().stop();
						sound.getLine().close();
						sounds.remove(i);
						if(sound.isBackgroundMusic()){
							changebackgroundMusic();
						}
					}
				}
			}
		}catch (Exception e) {e.printStackTrace();}
	}
	
	public void changebackgroundMusic(){
		if(Screen.playMusic){
			File[] musics=new File("data/music/").listFiles();
			new Sound(null,musics[(int)Math.floor(Math.random()*musics.length)],false);
		}
	}
	
	public static void addSound(Sound sound){
		sounds.add(sound);
	}
	
	public static int getSoundQuality(){
		return soundQuality;
	}
	
	public static void setSoundQuality(int qual){
		soundQuality=qual;
	}
	
}