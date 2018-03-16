package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.BufferPoolMXBean;
import java.util.List;
import java.util.ArrayList;

public class PeerInfoConfig {
	
	private ArrayList<PeerInfo> peers = new ArrayList<>();
	
	public PeerInfoConfig() {
		//TODO: read in peers from PeerInfo.cfg
		File configFile = new File("PeerInfo.cfg");
		if (!configFile.exists()) {
			System.out.println("Error: PeerInfo.cfg does not exist");
			System.exit(0);
		}
		String tempLine = null;
		try{
			BufferedReader peerInfoFromCfg = new BufferedReader(new FileReader(configFile));
			while((tempLine = peerInfoFromCfg.readLine()) != null) {
				this.peers.add(new PeerInfo(tempLine));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//peers.add(new PeerInfo(line)); //basically
	}



	public PeerInfo getPeer(int id) {
		for (PeerInfo peer : peers) {
			if (id == peer.getId())
				return peer;
		}
		return null;
	}
	
	public List<PeerInfo> getPeers() {
		return peers;
	}

}
