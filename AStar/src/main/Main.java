package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

public class Main implements Runnable{
	
	public static Random rand = new Random();
	
	private JFrame frame;
	private Canvas canvas;
	private Thread thread;
	
	public static int WIDTH = 600,HEIGHT=600;
	public static int cols=20,rows=20;
	public static int w = WIDTH/cols , h = HEIGHT/rows;
	private BufferStrategy bs;
	private Graphics g;
	
	//REAL STUFF
	ArrayList<Spot> path ;
	public ArrayList<Spot> openSet,closedSet;
	public Spot start,end;
	Spot[][] grid;
	public boolean done=false;
	
	public Main(){
		frame=new JFrame("A Star Path Finder");
		frame.setSize(WIDTH,HEIGHT);
		frame.setVisible(true);;
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		canvas.setMaximumSize(new Dimension(WIDTH,HEIGHT));
		canvas.setMinimumSize(new Dimension(WIDTH,HEIGHT));
		
		frame.add(canvas);
		frame.pack();
		
		init();
		
	}
	
	public void init(){
		grid = new Spot[rows][cols];
		for(int x=0;x<cols;x++){
			for(int y=0;y<rows;y++){
				grid[y][x] = new Spot(x,y);
			}
		}
		for(int x=0;x<cols;x++){
			for(int y=0;y<rows;y++){
				grid[y][x].addNeighbors(grid);
			}
		}
		start = grid[0][0];
		end = grid[rows-10][cols-1];
		openSet=new ArrayList<Spot>();
		closedSet=new ArrayList<Spot>();
		openSet.add(start);
		path = new ArrayList<Spot>();
		start.wall=false;
		end.wall=false;
	}
	
	public synchronized void start(){
		thread = new Thread(this);
		thread.start();
	}
	
	public void tick(){
		
	}
	
	public void render(){
		bs=canvas.getBufferStrategy();
		if(bs==null){
			canvas.createBufferStrategy(3);
			return;
		}
		g=bs.getDrawGraphics();
		
		//background
		g.clearRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, WIDTH,HEIGHT);
		//start draw
		
		if(openSet.size()>0 && !done){
			int winner = 0;//index of winner
			
			//*****find spot with lowest f in open set*****
			for(int i=0;i<openSet.size();i++){
				if(openSet.get(i).f<openSet.get(winner).f){
					winner =i;
				}
			}
			//*********
			
			Spot current = openSet.get(winner);
			
			if(current.equals(end)){
				Spot temp = current;
				path.add(temp);
				while(temp.previous !=null){
					path.add(temp.previous);
					temp=temp.previous;
				}
				System.out.println("DONE");
				done=true;
			}
			
			closedSet.add(current);
			openSet.remove(current);
			
			ArrayList<Spot> neighbors = current.neighbors;
			for(Spot neighbor:neighbors){
				if(!closedSet.contains(neighbor) && !neighbor.wall){
					int tempG=current.g+1;
					boolean newPath=false;
					if(openSet.contains(neighbor)){
						if(tempG<neighbor.g){
							neighbor.g=tempG;
							newPath=true;
						}
					}else{
						neighbor.g = tempG;
						openSet.add(neighbor);
						newPath=true;
					}
					if(newPath){
						neighbor.h = heuristic(neighbor,end);
						neighbor.f = neighbor.g+neighbor.h;
						neighbor.previous = current;
					}
				}
			}
			//continue solving 
		}else{
			//no solution
//			System.out.println("No Solution");
		}
		
		for(int x=0;x<cols;x++){
			for(int y=0;y<rows;y++){
				grid[y][x].render(g,Color.LIGHT_GRAY);
			}
		}
		if(!done){
			for(int i=0;i<openSet.size();i++){
				openSet.get(i).render(g, Color.GREEN.brighter());
			}
			for(int i=0;i<closedSet.size();i++){
				closedSet.get(i).render(g, Color.RED.brighter());
			}
			
			for(int i=0;i<path.size();i++){
				path.get(i).render(g, Color.BLUE.brighter());
			}
		}
		g.setColor(Color.RED.darker());
		for(int i=0;i<path.size()-1;i+=1){
			g.drawLine(path.get(i).x*w+w/2, path.get(i).y*h+h/2,path.get(i+1).x*w+w/2, path.get(i+1).y*h+h/2);
			g.drawLine(path.get(i).x*w+w/2, path.get(i).y*h+h/2,path.get(i+1).x*w+w/2, path.get(i+1).y*h+h/2);

		}
		start.highlight(g,Color.ORANGE);
		end.highlight(g,Color.BLUE);
		
		//end draw
		bs.show();
		g.dispose();
	}
	
	public int heuristic(Spot a,Spot b){
		int d=0;
		d= (int) Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
		return d;
		
	}
	
	@Override
	public void run(){
		while(true){
			tick();
			render();
//			try {
//				thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
	}
	
	public synchronized void stop(){
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Main main = new Main();
		main.start();
	}
	
	class Spot{
		public int f=0,g=0,h=0;
		public int x,y;
		public ArrayList<Spot> neighbors;
		public Spot previous=null;
		public boolean wall=false;
		public Spot(int x ,int y){
			this.x=x;
			this.y=y;
			this.neighbors = new ArrayList<Spot>();
			if(Math.random()<0.25)
				wall=true;
		}
		
		public void render(Graphics g,Color color){
			g.setColor(Color.DARK_GRAY);
			//g.drawRect(x*Main.w, y*Main.h, Main.w-1, Main.h-1);
			g.setColor(color);
			if(this.wall)
				g.setColor(Color.BLACK);
			g.fillOval(x*Main.w, y*Main.h, Main.w-1, Main.h-1);
			

		}
		public void highlight(Graphics g,Color c){
			g.setColor(c);
			g.fillOval(x*Main.w, y*Main.h, Main.w-1, Main.h-1);
		}
		public void addNeighbors(Spot[][] grid){
			if(y>0) neighbors.add(grid[y-1][x]);
			if(y<rows-1) neighbors.add(grid[y+1][x]);
			if(x>0) neighbors.add(grid[y][x-1]);
			if(x<cols-1) neighbors.add(grid[y][x+1]);
			
			if(y>0 && x>0) neighbors.add(grid[y-1][x-1]);
			if(y<rows-1 && x<cols-1) neighbors.add(grid[y+1][x+1]);
			if(y>0 && x<cols-1) neighbors.add(grid[y-1][x+1]);
			if(y<rows-1 && x>0) neighbors.add(grid[y+1][x-1]);

		}
		
		
	}
	
}
