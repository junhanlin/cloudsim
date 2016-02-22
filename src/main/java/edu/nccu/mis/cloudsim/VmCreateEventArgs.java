package edu.nccu.mis.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

public class VmCreateEventArgs
{
	
	private ProfiledVM vm;
	
	private OrderedVmCreateEventArgs orderedVmCreateEArgs;
	private boolean shouldAck=true;
	public VmCreateEventArgs(ProfiledVM vm,boolean shouldAck)
	{
		
		this.vm=vm;
		this.shouldAck=shouldAck;
	}

	public void initOrderedVmCreateEArgs(List<Integer> freePes,List<RankedHost> hostList,FreePePolicy freePePolicy)
	{
		this.orderedVmCreateEArgs = new OrderedVmCreateEventArgs(vm, freePes, hostList, freePePolicy);
	}
	
	public boolean shouldAck()
	{
		return shouldAck;
	}

	public void setShouldAck(boolean shouldAck)
	{
		this.shouldAck = shouldAck;
	}

	public OrderedVmCreateEventArgs getOrderedVmCreateEArgs()
	{
		return orderedVmCreateEArgs;
	}

	public void setOrderedVmCreateEArgs(
			OrderedVmCreateEventArgs orderedVmCreateEArgs)
	{
		this.orderedVmCreateEArgs = orderedVmCreateEArgs;
	}

	public boolean isSuccess()
	{
		return orderedVmCreateEArgs!=null && orderedVmCreateEArgs.getAllocatedHost()!=null;
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
		if(orderedVmCreateEArgs!=null && orderedVmCreateEArgs.getAllocatedHost()!=null)
		{
			return orderedVmCreateEArgs.getAllocatedHost();
		}
		return null;
	}

	
	

}
