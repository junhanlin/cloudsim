package edu.nccu.mis.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.VmAllocationPolicy;

public class OrderedVmCreateEventArgs
{
	private ProfiledVM vm;
	private List<Integer> freePes;
	private List<RankedHost> hostList;
	private int lastCheckIdx=-1;
	private Host allocatedHost;
	private FreePePolicy freePePolicy;
	private VmMigrateForVmEventArgs vmMigrateForVmEventArgs;
	
	public OrderedVmCreateEventArgs(ProfiledVM vm,List<Integer> freePesList,List<RankedHost> hostList,FreePePolicy freePePolicy)
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
		return freePePolicy.getDistributableHost(vmType, getHostList(), getFreePes());

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
