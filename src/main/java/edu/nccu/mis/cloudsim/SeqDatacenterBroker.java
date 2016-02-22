package edu.nccu.mis.cloudsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

public class SeqDatacenterBroker extends DatacenterBroker
{

    public SeqDatacenterBroker(String name) throws Exception
    {
	super(name);
	// TODO Auto-generated constructor stub
    }

    @Override
    protected void processCloudletReturn(SimEvent ev)
    {
	Cloudlet cloudlet = (Cloudlet) ev.getData();
	getCloudletReceivedList().add(cloudlet);
	Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");
	cloudletsSubmitted--;
	if (getCloudletList().size() == 0 && cloudletsSubmitted == 0)
	{ // all cloudlets executed
	    Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
	    clearDatacenters();
	    finishExecution();
	}
	else
	{ // some cloudlets haven't finished yet
	    if (getCloudletList().size() > 0 && cloudletsSubmitted == 0)
	    {
		// all the cloudlets sent finished. but some bount
		// cloudlet is waiting its VM be created
		clearDatacenters();
		createVmsInDatacenter(getDatacenterIdsList().get(0));
	    }

	}
    }

    @Override
    protected void createVmsInDatacenter(int datacenterId)
    {
	// send as much vms as possible for this datacenter before trying the
	// next one
	int requestedVms = 0;
	String datacenterName = CloudSim.getEntityName(datacenterId);
	int i = 0;
	for (Vm vm : getVmList())
	{
	    if (!getVmsToDatacentersMap().containsKey(vm.getId()))
	    {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + datacenterName);
		// sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK,vm);
		send(datacenterId, i * CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, vm);
		requestedVms++;
	    }
	    i++;
	}

	getDatacenterRequestedIdsList().add(datacenterId);

	setVmsRequested(requestedVms);
	setVmsAcks(0);
    }

}
