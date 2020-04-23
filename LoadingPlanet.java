import java.util.Vector;


public class LoadingPlanet {	// planètes de l'animation de chargement

	public LoadingPlanet orbit;	// cette planète tourne autour de orbit
	public double dist, speed;	// et est à dist pixels de orbit et tourne a speed degrés/step
	public double angle=Math.random()*360;
	
	public static Vector<LoadingPlanet> planets=new Vector<LoadingPlanet>();
	
	public LoadingPlanet(LoadingPlanet orbit, double dist, double speed){
		this.orbit=orbit;
		this.dist=dist;
		this.speed=speed;
		planets.add(this);
	}
	
}
