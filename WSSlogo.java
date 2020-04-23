import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.Path2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class WSSlogo{
	
	private int step=0;
	private int alphaWave=0;
	private int alphaIo=0;
	private int posWave;
	private int[] valStream=new int[6];	// pourcentage remplissage stream
	private boolean[] evoUpStream=new boolean[6];	// est que le pourcentage augmente ?
	private int[] toStream=new int[6];
	private boolean evoUpAlphaStud;
	private int alphaStud=383;
	
	public void showLogo(Graphics2D g2d, int W, int H){
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(new Font("arial",Font.PLAIN,96));
		FontMetrics fm=g2d.getFontMetrics();
		int speed=10;	// plus c'est petit, plus la vitesse est élevée


		g2d.setColor(Color.white);
		g2d.fillRect(0,0,W,H);
		g2d.setColor(Color.black);

		// PARTIE "WAVE"

		String str="WAVE";
		int xx=0;
		int yy=H-fm.getHeight();

		// -> TEXTE 

		if(step>speed*6){	// pour que l'animation ne commence qu'après la fin de STREAM
			int h=fm.getAscent();
			for(int i=0;i<str.length();i++){
				String chr=str.substring(i,i+1);
				int l=fm.stringWidth(chr);
				int t=(step-speed*6-i*speed/3)*speed;

				g2d.drawString(chr,xx,yy+h-Math.min(h,t));

				xx+=l;
			}
		}

		// -> VAGUE

		if(step>=speed*6+18){
			int h=(fm.getAscent()+fm.getDescent())/3;
			yy-=h;
			int ww=fm.stringWidth("WAVE");
			Stroke defaultStroke=g2d.getStroke();
			g2d.setStroke(new BasicStroke(16f));
			g2d.setColor(new Color(255,255,255,alphaWave));
			//g2d.setColor(Color.white);

			posWave--;
			if(posWave<-ww) posWave=0;

			Path2D vague=new Path2D.Double();
			vague.moveTo(posWave,yy);
			vague.curveTo(ww*1/3+posWave,yy-h*2,ww*2/3+posWave,yy+h*2,ww+posWave,yy);
			vague.curveTo(ww+ww*1/3+posWave,yy-h*2,ww+ww*2/3+posWave,yy+h*2,ww*2+posWave,yy);
			/*vague.curveTo(ww*1/3,yy-h*2+y1Wave*h*4,ww*2/3,yy-h*2+y2Wave*h*4,ww,yy);

				if(wave1Up){
					y1Wave+=0.01;
					y2Wave-=0.01;
					if(y1Wave>=1){
						y1Wave=1;
						y2Wave=0;
						wave1Up=false;
					}
				}else{
					y1Wave-=0.01;
					y2Wave+=0.01;
					if(y1Wave<=0){
						y1Wave=0;
						y2Wave=1;
						wave1Up=true;
					}
				}*/



			g2d.draw(vague);
			g2d.setColor(Color.black);
			g2d.setStroke(defaultStroke);
			GlyphVector outline=g2d.getFont().createGlyphVector(fm.getFontRenderContext(),"WAVE");
			g2d.translate(0,yy+h);
			//g2d.draw(outline.getOutline());
			g2d.translate(0,-yy-h);
			g2d.setColor(Color.black);
			alphaWave=Math.min(128, alphaWave+3);
		}

		// PARTIE STREAM

		str="STREAM";
		xx=fm.stringWidth(str);
		yy=H-fm.getDescent();
		for(int i=str.length();i>0;i--){
			String chr=str.substring(Math.max(0,i-1),i);
			int l=fm.stringWidth(chr);
			xx-=l;
			// -> animation dernière lettre
			if((i-1)*speed<step&&i*speed>step){
				g2d.drawString(chr,xx-l+(step%speed*l/speed),yy);
			}
			// -> affichage autres lettres + rectangle blanc
			if(i*speed<=step){
				g2d.setColor(Color.white);
				g2d.fillRect(0,H-fm.getHeight(),xx+l,H);
				g2d.setColor(Color.black);
				g2d.drawString(chr,xx,yy);
			}
		}

		// -> ondulations
		if(step==0){
			for(int i=0;i<6;i++){
				valStream[i]=0;
				evoUpStream[i]=false;
				toStream[i]=0;
			}
		}

		if(step>speed*6){
			for(int i=0;i<6;i++){
				//if(evoUpStream[i]) valStream[i]++; else valStream[i]--;
				if(Math.abs(valStream[i]-toStream[i])<20){
					/*if(evoUpStream[i]){
							toStream[i]=(int)(Math.random()*valStream[i]);
							evoUpStream[i]=false;
						}else{
							toStream[i]=(int)(Math.random()*(100-valStream[i]));
							evoUpStream[i]=true;
						}*/
					toStream[i]=(int)(Math.random()*100);
				}else{
					valStream[i]+=(toStream[i]-valStream[i])*0.05;
					//if(valStream[i]<toStream[i]) valStream[i]++; else valStream[i]--;
				}
				valStream[i]=Math.max(0,Math.min(100,valStream[i]));
				//if((int)(Math.random()*15)==0) evoUpStream[i]=!evoUpStream[i];

				g2d.setColor(new Color(255,255,255,128));
				g2d.fillRect(xx+fm.stringWidth(str.substring(0,i)),fm.getHeight()-1,
						fm.stringWidth(str.charAt(i)+""),valStream[i]*(fm.getAscent()-fm.getDescent())/100);
			}
		}

		// PARTIE STUDIO


		xx=fm.stringWidth("WAVE");
		yy=fm.getDescent();
		int ww=fm.stringWidth("STREAM")-fm.stringWidth("WAVE")-fm.getLeading()*2;
		int hh=fm.getHeight()-fm.getDescent()*2-fm.getLeading();
		g2d.setColor(Color.black);

		if(step>=speed*6+18){
			int t=(step-speed*6-18)*3;
			g2d.fillRect(xx,yy,Math.min(ww,t),hh);
		}

		g2d.setFont(new Font("arial",Font.PLAIN,30));
		FontMetrics smallFm=g2d.getFontMetrics();
		g2d.setColor(Color.white);

		// -> PARTIE "STUD"

		str="STUD";
		int space=ww/8;
		xx+=space/4;
		yy+=smallFm.getAscent();

		//alphaStud+=(255-alphaStud)*0.1;

		//g2d.setColor(new Color(255,255,255,alphaStud));

		// -> clignotement

		if(step>=speed*6+18+ww/3){
			if(evoUpAlphaStud) alphaStud++; else alphaStud--;
			if(alphaStud<=128) evoUpAlphaStud=true;
			if(alphaStud>=383) evoUpAlphaStud=false;
			g2d.setColor(new Color(255,255,255,Math.min(255,alphaStud)));
		}

		for(int i=0;i<str.length();i++){
			g2d.drawString(str.charAt(i)+"",xx,yy);
			xx+=space*2;
		}

		/*if(step>=speed*6+18+ww/3){
				/*posLightStud++;
				//if(posLightStud>ww) posLightStud=0;
				xx=fm.stringWidth("WAVE");
				Paint defaultPaint=g2d.getPaint();
				GradientPaint gp=new GradientPaint(posLightStud,yy,new Color(255,255,255,128),posLightStud+ww,yy,new Color(255,255,255,0),true);
				g2d.setPaint(gp);
				g2d.fillRect(xx,yy-smallFm.getAscent(),ww,smallFm.getAscent());
				g2d.setPaint(defaultPaint);


			}*/

		// -> PARTIE "IO"

		xx=fm.stringWidth("WAVE")+ww/2;
		yy+=(smallFm.getHeight()-smallFm.getAscent())/2;
		hh=hh-smallFm.getHeight()-(smallFm.getHeight()-smallFm.getAscent())/2;
		int www=hh*5/3;	// largeur du IO

		if(step>=speed*6+18+ww/3){
			alphaIo+=(255-alphaIo)*0.1;
			if(alphaIo==246) alphaIo=255;

			g2d.setColor(new Color(255,255,255,alphaIo));
			g2d.fillRect(xx-www/2,yy,hh/3,hh);

			int t=(step-(speed*6+18+ww/3))*6;
			g2d.fillArc(xx-www/2+hh*2/3,yy,hh,hh,90,-Math.min(360,t));
			g2d.setColor(Color.black);
			g2d.fillOval(xx-www/2+hh,yy+hh/3,hh/3,hh/3);
		}



		// *****************

		step+=2;
	}
}
