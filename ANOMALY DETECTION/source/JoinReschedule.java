package source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Detection of Anomalies using
 * Batch Monitoring through Local Clustering
 * and Piecewise VP Tree Indexing
 */

public class JoinReschedule 
{
	/**
	 * total number of distance computations performed
	 */
	private static int NoOfDistComps = 0;

	/**
	 * number of anomalies in trajectory stream
	 */
	private int anomalyCount = 0;
	
	/**
	 * current pivot
	 */
	private LocalCluster pCurrent = null;

	/**
	 * radius of current cluster
	 */
	private float currentRadius = 0;
	
	/**
	 * pivot found flag
	 */
	private boolean pivotFound = true;

	/**
	 * max radius of cluster
	 */
	private float radius = 0;

	/**
	 * max left and right bin size of local cluster
	 */
	private int binSize = 0;

	/**
	 * list of clusters in current left bin
	 */
	private ArrayList <LocalCluster> currentLeftBin = new ArrayList<LocalCluster> (1000);
	
	/**
	 * index to trajectory stream beginning
	 */
	private int startStream = 0;

	/**
	 * index to trajectory stream ending
	 */
	private int endStream = 0;

	/**
	 * Collection of VPTrees in piece window
	 */
	private ArrayList <PiecewiseVPTree> VPTrees = new ArrayList<PiecewiseVPTree> ();

	/**
	 * radius of pivot in VPTree
	 */
	private float pivotRadius = 0;
	
	/**
	 * List of out of scope pivots - VPTree to be removed
	 */
	private ArrayList<LocalCluster> Lold = new ArrayList<LocalCluster> (1000);

	/**
	 * List of recent pivots - VPTree to be constructed
	 */
	private ArrayList <LocalCluster> Lnew = new ArrayList<LocalCluster> (1000);

	/**
	 * recent anomaly window
	 */
	private int currentAnomaly = -1;
	
	/**
	 * distances from current pivot to query windows
	 */
	private float[][] pCurrentQDist;
	
	/**
	 * Constructor
	 * loads dataset to simulate trajectory stream
	 */
	
	public JoinReschedule() 
	{
		LoadData.loadDataset();	
		
	 	Parameters.D = ComputeStats.findDiameterofDataset();
	 	StartDetection.t2.setText(Float.toString(Parameters.D));
	 	StartDetection.ta.append("D: " + Parameters.D + "\n");
	 	System.out.println("D: " + Parameters.D + "\n");

		/**
		 * max bin size of local cluster
		 */
		binSize = Parameters.baseWindowLength * 2;
		
		/**
		 * max radius of local cluster = half of diameter of dataset
		 */
		radius = Parameters.D / 2;	 
		
		pCurrentQDist = 
				new float[Parameters.pieceWindowSize][Parameters.pieceWindowSize];
	}
	
	/**
	 * detect anomalies in trajectory stream 
	 * using Batch Monitoring through Local Clustering and
	 * Piecewise VP Tree Indexing
	 */
	
