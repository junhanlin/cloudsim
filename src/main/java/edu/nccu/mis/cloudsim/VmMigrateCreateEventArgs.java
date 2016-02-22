package edu.nccu.mis.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

public class VmMigrateCreateEventArgs
{
	private boolean isSuccess;
	private ProfiledVM vm;
	
	private Host oriHost;
	private OrderedVmMigrateCreateEventArgs orderedVmMigrateCreateEArgs;
	public VmMigrateCreateEventArgs(ProfiledVM vm)
	{
		this.isSuccess=false;
		this.vm=vm;
		this.oriHost = vm.getHost();
		
	}

	public void initOrderedVmMigrateCreateEArgs(List<Integer> freePes,List<RankedHost> hostList,FreePePolicy freePePolicy)
	{
		this.orderedVmMigrateCreateEArgs = new OrderedVmMigrateCreateEventArgs(vm, freePes, hostList, freePePolicy);
	}
	
	public Host getOriHost()
	{
		return oriHost;
	}

	public void setOriHost(Host oriHost)
	{
		this.oriHost = oriHost;
	}

	public OrderedVmMigrateCreateEventArgs getOrderedVmMigrateCreateEArgs()
	{
		return orderedVmMigrateCreateEArgs;
	}

	public void setOrderedVmMigrateCreateEArgs(
			OrderedVmMigrateCreateEventArgs orderedVmMigrateCreateEArgs)
	{
		this.orderedVmMigrateCreateEArgs = orderedVmMigrateCreateEArgs;
	}

	public boolean isSuccess()
	{
		return orderedVmMigrateCreateEArgs!=null && orderedVmMigrateCreateEArgs.getAllocatedHost()!=null;
	}

	

	public Vm getVm()
	{
		return vm;
	}

	public void setVm(ProfiledVM vm)
	{
		this.vm = vm;
	}

	public Host getAllocatedHost()
	{
		if(orderedVmMigrateCreateEArgs!=null && orderedVmMigrateCreateEArgs.getAllocatedHost()!=null)
		{
			return orderedVmMigrateCreateEArgs.getAllocatedHost();
		}
		return null;
	}

}
