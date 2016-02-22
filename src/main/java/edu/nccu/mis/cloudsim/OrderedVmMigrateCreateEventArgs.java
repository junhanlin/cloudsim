package edu.nccu.mis.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.VmAllocationPolicy;

public class OrderedVmMigrateCreateEventArgs
{
	private ProfiledVM vm;
	private List<Integer> freePes;
	private List<RankedHost> hostList;
	private int lastCheckIdx=-1;
	private Host allocatedHost;
	private FreePePolicy freePePolicy;
	private VmMigrateForVmEventArgs vmMigrateForVmEventArgs;
	
	public OrderedVmMigrateCreateEventArgs(ProfiledVM vm,List<Integer> freePesList,List<RankedHost> hostList,FreePePolicy freePePolicy)
	{
		this.vm=vm;
		this.freePes = freePesList;
		this.hostList = hostList;
		this.allocatedHost=null;
		this.freePePolicy = freePePolicy;
		this.lastCheckIdx=-1;
	}
	public VmMigrateForVmEventArgs getVmMigrateForVmEventArgs()
	{
		return vmMigrateForVmEventArgs;
	}
	public void setVmMigrateForVmEventArgs(
			VmMigrateForVmEventArgs vmMigrateForVmEventArgs)
	{
		this.vmMigrateForVmEventArgs = vmMigrateForVmEventArgs;
	}
	public void initVmMigrateForVmEventArgs(List<ProfiledVM> migrateOutVms)
	{
		this.vmMigrateForVmEventArgs = new VmMigrateForVmEventArgs(vm,migrateOutVms);
		
	}
	public boolean hasCheckAllHosts()
	{
		
		for(Integer pPe :freePes)
		{
			if(pPe !=-1)
			{
				return false;
			}
		}
		return true;
	}
	public RankedHost getDistributableHost(VMType vmType)
			throws Exception
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
				pHost = (RankedHost) getHostList().get(i);
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
				if (pPe >= lastFreePe)
				{
					bestHost = pHost;
				}
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
		if (bestHost != null)
		{
			return bestHost;
		} else if (niceHost != null)
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
	public ProfiledVM getVm()
	{
		return vm;
	}
	public void setVm(ProfiledVM vm)
	{
		this.vm = vm;
	}
	public List<Integer> getFreePes()
	{
		return freePes;
	}
	public void setFreePes(List<Integer> freePes)
	{
		this.freePes = freePes;
	}
	public List<RankedHost> getHostList()
	{
		return hostList;
	}
	public void setHostList(List<RankedHost> hostList)
	{
		this.hostList = hostList;
	}
	public Host getAllocatedHost()
	{
		return allocatedHost;
	}
	public void setAllocatedHost(Host result)
	{
		this.allocatedHost = result;
	}
	
	public int getLastCheckIdx()
	{
		return lastCheckIdx;
	}
	public void setLastCheckIdx(int lastCheckIdx)
	{
		this.lastCheckIdx = lastCheckIdx;
	}

}
