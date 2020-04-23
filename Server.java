import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;


public class Server/* extends Thread*/{

	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	private Vector<String> messages=new Vector<String>();
	private boolean canPlay=false;
	public static Obj waitingForId=null;

	public Server(){
		try {
			socket=new Socket("192.168.1.23",2468);
			writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			
			
			(new Thread(){
				public void run(){
					try {
						String line;
						while((line=reader.readLine())!=null) {
							System.out.println(line);
							parseMessage(line);
						}
					} catch (IOException e) {/*e.printStackTrace();*/}
				}
			}).start();
		} catch (UnknownHostException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
		//start();
	}
	
	private void parseMessage(String msg){
		int pos=msg.indexOf(" ");
		String request=msg;
		String[] args=null;
		if(pos!=-1){
			request=msg.substring(0,pos);
			args=msg.substring(pos+1).split("&%&");
		}
		
		switch(request){
			case "CAN_PLAY":
				System.out.println("Tous les joueurs sont connectés");
				canPlay=true;
				break;
			case "DISCONNECT":
				System.out.println("Fin du jeu : "+args[0]);
				Screen.exit();
				break;
			case "SET_ID":
				Obj obj=waitingForId;
				obj.id=toInt(args[0]);
				obj.mustSendData=true;
				System.out.println("->"+obj.getClass().getName());
				waitingForId=null;
				break;
			case "CREATE":
				Obj newObj=null;
				switch(args[1]){
					case "Player":
						/*newObj=new Player();
						((Player)newObj).setColor(new Color(toInt(args[3]),toInt(args[4]),toInt(args[5])));*/
						break;
				}
				
				newObj.id=Integer.parseInt(args[0]);
				break;
			case "UPDATE":
				//Screen.getObjById(toInt(args[0])).update(args);
				break;
		}
	}
	
	/*public void run(){
		while(true){
			for(int i=0;i<messages.size();i++){
				try {
					String msg=messages.get(i);
					writer.write(msg+"\n");
					writer.flush();
				} catch (IOException e) {e.printStackTrace();}
				messages.removeAllElements();
			}
		}
	}*/
	
	public void sendMessage(String msg){
		//messages.add(msg);
		try {
			writer.write(msg+"\n");
			writer.flush();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public boolean canPlay(){
		return canPlay;
	}
	
	public void exit(){
		try {
			writer.close();
			reader.close();
			socket.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	private int toInt(String str){
		return Integer.parseInt(str);
	}
	
}
