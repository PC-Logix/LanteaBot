package pcl.lc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import pcl.lc.irc.IRCBot;

public class InputThread extends Thread 
{ 
	boolean done = false;
	// reader to read keyboard input 
	private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 
	// writer to send data down the socket 
	private PrintWriter writer; 
 
 
	public void run() 
	{ 
		while(!done) 
		{ 
			//attempt to read from kb, then send  
			// the output straight along the socket 
			try {
				String line = reader.readLine();
				String[] split = line.split("\\s+");
				String command = split[0];
				String target = split[1];
				split[1] = "";
				split[0] = "";
				StringBuilder builder = new StringBuilder();
				for(String s : split) {
				    builder.append(s + " ");
				}
				String message = builder.toString();
				if (command.equals("msg")) {
					Helper.sendMessage(target, message);
				}else if (command.equals("act")) {
					Helper.sendAction(target, message);
				}
				//System.out.println(reader.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} 
	} 
}