package main;

import gui.panels.map.MapPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;

public class LogVisualizer extends JFrame {
	
	private static String FOLDER = "logs";
	private MapPanel map;
	private JSlider slider;
	private ArrayList<LogData> allData;
	private int currentStep = 0;
	private PlayThread playThread;
	private JLabel currentStepLabel;
	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	private String IPforEntities = "1";
	private int lastIncrementStep = 0;
	
	public static void main(String[] args) {
		new LogVisualizer();
	}
	
	public LogVisualizer() {
		
		try {
			allData = readFile();
			Collections.sort(allData);
			
			playThread = new PlayThread();
			playThread.start();
		
			map = new MapPanel();
			
			setLayout(new BorderLayout());
			
			add(map,BorderLayout.CENTER);
			
			slider = new JSlider(0,allData.size());
			slider.setValue(0);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					currentStep = slider.getValue();
					slider.setToolTipText(""+slider.getValue());
					if(!playThread.isPlaying())
						moveTo(slider.getValue());
				}
			});
			
			JButton playButton = new JButton("Play/Pause");
			playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					playThread.toggle();
				}
			});
			
			currentStepLabel = new JLabel();
			updateCurrentStepLabel();
			
			JButton slower = new JButton("Speed --");
			slower.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					playThread.playSlower();
				}
			});
			
			JButton faster = new JButton("Speed ++");
			faster.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					playThread.playFaster();
				}
			});
			
			JPanel controlsPanel = new JPanel(new BorderLayout());
			JPanel buttonsPanel = new JPanel();
			
			buttonsPanel.add(slower);
			buttonsPanel.add(playButton);
			buttonsPanel.add(faster);
			
			controlsPanel.add(currentStepLabel, BorderLayout.NORTH);
			controlsPanel.add(slider, BorderLayout.CENTER);
			controlsPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			add(controlsPanel,BorderLayout.SOUTH);
			
			setSize(800, 800);
			setVisible(true);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void moveTo(int step) {
		
		if(step > allData.size()){
			playThread.pause();
			return;
		}
		
		currentStep = step;
		map.clearHistory();
		
		for(int i = 0 ; i < step ; i++) {
			LogData d = allData.get(i);
			
			map.displayData(new RobotLocation(d.ip, d.latLon, d.compassOrientation, d.droneType));
			
			if(d.entities != null && d.ip.equals(IPforEntities)) {
				map.replaceEntities(d.entities);
			}
		}
		
		updateCurrentStepLabel();
		
	}
	
	private void incrementPlay() {
		
		if(currentStep + 1 > allData.size()){
			playThread.pause();
			return;
		}
		
		if(lastIncrementStep == currentStep) {
			currentStep++;
			slider.setValue(currentStep);
			LogData d = allData.get(currentStep);
			
			map.displayData(new RobotLocation(d.ip, d.latLon, d.compassOrientation, d.droneType));
			
			if(d.entities != null && d.ip.equals(IPforEntities)) {
				map.replaceEntities(d.entities);
			}
			updateCurrentStepLabel();
		} else {
			moveTo(currentStep);
		}
		
		lastIncrementStep = currentStep;
	}
	
	private ArrayList<LogData> readFile() throws IOException {
		
		File folder = new File(FOLDER);
		
		ArrayList<LogData> result = new ArrayList<LogData>();
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy_HH:mm:ss.SS");
		
		for(String file : folder.list()) {
			
			System.out.println(file);
			
			if(!file.contains(".log"))
				continue;
		
			Scanner s = new Scanner(new File(FOLDER+"/"+file));
			
			String lastComment = "";
			ArrayList<LogData> data = new  ArrayList<LogData>();
			
			int step = 0;
			
			String ip = "";
			
			ArrayList<Entity> currentEntities = new ArrayList<Entity>();
			
			while(s.hasNext()) {
				String l = s.nextLine();
				
				if(!l.startsWith("[") && !l.startsWith("#") && !l.trim().isEmpty()) {
					
					Scanner sl = new Scanner(l);
					
					try {
					
						LogData d = new LogData();
						
						d.time = sl.next();
						
						double lat = sl.nextDouble();
						double lon = sl.nextDouble();
						
						d.latLon = new LatLon(lat,lon);
						
						d.GPSorientation = sl.nextDouble();
						d.compassOrientation = sl.nextDouble();
						d.GPSspeed = sl.nextDouble();
						
						String dateStr = sl.next();
						
						try {
							
							d.date = dtf.parseDateTime(dateStr);
						
							double left = sl.nextDouble();
							double right = sl.nextDouble();
							
							d.leftSpeed = left;
							d.rightSpeed = right;
							
						} catch(Exception e){}
						
						d.droneType = AquaticDroneCI.DroneType.valueOf(sl.next());
						
						d.lastComment = lastComment;
						
						d.timestep = step++;
						
						d.entities = new ArrayList<Entity>();
						d.entities.addAll(currentEntities);
						
						d.file = file;
						
						data.add(d);
					
					}catch(Exception e){
						System.out.println(l);
						e.printStackTrace();
					}
					
					sl.close();
					
				} else if(l.startsWith("#")){
					
					if(l.startsWith("#entity")) {
						handleEntity(l,currentEntities);
					} if(l.startsWith("#IP")) {
						ip = l.replace("#IP ", "").trim();
					} else
						lastComment = l.substring(1);
				}
			}
			
			System.out.println(step);
			
			if(!ip.isEmpty()) {
				
				for(LogData d : data)
					d.ip = ip;
				
				result.addAll(data);
			}
			
			s.close();
		}
		
		return result;
	}
	
	private void handleEntity(String line, ArrayList<Entity> entities) {
		Scanner s = new Scanner(line);
		s.next();//ignore first token
		
		String event = s.next();
		
		if(event.equals("added")) {
			
			String className = s.next();
			
			String name = s.next();
			
			if(className.equals(GeoFence.class.getSimpleName())) {
				
				GeoFence fence = new GeoFence(name);
				
				int number = s.nextInt();
				
				for(int i = 0 ; i < number ; i++) {
					double lat = s.nextDouble();
					double lon = s.nextDouble();
					fence.addWaypoint(new LatLon(lat,lon));					
				}
				entities.add(fence);
			} else if(className.equals(Waypoint.class.getSimpleName())) {
				
				double lat = s.nextDouble();
				double lon = s.nextDouble();
				Waypoint wp = new Waypoint(name, new LatLon(lat,lon));
				entities.remove(wp);
				entities.add(wp);
				
			} else if(className.equals(ObstacleLocation.class.getSimpleName())) {
				
				double lat = s.nextDouble();
				double lon = s.nextDouble();
				
				double radius = s.nextDouble();
				entities.add(new ObstacleLocation(name, new LatLon(lat,lon),radius));
			}
			
		} else if(event.equals("removed")) {
			
			String name = s.next();
			
			Iterator<Entity> i = entities.iterator();
			while(i.hasNext()) {
				if(i.next().getName().equals(name)) {
					i.remove();
					break;
				}
			}
		}
		
		s.close();
	}
	
	private void updateCurrentStepLabel() {
		
		if(currentStep < allData.size()) {
			LogData d = allData.get(currentStep);
			
			String text = "Step: "+currentStep+"/"+allData.size();
			text+="\t Time: "+d.date.toString(hourFormatter)+" ("+(1/playThread.getMultiplier())+"x)";
			currentStepLabel.setText(text);
		}
	}
	
	private long compareTimeWithNextStep() {
		
		if(currentStep + 1 < allData.size()) {
			DateTime d1 = allData.get(currentStep).date;
			DateTime d2 = allData.get(currentStep + 1).date;
			return d2.getMillis() - d1.getMillis();
		}
		
		return 0;
	}
	
	public class PlayThread extends Thread {
		
		private boolean play = false;
		private double multiplier = 1.0;
		
		@Override
		public void run() {
			
			while(true) {
				
				try {
					synchronized(this) {
						if(!play)
							wait();
					}
					
					incrementPlay();
					
					long time = (long)(compareTimeWithNextStep()*multiplier);
					
					if(time > 1000)
						time = 1000;
					
					Thread.sleep(time);
					
				} catch(Exception e){}
			}
		}
		
		public synchronized void play() {
			play = true;
			notify();
		}
		
		public void toggle() {
			if(play)
				pause();
			else
				play();
		}
		
		public void pause() {
			play = false;
		}
		
		public void playFaster() {
			multiplier*=0.5;
			updateCurrentStepLabel();
			interrupt();
		}
		
		public void playSlower() {
			if(multiplier < Math.pow(2, 4)) {
				multiplier*=2;
				updateCurrentStepLabel();
				interrupt();
			}
		}
		
		public boolean isPlaying() {
			return play;
		}
		
		public double getMultiplier() {
			return multiplier;
		}
	}
	
	public class LogData implements Comparable<LogData>{
		String time;
		String file;
		String ip;
		int timestep;
		LatLon latLon;
		double GPSorientation;
		double compassOrientation;
		double GPSspeed;
		double leftSpeed;
		double rightSpeed;
		DateTime date;
		String lastComment;
		AquaticDroneCI.DroneType droneType;
		ArrayList<Entity> entities = null;
		
		@Override
		public int compareTo(LogData o) {
			return date.compareTo(o.date);
		}
	}

}