	public void detectAnomalies() 
	{	
		//StartDetection.ta.append("Join Reschedule \n");
		//StartDetection.ta.update(StartDetection.ta.getGraphics());			

		/**
		 * set the initial and ending trajectory points for processing
		 */
		startStream = Parameters.baseWindowLength - 1;
		endStream = Parameters.baseWindowLength + Parameters.leftSlidingWindowSize - 1;
	
		/**
		 * initially current pivot found 
		 */
		pCurrent = new LocalCluster(Parameters.baseWindowLength - 1);
		pivotFound = false;
		
		long timeStart = System.currentTimeMillis();
	 
		/**
		 * compute initial local clusters
		 */
		for (int i = startStream; i <= endStream; i++) 
		{
			onlineLClustering(i);
		}

		endStream = pCurrent.getRightBinEnd();

		for (int i = 0; i < VPTrees.get(0).getPivots().length; i++) 
		{
			Lold.add(VPTrees.get(0).getPivots()[i]);
		}

		/**
		 * Remove Initial VP Tree
		 */
		VPTrees.remove(0);
		
		/**
		 * 
		 */
		endStream++;
	
		for (int i = endStream; i < Parameters.bufferSize; 
							i++, endStream++, startStream++) 
		{
			onlineLClustering(endStream);
			
			/**
			 * search right sliding window, only if number of 
			 * neighbors in left sliding window are less than 
			 * neighbor threshold
			 */
			if (LoadData.trajectoryStream[startStream].getNeighborCount() 
														< Parameters.K) 
			{
				searchRightWindow();
			}
			
			if (LoadData.trajectoryStream[startStream].getNeighborCount() 
														< Parameters.K) 
			{
				if(isBWAnomaly(startStream) == true) 
				{		
					currentAnomaly = startStream;
					LoadData.trajectoryStream[startStream].setIsAnomaly();
					
					anomalyCount++;
					StartDetection.ta.append("Anomaly " + anomalyCount 
								+ " - Base Window ending at " + startStream + "\n");
					//StartDetection.ta.append("------------------------------"
					//				+ "---------------------------------\n");
					StartDetection.ta.update(StartDetection.ta.getGraphics());			
					
					System.out.println("Anomaly " + anomalyCount 
								+ " - Base Window ending at " + startStream +
								"\t" + LoadData.trajectoryStream[startStream].getNeighborCount());	
				}
			}
			/**
			 * Construct old candidate local clusters
			 * and remove
			 */
			constructLold();
			removeLold();			
		}
		
		long timeEnd = System.currentTimeMillis();

		/**
		 * total time taken to detect anomalies
		 */
		StartDetection.ta.append("Time taken to Detect Anomalies = " 
									+ (timeEnd - timeStart) + "\n");
		System.out.println("Time taken to Detect Anomalies = " 
										+ (timeEnd - timeStart));

		/**
		 * total no. of anomalies detected
		 */
		StartDetection.ta.append("Number of Anomalies Detected = " 
											+ anomalyCount + "\n");
		System.out.println("Number of Anomalies Detected = " 
												+ anomalyCount);
		
		/**
		 * total no. of distance computations
		 */
		StartDetection.ta.append("Total Number of Distance Computations = " 
												+ NoOfDistComps + "\n");
		System.out.println("Total Number of Distance Computations = " 
														+ NoOfDistComps);	
	}

	/**
	 *  Find if current pivot is an anomaly
	 * @param BWEnd - current pivot
	 * @return - is current pivot anomaly
	 */
	public boolean isBWAnomaly(int BWEnd) 
	{
		if(BWEnd <= 2 * Parameters.baseWindowLength)
		{
			return false;
		}
		else if (currentAnomaly == -1)
		{
			return true;
		}
		else if((BWEnd - currentAnomaly) >= Parameters.baseWindowLength)
		{
			return true;
		}	
		else
		{
			return false;
		}
	}

	/**
	 * form local cluster for new trajectory point position
	 * @param Bnew - new trajectory point
	 */
	private void onlineLClustering(int Bnew) {
		
		/**
		 * accumulate points in right bin
		 */
		if (pivotFound == true)
		{
			/**
			 * if current pivot is not available
			 */
			int start = Parameters.baseWindowLength - 1;
			
			/**
			 * if current pivot is available
			 */
			if (pCurrent != null)
			{
				start = pCurrent.getRightBinEnd() + 1;
			}
			
			boolean flag = true;
			for (int i = start; i < Bnew; i++) 
			{
				/**
				 * find distance between new point and current pivot
				 */
				NoOfDistComps++;
				float dist = ComputeStats.findBaseWindowDistance(i, Bnew);
				if (dist > radius) 
				{
					flag = false;
					break;
				}
				
				if (dist < radius && dist > currentRadius) 
				{
					currentRadius = dist;
				}
			}
			if (flag == false || (Bnew - start) > binSize) 
			{
				/**
				 * form local cluster
				 */
				pCurrent = new LocalCluster(Bnew - 1);
				pCurrent.setLeftBinStart(start);
				pCurrent.setRightBinEnd(Bnew - 1);
				//pCurrent = null;
				pivotFound = false;
			}
		}
		
		/**
		 * accumulate points in the left bin
		 */
		if (pivotFound == false) 
		{
			int point = pCurrent.getPivot();
			
			/**
			 * find distance between new point and current pivot
			 */
			NoOfDistComps++;
			float dist = ComputeStats.findBaseWindowDistance(point, Bnew);
			
			if (dist < radius && (Bnew - point) < binSize) 
			{
				if (dist < radius && dist > currentRadius)
				{
					currentRadius = dist;
				}
			} 
			else 
			{
				/**
				 * set time tick t-1 as right bin end
				 */
				pCurrent.setRightBinEnd(Bnew - 1);
				pCurrent.setRadius(currentRadius);
				
				/**
				 * current pivot is added to current left bin
				 */
				currentLeftBin.add(pCurrent);
				Lnew.add(pCurrent);
				
				/**
				 * reinitialize temporary variables
				 */
				pivotFound = true;
				currentRadius = 0;				

				if (pCurrent.getRadius() > pivotRadius)
				{
					pivotRadius = pCurrent.getRadius();
				}
				/**
				 * construct new PWVP Tree
				 */
				constructNewVPTree();
			}
		}	
	}
	
