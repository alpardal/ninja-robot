package br.com.nrobot;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import br.com.etyllica.core.animation.OnAnimationFinishListener;
import br.com.etyllica.core.animation.script.OpacityAnimation;
import br.com.etyllica.core.context.UpdateIntervalListener;
import br.com.etyllica.core.event.KeyEvent;
import br.com.etyllica.core.event.MouseButton;
import br.com.etyllica.core.event.PointerEvent;
import br.com.etyllica.core.graphics.Graphic;
import br.com.etyllica.layer.ImageLayer;
import br.com.nrobot.fallen.Bomb;
import br.com.nrobot.fallen.Fallen;
import br.com.nrobot.fallen.Glue;
import br.com.nrobot.fallen.Nut;
import br.com.nrobot.network.client.NRobotClientListener;
import br.com.nrobot.network.client.model.GameState;
import br.com.nrobot.network.server.NRobotBattleServerProtocol;
import br.com.nrobot.network.server.model.NetworkRole;
import br.com.nrobot.player.BlueNinja;
import br.com.nrobot.player.Player;
import br.com.nrobot.player.RobotNinja;
import br.com.nrobot.player.ServerPlayer;

public class StoryModeGame extends Game implements OnAnimationFinishListener, UpdateIntervalListener, NRobotClientListener {

	private ImageLayer background;
	
	private boolean gameIsOver = false;

	private Set<Fallen> pieces = new HashSet<Fallen>();
	
	public boolean isDrawing = false;

	public StoryModeGame(int w, int h, GameState state) {
		super(w, h, state);
	}

	@Override
	public void load() {
		super.load();
		
		loadingInfo = "Loading background";

		background = new ImageLayer("background/forest.png");

		loading = 60;

		loadingInfo = "Carregando Jogador";

		loading = 100;
		
		updateAtFixedRate(50, this);
	}

	@Override
	public void timeUpdate(long now) {

		for(Entry<String, Player> entry : state.players.entrySet()) {
			Player player = entry.getValue();
			player.updatePlayer(now);
			if (me.equals(entry.getKey())) {				
				background.setX(-player.getX());	
			}
		}
	}
		
	@Override
	public void draw(Graphic g) {

		background.draw(g);

		g.setFont(g.getFont().deriveFont(28f));

		int i = 0;
		for(Player player : state.players.values()) {
			g.drawShadow(60+120*i, 60, "Pts: "+Integer.toString(player.getPoints()));
			g.drawShadow(60+120*i, 90, "Item: "+player.getItem());
			
			player.draw(g);
			g.drawShadow(player.getX(), player.getY()-20, player.getName());
			
			drawPlayerModifier(g, player);

			i++;
		}

		isDrawing = true;
		for(ImageLayer layer: pieces) {
			layer.simpleDraw(g);
			//layer.draw(g);
		}
		isDrawing = false;

		if (gameIsOver) {
			gameOver.draw(g);

			g.setOpacity(gameOver.getOpacity());

			g.drawStringShadowX(350, gameOverMessage);

			g.drawStringShadowX(420, "Clique para voltar ao menu inicial...");

			g.resetOpacity();
		}

	}

	private void drawPlayerModifier(Graphic g, Player player) {
		if(ServerPlayer.STATE_FREEZE.equals(player.getState())) {
			ice.simpleDraw(g, player.getX(), player.getY());
		} else if(ServerPlayer.STATE_DEAD.equals(player.getState())) {
			skull.simpleDraw(g, player.getX(), player.getY());
		}
		
		if(ServerPlayer.ITEM_RESSURRECT.equals(player.getItem())) {
			skull.simpleDraw(g, player.getX(), player.getY());
		}
	}

	@Override
	public void updateKeyboard(KeyEvent event) {
		
		if(NetworkRole.SERVER == client.getRole()) {
			if(event.isKeyDown(KeyEvent.VK_ENTER)) {
				client.getProtocol().sendRessurrect();
			}
		}
		
		if (client != null) {
			client.handleEvent(event);
		}
		
		if(event.isKeyDown(KeyEvent.VK_ESC)) {
			nextApplication = new MainMenu(w, h);
		}
	}

	@Override
	public void updateMouse(PointerEvent event) {

		if(gameIsOver) {
			if(event.isButtonDown(MouseButton.MOUSE_BUTTON_LEFT)) {
				nextApplication = new MainMenu(w, h);
			}
		}

	}

	private String gameOverMessage = "Voce nao fez nenhum ponto.";

	@Override
	public void onAnimationFinish(long now) {

		gameIsOver = true;

		int points = state.players.get(me).getPoints(); 

		if(points > 0) {
			if(points == 1) {
				gameOverMessage = "Voce fez um ponto.";
			} else {
				gameOverMessage = "Voce fez "+points+" pontos.";
			}
		}

		OpacityAnimation gameOverAnimation = new OpacityAnimation(gameOver, 10000);
		gameOverAnimation.setInterval(0, 255);

		this.scene.addAnimation(gameOverAnimation);
	}

	@Override
	public void exitClient(String id) {
		state.players.remove(id);
	}

	@Override
	public void joinedClient(String id, String name) {
		addPlayer(id, name);
	}

	@Override
	public void receiveMessage(String id, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(String[] ids) {
		me = ids[0];

		for(int i = 1; i < ids.length; i+=2) {
			addPlayer(ids[i], ids[i+1]);
		}
	}

	private void addPlayer(String id, String name) {
		if(id.startsWith(NRobotBattleServerProtocol.PREFIX_BOT)) {
			RobotNinja player = new RobotNinja(0, 540);
			player.setName(name);
			state.players.put(id, player);	
		} else {
			BlueNinja player = new BlueNinja(0, 540);
			player.setName(name);
			state.players.put(id, player);	
		}
		
	}

	/**
	 * @see ServerPlayer.asText()
	 */
	@Override
	public void updatePositions(String positions) {
		if (!stateReady)
			return;
		
		//System.out.println(positions);
		String[] values = positions.split(" ");

		int attributes = 6;

		for (int i = 0;i < values.length; i += attributes) {
			String id = values[i];

			if(NRobotBattleServerProtocol.PREFIX_NUT.equals(id)) {
				break;
			}

			Player player = state.players.get(id);
			if(player == null) {
				continue;
			}

			int x = Integer.parseInt(values[i+1]);
			int y = Integer.parseInt(values[i+2]);
			String state = values[i+3];
			String item = values[i+4];
			int points = Integer.parseInt(values[i+5]);

			player.setPosition(x, y);
			player.setState(state);
			player.setItem(item);
			player.setPoints(points);
		}
	}

	@Override
	public void updateName(String id, String name) {
		Player player = state.players.get(id);
		player.setName(name);
	}

	@Override
	public void updateSprite(String id, String sprite) {
		Player player = state.players.get(id);
		//player.setSprite(sprite);
	}

}
