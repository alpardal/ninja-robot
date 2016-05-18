package br.com.nrobot.network.server;

import br.com.midnight.model.Peer;
import br.com.midnight.server.TCPServer;
import examples.action.client.ActionClientProtocol;

public class ActionServer extends TCPServer {

	private ActionServerProtocol listener;

	public ActionServer(int port) {
		super(port);

		name = "Action Server";
				
		listener = new ActionServerProtocol(ActionClientProtocol.PREFIX_ACTION);
		
		handshaker = new ActionHandshaker(listener.getPlayers());

		addProtocol(ActionClientProtocol.PREFIX_ACTION, listener);
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	@Override
	public void joinPeer(Peer peer) {
		System.out.println("ActionPeer "+peer.getSessionID()+" connected.");
		
		listener.addPeer(peer);
	}
	
	@Override
	public void leftPeer(Peer peer) {
		System.out.println("Player "+peer.getSessionID()+" disconnected.");
		
		listener.removePeer(peer);
	}
	
}
