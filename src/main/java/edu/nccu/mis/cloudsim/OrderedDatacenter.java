package edu.nccu.mis.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.omg.CosNaming.IstringHelper;

public class OrderedDatacenter extends Datacenter
{

	public OrderedDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception
	{
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processEvent(SimEvent ev)
	{
		int srcId = -1;

		switch (ev.getTag())
		{
		// Resource characteristics inquiry
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), getCharacteristics());
				break;

			// Resource dynamic info inquiry
			case CloudSimTags.RESOURCE_DYNAMICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), 0);
				break;

			case CloudSimTags.RESOURCE_NUM_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int numPE = getCharacteristics().getNumberOfPes();
				sendNow(srcId, ev.getTag(), numPE);
				break;

			case CloudSimTags.RESOURCE_NUM_FREE_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int freePesNumber = getCharacteristics().getNumberOfFreePes();
				sendNow(srcId, ev.getTag(), freePesNumber);
				break;

			// New Cloudlet arrives
			case CloudSimTags.CLOUDLET_SUBMIT:
				processCloudletSubmit(ev, false);
				break;

			// New Cloudlet arrives, but the sender asks for an ack
			case CloudSimTags.CLOUDLET_SUBMIT_ACK:
				processCloudletSubmit(ev, true);
				break;

			// Cancels a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
				break;

			// Pauses a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
				break;

			// Pauses a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
				break;

			// Resumes a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_RESUME:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
				break;

			// Resumes a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE:
				processCloudletMove((int[]) ev.getData(),
						CloudSimTags.CLOUDLET_MOVE);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE_ACK:
				processCloudletMove((int[]) ev.getData(),
						CloudSimTags.CLOUDLET_MOVE_ACK);
				break;

			// Checks the status of a Cloudlet
			case CloudSimTags.CLOUDLET_STATUS:
				processCloudletStatus(ev);
				break;

			// Ping packet
			case CloudSimTags.INFOPKT_SUBMIT:
				processPingRequest(ev);
				break;

			case CloudSimTags.VM_CREATE:
				processVmCreate(ev,false);
				break;
			
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev,true);
				break;
			 
			case CloudSimTags.VM_CREATE_COMPLETE:
				processVmCreateComplete(ev);
				break;
			case CloudSimTags.VM_DESTROY:
				processVmDestroy(ev, false);
				break;

			case CloudSimTags.VM_DESTROY_ACK:
				processVmDestroy(ev, true);
				break;

			case CloudSimTags.VM_MIGRATE:
				processVmMigrate(ev, false);
				break;

			case CloudSimTags.VM_MIGRATE_ACK:
				processVmMigrate(ev, true);
				break;

			case CloudSimTags.VM_DATA_ADD:
				processDataAdd(ev, false);
				break;

			case CloudSimTags.VM_DATA_ADD_ACK:
				processDataAdd(ev, true);
				break;

			case CloudSimTags.VM_DATA_DEL:
				processDataDelete(ev, false);
				break;

			case CloudSimTags.VM_DATA_DEL_ACK:
				processDataDelete(ev, true);
				break;

			case CloudSimTags.VM_DATACENTER_EVENT:
				updateCloudletProcessing();
				checkCloudletCompletion();
				break;
			case CloudSimTags.ORDERED_VM_CREATE:
				processOrderedVmCreate(ev);
				break;
			case CloudSimTags.ORDERED_VM_CREATE_COMPLETE:
				processOrderedVmCreateComplete(ev);
				break;
			case CloudSimTags.VM_MIGRATE_FOR_VM:
				processVmMigrateForVm(ev);
				break;
			case CloudSimTags.VM_MIGRATE_FOR_VM_COMPLETE:
				processVmMigrateForVmComplete(ev);
				break;
			case CloudSimTags.VM_MIGRATE_CREATE:
				processVmMigrateCreate(ev);
				break;
			case CloudSimTags.VM_MIGRATE_CREATE_COMPLETE:
				processVmMigrateCreateComplete(ev);
				break;
			case CloudSimTags.ORDERED_VM_MIGRATE_CREATE:
				processOrderedVmMigrateCreate(ev);
				break;
			case CloudSimTags.ORDERED_VM_MIGRATE_CREATE_COMPLETE:
				processOrderedVmMigrateCreateComplete(ev);
				break;

			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	protected void processVmCreate(SimEvent ev,boolean ack)
	{
		getOrderedVmAllocationPolicy().processVmCreate(ev, ack);

	}

	protected void processVmCreateComplete(SimEvent ev)
	{
		
		getOrderedVmAllocationPolicy().processVmCreateComplete(ev);
		
		VmCreateEventArgs vmCreateEventArgs = (VmCreateEventArgs) ev.getData();
		
		OrderedVmCreateEventArgs orderedVmCreateEventArgs = vmCreateEventArgs.getOrderedVmCreateEArgs();
		
		//if successfully create VM or we have try all the host and none of them is available
		//acknowledge data broker
		if(vmCreateEventArgs.isSuccess() || orderedVmCreateEventArgs.hasCheckAllHosts())
		{
			if(vmCreateEventArgs.shouldAck())
			{
				int[] data = new int[3];
				data[0] = getId();
				data[1] = vmCreateEventArgs.getVm().getId();
		
				if (vmCreateEventArgs.isSuccess())
				{
					data[2] = CloudSimTags.TRUE;
				} else
				{
					data[2] = CloudSimTags.FALSE;
				}
				/*
				send(vmCreateEventArgs.getVm().getUserId(), CloudSim.getMinTimeBetweenEvents(),
						CloudSimTags.VM_CREATE_ACK, data);*/
				sendNow(vmCreateEventArgs.getVm().getUserId(),CloudSimTags.VM_CREATE_ACK, data);
			}
		}
		
	}

	protected void processVmMigrateCreate(SimEvent ev)
	{
		getOrderedVmAllocationPolicy().processVmMigrateCreate(ev);
	}

	protected void processVmMigrateCreateComplete(SimEvent ev)
	{
		getOrderedVmAllocationPolicy().processVmMigrateCreateComplete(ev);
	}

	protected void processOrderedVmCreate(SimEvent ev)
	{
		getOrderedVmAllocationPolicy().processOrderedVmCreate(ev);

	}

	protected void processOrderedVmCreateComplete(SimEvent ev)
	{
		getOrderedVmAllocationPolicy().processOrderedVmCreateComplete(ev);
	}

	protected void processVmMigrateForVm(SimEvent ev)
	{

		getOrderedVmAllocationPolicy().processVmMigrateForVm(ev);
		

	}

	protected void processVmMigrateForVmComplete(SimEvent ev)
	{
		getOrderedVmAllocationPolicy().processVmMigrateForVmComplete(ev);
		
	}

	protected void processOrderedVmMigrateCreate(SimEvent ev)
	{
		getOrderedVmAllocationPolicy().processOrderedVmMigrateCreate(ev);
		
	}

	protected void processOrderedVmMigrateCreateComplete(SimEvent ev)
	{
		getOrderedVmAllocationPolicy().processOrderedVmMigrateCreateComplete(ev);
	}
	
	private OrderedVmAllocationPolicy getOrderedVmAllocationPolicy()
	{
		return (OrderedVmAllocationPolicy)getVmAllocationPolicy();
	}

}
