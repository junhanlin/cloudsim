/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package edu.nccu.mis.cloudsim.example;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.junit.Test;

import edu.nccu.mis.cloudsim.FreePeBestFitPolicy;
import edu.nccu.mis.cloudsim.FreePeFirstFitPolicy;
import edu.nccu.mis.cloudsim.OrderedDatacenter;
import edu.nccu.mis.cloudsim.ProfiledVM;
import edu.nccu.mis.cloudsim.RankedHost;
import edu.nccu.mis.cloudsim.SeqDatacenterBroker;
import edu.nccu.mis.cloudsim.VMOrder;
import edu.nccu.mis.cloudsim.VMType;
import edu.nccu.mis.cloudsim.OrderedVmAllocationPolicy;

/**
 * A simple example showing how to create a datacenter with one host and run two
 * cloudlets on it. The cloudlets run in VMs with the same MIPS requirements.
 * The cloudlets will take the same time to complete the execution.
 */
public class TestOrderedExample2
{
    @Test
    public void testOrderedExample2()
    {
	main(new String[0]);
    }
    
    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vmlist. */
    private static List<Vm> vmlist;

    /**
     * Creates main() to run this example
     */
    
    public static void main(String[] args)
    {

	Log.printLine("Starting CloudSimExample2...");

	try
	{
	    // First step: Initialize the CloudSim package. It should be called
	    // before creating any entities.
	    int num_user = 1; // number of cloud users
	    Calendar calendar = Calendar.getInstance();
	    boolean trace_flag = false; // mean trace events

	    // Initialize the CloudSim library
	    CloudSim.init(num_user, calendar, trace_flag);

	    // Second step: Create Datacenters
	    // Datacenters are the resource providers in CloudSim. We need at
	    // list one of them to run a CloudSim simulation
	    @SuppressWarnings("unused")
	    Datacenter datacenter0 = createDatacenter("Datacenter_0");

	    // Third step: Create Broker
	    DatacenterBroker broker = createBroker();
	    int brokerId = broker.getId();

	    // Fourth step: Create one virtual machine
	    vmlist = new ArrayList<Vm>();

	    // VM description
	    int vmid = 0;
	    String vmm = "Xen"; // VMM name

	    // create VMs
	    ProfiledVM vm1 = ProfiledVM.genDefaultProfiledVM(vmid, brokerId, vmm, VMType.CPU, VMOrder.LOW, new CloudletSchedulerTimeShared());
	    vmlist.add(vm1);

	    vmid++;
	    for (; vmid < 40; vmid++)
	    {
		if (vmid < 20)
		{
		    ProfiledVM highVm = ProfiledVM.genDefaultProfiledVM(vmid, brokerId, vmm, VMType.CPU, VMOrder.HIGH, new CloudletSchedulerTimeShared());
		    vmlist.add(highVm);
		}
		else
		{
		    ProfiledVM lowVm = ProfiledVM.genDefaultProfiledVM(vmid, brokerId, vmm, VMType.CPU, VMOrder.LOW, new CloudletSchedulerTimeShared());
		    vmlist.add(lowVm);
		}

	    }

	    // submit vm list to the broker
	    broker.submitVmList(vmlist);

	    // Fifth step: Create Cloudlets
	    cloudletList = new ArrayList<Cloudlet>();

	    // Cloudlet properties
	    int cid = 0;
	    // int pesNumber = 2;
	    long length = 250000;
	    long fileSize = 300;
	    long outputSize = 300;
	    UtilizationModel utilizationModel = new UtilizationModelFull();

	    // we assign same spec of cloudlet to its bounded vms
	    for (Vm vm : vmlist)
	    {
		// we assume every cloudlet will use full pesNumber of its bound
		// vm
		Cloudlet cloudlet = new Cloudlet(cid, length, vm.getNumberOfPes(), fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setUserId(brokerId);
		cloudletList.add(cloudlet);
		cid++;

	    }

	    // submit cloudlet list to the broker
	    broker.submitCloudletList(cloudletList);

	    // bind the cloudlets to the vms. This way, the broker
	    // will submit the bound cloudlets only to the specific VM

	    for (int i = 0; i < vmlist.size(); i++)
	    {
		broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmlist.get(i).getId());
	    }
	    
	    // Sixth step: Starts the simulation
	    CloudSim.startSimulation();

	    // Final step: Print results when simulation is over
	    List<Cloudlet> newList = broker.getCloudletReceivedList();

	    CloudSim.stopSimulation();

	    printCloudletList(newList);

	    Log.printLine("CloudSimExample2 finished!");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    Log.printLine("The simulation has been terminated due to an unexpected error");
	}
    }