	/**
	 * Construct old candidates to remove
	 */
	private void constructLold()
	{
		if (Lold.size() == 0)
		{
			LocalCluster[] LCold = VPTrees.get(0).getPivots();
			for (int j = 0; j < LCold.length; j++) 
			{
				Lold.add(LCold[j]);
			}
			VPTrees.remove(0);
		}	
	}
	
	/**
	 * Construct New VP Tree
	 */
	private void constructNewVPTree()
	{
		
		findNeighbors(pCurrent);
		
		int lnewsize;
		
		for (lnewsize = 0; lnewsize < Lnew.size() - 1; lnewsize++) 
		{
			LocalCluster C = (LocalCluster) Lnew.get(lnewsize);
			
			/**
			 * find distance between new point and current pivot
			 */
			
			float dist = ComputeStats.findBaseWindowDistance
					(C.getPivot(), pCurrent.getPivot());
			
			pCurrentQDist[Lnew.size() - 1][lnewsize] = dist;
			pCurrentQDist[lnewsize][Lnew.size() - 1] = dist;
		}
		pCurrentQDist[lnewsize][lnewsize] = 0;
		
		if (Lnew.size() >= Parameters.pieceWindowSize)
		{										
			PiecewiseVPTree newVPTree = new PiecewiseVPTree
				(Lnew.toArray(new LocalCluster[Lnew.size()]), pCurrentQDist);
			VPTrees.add(newVPTree);
			Lnew.clear();
		}
	}
	
	/**
	 * Remove out of scope VP Tree from piece window
	 */
	private void removeLold()
	{
		LocalCluster clb = (LocalCluster) currentLeftBin.get(0);

		if (startStream < clb.getRightBinEnd()) 
		{
			clb.setLeftBinStart(startStream + 1);
		}

		if (startStream == clb.getRightBinEnd()) 
		{
			currentLeftBin.remove(0);
			Lold.remove(0);
		}
	}
	
	/**
	 * Search right sliding window if necessary
	 */
	private void searchRightWindow()
	{
		LocalCluster Pold = (LocalCluster) currentLeftBin.get(0);

		if (Pold.getSearchedRight() == false) 
		{
			findNeighbors(Pold);
			Pold.setSearchRight();

			int rightMost = 0;
			if (Lnew.size() != 0) 
			{
				LocalCluster rlc = (LocalCluster) Lnew.get(Lnew.size() - 1);
				rightMost = rlc.getRightBinEnd();
			} 
			else
			{
				LocalCluster Pnew = 
					(LocalCluster) currentLeftBin.get(currentLeftBin.size() - 1);
				rightMost = Pnew.getRightBinEnd();
			}
			
			Pold.setRightMostSearch(rightMost);
		}
		
		if (LoadData.trajectoryStream[startStream].getNeighborCount() 
						< Parameters.K)
		{
			for (int i = Pold.getRightMostSearch()+1; 
										i <= endStream; i++)
			{		
				if (Math.abs(startStream - i) 
							>= Parameters.baseWindowLength)
				{
					/**
					 * find distance between new point and current pivot
					 */
					;
					float dist = ComputeStats.findBaseWindowDistance
														(startStream, i);
				
					if (dist < Parameters.K)
					{
						LoadData.trajectoryStream[startStream].incNeighborCount(1);
					}
				}
			}
		}	
	}
	
