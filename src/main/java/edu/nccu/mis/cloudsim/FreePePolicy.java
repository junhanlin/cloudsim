package edu.nccu.mis.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Host;

public interface FreePePolicy
{
	public RankedHost getDistributableHost(VMType vmType,List<RankedHost> hostList,List<Integer> freePes)
			throws Exception;
}
