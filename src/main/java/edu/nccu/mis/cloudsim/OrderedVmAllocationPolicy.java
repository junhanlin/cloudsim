/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package edu.nccu.mis.cloudsim;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.Data;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host
 * for a VM, the host with less PEs in use.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class OrderedVmAllocationPolicy extends VmAllocationPolicy
{

    /** The vm table. */
    private Map<String, Host> vmTable;

    /** The used pes. */
    private Map<String, Integer> usedPes;

    /** The free pes. */
    private List<Integer> freePes;

    private FreePePolicy freePePolicy;

    public OrderedVmAllocationPolicy(List<? extends RankedHost> list, FreePePolicy freePePolicy)
    {
	super(list);
	setFreePes(new ArrayList<Integer>());
	for (Host host : getHostList())
	{
	    getFreePes().add(host.getNumberOfPes());

	}

	setVmTable(new HashMap<String, Host>());
	setUsedPes(new HashMap<String, Integer>());
	this.freePePolicy = freePePolicy;
    }

    public Datacenter getDatacenter()
    {
	if (getHostList().size() > 0)
	{
	    return getHostList().get(0).getDatacenter();
	}
	return null;
    }

    /**
     * Allocates a host for a given VM.
     * 
     * @param vm
     *            VM specification
     * @return $always return true
     * @pre $none
     * @post $none
     */

    public boolean allocateHostForVm(Vm vm, VmCreateEventArgs vmCreateEventArgs)
    {

	if (!(vm instanceof ProfiledVM))
	{
	    throw new IllegalArgumentException("Cannot Perform FreePe Policy on a Non-Profiled VM");

	}
	ProfiledVM profiledVM = (ProfiledVM) vm;

	boolean result = false;

	List<Integer> freePesTmp = new ArrayList<Integer>();
	for (Integer freePes : getFreePes())
	{
	    freePesTmp.add(freePes);
	}

	if (!getVmTable().containsKey(vm.getUid()))
	{
	    // if this vm was not created
	    vmCreateEventArgs.initOrderedVmCreateEArgs(freePesTmp, getHostList(), freePePolicy);

	    getDatacenter().scheduleNow(getDatacenter().getId(), CloudSimTags.ORDERED_VM_CREATE, vmCreateEventArgs);
	    result = true;// true mean has been scheduled into future queue

	}

	return result;
    }

    public boolean allocateHostForMigrateVm(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
	VmMigrateForVmEventArgs vmMigrateForVmEventArgs = orderedVmCreateEventArgs.getVmMigrateForVmEventArgs();
	VmMigrateCreateEventArgs vmMigrateCreateEventArgs = vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().get(vmMigrateForVmEventArgs.getCurrMigrateIdx());
	Vm vm = (ProfiledVM) vmMigrateCreateEventArgs.getVm();
	if (!(vm instanceof ProfiledVM))
	{
	    throw new IllegalArgumentException("Cannot Perform FreePe Policy on a Non-Profiled VM");

	}

	boolean result = false;

	List<Integer> freePesTmp = new ArrayList<Integer>();
	List<RankedHost> hostListTmp = new ArrayList<RankedHost>();
	for (int i = 0; i < getFreePes().size(); i++)
	{
	    if (getHostList().get(i) == vmMigrateCreateEventArgs.getOriHost())
	    {
		continue;
	    }
	    hostListTmp.add(getHostList().get(i));
	    freePesTmp.add(getFreePes().get(i));
	}

	vmMigrateCreateEventArgs.initOrderedVmMigrateCreateEArgs(freePesTmp, hostListTmp, freePePolicy);

	getDatacenter().scheduleNow(getDatacenter().getId(), CloudSimTags.ORDERED_VM_MIGRATE_CREATE, ev.getData());
	result = true;// true mean has been scheduled into future queue

	return result;
    }

    /**
     * Releases the host used by a VM.
     * 
     * @param vm
     *            the vm
     * @pre $none
     * @post none
     */
    @Override
    public void deallocateHostForVm(Vm vm)
    {
	Host host = getVmTable().remove(vm.getUid());
	int idx = getHostList().indexOf(host);
	int pes = getUsedPes().remove(vm.getUid());
	if (host != null)
	{
	    host.vmDestroy(vm);
	    getFreePes().set(idx, getFreePes().get(idx) + pes);
	}
    }

    /**
     * Gets the host that is executing the given VM belonging to the given user.
     * 
     * @param vm
     *            the vm
     * @return the Host with the given vmID and userID; $null if not found
     * @pre $none
     * @post $none
     */
    @Override
    public Host getHost(Vm vm)
    {
	return getVmTable().get(vm.getUid());
    }

    /**
     * Gets the host that is executing the given VM belonging to the given user.
     * 
     * @param vmId
     *            the vm id
     * @param userId
     *            the user id
     * @return the Host with the given vmID and userID; $null if not found
     * @pre $none
     * @post $none
     */
    @Override
    public Host getHost(int vmId, int userId)
    {
	return getVmTable().get(Vm.getUid(userId, vmId));
    }

    /**
     * Gets the vm table.
     * 
     * @return the vm table
     */
    public Map<String, Host> getVmTable()
    {
	return vmTable;
    }

    /**
     * Sets the vm table.
     * 
     * @param vmTable
     *            the vm table
     */
    protected void setVmTable(Map<String, Host> vmTable)
    {
	this.vmTable = vmTable;
    }

    /**
     * Gets the used pes.
     * 
     * @return the used pes
     */
    protected Map<String, Integer> getUsedPes()
    {
	return usedPes;
    }

    /**
     * Sets the used pes.
     * 
     * @param usedPes
     *            the used pes
     */
    protected void setUsedPes(Map<String, Integer> usedPes)
    {
	this.usedPes = usedPes;
    }

    /**
     * Gets the free pes.
     * 
     * @return the free pes
     */
    protected List<Integer> getFreePes()
    {
	return freePes;
    }

    /**
     * Sets the free pes.
     * 
     * @param freePes
     *            the new free pes
     */
    protected void setFreePes(List<Integer> freePes)
    {
	this.freePes = freePes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.VmAllocationPolicy#optimizeAllocation(double,
     * cloudsim.VmList, double)
     */
    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList)
    {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus
     * .cloudsim.Vm, org.cloudbus.cloudsim.Host)
     */
    @Override
    public boolean allocateHostForVm(Vm vm, Host host)
    {
	if (host.vmCreate(vm))
	{ // if vm has been succesfully created in the host
	    getVmTable().put(vm.getUid(), host);

	    int requiredPes = vm.getNumberOfPes();
	    int idx = getHostList().indexOf(host);
	    getUsedPes().put(vm.getUid(), requiredPes);
	    getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

	    Log.formatLine("%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(), CloudSim.clock());
	    return true;
	}

	return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm)
    {
	Log.formatLine("[ERROR]: Do not call VmAcclocationPolicyFreePe.allocateHostForVm(Vm vm);");
	return false;
    }

    public void processVmCreate(SimEvent ev, boolean ack)
    {
	Vm vm = (Vm) ev.getData();
	VmCreateEventArgs vmCreateEventArgs = new VmCreateEventArgs((ProfiledVM) vm, ack);
	boolean result = false;
	Log.formatLine("%.2f:[processVmCreate]: VM #%d",CloudSim.clock(), vm.getId());
	result = this.allocateHostForVm(vm, vmCreateEventArgs);

	if (!result)
	{
	    // can not allocate due to some exception
	    // this shouldn't happen
	    // vmCreateEventArgs.setSuccess(false);
	    sendToDatacenterNow(CloudSimTags.VM_CREATE_COMPLETE, vmCreateEventArgs);
	}

    }

    public void processVmCreateComplete(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();

	if (vmCreateEventArgs.isSuccess())
	{
	    Log.formatLine("%.2f:[processVmCreateComplte]: VM #%d has been allocate to Host #%d",CloudSim.clock(), vmCreateEventArgs.getVm().getId(), vmCreateEventArgs.getAllocatedHost().getId());
	}
	else
	{

	    OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();

	    if (!orderedVmCreateEventArgs.hasCheckAllHosts())
	    {
		sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE, ev.getData());
	    }
	    else
	    {
		Log.formatLine("%.2f:[processVmCreateComplte]: VM #%d fail to allocate",CloudSim.clock(), vmCreateEventArgs.getVm().getId());
	    }

	}

    }

    public void processVmMigrateCreate(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
	VmMigrateForVmEventArgs vmMigrateForVmEventArgs = orderedVmCreateEventArgs.getVmMigrateForVmEventArgs();
	VmMigrateCreateEventArgs vmMigrateCreateEventArgs = vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().get(vmMigrateForVmEventArgs.getCurrMigrateIdx());
	ProfiledVM profiledVM = (ProfiledVM) vmMigrateCreateEventArgs.getVm();
	boolean result = false;

	Log.formatLine("%.2f:[processVmMigrateCreate]: Try to create low order migrated VM #%d (for high order VM #%d)",CloudSim.clock(), profiledVM.getId(),
		vmCreateEventArgs.getVm().getId());
	result = allocateHostForMigrateVm(ev);

	if (!result)
	{
	    // vmMigrateCreateEventArgs.setSuccess(false);
	    sendToDatacenterNow(CloudSimTags.VM_MIGRATE_CREATE_COMPLETE, ev.getData());
	}

    }

    public void processVmMigrateCreateComplete(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
	VmMigrateForVmEventArgs vmMigrateForVmEventArgs = orderedVmCreateEventArgs.getVmMigrateForVmEventArgs();
	VmMigrateCreateEventArgs vmMigrateCreateEventArgs = vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().get(vmMigrateForVmEventArgs.getCurrMigrateIdx());
	OrderedVmMigrateCreateEventArgs orderedVmMigrateCreateEventArgs = vmMigrateCreateEventArgs.getOrderedVmMigrateCreateEArgs();
	if (!vmMigrateCreateEventArgs.isSuccess())
	{
	    Log.formatLine("%.2f:[processVmMigrateCreate]: Fail to create low order migrated VM #%d (for high order VM #%d)",CloudSim.clock(), vmMigrateCreateEventArgs.getVm().getId(),
		    vmCreateEventArgs.getVm().getId());
	    if (!orderedVmMigrateCreateEventArgs.hasCheckAllHosts())
	    {
		sendToDatacenterNow(CloudSimTags.ORDERED_VM_MIGRATE_CREATE, ev.getData());
	    }
	    else
	    {
		sendToDatacenterNow(CloudSimTags.VM_MIGRATE_FOR_VM_COMPLETE, vmCreateEventArgs);

	    }

	}
	else
	{

	    Log.formatLine("%.2f:[processVmCreateComplte]: Low order VM #%d has been  allocated to Host #%d",CloudSim.clock() ,vmMigrateCreateEventArgs.getVm().getId(),
		    vmMigrateCreateEventArgs.getAllocatedHost().getId());
	    sendToDatacenterNow(CloudSimTags.VM_MIGRATE_FOR_VM_COMPLETE, ev.getData());

	}

    }

    public void processOrderedVmCreate(SimEvent ev)
    {
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = ((VmCreateEventArgs) ev.getData()).getOrderedVmCreateEArgs();
	List<Integer> freePes = orderedVmCreateEventArgs.getFreePes();
	List<RankedHost> hostList = orderedVmCreateEventArgs.getHostList();
	ProfiledVM profiledVM = orderedVmCreateEventArgs.getVm();

	if (!orderedVmCreateEventArgs.hasCheckAllHosts())
	{

	    RankedHost host = null;
	    boolean result = false;
	    try
	    {

		host = orderedVmCreateEventArgs.getDistributableHost(profiledVM.getVmType());
	    }
	    catch (Exception e)
	    {

		orderedVmCreateEventArgs.setAllocatedHost(null);
		sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE_COMPLETE, ev.getData());
	    }

	    Log.formatLine("%.2f:[processOrderedVmCreate]: Try allocate VM #%d to Host #%d",CloudSim.clock(), profiledVM.getId(), host.getId());
	    int idx = orderedVmCreateEventArgs.getHostList().indexOf(host);
	    orderedVmCreateEventArgs.setLastCheckIdx(idx);
	    if (host.isSuitableForVm(profiledVM))
	    {
		result = host.vmCreate(profiledVM);

		if (result)
		{
		    // if vm were succesfully created in the host

		    orderedVmCreateEventArgs.setAllocatedHost(host);
		    getVmTable().put(profiledVM.getUid(), host);
		    getUsedPes().put(profiledVM.getUid(), profiledVM.getNumberOfPes());
		    getFreePes().set(idx, getFreePes().get(idx) - profiledVM.getNumberOfPes());

		    orderedVmCreateEventArgs.setAllocatedHost(host);
		    sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE_COMPLETE, ev.getData());

		}

	    }
	    else
	    {
		if (profiledVM.getVmOrder() == VMOrder.HIGH)
		{
		    // try release resource by migrate low order vms in the
		    // host
		    List<ProfiledVM> lowOrderVms = new ArrayList<ProfiledVM>();
		    for (Vm pVm : host.getVmList())
		    {
			ProfiledVM pProfiledVM = (ProfiledVM) pVm;
			if (pProfiledVM.getVmOrder() == VMOrder.LOW)
			{
			    lowOrderVms.add(pProfiledVM);
			}
		    }

		    List<ProfiledVM> removedVms = new ArrayList<ProfiledVM>();
		    while (removedVms.size() < lowOrderVms.size())
		    {
			removedVms.add(lowOrderVms.remove(0));

			if (host.isSuitableAfterMigrateVms(profiledVM, removedVms))
			{
			    orderedVmCreateEventArgs.initVmMigrateForVmEventArgs(removedVms);
			    Log.formatLine("%.2f:[processOrderedVmCreate]: Try  allocating VM(high) #%d to Host #%d by migrate %d low ordered VM",CloudSim.clock() ,profiledVM.getId(), host.getId(),
				    removedVms.size());
			    sendToDatacenterNow(CloudSimTags.VM_MIGRATE_FOR_VM, ev.getData());
			    return;
			}
		    }

		    // there is no low order vm or even if we migrate all low
		    // order vms, we still not getting enough resource
		    // orderedVmCreateEventArgs.getFreePes().set(idx, -1);
		    Log.formatLine("%.2f:[processOrderedVmCreate]: Fail to allocate VM(high) #%d to Host #%d, no low ordered VM is available for migration or migration is of no used",CloudSim.clock(),
			    profiledVM.getId(), host.getId());
		    sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE_COMPLETE, ev.getData());

		}
		else
		{

		    // this if just a low order VM
		    // mark allocation fail on this host
		    // orderedVmCreateEventArgs.getFreePes().set(idx, -1);
		    Log.formatLine("%.2f:[processOrderedVmCreateComplete]: Fail to allocate VM(low/mid) #%d to Host #%d",CloudSim.clock() ,profiledVM.getId(), host.getId());
		    sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE_COMPLETE, ev.getData());
		}

	    }
	}

    }

    public void processOrderedVmCreateComplete(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = (vmCreateEventArgs).getOrderedVmCreateEArgs();
	List<Integer> freePes = orderedVmCreateEventArgs.getFreePes();
	List<RankedHost> hostList = orderedVmCreateEventArgs.getHostList();
	ProfiledVM profiledVM = orderedVmCreateEventArgs.getVm();
	Host allocatedHost = orderedVmCreateEventArgs.getAllocatedHost();
	int lastCheckIdx = orderedVmCreateEventArgs.getLastCheckIdx();
	Host lastCheckHost = hostList.get(lastCheckIdx);
	if (allocatedHost != null)
	{
	    // create vm success

	    getDatacenter().getVmList().add(profiledVM);
	    if (profiledVM.isBeingInstantiated())
	    {
		profiledVM.setBeingInstantiated(false);
	    }
	    Log.formatLine("%.2f:[processOrderedVmCreateComplete]: Successfully allocate VM #%d to Host #%d",CloudSim.clock() ,profiledVM.getId(), lastCheckHost.getId());
	    sendToDatacenterNow(CloudSimTags.VM_CREATE_COMPLETE, vmCreateEventArgs);
	    profiledVM.updateVmProcessing(CloudSim.clock(), getHost(profiledVM).getVmScheduler().getAllocatedMipsForVm(profiledVM));
	}
	else
	{
	    Log.formatLine("[processOrderedVmCreateComplete]: Fail to allocate VM #%d to Host #%d", profiledVM.getId(), lastCheckHost.getId());
	    orderedVmCreateEventArgs.getFreePes().set(lastCheckIdx, -1);
	    sendToDatacenterNow(CloudSimTags.VM_CREATE_COMPLETE, ev.getData());
	}
    }

    public void processVmMigrateForVm(SimEvent ev)
    {

	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
	VmMigrateForVmEventArgs vmMigrateForVmEventArgs = orderedVmCreateEventArgs.getVmMigrateForVmEventArgs();
	ProfiledVM migrateInVm = vmMigrateForVmEventArgs.getMigrateInVm();

	VmMigrateCreateEventArgs vmMigrateCreateEventArgs = vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().get(vmMigrateForVmEventArgs.getCurrMigrateIdx());
	ProfiledVM migrateOutVm = (ProfiledVM) vmMigrateCreateEventArgs.getVm();
	RankedHost host = (RankedHost) vmMigrateForVmEventArgs.getHost();

	Log.formatLine("%.2f:[processVmMigrateForVm] Try migrate low order VM #%d out (for high order VM #%d)",CloudSim.clock(), migrateOutVm.getId(), migrateInVm.getId());

	if (!migrateOutVm.isBeingInstantiated())
	{
	    migrateOutVm.setBeingInstantiated(true);
	}
	sendToDatacenterNow(CloudSimTags.VM_MIGRATE_CREATE, ev.getData());

    }

    public void processVmMigrateForVmComplete(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
	VmMigrateForVmEventArgs vmMigrateForVmEventArgs = orderedVmCreateEventArgs.getVmMigrateForVmEventArgs();
	ProfiledVM migrateInVm = vmMigrateForVmEventArgs.getMigrateInVm();

	VmMigrateCreateEventArgs vmMigrateCreateEventArgs = vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().get(vmMigrateForVmEventArgs.getCurrMigrateIdx());
	ProfiledVM migrateOutVm = (ProfiledVM) vmMigrateCreateEventArgs.getVm();
	if (vmMigrateCreateEventArgs.isSuccess())
	{

	    vmMigrateForVmEventArgs.getHost().removeMigratingInVm(migrateOutVm);
	    // deallocateHostForVm(migrateOutVm);
	    Log.formatLine("%.2f:[processVmMigrateForVmComplete] Successfully migrate low order VM #%d out (for high order VM #%d)",CloudSim.clock(), migrateOutVm.getId(), migrateInVm.getId());
	    if (vmMigrateForVmEventArgs.getCurrMigrateIdx() < vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().size() - 1)
	    {
		// there are more low order vm to migrate
		sendToDatacenterNow(CloudSimTags.VM_MIGRATE_FOR_VM, ev.getData());
	    }
	    else
	    {
		// all low order has been migrated out
		vmMigrateForVmEventArgs.setAllMigrateSuccess(true);
		// create the high order VM here
		int lastCheckIdx = orderedVmCreateEventArgs.getLastCheckIdx();
		Host host = orderedVmCreateEventArgs.getHostList().get(lastCheckIdx);
		int idx = getHostList().indexOf(host);

		boolean result = false;
		if (host.isSuitableForVm(migrateInVm))
		{
		    result = host.vmCreate(migrateInVm);

		    if (result)
		    {
			// if vm were succesfully created in the host

			getVmTable().put(migrateInVm.getUid(), host);
			getUsedPes().put(migrateInVm.getUid(), migrateInVm.getNumberOfPes());
			getFreePes().set(idx, getFreePes().get(idx) - migrateInVm.getNumberOfPes());

			orderedVmCreateEventArgs.setAllocatedHost(host);
			sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE_COMPLETE, ev.getData());

		    }
		    else
		    {
			sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE_COMPLETE, ev.getData());
		    }

		}

	    }

	}
	else
	{
	    Log.formatLine("%.2f:[processVmMigrateForVmComplete] Fail to migrate low order VM #%d out (for high order VM #%d)",CloudSim.clock(), migrateOutVm.getId(), migrateInVm.getId());
	    // fail to migrate the low order VM out
	    // vmMigrateForVmEventArgs.setAllMigrateSuccess(false);
	    sendToDatacenterNow(CloudSimTags.ORDERED_VM_CREATE_COMPLETE, ev.getData());
	}

    }

    public void processOrderedVmMigrateCreate(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
	VmMigrateForVmEventArgs vmMigrateForVmEventArgs = orderedVmCreateEventArgs.getVmMigrateForVmEventArgs();
	VmMigrateCreateEventArgs vmMigrateCreateEventArgs = vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().get(vmMigrateForVmEventArgs.getCurrMigrateIdx());
	OrderedVmMigrateCreateEventArgs orderedVmMigrateCreateEventArgs = vmMigrateCreateEventArgs.getOrderedVmMigrateCreateEArgs();
	List<Integer> freePes = orderedVmMigrateCreateEventArgs.getFreePes();
	List<RankedHost> hostList = orderedVmMigrateCreateEventArgs.getHostList();
	ProfiledVM profiledVM = orderedVmMigrateCreateEventArgs.getVm();

	if (!orderedVmMigrateCreateEventArgs.hasCheckAllHosts())
	{

	    RankedHost host = null;
	    boolean result = false;
	    try
	    {

		host = orderedVmMigrateCreateEventArgs.getDistributableHost(profiledVM.getVmType());
	    }
	    catch (Exception e)
	    {

		sendToDatacenterNow(CloudSimTags.ORDERED_VM_MIGRATE_CREATE_COMPLETE, ev.getData());
	    }

	    Log.formatLine("%.2f:[processOrderedVmMigrateCreate]: Try migrate low order VM #%d to Host #%d",CloudSim.clock(), profiledVM.getId(), host.getId());
	    int idx = orderedVmMigrateCreateEventArgs.getHostList().indexOf(host);
	    orderedVmMigrateCreateEventArgs.setLastCheckIdx(idx);
	    if (host.isSuitableForVm(profiledVM))
	    {
		result = host.vmCreate(profiledVM);

		if (result)
		{
		    // if vm were succesfully created in the host

		    getVmTable().put(profiledVM.getUid(), host);
		    getUsedPes().put(profiledVM.getUid(), profiledVM.getNumberOfPes());
		    getFreePes().set(idx, getFreePes().get(idx) - profiledVM.getNumberOfPes());

		    orderedVmMigrateCreateEventArgs.setAllocatedHost(host);
		    sendToDatacenterNow(CloudSimTags.ORDERED_VM_MIGRATE_CREATE_COMPLETE, ev.getData());

		}

	    }
	    else
	    {

		// orderedVmMigrateCreateEventArgs.getFreePes().set(idx, -1);
		sendToDatacenterNow(CloudSimTags.ORDERED_VM_MIGRATE_CREATE_COMPLETE, ev.getData());

	    }
	}

    }

    public void processOrderedVmMigrateCreateComplete(SimEvent ev)
    {
	VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
	OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
	VmMigrateForVmEventArgs vmMigrateForVmEventArgs = orderedVmCreateEventArgs.getVmMigrateForVmEventArgs();
	VmMigrateCreateEventArgs vmMigrateCreateEventArgs = vmMigrateForVmEventArgs.getVmMigrateCreateEventArgsList().get(vmMigrateForVmEventArgs.getCurrMigrateIdx());
	OrderedVmMigrateCreateEventArgs orderedVmMigrateCreateEventArgs = vmMigrateCreateEventArgs.getOrderedVmMigrateCreateEArgs();
	List<Integer> freePes = orderedVmMigrateCreateEventArgs.getFreePes();
	List<RankedHost> hostList = orderedVmMigrateCreateEventArgs.getHostList();
	ProfiledVM profiledVM = orderedVmMigrateCreateEventArgs.getVm();
	Host allocatedHost = orderedVmMigrateCreateEventArgs.getAllocatedHost();
	int lastCheckIdx = orderedVmMigrateCreateEventArgs.getLastCheckIdx();
	Host lastCheckHost = orderedVmCreateEventArgs.getHostList().get(lastCheckIdx);
	if (allocatedHost != null)
	{
	    // create vm success
	    if (profiledVM.isBeingInstantiated())
	    {
		profiledVM.setBeingInstantiated(false);
	    }

	    Log.formatLine("%.2f:[processOrderedVmMigrateCreate]: Successfully migrate low ordered VM #%d to Host #%d",CloudSim.clock(), profiledVM.getId(), allocatedHost.getId());

	    sendToDatacenterNow(CloudSimTags.VM_MIGRATE_CREATE_COMPLETE, vmCreateEventArgs);
	    profiledVM.updateVmProcessing(CloudSim.clock(), getHost(profiledVM).getVmScheduler().getAllocatedMipsForVm(profiledVM));
	}
	else
	{

	    // fail to migrate out the low order Vm, so try next host availble
	    // in freePes
	    Log.formatLine("%.2f:[processOrderedVmMigrateCreate]: Fail to migrate low ordered VM #%d to Host #%d",CloudSim.clock() ,profiledVM.getId(), lastCheckHost.getId());
	    orderedVmMigrateCreateEventArgs.getFreePes().set(lastCheckIdx, -1);
	    sendToDatacenterNow(CloudSimTags.VM_MIGRATE_CREATE_COMPLETE, vmCreateEventArgs);
	}
    }

    public void sendToDatacenterNow(int cloudSimTag, Object data)
    {
	getDatacenter().scheduleNow(getDatacenter().getId(), cloudSimTag, data);
	
    }

    public List<RankedHost> getHostList()
    {
	ArrayList<RankedHost> result = new ArrayList<RankedHost>();
	for (Host host : super.getHostList())
	{
	    result.add((RankedHost) host);
	}
	return result;
    }

}
