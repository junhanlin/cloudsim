package edu.nccu.mis.cloudsim;

import java.util.List;

public class FreePeFirstFitPolicy implements FreePePolicy
{

	public FreePeFirstFitPolicy()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public RankedHost getDistributableHost(VMType vmType,List<RankedHost> hostList,List<Integer> freePes) throws Exception
	{
		int lastFreePe = Integer.MIN_VALUE;
		RankedHost bestHost = null;
		RankedHost niceHost = null;
		RankedHost sosoHost = null;
		for (int i = 0; i < freePes.size(); i++)
		{

			int pPe = freePes.get(i);
			RankedHost pHost = null;

			if (pPe == -1)
			{
				// host has been marked as checked
				continue;
			}

			/* cast host into RankedHost */
			try
			{
				pHost = hostList.get(i);
			} catch (ClassCastException e)
			{
				e.initCause(new Exception(
						"Cannot Perform FreePe Policy on Non-RankedHost"));
				throw e;
			}

			/* get the score of vmType */
			float performanceScore = -1;

			switch (vmType)
			{
				case CPU:
					performanceScore = pHost.getCpuScore();
					break;
				case IO:
					performanceScore = pHost.getIoScore();
					break;
				case NET:
					performanceScore = pHost.getNetScore();
					break;

				default:
					// Unreachable code
					break;
			}

			if (performanceScore == RankedHost.HIGH_PERFORMANCE_SCORE)
			{
				return pHost;
				
			} else if (performanceScore == RankedHost.MEDIUM_PERFORMANCE_SCORE)
			{
				if (pPe >= lastFreePe)
				{
					niceHost = pHost;
				}
			} else if (performanceScore == RankedHost.LOW_PERFORMANCE_SCORE)
			{
				if (pPe >= lastFreePe)
				{
					sosoHost = pHost;
				}
			} else
			{
				throw new Exception("Unknown Performace Score:"
						+ performanceScore);
			}
			lastFreePe = pPe;

		}

		/* return allocatable host id */
		if (niceHost != null)
		{
			return niceHost;
		} else if (sosoHost != null)
		{
			return sosoHost;
		} else
		{
			return null;
		}
	}

}
