package org.polito.unvim;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import org.polito.model.template.VnfTemplate;
import org.polito.proxy.DatastoreProxy;
import org.polito.proxy.UniversalNodeProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnClient extends VimDriver {
	private static Logger log = LoggerFactory.getLogger(UnClient.class);
	private static Lock lock;

	public static void main(String[] args)
		      throws NoSuchMethodException, IOException, InstantiationException, TimeoutException,
		          IllegalAccessException, InvocationTargetException {
		    UnClient.lock = new ReentrantLock();
		    if (args.length == 6) {
		      PluginStarter.registerPlugin(
		    		  UnClient.class,
		          args[0],
		          args[1],
		          Integer.parseInt(args[2]),
		          Integer.parseInt(args[3]),
		          args[4],
		          args[5]);
		    } else if (args.length == 4) {
		      PluginStarter.registerPlugin(
		    		  UnClient.class,
		          args[0],
		          args[1],
		          Integer.parseInt(args[2]),
		          Integer.parseInt(args[3]));
		    } else
		      PluginStarter.registerPlugin(UnClient.class, "un", "localhost", 8000, 10);
		}

	@Override
	public Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair,
			Set<String> network, Set<String> secGroup, String userData) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NFVImage> listImages(VimInstance vimInstance) throws VimDriverException {
		log.debug("Listing images for VimInstance with name: " + vimInstance.getName());
		List<VnfTemplate> templates = UniversalNodeProxy.getTemplates(vimInstance.getAuthUrl());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Network> listNetworks(VimInstance vimInstance) throws VimDriverException {
		// TODO Auto-generated method stub
		return new ArrayList<Network>();
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
			String keyPair, Set<String> networks, Set<String> securityGroups, String s, Map<String, String> floatingIps,
			Set<Key> keys) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId,
			String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteServerByIdAndWait(VimInstance vimInstance, String id) throws VimDriverException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Network createNetwork(VimInstance vimInstance, Network network) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Network updateNetwork(VimInstance vimInstance, Network network) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet)
			throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId) throws VimDriverException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteNetwork(VimInstance vimInstance, String extId) throws VimDriverException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Network getNetworkById(VimInstance vimInstance, String id) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quota getQuota(VimInstance vimInstance) throws VimDriverException {
		Quota q = new Quota();
		q.setCores(4);
		q.setFloatingIps(0);
		q.setId("1");
		q.setInstances(10);
		q.setKeyPairs(0);
		q.setRam(2048);
		q.setTenant(vimInstance.getTenant());
		q.setVersion(1);
		return q;
	}

	@Override
	public String getType(VimInstance vimInstance) throws VimDriverException {
		// TODO Auto-generated method stub
		return null;
	}

}
