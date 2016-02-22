package edu.nccu.mis.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class VmMigrateForVmEventArgs
{
	
	private ProfiledVM migrateInVm;
	private RankedHost host;
	private List<VmMigrateCreateEventArgs> vmMigrateCreateEventArgsList;
	private int currMigrateIdx;
	private boolean isAllMigrateSuccess;
	


	public boolean isAllMigrateSuccess()
	{
		return isAllMigrateSuccess;
	}
	public void setAllMigrateSuccess(boolean isAllMigrateSuccess)
	{
		this.isAllMigrateSuccess = isAllMigrateSuccess;
	}
	public VmMigrateForVmEventArgs(ProfiledVM migrateInVm, List<ProfiledVM> migrateOutVms)
	{
		this.host = (RankedHost)migrateOutVms.get(0).getHost();
		this.migrateInVm = migrateInVm;
		this.currMigrateIdx=0;
		this.vmMigrateCreateEventArgsList = new ArrayList<VmMigrateCreateEventArgs>();
		for(ProfiledVM pVm : migrateOutVms)
		{
			getVmMigrateCreateEventArgsList().add(new VmMigrateCreateEventArgs(pVm));
		}
		this.isAllMigrateSuccess=false;
	}
	public int getCurrMigrateIdx()
	{
		return currMigrateIdx;
	}


	public void setCurrMigrateIdx(int currMigrateIdx)
	{
		this.currMigrateIdx = currMigrateIdx;
	}
	
	public List<VmMigrateCreateEventArgs> getVmMigrateCreateEventArgsList()
	{
		return vmMigrateCreateEventArgsList;
	}


	public void setVmMigrateCreateEventArgsList(
			List<VmMigrateCreateEventArgs> vmMigrateCreateEventArgsList)
	{
		this.vmMigrateCreateEventArgsList = vmMigrateCreateEventArgsList;
	}


	public ProfiledVM getMigrateInVm()
	{
		return migrateInVm;
	}
	public void setMigrateInVm(ProfiledVM migrateInVm)
	{
		this.migrateInVm = migrateInVm;
	}
	
	public RankedHost getHost()
	{
		return host;
	}
	public void setHost(RankedHost host)
	{
		this.host = host;
	}
	
	

}
