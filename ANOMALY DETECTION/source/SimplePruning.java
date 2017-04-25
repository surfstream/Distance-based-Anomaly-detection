package source;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Detection of Anomalies using 
 * Simple Pruning Technique
 */
public class SimplePruning 
{
	/**
	 * total number of distance computations performed
	 */
	public long NoOfDistComps = 0;
	
	/**
	 * number of anomalies in trajectory stream
	 */
	private int anomalyCount = 0;
	
	/**
	 * default constructor
	 * loads dataset to simulate trajectory stream
	 */
	
	/**
	 * recent anomaly window
	 */
	private int currentAnomaly = -1;
	
	
	public SimplePruning() 
	{		
		LoadData.loadDataset();
		         
	 	//Parameters.D = ComputeStats.findDiameterofDataset();
	 	//StartDetection.t2.setText(Float.toString(Parameters.D));
	 	//StartDetection.ta.append("D: " + Parameters.D + "\n");
	 	//System.out.println("D: " + Parameters.D + "\n");
	}

	/**
	 * detect anomalies in trajectory stream 
	 * using Simple Pruning process
	 */
	public void detectAnomalies() 
	{
		/**
		 * set the initial and ending trajectory points for processing
		 */
		int startStream = Parameters.baseWindowLength - 1;
		int endStream = Parameters.bufferSize - 1;
		
		long timeStart = System.currentTimeMillis();
		
		for (int beg = startStream; beg <= endStream; beg++) 
		{		
			if(isBWAnomaly(beg)) 
			{
				currentAnomaly = beg;
				anomalyCount++;
				LoadData.trajectoryStream[beg].setIsAnomaly();
				StartDetection.ta.append("Anomaly " + anomalyCount 
						+ " - Base Window ending at " + beg + "\n");
				
				StartDetection.ta.update(StartDetection.ta.getGraphics());
				
				System.out.println("Anomaly " + anomalyCount 
							+ " - Base Window ending at " + beg +
							"\t" + LoadData.trajectoryStream[beg].getNeighborCount());
			}
			//else
				//System.out.println("Not Anomaly \n");
		}
		
		/**
		 * total time taken to detect anomalies
		 */
		long timeEnd = System.currentTimeMillis();
		StartDetection.ta.append("Time taken to Detect Anomalies = " 
												+ (timeEnd - timeStart) + "\n");
		System.out.println("Time taken to Detect Anomalies = " 
													+ (timeEnd - timeStart));
		
		/**
		 * total no. of anomalies detected
		 */
		StartDetection.ta.append("Number of Anomalies Detected = " + anomalyCount + "\n");
		System.out.println("Number of Anomalies Detected = " + anomalyCount);
		
		/**
		 * total no. of distance computations
		 */
		StartDetection.ta.append("Total Number of Distance Computations = " + NoOfDistComps + "\n");
		System.out.println("Total Number of Distance Computations = " + NoOfDistComps);
	}

	/**
	 * Find if current pivot is an anomaly
	 * @param BWEnd - current pivot
	 * @return - is current pivot anomaly
	 */
	public boolean isBWAnomaly(int BWEnd) 
	{
		
		/**
		 * find number of neighbors in Left and Right Sliding Windows
		 * of Base Window ending at BWEnd
		 */
		int nn = findNeighbors(BWEnd);
		
		/**
		 * set the neighbor count of Base Window to the 
		 * ending trajectory point of Base Window
		 */
		LoadData.trajectoryStream[BWEnd].setNeighborCount(nn);
		
		/**
		 * if number of neighbors are greater than neighbor threshold
		 * BWEnd is not an anomaly
		 */
		if(nn >= Parameters.K)
		{
			return false;
		}
		
		/**
		 * if number of neighbors are less than neighbor threshold
		 * BWEnd might be an anomaly
		 */
		/*if(nn < Parameters.K)
		{
			for (int i = BWEnd - 1 ; i > BWEnd - Parameters.baseWindowLength ; i--)
			{
				if (LoadData.trajectoryStream[i].getIsAnomaly())
				{
					return false;
				}
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
	 * find number of neighbors for BWEnd in left and right sliding windows
	 * @param BWEnd - Base window ending at BWEnd
	 * @return - number of neighbors
	 */
	public int findNeighbors(int BWEnd)
	{
		int ncount = 0;
		float dist = 0;
		int BWBeg = BWEnd - Parameters.baseWindowLength + 1;
		
		/**
		 * determine if there is left window for base window
		 */
		if(BWBeg >= Parameters.leftSlidingWindowSize)
		{	
			/**
			 * initialize Left Sliding Window Boundaries
			 */
			int LSWStart = BWEnd - Parameters.leftSlidingWindowSize;
			int LSWStop = BWEnd - 1;
			
		
			/**
			 * find neighbors of Base Window in Left Sliding Window
			 */
			for( ; LSWStart <= LSWStop ; LSWStart++, NoOfDistComps++) 
			{ 
				dist = ComputeStats.findBaseWindowDistance(LSWStart, BWEnd);
				
				if(dist <= Parameters.D) 
				{
					ncount++;
				}
				/**
				 * return if number of neighbors has reached
				 * the neighbor threshold count
				 */
				if(ncount == Parameters.K) 
				{
					return ncount;
				}
			}
		}
		
		/**
		 * Determine if there is right window for base window
		 */
		if(BWEnd + Parameters.rightSlidingWindowSize
												<= Parameters.bufferSize) 
		{	
			/**
			 * Continue neighbor search in right sliding window
			 * if neighbor count of BWEnd in left window
			 * is less than neighbor threshold
			 */
			if(ncount < Parameters.K) 
			{
				/**
				 * initialize Right Sliding Window Boundaries
				 */
				
				int RSWStart = BWEnd + 1;
				int RSWStop = BWEnd + Parameters.rightSlidingWindowSize;
			
				/**
				 * find neighbors of BWEnd in Right Sliding Window
				 */
				for( ; RSWStart <= RSWStop ; RSWStart++, NoOfDistComps++) 
				{ 
					dist = ComputeStats.findBaseWindowDistance(RSWStart, BWEnd);
					
					if(dist <= Parameters.D) 
					{
						ncount++;
					}
					if(ncount == Parameters.K) 
					{
						return ncount;
					}
				}		
			}
		}
		return ncount;
	}
}