    private static Datacenter createDatacenter(String name)
    {

	// Here are the steps needed to create a PowerDatacenter:
	// 1. We need to create a list to store
	// our machine
	List<RankedHost> hostList = new ArrayList<RankedHost>();

	// 2. A Machine contains one or more PEs or CPUs/Cores.
	// In this example, it will have only one core.
	List<Pe> peList = new ArrayList<Pe>();

	int mips = 1000;

	// 3. Create PEs and add these into a list.
	peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store
							      // Pe id and
							      // MIPS Rating

	// 4. Create Host with its id and list of PEs and add them to the list
	// of machines

	int hostId = 0;
	hostList.add(RankedHost.genDefaultRankedHost(hostId++, 2, 0, 1, VmSchedulerTimeShared.class));
	hostList.add(RankedHost.genDefaultRankedHost(hostId++, 0, 1, 2, VmSchedulerTimeShared.class));

	// This is our machine

	// 5. Create a DatacenterCharacteristics object that stores the
	// properties of a data center: architecture, OS, list of
	// Machines, allocation policy: time- or space-shared, time zone
	// and its price (G$/Pe time unit).
	String arch = "x86"; // system architecture
	String os = "Linux"; // operating system
	String vmm = "Xen";
	double time_zone = 10.0; // time zone this resource located
	double cost = 3.0; // the cost of using processing in this resource
	double costPerMem = 0.05; // the cost of using memory in this resource
	double costPerStorage = 0.001; // the cost of using storage in this
				       // resource
	double costPerBw = 0.0; // the cost of using bw in this resource
	LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
								     // not
								     // adding
								     // SAN
								     // devices
								     // by
								     // now

	DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

	// 6. Finally, we need to create a PowerDatacenter object.
	Datacenter datacenter = null;
	try
	{
	    // datacenter = new Datacenter(name, characteristics, new
	    // VmAllocationPolicyFreePe(hostList), storageList, 0);
	    // datacenter = new OrderedDatacenter(name, characteristics, new
	    // OrderedVmAllocationPolicy(hostList,new FreePeBestFitPolicy()),
	    // storageList, 0);
	    datacenter = new OrderedDatacenter(name, characteristics, new OrderedVmAllocationPolicy(hostList, new FreePeFirstFitPolicy()), storageList, 0);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return datacenter;
    }

    // We strongly encourage users to develop their own broker policies, to
    // submit vms and cloudlets according
    // to the specific rules of the simulated scenario
    private static DatacenterBroker createBroker()
    {

	DatacenterBroker broker = null;
	try
	{
	    broker = new SeqDatacenterBroker("Broker");
	    // broker = new DatacenterBroker("Broker");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
	return broker;
    }

    /**
     * Prints the Cloudlet objects
     * 
     * @param list
     *            list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list)
    {
	int size = list.size();
	Cloudlet cloudlet;

	String indent = "    ";
	Log.printLine();
	Log.printLine("========== OUTPUT ==========");
	Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

	DecimalFormat dft = new DecimalFormat("###.##");
	for (int i = 0; i < size; i++)
	{
	    cloudlet = list.get(i);
	    Log.print(indent + cloudlet.getCloudletId() + indent + indent);

	    if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS)
	    {
		Log.print("SUCCESS");

		Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
			+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + dft.format(cloudlet.getFinishTime()));
	    }
	}

    }
}
