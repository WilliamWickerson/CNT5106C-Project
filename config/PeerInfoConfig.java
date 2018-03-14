package config;

import java.util.List;
import java.util.ArrayList;

public class PeerInfoConfig {
	
	private ArrayList<PeerInfo> peers;
	
	public PeerInfoConfig() {
		//TODO: read in peers from PeerInfo.cfg
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