	/**
	 * @param C1 - Find spatial neighbors of new cluster C1 using VPTree
	 */
	private void findNeighbors(LocalCluster C1) 
	{
		/**
		 * check if neighbor count of all points current left bin
		 * stop joining C cluster with other clusters if all points
		 * neighbor count has become greater than or 
		 * equal to neighbor threshold
		 */
		
		boolean flag = true;
		for (int lbs = C1.getLeftBinStart(); 
								lbs <= C1.getRightBinEnd(); lbs++)
		{
			if (LoadData.trajectoryStream[lbs].getNeighborCount() 
							< Parameters.K) 
			{
				flag = false;
				break;
			}
		}	
		if (flag == true) 
		{
			return;
		}
					
		ArrayList<LocalCluster> Pnew = new ArrayList<LocalCluster>();

		for (int i = 0; i < VPTrees.size(); i++) 
		{
			PiecewiseVPTree vptree = (PiecewiseVPTree) VPTrees.get(i);
			
			/**
			 * Find spatial neighbors of new cluster C1 using VPTree
			 */
			if (vptree != null) 
			{
				vptree.findNeighbors(vptree.getRoot(), Pnew, C1, pivotRadius);
				
				/**
				 * check if neighbor count of all points current left bin
				 * stop joining C cluster with other clusters if all points
				 * neighbor count has become greater than or equal 
				 * to neighbor threshold
				 */
				flag = true;
				for (int lbs = C1.getLeftBinStart(); 
								lbs <= C1.getRightBinEnd(); lbs++)
				{
					if (LoadData.trajectoryStream[lbs].getNeighborCount() 
														< Parameters.K) 
					{
						flag = false;
						break;
					}
				}	
				if (flag == true) 
				{
					return;
				}
				
				if (i % 2 == 0) 
				{
					clusterJoin(C1, Pnew);
					Pnew.clear();
				}
			}
		}

		/**
		 * join New cluster with New Candidate Clusters 
		 */
		clusterJoin(C1, Lnew);
		
		/**
		 * join New cluster with Old Candidate Clusters 
		 */
		clusterJoin(C1, Lold);
	}

	/**
	 * join cluster C with precedent clusters in current left bin 
	 * until neighbor count of all points in cluster becomes 
	 * equal to neighbor threshold
	 * @param C - Local Cluster
	 * @param tempALC - Candidate Clusters to be joined
	 */
	private void clusterJoin(LocalCluster C, ArrayList<LocalCluster> tempALC) 
	{
		
		float dist[] = new float[tempALC.size()];
		LocalCluster localclus[] = new LocalCluster[tempALC.size()];
		
		/**
		 * Compute distance between Comparing Cluster
		 * and Query Clusters
		 */
		for (int i = 0; i < tempALC.size(); i++)
		{
			localclus[i] = (LocalCluster) tempALC.get(i);
			
			
			dist[i] = ComputeStats.findBaseWindowDistance(C.getPivot(), 
					localclus[i].getPivot());
		}
			
		for(int c = 0; c < tempALC.size()-1 ; c++)
		{
			for(int d = 0; d < tempALC.size()-1-c ; d++)
			{
				if(dist [d] > dist [d+1])
				{
					float t = dist[d];
					dist[d] = dist[d+1];
					dist[d+1] = t;
					LocalCluster tlc = localclus[d];
					localclus[d] = localclus[d+1];
					localclus[d+1] = tlc;		
				}
			}
		}	
		
		/**
		 * join Comparing cluster with query clusters
		 */
		for (int i = 0; i < localclus.length; i++) 
		{
			LocalCluster Q =  localclus[i];
			
			if(C.getPivot() != Q.getPivot())
			{
				NoOfDistComps = NoOfDistComps + LocalCluster.joinClusters(C, Q);
			}
			
			/**
			 * check if neighbor count of all points current left bin
			 * stop joining C cluster with other clusters if all points
			 * neighbor count has become greater than 
			 * or equal to neighbor threshold
			 */
			boolean flag = true;
			for (int lbs = C.getLeftBinStart(); 
					lbs <= C.getRightBinEnd(); lbs++)
			{
				if (LoadData.trajectoryStream[lbs].getNeighborCount() 
														< Parameters.K) 
				{
					flag = false;
					break;
				}
			}	
			if (flag == true) 
			{
				return;
			}
		}
	}
}
