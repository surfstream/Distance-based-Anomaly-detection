package source;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Detection of Anomalies using 
 * Batch Monitoring and Local Clustering Technique
 */

public class BatchMonitor
{
	/**
	 * total number of distance computations performed
	 */
	public static long NoOfDistComps = 0;
		
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
	private List <LocalCluster> currentLeftBin = new ArrayList<LocalCluster> (1000);
		
	/**
	 * index to trajectory stream beginning
	 */
	private int startStream = 0;

	/**
	 * index to trajectory stream ending
	 */
	private int endStream = 0;
	
	/**
	 * recent anomaly window
	 */
	private int currentAnomaly = -1;

	/**
	 * default constructor
	 * loads dataset to simulate trajectory stream
	 */
	public BatchMonitor() 
	{

		LoadData.loadDataset(); 
        
	 	//Parameters.D = ComputeStats.findDiameterofDataset();
	 	//StartDetection.t2.setText(Float.toString(Parameters.D));
	 	//StartDetection.ta.append("D: " + Parameters.D + "\n");
		//StartDetection.ta.update(StartDetection.ta.getGraphics());
	 	//System.out.println("D: " + Parameters.D + "\n");

		/**
		 *  max bin size of local cluster
		 */
		binSize = Parameters.baseWindowLength * 2;
		
		/**
		 *  max radius of local cluster
		 */
		radius = Parameters.D / 2;		
	}

	/**
	 * detect anomalies in trajectory stream 
	 * using Batch Monitoring process
	 */
	public void detectAnomalies() 
	{
		/**
		 * set the initial and ending trajectory points for processing
		 */
		startStream = Parameters.baseWindowLength - 1;
		endStream = Parameters.baseWindowLength + 
							Parameters.leftSlidingWindowSize - 1;
		
		/**
		 * initially current pivot found is null
		 */
		pCurrent = null;
		pivotFound = true;
		
		long timeStart = System.currentTimeMillis();
		
		/**
		 * compute initial local clusters
		 */
		for (int i = startStream; i <= endStream; i++) 
		{
			onlineLClustering(i);
		}		

		endStream++;
		
		for (int i = endStream; i < Parameters.bufferSize; i++, endStream++) 
		{	
			onlineLClustering(endStream);
							
			LocalCluster leftbinstart = (LocalCluster) currentLeftBin.get(0);
			
			if (LoadData.trajectoryStream[startStream].getNeighborCount() 
															< Parameters.K) 
			{
				if (!leftbinstart.getSearchedRight()) 
				{
					clusterJoin(leftbinstart);
					leftbinstart.setSearchRight();
				}
			}
		
			if (LoadData.trajectoryStream[startStream].getNeighborCount() 
															< Parameters.K) 
			{
				if(isBWAnomaly(startStream) == true) 
				{
					currentAnomaly = startStream;
					anomalyCount++;
					LoadData.trajectoryStream[startStream].setIsAnomaly();
					StartDetection.ta.append("Anomaly " + anomalyCount 
								+ " - Base Window ending at " + startStream + "\n");
					StartDetection.ta.update(StartDetection.ta.getGraphics());			
					System.out.println("Anomaly " + anomalyCount 
									+ " - Base Window ending at " + startStream +
									"\t" + LoadData.trajectoryStream[startStream].getNeighborCount());
				}
			}		

			/**
			 * slide left bin start
			 */
			if (startStream < leftbinstart.getRightBinEnd()) 
			{
				leftbinstart.setLeftBinStart(startStream + 1);
			}
			
			else if (startStream == leftbinstart.getRightBinEnd()) 
			{
				currentLeftBin.remove(0);
			}
			/**
			 * slide to next time tick
			 */
			startStream++;
		
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
		
		//System.out.println("C1 First = " + LocalCluster.C1FirstCount);
		//System.out.println("C2 First = " + LocalCluster.C2FirstCount);
	}
	
	/**
	 * Find if current pivot is an anomaly
	 * @param BWEnd - current pivot
	 * @return - is current pivot anomaly
	 */
	public boolean isBWAnomaly(int BWEnd) 
	{
		/*
		for (int i = BWEnd - 1 ; i > BWEnd - Parameters.baseWindowLength ; i--) 
		{
			if (LoadData.trajectoryStream[i].getIsAnomaly()) 
			{
				return false;
			}
		}
		return true;
		*/
		
		if(BWEnd <= 2*Parameters.baseWindowLength)
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
	private void onlineLClustering(int Bnew) 
	{
		
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
			 * if current pivot is available, 
			 */
			if (pCurrent != null)
			{
				start = pCurrent.getRightBinEnd() + 1;
			}

			boolean flag = true;
			for (int i = start; i < Bnew; i++) 
			{
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
			
			if (dist < radius && Bnew - point < binSize) 
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
				
				/**
				 * join current cluster with precedent clusters
				 */
				clusterJoin(pCurrent);
			
				/**
				 * reinitialize temporary variables
				 */
				pivotFound = true;
				currentRadius = 0;
			}
		}
	}

	/**
	 * join cluster C with precedent clusters in current left bin
	 * until neighbor count of all points in cluster becomes 
	 * equal to neighbor threshold
	 */
 
	private void clusterJoin(LocalCluster C) 
	{
		
		for (int i = 0; i < currentLeftBin.size(); i++) 
		{
			LocalCluster Q = (LocalCluster) currentLeftBin.get(i);
			if (C.getPivot() != Q.getPivot()) 
			{
				NoOfDistComps = NoOfDistComps + LocalCluster.joinClusters(C, Q);
			}
			
			/**
			 * check if neighbor count of all points current left bin
			 * stop joining C cluster with other clusters if all points
			 * neighbor count has become greater than or equal to neighbor threshold
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
