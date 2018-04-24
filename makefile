all: peerProcess.class StartRemotePeers.class

peerProcess.class: peerProcess.java config/*.java connection/*.java logger/*.java utility/*.java
	javac peerProcess.java

StartRemotePeers.class: StartRemotePeers.java config/PeerInfoConfig.java config/PeerInfo.java
	javac -cp jsch-0.1.54.jar:. StartRemotePeers.java
