package source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * methods to load data set 
 * to simulate trajectory data stream
 *
 */
public class LoadData 
{
	/**
	 * buffer to hold trajectory points
	 */
	public static TrajectoryPoint[] trajectoryStream; 

	/**
	 * size of data set
	 */
	public static int attribs = 0, tuples = 0;

	/**
	 * path of data set
	 */
	public static String datasetPath = null;
	
	/**
	 * Default Constructor
	 */
	public LoadData() 
	{
		// Default Constructor
	}
	
	/**
	 * load data set in to buffer
	 */
	public static void loadDataset() 
	{
		
		try 
		{
			attribs = tuples = 0;
			String l = "";
			BufferedReader br;
			float[][] dataset;
			
			br = new BufferedReader(new InputStreamReader
					(new FileInputStream(datasetPath)));

			/**
			 * determine the number of attributes and records in data set
			 */
			for ( ; (l = br.readLine()) != null ; tuples++) 
			{
				if (tuples == 0) 
				{
					attribs = l.split(" ").length;
				}
			}
			System.out.println("Data Set has " + attribs + " attributes, " 
									+ tuples + " instances" + "\n");
			
			//StartDetection.ta.append("\nData Set has " + attribs 
				//	+ " attributes, " + tuples + " instances" + "\n");
			//StartDetection.ta.update(StartDetection.ta.getGraphics());
 
			/**
			 * create array to load data set
			 */
			dataset = new float[tuples][attribs];

			/**
			 * load data set into temporary array
			 */
			br.close();
			br = new BufferedReader(new InputStreamReader
							(new FileInputStream(datasetPath)));

			float avg[] = new float[attribs];
			for(int i = 0 ; i < attribs ; i++) 
			{
				avg[i] = 0;
			}
				
			for( int t = 0 ; (l = br.readLine()) != null ; t++)
			{
				String[] tuple = l.split(" ");
				for (int a = 0; a < attribs; a++) 
				{
					dataset[t][a] = Float.parseFloat(tuple[a]);
					avg[a] = avg[a] + dataset[t][a];
				}
			}
	
			/**
			 * normalize data set
			 */
			for (int a = 0; a < attribs; a++) 
			{
				avg[a] = avg[a] / (tuples - 1);
				
				float squaredDev[] = new float[attribs];
				for(int i = 0 ; i < attribs ; i++)
				{
					squaredDev[i] = 0;
				}				
				for (int t = 0; t <tuples; t++)
				{
					squaredDev[a] = squaredDev[a] + 
								(dataset[t][a]-avg[a]) * (dataset[t][a]-avg[a]) ;
				}
				squaredDev[a] = (float) Math.sqrt((float) squaredDev[a] / (tuples - 1));
				for (int t = 0; t < tuples; t++) 
				{
					dataset[t][a] = (dataset[t][a] - avg[a]) / squaredDev[a];
				}
			}
						
			/**
			 * simulate Trajectory Stream
			 */
			LoadData.trajectoryStream = new TrajectoryPoint[Parameters.bufferSize];

			for (int i = 0; i < LoadData.trajectoryStream.length; i++) 
			{
					float[] tuple = new float[attribs];
					for (int y = 0; y < attribs; y++) 
					{
						tuple[y] = dataset[i % dataset.length][y] 
									+ (float) Math.random() / 10000;
					}
					LoadData.trajectoryStream[i] = new TrajectoryPoint(tuple);
				}
			
			br.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		StartDetection.ta.append("\nTrajectory Data Stream is created" + "\n");
		StartDetection.ta.update(StartDetection.ta.getGraphics());
	}
}