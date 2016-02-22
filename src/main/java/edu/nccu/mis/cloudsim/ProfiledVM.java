package edu.nccu.mis.cloudsim;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

public class ProfiledVM extends Vm
{
	private VMType vmType;
	private VMOrder vmOrder;

	

	public ProfiledVM(int id, int userId, double mips, int numberOfPes,
			int ram, long bw, long size, VMType vmType, VMOrder vmOrder,
			String vmm, CloudletScheduler cloudletScheduler)
	{

		super(id, userId, mips, numberOfPes, ram, bw, size, vmm,
				cloudletScheduler);
		this.vmType = vmType;
		this.vmOrder = vmOrder;

	}
	public VMType getVmType()
	{
		return vmType;
	}

	public void setVmType(VMType vmType)
	{
		this.vmType = vmType;
	}

	public VMOrder getVmOrder()
	{
		return vmOrder;
	}

	public void setVmOrder(VMOrder vmOrder)
	{
		this.vmOrder = vmOrder;
	}
	public static ProfiledVM genDefaultProfiledVM(int id,int userId,String vmm,VMType vmType, VMOrder vmOrder,CloudletScheduler cloudletScheduler)
	{
		switch (vmType)
		{
			case CPU:
				return new ProfiledVM(id, userId, 2000, 2, 1024, 1000, 40000, vmType, vmOrder, vmm, cloudletScheduler);
				
			case IO:

				return new ProfiledVM(id, userId, 550, 1, 1024, 1000, 100000, vmType, vmOrder, vmm, cloudletScheduler);
			case NET:
				return new ProfiledVM(id, userId, 500, 1, 512, 10000, 30000, vmType, vmOrder, vmm, cloudletScheduler);
				
			default:
				break;
		}
		throw new IllegalArgumentException("Illegal vmType:" + vmType.toString()+" ,since this type of VM did not have default configuration");
	}

}
