package org.polito.unvim;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.plugin.PluginStarter;
import org.openbaton.vim.drivers.interfaces.VimDriver;
import org.polito.management.ComputeManager;
import org.polito.management.NetworkManager;
import org.polito.management.NffgManager;
import org.polito.model.message.FloatingIpPool;
import org.polito.model.message.UnConfiguration;
import org.polito.model.nffg.Nffg;
import org.polito.model.template.VnfTemplate;
import org.polito.proxy.UniversalNodeProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class UnClient extends VimDriver {
	private static Logger log = LoggerFactory.getLogger(UnClient.class);
	private static Lock un_lock, config_lock;
	private Gson gson = new GsonBuilder().create();
	private static String MANAGEMENT_GRAPH = "management_graph";
	private static String OPERATOR_GRAPH = "operator_graph";

	public static void main(String[] args)
		      throws NoSuchMethodException, IOException, InstantiationException, TimeoutException,
		          IllegalAccessException, InvocationTargetException {
		    UnClient.un_lock = new ReentrantLock();
		    UnClient.config_lock = new ReentrantLock();
		    if (args.length == 6) {
		      PluginStarter.registerPlugin(
		    		  UnClient.class,
		          args[0],
		          args[1],
		          Integer.parseInt(args[2]),
		          5);
		    } else if (args.length == 4) {
		      PluginStarter.registerPlugin(
		    		  UnClient.class,
		          args[0],
		          args[1],
		          Integer.parseInt(args[2]),
		          5);
		    } else
		      PluginStarter.registerPlugin(UnClient.class, "unvim", "130.192.225.193", 5672, 5);
		}

	@Override
	public Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair,
			Set<VNFDConnectionPoint> network, Set<String> secGroup, String userData) throws VimDriverException {
		return launchInstanceAndWait(vimInstance, name, image, null, keypair, network, secGroup, userData, null, null);
	}

	@Override
	public List<NFVImage> listImages(VimInstance vimInstance) throws VimDriverException {
		log.debug("Listing images for VimInstance with name: " + vimInstance.getName());
		List<VnfTemplate> templates = UniversalNodeProxy.getTemplates(vimInstance);
		List<NFVImage> images = new ArrayList<>();
		for(VnfTemplate template: templates)
		{
			NFVImage image = new NFVImage();
			image.setExtId(template.getId());
			image.setName(template.getName());
			images.add(image);
		}
		return images;
	}

	@Override
	public List<Server> listServer(VimInstance vimInstance) throws VimDriverException {
		List<Server> servers = new ArrayList<>();
		log.debug("Listing server for VimInstance with name: " + vimInstance.getName());
		Nffg nffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		Nffg operatorNffg = UniversalNodeProxy.getNFFG(vimInstance, OPERATOR_GRAPH);
		if(nffg!=null)
			servers = ComputeManager.getServers(operatorNffg,nffg,UniversalNodeProxy.getConfiguration(vimInstance).getConfigurationServiceEndpoint());
		return servers;
	}

	@Override
	public List<Network> listNetworks(VimInstance vimInstance) throws VimDriverException {
		log.debug("Listing networks for VimInstance with name: " + vimInstance.getName());
		Map<String,Nffg> bootNffgs = UniversalNodeProxy.getNFFGs(vimInstance, vimInstance.getTenant(), MANAGEMENT_GRAPH, OPERATOR_GRAPH);
		if(bootNffgs.get(MANAGEMENT_GRAPH)==null)
		{
			log.debug("The management graph doesn't exist yet. Creating one");
			bootNffgs = NetworkManager.createBootGraphs();
			for(Nffg nffg: bootNffgs.values())
				UniversalNodeProxy.sendNFFG(vimInstance, nffg);
		}
		Nffg tenantNffg = bootNffgs.get(vimInstance.getTenant());
		List<Network> networks = NetworkManager.getNetworks(tenantNffg,UniversalNodeProxy.getConfiguration(vimInstance).getConfigurationServiceEndpoint());
		for(Network nw: networks) log.debug("network found: " + nw.getName() + " ID => " + nw.getId() + " EXT_ID =>" + nw.getExtId());
		return networks;
	}

	@Override
	public List<DeploymentFlavour> listFlavors(VimInstance vimInstance) throws VimDriverException {
		List<DeploymentFlavour> flavors = new ArrayList<DeploymentFlavour>();
		DeploymentFlavour f1 = new DeploymentFlavour();
		f1.setExtId("1");
		f1.setRam(512);
		f1.setDisk(1);
		f1.setVcpus(1);
		f1.setFlavour_key("m1.tiny");
		flavors.add(f1);
		DeploymentFlavour f2 = new DeploymentFlavour();
		f2.setExtId("2");
		f2.setRam(2048);
		f2.setDisk(20);
		f2.setVcpus(1);
		f2.setFlavour_key("m1.small");
		flavors.add(f2);
		DeploymentFlavour f3 = new DeploymentFlavour();
		f3.setExtId("3");
		f3.setRam(4096);
		f3.setDisk(40);
		f3.setVcpus(2);
		f3.setFlavour_key("m1.medium");
		flavors.add(f3);
		DeploymentFlavour f4 = new DeploymentFlavour();
		f4.setExtId("4");
		f4.setRam(8192);
		f4.setDisk(80);
		f4.setVcpus(4);
		f4.setFlavour_key("m1.large");
		flavors.add(f4);
		DeploymentFlavour f5 = new DeploymentFlavour();
		f5.setExtId("5");
		f5.setRam(16384);
		f5.setDisk(160);
		f5.setVcpus(8);
		f5.setFlavour_key("m1.xlarge");
		flavors.add(f5);
		return flavors;
	}

	@Override
	public Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId,
			String keyPair, Set<VNFDConnectionPoint> networksDict, Set<String> securityGroups, String userData, Map<String, String> floatingIps,
			Set<Key> keys) throws VimDriverException {
		try
			{
            String oldVNFDCP = gson.toJson(networksDict);
            Set<VNFDConnectionPoint> networks =
                    gson.fromJson(oldVNFDCP, new TypeToken<Set<VNFDConnectionPoint>>() {}.getType());

			log.debug("New server required:");
			log.debug("hostname: " + (hostname==null? "null":hostname) + ", image: " + (image==null? "null":image) + ", extId: " + (extId==null? "null":extId) +  ", keyPair: " + (keyPair==null? "null":keyPair) + ", networks: " + (networks==null? "null":networks.toString()) + ", securityGroups: " + (securityGroups==null? "null":securityGroups) + ", floatingIps: " + (floatingIps==null? "null":floatingIps));
			// Given an image name search the template:
			String templateId=null;
			List<VnfTemplate> templates = UniversalNodeProxy.getTemplates(vimInstance);
			for(VnfTemplate template: templates)
				if(template.getId().equals(image))
					templateId=template.getId();
			if(templateId==null)
				throw new VimDriverException("The required image is no longer present");
	
			if (keys != null && !keys.isEmpty())
				userData = addKeysToUserData(userData, keys);
	
			Server server;
			String serverId;
			Nffg tenantNffg, operatorNffg, managementNffg;
			synchronized(un_lock)
			{
				Map<String,Nffg> graphs = UniversalNodeProxy.getNFFGs(vimInstance, MANAGEMENT_GRAPH, OPERATOR_GRAPH, vimInstance.getTenant());
				tenantNffg = graphs.get(vimInstance.getTenant());
				operatorNffg = graphs.get(OPERATOR_GRAPH);
				managementNffg = graphs.get(MANAGEMENT_GRAPH);
				if(tenantNffg==null || operatorNffg==null)
					throw new VimDriverException("Illegal state. A tenant nffg + operator nffg must be already deployed");
				// Create the server
                log.debug("Sending a VNF request to the UN");
				serverId = ComputeManager.createServer(managementNffg, tenantNffg, hostname, templateId, keyPair, networks, securityGroups, userData);
				log.debug("VNF created into the UN domain");
				UniversalNodeProxy.sendNFFGs(vimInstance, managementNffg, tenantNffg);
			}
			UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
			try
			{
				server = ComputeManager.getServerById(operatorNffg, tenantNffg, serverId, unConfig.getConfigurationServiceEndpoint());
			} catch (Exception e) {
				log.debug("An error occurs during the creation of the server with name: " + hostname);
				deleteServerById(vimInstance, serverId);
				throw new VimDriverException(e.getMessage());
			}
			try
			{
				if(floatingIps!=null && !floatingIps.isEmpty())
				{
					synchronized(config_lock)
					{
						ComputeManager.assigneFloatingIps(operatorNffg,server,floatingIps, unConfig.getConfigurationServiceEndpoint(), unConfig.getExternalNetwork(), unConfig.getFloatingIpPool());
					}
				}
			} catch (Exception e) {
				log.debug("An error occurs during the creation of the server with name: " + hostname);
				deleteServerByIdAndWait(vimInstance, serverId);
				throw new VimDriverException(e.getMessage());
			}
			log.debug("END create server");
			return server;
		}
		catch(Exception e)
		{
			log.debug(e.getMessage());
			log.debug(e.getStackTrace().toString());
			throw new VimDriverException(e.getMessage());
		}
	}

	private String addKeysToUserData(
		      String userData, Set<Key> keys) {
		    log.debug("Going to add all keys: " + keys.size());
		    userData += "for x in `find /home/ -name authorized_keys`\n";
		    userData += "do\n";
		    String oldKeys = gson.toJson(keys);

		    Set<Key> keysSet =
		        new Gson()
		            .fromJson(
		                oldKeys, new TypeToken<Set<Key>>() {}.getType());

		    for (Key key : keysSet) {
		      log.debug("Adding key: " + key.getName());
		      userData += "\techo \"" + key.getPublicKey() + "\" >> $x\n";
		    }
		    userData += "done\n";
		    return userData;
		}

	@Override
	public Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId,
			String keyPair, Set<VNFDConnectionPoint> networks, Set<String> securityGroups, String userData) throws VimDriverException {
		return launchInstanceAndWait(vimInstance, hostname, image, extId, keyPair, networks, securityGroups, userData, null, null);
	}

	public void deleteServerById(VimInstance vimInstance, String id) throws VimDriverException {
		synchronized(un_lock)
		{
			log.debug("Delete required for server with id: " + id);
			Map<String,Nffg> graphs = UniversalNodeProxy.getNFFGs(vimInstance, OPERATOR_GRAPH, MANAGEMENT_GRAPH, vimInstance.getTenant());
			Nffg tenantNffg = graphs.get(vimInstance.getTenant());
			Nffg operatorNffg = graphs.get(OPERATOR_GRAPH);
			Nffg managementNffg = graphs.get(MANAGEMENT_GRAPH);
			if(tenantNffg==null || managementNffg==null)
				throw new VimDriverException("Illegal state. A tenant nffg + management nffg must be already deployed");
			UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
			ComputeManager.destroyServer(managementNffg, operatorNffg, tenantNffg, id, unConfig.getConfigurationServiceEndpoint(),true);
			UniversalNodeProxy.sendNFFGs(vimInstance, managementNffg, tenantNffg);
		}
	}

	@Override
	public void deleteServerByIdAndWait(VimInstance vimInstance, String id) throws VimDriverException {
		synchronized(un_lock)
		{
			log.debug("Delete required for server with id: " + id);
			Map<String,Nffg> graphs = UniversalNodeProxy.getNFFGs(vimInstance, OPERATOR_GRAPH, MANAGEMENT_GRAPH, vimInstance.getTenant());
			Nffg tenantNffg = graphs.get(vimInstance.getTenant());
			Nffg operatorNffg = graphs.get(OPERATOR_GRAPH);
			Nffg managementNffg = graphs.get(MANAGEMENT_GRAPH);
			if(tenantNffg==null || operatorNffg==null)
				throw new VimDriverException("Illegal state. A tenant nffg + operator nffg must be already deployed");
			UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
			synchronized(config_lock)
			{
				ComputeManager.destroyServer(managementNffg,operatorNffg, tenantNffg, id, unConfig.getConfigurationServiceEndpoint(),false);
			}
			UniversalNodeProxy.sendNFFGs(vimInstance, managementNffg, tenantNffg);
		}
	}

	@Override
	public Network createNetwork(VimInstance vimInstance, Network network) throws VimDriverException {
		log.debug("New network required:");
		log.debug(network.toString());
		Map<String,Nffg> bootNffgs = UniversalNodeProxy.getNFFGs(vimInstance, MANAGEMENT_GRAPH, OPERATOR_GRAPH);
		if(bootNffgs.get(MANAGEMENT_GRAPH)==null)
		{
			log.debug("The management graph doesn't exist yet. Creating one");
			bootNffgs = NetworkManager.createBootGraphs();
			for(Nffg nffg: bootNffgs.values())
				UniversalNodeProxy.sendNFFG(vimInstance, nffg);
		}
		Nffg tenantNffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		Nffg operatorNffg = bootNffgs.get(OPERATOR_GRAPH);
		if(tenantNffg==null)
			tenantNffg = NffgManager.createEmptyNffg(vimInstance.getTenant());
		NetworkManager.createNetwork(operatorNffg, tenantNffg, network);
        log.debug("Network creation request to the UN");
		UniversalNodeProxy.sendNFFGs(vimInstance, operatorNffg, tenantNffg);
        log.debug("Network created in the UN domain");
		log.debug("END create network");
		return network;
	}

	@Override
	public DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
			throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NFVImage addImage(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NFVImage addImage(VimInstance vimInstance, NFVImage image, String image_url) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NFVImage updateImage(VimInstance vimInstance, NFVImage image) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NFVImage copyImage(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteImage(VimInstance vimInstance, NFVImage image) throws VimDriverException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DeploymentFlavour updateFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
			throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteFlavor(VimInstance vimInstance, String extId) throws VimDriverException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet)
			throws VimDriverException {
		log.debug("New subnet required:");
		log.debug(subnet.toString());
		Map<String,Nffg> graphs = UniversalNodeProxy.getNFFGs(vimInstance, MANAGEMENT_GRAPH, OPERATOR_GRAPH, vimInstance.getTenant());
		Nffg managementNffg = graphs.get(MANAGEMENT_GRAPH);
		Nffg tenantNffg = graphs.get(vimInstance.getTenant());
		Nffg operatorNffg = graphs.get(OPERATOR_GRAPH);
		UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
		NetworkManager.createSubnet(managementNffg, tenantNffg, createdNetwork, subnet, unConfig.getDatastoreEndpoint());
        log.debug("Subnet creation request to the UN");
		UniversalNodeProxy.sendNFFGs(vimInstance, managementNffg, tenantNffg);
        log.debug("Subnet created into the UN domain");
		synchronized (config_lock) {
			NetworkManager.configureSubnet(managementNffg, operatorNffg, tenantNffg,createdNetwork,subnet,properties,unConfig.getConfigurationServiceEndpoint());
		}
		return subnet;
	}

	@Override
	public Network updateNetwork(VimInstance vimInstance, Network network) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet)
			throws VimDriverException {
		log.debug("Update required for subnet with id: " + subnet.getExtId());
		Map<String,Nffg> graphs = UniversalNodeProxy.getNFFGs(vimInstance, MANAGEMENT_GRAPH, OPERATOR_GRAPH, vimInstance.getTenant());
		Nffg managementNffg = graphs.get(MANAGEMENT_GRAPH);
		Nffg tenantNffg = graphs.get(vimInstance.getTenant());
		Nffg operatorNffg = graphs.get(OPERATOR_GRAPH);
		String configurationServiceEndpoint = UniversalNodeProxy.getConfiguration(vimInstance).getConfigurationServiceEndpoint();
		NetworkManager.configureSubnet(managementNffg, operatorNffg,tenantNffg,updatedNetwork,subnet,properties,configurationServiceEndpoint);
		return subnet;
	}

	@Override
	public List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId) throws VimDriverException {
		log.debug("Required subnets ids for network with id: " + network_extId);
		Nffg nffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		if(nffg==null)
			throw new VimDriverException("Illegal state. A nffg must be already deployed");
		return NetworkManager.getSubnetsIds(nffg,network_extId);
	}

	@Override
	public boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId) throws VimDriverException {
		log.debug("Delete required for subnet with id: " + existingSubnetExtId);
		Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
		Nffg tenantNffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		NetworkManager.deleteSubnet(tenantNffg, managementNffg, existingSubnetExtId);
		UniversalNodeProxy.sendNFFGs(vimInstance, managementNffg, tenantNffg);
		return true;
	}

	@Override
	public boolean deleteNetwork(VimInstance vimInstance, String extId) throws VimDriverException {
		log.debug("Delete required for network with id: " + extId);
		Nffg operatorNffg = UniversalNodeProxy.getNFFG(vimInstance, OPERATOR_GRAPH);
		Nffg tenantNffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		if(tenantNffg==null)
			throw new VimDriverException("Illegal state. A nffg must be already deployed");
		NetworkManager.deleteNetwork(tenantNffg, operatorNffg, extId);
		UniversalNodeProxy.sendNFFGs(vimInstance, operatorNffg, tenantNffg);
		return true;
	}

	@Override
	public Network getNetworkById(VimInstance vimInstance, String id) throws VimDriverException {
		log.debug("Required network with id: " + id);
		Nffg nffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		if(nffg==null)
			throw new VimDriverException("Illegal state. A nffg must be already deployed");
		return NetworkManager.getNetwork(nffg,id,UniversalNodeProxy.getConfiguration(vimInstance).getConfigurationServiceEndpoint());
	}

	@Override
	public Quota getQuota(VimInstance vimInstance) throws VimDriverException {
		log.debug("Required Quota.");
		Quota q = new Quota();
		q.setCores(4);
		q.setFloatingIps(10);
		q.setId("1");
		q.setInstances(10);
		q.setKeyPairs(100);
		q.setRam(2048);
		q.setTenant(vimInstance.getTenant());
		q.setVersion(1);
		return q;
	}

	@Override
	public String getType(VimInstance vimInstance) throws VimDriverException {
		return properties.getProperty("type");
	}

}
