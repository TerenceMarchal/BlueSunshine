import java.util.Vector;


public class LoadingPlanet {	// plan�tes de l'animation de chargement

	public LoadingPlanet orbit;	// cette plan�te tourne autour de orbit
	public double dist, speed;	// et est � dist pixels de orbit et tourne a speed degr�s/step
	public double angle=Math.random()*360;
	
	public static Vector<LoadingPlanet> planets=new Vector<LoadingPlanet>();
	
	public LoadingPlanet(LoadingPlanet orbit, double dist, double speed){
		this.orbit=orbit;
		this.dist=dist;
		this.speed=speed;
		planets.add(this);
	}
	
}
