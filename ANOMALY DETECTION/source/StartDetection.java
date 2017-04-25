package source;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.io.File;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * main class for setup of parameters needed for anomaly detection
 * and beginning anomaly detection through simple pruning,
 * batch monitoring and join rescheduling techniques
 *
 */
public class StartDetection extends JFrame implements ActionListener
{
	private JLabel l1, l2, l3, l4, l5, l6, l7, l8;
	public static JTextField t1, t2, t3, t4, t5, t6, t7, t8, dt;
	private JButton dab, db;
	private JFileChooser fc;
	public static JTextArea ta;
	private static JScrollPane scroll;
	JPanel panup, pandown;
	  
	public StartDetection() 
	{
	 	/**
	 	 * create frame
	 	 */
	 	super("Anomaly Detection in Trajectory Streams");
	        
	 	setDefaultLookAndFeelDecorated(true);
	        
	 	/**
	 	 * set default close operation
	 	 */
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 
	    /**
	     * set layout
	     */
	    setLayout(new GridLayout(2, 1));
	    
	    /**
	     * create top panel
	     */
	    panup = new JPanel();
	    panup.setLayout(new GridLayout(13, 2));
	        
	               
	    /**
	     * create bottom panel
	     */
	    pandown = new JPanel();
	    pandown.setLayout(new GridLayout(1, 1));
	        
	    /**
	     * instantiate components
	     */
	    fc = new JFileChooser();
	        
	    l1 = new JLabel("Neighbor Threshold, K: ");
	    l2 = new JLabel("Distance Threshold, D:");
	    l3 = new JLabel("Buffer Size: ");
	    l4 = new JLabel("Base Window Length: ");
	    l5 = new JLabel("Left Sliding Window Size: ");
	    l6 = new JLabel("Right Sliding Window Size: ");
	    l7 = new JLabel("Piece Window Size: ");
	    l8 = new JLabel(" VP Tree Leaf Size: ");
	    
	    /**
	     * set default parameter values
	     */
	    t1 = new JTextField("8", 20);
	    t2 = new JTextField("17.891367",20);
	    t3 = new JTextField("81512", 20);
	    t4 = new JTextField("256", 20);	
	    t5 = new JTextField("16512", 20);
	    t6 = new JTextField("16512", 20);
	    t7 = new JTextField("480", 20);
	    t8 = new JTextField("10", 20);
	    dt = new JTextField("");
	    //t2.setEditable(false);
	    //dt.setEditable(false);
	    
	    ta = new JTextArea(100,50);
	    scroll = new JScrollPane(ta);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	    //scroll.getViewport().addChangeListener(this);
	    
	    //scroll.getVerticalScrollBar().addAdjustmentListener(this);
	    db = new JButton("Choose Dataset");
	    dab = new JButton("Detect Anomalies");
	    
	    db.addActionListener(this);
	    dab.addActionListener(this);
	    	
	    panup.add(db);
	    panup.add(dt);
	    	
	    panup.add(l1);
	    panup.add(t1);
	        		
	    panup.add(l2);
	    panup.add(t2);
	        
	    panup.add(l3);
	    panup.add(t3);
	        
	    panup.add(l4);
	    panup.add(t4);
	        
	    panup.add(l5);
	    panup.add(t5);
	        
	    panup.add(l6);
	    panup.add(t6);
	       
	    panup.add(l7);
	    panup.add(t7);
	        
	    panup.add(l8);
	    panup.add(t8);
	        	    	
	    panup.add(dab);
	    
	    pandown.add(scroll);
	   // pandown.add(ta);     
	    	
	    add(panup);
	    	
	    add(pandown);
	    	
	    /**
	     * show frame
	     */
	    setSize(400, 125);
	    pack();
	    setVisible(true);
	    this.repaint(1000);
	 }
	 
	 public void actionPerformed(ActionEvent e)
	 {
		if(e.getSource() == dab) 
		{			 
			try 
			{
				Parameters.K = Integer.parseInt((String)t1.getText());
				Parameters.D = Float.parseFloat((String)t2.getText());
				Parameters.bufferSize = Integer.parseInt((String)t3.getText());
				Parameters.baseWindowLength = Integer.parseInt((String)t4.getText());
				Parameters.leftSlidingWindowSize = Integer.parseInt((String)t5.getText());
				Parameters.rightSlidingWindowSize = Integer.parseInt((String)t6.getText());
				Parameters.pieceWindowSize = Integer.parseInt(t7.getText());
				Parameters.LeafSize = Integer.parseInt(t8.getText());
				
				//StartDetection.ta.append("Neighbor Threshold, K: " + Parameters.K + "\n");
				//StartDetection.ta.append("Distance Threshold, D: " + Parameters.D + "\n");
				//StartDetection.ta.append("Buffer Size: " + Parameters.bufferSize + "\n");
				//StartDetection.ta.append("Base Window Length: " + Parameters.baseWindowLength + "\n");
				//StartDetection.ta.append("Left Sliding Window Size: " + Parameters.leftSlidingWindowSize + "\n");
				//StartDetection.ta.append("Right Sliding Window Size: " + Parameters.rightSlidingWindowSize + "\n");
				//StartDetection.ta.append("Piece Window Size" + Parameters.pieceWindowSize);
				//StartDetection.ta.append("VP Tree Leaf Size" + Parameters.LeafSize);
				
				//StartDetection.ta.update(StartDetection.ta.getGraphics());
				
			}
			catch(Exception ex) 
			{
				System.out.println("Error in Reading Input" + ex);
			}
			detect();
		}
		else if(e.getSource() == db)  
		{
			int returnVal = fc.showOpenDialog(StartDetection.this);
			 
	        if (returnVal == JFileChooser.APPROVE_OPTION) 
	        {
	        	File file = fc.getSelectedFile();
	            LoadData.datasetPath = (String) file.getPath();
	            dt.setText( file.getPath());
	      
	        }	      
		}
	}
 
	/**
	 * start detection of anomaly windows
	 * in trajectory data stream
	 */
	private void detect() 	 
	{	
	 	/**
	 	 * Anomaly Detection using Join Rescheduling
	 	 * and Piecewise VP Tree Indexing
	 	 */
		StartDetection.ta.append("Join Rescheduling - Piecewise VP Tree Indexing");
		StartDetection.ta.update(StartDetection.ta.getGraphics());
		System.out.println("Join Rescheduling - Piecewise VP Tree Indexing");
	 	JoinReschedule JR = new JoinReschedule();
		JR.detectAnomalies();
		
		/**
		 * Anomaly Detection using Batch Monitoring and Online Local Clustering
		 */
		StartDetection.ta.append("\n-----------------------------------------------------------------" +
				"\nBatch Monitoring");
		StartDetection.ta.update(StartDetection.ta.getGraphics());
		System.out.println("\n-----------------------------------------------------------------" +
				"\nBatch Monitoring");
		BatchMonitor BM = new BatchMonitor();
		BM.detectAnomalies();
		
		/**
		 * Anomaly Detection using Simple Pruning
		 */
		StartDetection.ta.append("\n-------------------------------------------------------------------" +
						"\nSimple Pruning");
		StartDetection.ta.update(StartDetection.ta.getGraphics());
		System.out.println("\n-----------------------------------------------------------------" +
				"\nSimple Pruning");
		SimplePruning SP = new SimplePruning();
	 	SP.detectAnomalies();		
	}
	 
	public static void main(String[] args) 
	{	
		new StartDetection();
	}
}

