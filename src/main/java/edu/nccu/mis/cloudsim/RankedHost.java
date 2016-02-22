package edu.nccu.mis.cloudsim;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class RankedHost extends Host
{

	public final static int HIGH_PERFORMANCE_SCORE = 2;
	public final static int MEDIUM_PERFORMANCE_SCORE = 1;
	public final static int LOW_PERFORMANCE_SCORE = 0;
	private int netScore;
	private int ioScore;
	private int cpuScore;

	public RankedHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, int netScore, long storage,
			int ioScore, List<? extends Pe> peList, int cpuScore,
			VmScheduler vmScheduler)
	{
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);

		this.netScore = netScore;
		this.ioScore = ioScore;
		this.cpuScore = cpuScore;
	}

	/**
	 * Checks if is suitable for vm.
	 * 
	 * @param vm
	 *            the vm
	 * @return true, if is suitable for vm
	 */
	public boolean isSuitableForVm(Vm vm)
	{
		return getVmScheduler().getAvailableMips() >= vm
				.getCurrentRequestedTotalMips()
				&& getRamProvisioner().getAvailableRam() >= vm
						.getCurrentRequestedRam()
				&& getBwProvisioner().getAvailableBw() >= vm
						.getCurrentRequestedBw()
				&& getStorage() >= vm.getSize();
	}

	public boolean isSuitableAfterMigrateVm(Vm migrateInVm, Vm migrateOutVm)
	{
		return getVmScheduler().getAvailableMips()
				+ migrateOutVm.getCurrentRequestedTotalMips() >= migrateInVm
					.getCurrentRequestedTotalMips()
				&& getRamProvisioner().getAvailableRam()
						+ migrateOutVm.getCurrentAllocatedRam() >= migrateInVm
							.getCurrentRequestedRam()
				&& getBwProvisioner().getAvailableBw()
						+ migrateOutVm.getCurrentAllocatedBw() >= migrateInVm
							.getCurrentRequestedBw()
				&& getStorage() + migrateOutVm.getSize() >= migrateInVm
						.getSize();
	}

	public boolean isSuitableAfterMigrateVms(Vm migrateInVm,
			List<ProfiledVM> migrateOutVms)
	{
		int totalFreedMips = 0;
		int totalFreedRam = 0;
		int totalFreedBw = 0;
		int totalFreedStorage = 0;
		for (Vm pVm : migrateOutVms)
		{
			pVm.setBeingInstantiated(true);//tmp set this as true so that we can getCurrentRequestedTotalMips()
			totalFreedMips += pVm.getCurrentRequestedTotalMips();
			totalFreedRam += pVm.getCurrentAllocatedRam();
			totalFreedBw += pVm.getCurrentAllocatedBw();
			totalFreedStorage += pVm.getSize();
			pVm.setBeingInstantiated(false);
		}
		return getVmScheduler().getAvailableMips() + totalFreedMips >= migrateInVm
				.getCurrentRequestedTotalMips()
				&& getRamProvisioner().getAvailableRam() + totalFreedRam >= migrateInVm
						.getCurrentRequestedRam()
				&& getBwProvisioner().getAvailableBw() + totalFreedBw >= migrateInVm
						.getCurrentRequestedBw()
				&& getStorage() + totalFreedStorage >= migrateInVm.getSize();
	}

	public int getNetScore()
	{
		return netScore;
	}

	public void setNetScore(int netScore)
	{
		this.netScore = netScore;
	}

	public int getIoScore()
	{
		return ioScore;
	}

	public void setIoScore(int ioScore)
	{
		this.ioScore = ioScore;
	}

	public int getCpuScore()
	{
		return cpuScore;
	}

	public void setCpuScore(int cpuScore)
	{
		this.cpuScore = cpuScore;
	}

	public static <T extends VmScheduler> RankedHost genDefaultRankedHost(int id,int cpuScore,int ioScore,int netScore,Class<T> vmSchedulerClass)
	{
		int pes=0;
		int mips=0;
		int ram=0;
		int storage=0;
		int bw=0;
		if(cpuScore==LOW_PERFORMANCE_SCORE && ioScore == MEDIUM_PERFORMANCE_SCORE && netScore==HIGH_PERFORMANCE_SCORE)
		{
			pes= 4;
			mips=5000;
			ram=32*1000;
			storage=900*1000;
			bw=1000*1000;
			
		}
		else if(cpuScore==HIGH_PERFORMANCE_SCORE && ioScore == LOW_PERFORMANCE_SCORE && netScore==MEDIUM_PERFORMANCE_SCORE)
		{
			pes= 2;
			mips=15000;
			ram=32*1000;
			storage=700*1000;
			bw=500*1000;
		}
		else if(cpuScore==MEDIUM_PERFORMANCE_SCORE && ioScore == HIGH_PERFORMANCE_SCORE && netScore==LOW_PERFORMANCE_SCORE)
		{
			pes= 2;
			mips=10000;
			ram=32*1000;
			storage=5*1000*1000;
			bw=100*1000;
		}
		else if(cpuScore==HIGH_PERFORMANCE_SCORE && ioScore == HIGH_PERFORMANCE_SCORE && netScore==LOW_PERFORMANCE_SCORE)
		{
			pes= 4;
			mips=14000;
			ram=32*1000;
			storage=5*1000*1000;
			bw=100*1000;
		}
		else if(cpuScore==MEDIUM_PERFORMANCE_SCORE && ioScore == HIGH_PERFORMANCE_SCORE && netScore==MEDIUM_PERFORMANCE_SCORE)
		{
			pes= 4;
			mips=10000;
			ram=16*1000;
			storage=5*1000*1000;
			bw=500*1000;
		}
		else 
		{
			throw new IllegalArgumentException("The default RankedHost of combination {cupScore:"+cpuScore+",ioScore:"+ioScore+",netScore:"+netScore+"} is undefined");
		}
		List<Pe> peList = new ArrayList<Pe>();
		for(int i=0;i<pes;i++)
		{
			peList.add(new Pe(i,new PeProvisionerSimple(mips)));
		}
		
		VmScheduler vmScheduler=null;
		try
		{
			Constructor<?>[] vmSchedulerConstructors = vmSchedulerClass.getConstructors();
			for(Constructor<?> constructor:vmSchedulerConstructors) 
			{
			      Class<?>[] params = constructor.getParameterTypes();
			      if(params.length == 1 && params[0].isAssignableFrom(ArrayList.class)) 
	    		  {
			    	  vmScheduler = (VmScheduler)constructor.newInstance(peList);
			      }
			}
			
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new RankedHost(id, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), netScore, storage, ioScore, peList, cpuScore, vmScheduler);
	}
}
