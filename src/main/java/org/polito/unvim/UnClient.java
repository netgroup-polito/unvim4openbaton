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
import java.util.concurrent.locks.ReentrantLock;import org.openbaton.catalogue.mano.common.DeploymentFlavour;
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
		          5,
		          args[4],
		          args[5]);
		    } else if (args.length == 4) {
		      PluginStarter.registerPlugin(
		    		  UnClient.class,
		          args[0],
		          args[1],
		          Integer.parseInt(args[2]),
		          5);
		    } else
		      PluginStarter.registerPlugin(UnClient.class, "unvim", "localhost", 5672, 5);
		}

	@Override
	public Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair,
			Set<String> network, Set<String> secGroup, String userData) throws VimDriverException {
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
		Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
		if(nffg!=null)
			servers = ComputeManager.getServers(managementNffg,nffg,UniversalNodeProxy.getConfiguration(vimInstance).getConfigurationServiceEndpoint());
		return servers;
	}

	@Override
	public List<Network> listNetworks(VimInstance vimInstance) throws VimDriverException {
		log.debug("Listing networks for VimInstance with name: " + vimInstance.getName());
		Nffg nffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		List<Network> networks = NetworkManager.getNetworks(nffg,UniversalNodeProxy.getConfiguration(vimInstance).getConfigurationServiceEndpoint());
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
			String keyPair, Set<String> networks, Set<String> securityGroups, String userData, Map<String, String> floatingIps,
			Set<Key> keys) throws VimDriverException {
		try
			{
			log.debug("New server required:");
			log.debug("hostname: " + (hostname==null? "null":hostname) + ", image: " + (image==null? "null":image) + ", extId: " + (extId==null? "null":extId) +  ", keyPair: " + (keyPair==null? "null":keyPair) + ", networks: " + (networks==null? "null":networks) + ", securityGroups: " + (securityGroups==null? "null":securityGroups) + ", userData: " + (userData==null? "null":userData) + ", floatingIps: " + (floatingIps==null? "null":floatingIps) + ", keys: " + (keys==null? "null":keys) );
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
			Nffg nffg, managementNffg;
			String serverId;
			synchronized(un_lock)
			{
				nffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
				managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
				if(nffg==null || managementNffg==null)
					throw new VimDriverException("Illegal state. A tenant nffg + management nffg must be already deployed");
				// Create the server
				serverId = ComputeManager.createServer(nffg, hostname, templateId, keyPair, networks, securityGroups, userData);
				UniversalNodeProxy.sendNFFG(vimInstance, nffg);
			}
			UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
			try
			{
				server = ComputeManager.getServerById(managementNffg, nffg, serverId, unConfig.getConfigurationServiceEndpoint());
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
						ComputeManager.assigneFloatingIps(managementNffg,server,floatingIps, unConfig.getConfigurationServiceEndpoint(), unConfig.getExternalNetwork(), unConfig.getFloatingIpPool());
					}
				}
			} catch (Exception e) {
				log.debug("An error occurs during the creation of the server with name: " + hostname);
				deleteServerByIdAndWait(vimInstance, serverId);
				throw new VimDriverException(e.getMessage());
			}
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
			String keyPair, Set<String> networks, Set<String> securityGroups, String userData) throws VimDriverException {
		return launchInstanceAndWait(vimInstance, hostname, image, extId, keyPair, networks, securityGroups, userData, null, null);
	}

	public void deleteServerById(VimInstance vimInstance, String id) throws VimDriverException {
		synchronized(un_lock)
		{
			log.debug("Delete required for server with id: " + id);
			Nffg nffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
			Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
			if(nffg==null || managementNffg==null)
				throw new VimDriverException("Illegal state. A tenant nffg + management nffg must be already deployed");
			UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
			ComputeManager.destroyServer(managementNffg, nffg, id, unConfig.getConfigurationServiceEndpoint(),true);
			UniversalNodeProxy.sendNFFG(vimInstance, nffg);
		}
	}

	@Override
	public void deleteServerByIdAndWait(VimInstance vimInstance, String id) throws VimDriverException {
		synchronized(un_lock)
		{
			log.debug("Delete required for server with id: " + id);
			Nffg nffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
			Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
			if(nffg==null || managementNffg==null)
				throw new VimDriverException("Illegal state. A tenant nffg + management nffg must be already deployed");
			UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
			synchronized(config_lock)
			{
				ComputeManager.destroyServer(managementNffg, nffg, id, unConfig.getConfigurationServiceEndpoint(),false);
			}
			UniversalNodeProxy.sendNFFG(vimInstance, nffg);
		}
	}

	@Override
	public Network createNetwork(VimInstance vimInstance, Network network) throws VimDriverException {
		log.debug("New network required:");
		log.debug(network.toString());
		Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
		if(managementNffg==null)
		{
			List<String> unPhisicalPorts = UniversalNodeProxy.getConfiguration(vimInstance).getUnPhisicalPorts();
			if(unPhisicalPorts.size()==0)
				throw new VimDriverException("The Universal Node has 0 ports able to reach external network!");
			managementNffg = NffgManager.createBootNffg(MANAGEMENT_GRAPH);
			NetworkManager.createManagementNetwork(managementNffg,unPhisicalPorts);
			UniversalNodeProxy.sendNFFG(vimInstance, managementNffg);
		}
		Nffg tenantNffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		if(tenantNffg==null)
			tenantNffg = NffgManager.createBootNffg(vimInstance.getTenant());
		NetworkManager.createNetwork(managementNffg, tenantNffg, network);
		UniversalNodeProxy.sendNFFG(vimInstance, managementNffg);
		UniversalNodeProxy.sendNFFG(vimInstance, tenantNffg);
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
		Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
		Nffg tenantNffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		if(managementNffg==null || tenantNffg==null)
			throw new VimDriverException("Illegal state");
		UnConfiguration unConfig = UniversalNodeProxy.getConfiguration(vimInstance);
		NetworkManager.createSubnet(managementNffg, tenantNffg, createdNetwork, subnet, unConfig.getDatastoreEndpoint());
		UniversalNodeProxy.sendNFFG(vimInstance, managementNffg);
		UniversalNodeProxy.sendNFFG(vimInstance, tenantNffg);
		synchronized (config_lock) {
			NetworkManager.configureSubnet(managementNffg, tenantNffg,createdNetwork,subnet,properties,unConfig.getConfigurationServiceEndpoint());
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
		Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
		Nffg tenantNffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		if(managementNffg==null || tenantNffg==null)
			throw new VimDriverException("Illegal state. A nffg must be already deployed");
		String configurationServiceEndpoint = UniversalNodeProxy.getConfiguration(vimInstance).getConfigurationServiceEndpoint();
		NetworkManager.configureSubnet(managementNffg,tenantNffg,updatedNetwork,subnet,properties,configurationServiceEndpoint);
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
		if(tenantNffg==null)
			throw new VimDriverException("Illegal state. A nffg must be already deployed");
		NetworkManager.deleteSubnet(tenantNffg, managementNffg, existingSubnetExtId);
		UniversalNodeProxy.sendNFFG(vimInstance, managementNffg);
		UniversalNodeProxy.sendNFFG(vimInstance, tenantNffg);
		return true;
	}

	@Override
	public boolean deleteNetwork(VimInstance vimInstance, String extId) throws VimDriverException {
		log.debug("Delete required for network with id: " + extId);
		Nffg managementNffg = UniversalNodeProxy.getNFFG(vimInstance, MANAGEMENT_GRAPH);
		Nffg tenantNffg = UniversalNodeProxy.getNFFG(vimInstance, vimInstance.getTenant());
		if(tenantNffg==null)
			throw new VimDriverException("Illegal state. A nffg must be already deployed");
		NetworkManager.deleteNetwork(tenantNffg, managementNffg, extId);
		UniversalNodeProxy.sendNFFG(vimInstance, managementNffg);
		UniversalNodeProxy.sendNFFG(vimInstance, tenantNffg);
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
