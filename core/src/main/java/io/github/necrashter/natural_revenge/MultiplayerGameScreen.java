package io.github.necrashter.natural_revenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.github.necrashter.natural_revenge.network.GameClient;
import io.github.necrashter.natural_revenge.network.NetworkMessages;
import io.github.necrashter.natural_revenge.ui.MobileSpectatorControls;
import io.github.necrashter.natural_revenge.ui.MobileKnifeControls;
import io.github.necrashter.natural_revenge.world.player.Knife;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.GameWorldRenderer;
import io.github.necrashter.natural_revenge.world.LowResWorldRenderer;
import io.github.necrashter.natural_revenge.world.levels.Level1Swamp;

/**
 * Multiplayer game screen with spectator support
 */
public class MultiplayerGameScreen implements Screen {
    final Main game;
    public final GameWorld world;
    private final GameWorldRenderer worldRenderer;

    private GameClient client;
    private boolean isSpectator = false;
    private boolean gameOver = false;
    
    // Spectator camera
    private int currentSpectatedPlayer = -1;
    
    // UI elements
    private Label statusLabel;
    private Label spectatorLabel;
    private Label playerListLabel;
    
    // Mobile-specific components
    private MobileSpectatorControls mobileSpectatorControls;
    private Table mobileSpectatorHUD;
    private boolean isMobileSpectatorMode = false;
    
    // Mobile Knife controls
    private MobileKnifeControls mobileKnifeControls;
    private Table mobileKnifeHUD;
    private boolean isKnifeEquipped = false;
    
    public MultiplayerGameScreen(final Main game, GameClient client, int level, float easiness) {
        this.game = game;
        this.client = client;
        
        // Set up client callback
        client.setCallback(new GameClient.MultiplayerCallback() {
            @Override
            public void onConnected(int playerId, String playerName) {
                System.out.println("Connected as player " + playerId);
                updateStatusLabel();
            }
            
            @Override
            public void onDisconnected(String reason) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
            
            @Override
            public void onPlayerJoined(int playerId, String playerName) {
                System.out.println("Player " + playerName + " joined");
                updateStatusLabel();
            }
            
            @Override
            public void onPlayerLeft(int playerId) {
                System.out.println("Player " + playerId + " left");
                updateStatusLabel();
            }
            
            @Override
            public void onGameStateChanged(int level, float easiness, boolean friendlyFire) {
                System.out.println("Game state changed - Level: " + level + ", Friendly Fire: " + friendlyFire);
                updateStatusLabel();
            }
            
            @Override
            public void onPlayerStateChanged(NetworkMessages.PlayerStateMessage state) {
                if (state.playerId != client.getPlayerId()) {
                    // Update remote player state
                    updateRemotePlayer(state);
                }
            }
            
            @Override
            public void onDamageReceived(NetworkMessages.DamageMessage damage) {
                if (client.getPlayerId() == damage.targetPlayerId) {
                    // Handle local player taking damage
                    if (world.player != null) {
                        world.player.health -= damage.damage;
                        if (world.player.health <= 0 && !isSpectator) {
                            becomeSpectator();
                        }
                    }
                }
            }
            
            @Override
            public void onPlayerDied(NetworkMessages.PlayerDeathMessage death) {
                if (death.playerId == client.getPlayerId()) {
                    becomeSpectator();
                }
            }
            
            @Override
            public void onGameOver(NetworkMessages.GameOverMessage gameOver) {
                handleGameOver(gameOver);
            }
            
            @Override
            public void onServerDiscovered(GameClient.ServerInfo server) {
                // Not used in this context
            }
        });
        
        // Create world
        this.world = new Level1Swamp(game, level, easiness);
        this.world.screen = this;
        worldRenderer = new LowResWorldRenderer(world);
        
        isSpectator = client.isSpectator();
        if (isSpectator) {
            becomeSpectator();
        }
        
        setupUI();
        
        // Set up mobile spectator controls if on mobile
        if (Main.isMobile()) {
            setupMobileSpectatorControls();
            setupMobileKnifeControls();
        }
    }
    
    private void setupUI() {
        // Create status labels
        statusLabel = new Label("", Main.skin);
        statusLabel.setAlignment(Align.left);
        statusLabel.setFontScale(0.8f);
        
        spectatorLabel = new Label("", Main.skin);
        spectatorLabel.setAlignment(Align.center);
        spectatorLabel.setFontScale(1.2f);
        spectatorLabel.setColor(Color.YELLOW);
        
        playerListLabel = new Label("", Main.skin);
        playerListLabel.setAlignment(Align.right);
        playerListLabel.setFontScale(0.7f);
        
        updateStatusLabel();
    }
    
    private void updateStatusLabel() {
        StringBuilder status = new StringBuilder();
        status.append("Level: ").append(client.getCurrentLevel()).append("\n");
        status.append("Players: ").append(client.getRemotePlayers().size() + 1).append("\n");
        status.append("Friendly Fire: ").append(client.isFriendlyFire() ? "ON" : "OFF").append("\n");
        status.append("Mode: ").append(isSpectator ? "SPECTATOR" : "PLAYER").append("\n");
        
        statusLabel.setText(status.toString());
        updatePlayerListLabel();
    }
    
    private void updatePlayerListLabel() {
        StringBuilder players = new StringBuilder();
        players.append("Players:\n");
        
        // Add local player
        players.append("* ").append(client.getPlayerName()).append(" (You)\n");
        
        // Add remote players
        for (GameClient.RemotePlayer remotePlayer : client.getRemotePlayers().values()) {
            players.append(remotePlayer.isSpectator ? "~ " : "* ");
            players.append(remotePlayer.playerName);
            if (!remotePlayer.isAlive) {
                players.append(" (Dead)");
            }
            players.append("\n");
        }
        
        playerListLabel.setText(players.toString());
    }
    
    private void updateRemotePlayer(NetworkMessages.PlayerStateMessage state) {
        // Update remote player visual representation
        // This would involve creating/rendering remote player models
        // For now, just update the player list
        updatePlayerListLabel();
    }
    
    private void becomeSpectator() {
        isSpectator = true;
        
        // Disable player input
        if (world.player != null) {
            world.player.inputAdapter.disabled = true;
            world.player.firing1 = false;
            world.player.firing2 = false;
        }
        
        if (Main.isMobile()) {
            // Mobile spectator mode
            spectatorLabel.setText("SPECTATOR MODE\nTap controls below");
            setupMobileSpectatorMode();
        } else {
            // Desktop spectator mode
            spectatorLabel.setText("SPECTATOR MODE\nPress TAB to spectate players");
            setupSpectatorCamera();
        }
    }
    
    private void setupSpectatorCamera() {
        // Disable player collision and physics
        if (world.player != null) {
            world.player.movementInput.setZero();
            // In real implementation, would disable collision detection
        }
    }
    
    private void setupMobileSpectatorControls() {
        mobileSpectatorControls = new MobileSpectatorControls(game, new MobileSpectatorControls.SpectatorCallback() {
            @Override
            public void onSpectatePlayer(int playerId) {
                setSpectatedPlayer(playerId);
            }
            
            @Override
            public void onSpectateNext() {
                spectateNextPlayer();
            }
            
            @Override
            public void onSpectatePrevious() {
                spectatePreviousPlayer();
            }
            
            @Override
            public void onSpectateFree() {
                setFreeCameraMode();
            }
            
            @Override
            public void onExitSpectator() {
                returnToMainMenu();
            }
        });
        
        mobileSpectatorHUD = mobileSpectatorControls.createSpectatorHUD();
        stage.addActor(mobileSpectatorHUD);
    }
    
    private void setupMobileSpectatorMode() {
        isMobileSpectatorMode = true;
        
        // Update mobile spectator controls
        if (mobileSpectatorControls != null) {
            mobileSpectatorControls.updateSpectatorStatus(currentSpectatedPlayer, 
                getCurrentSpectatedPlayerName(), 
                new java.util.ArrayList<>(client.getRemotePlayers().values()));
        }
    }
    
    private void setSpectatedPlayer(int playerId) {
        currentSpectatedPlayer = playerId;
        
        // Update UI
        if (mobileSpectatorControls != null) {
            String playerName = getPlayerName(playerId);
            mobileSpectatorControls.updateSpectatorStatus(playerId, playerName, 
                new java.util.ArrayList<>(client.getRemotePlayers().values()));
        }
    }
    
    private void spectateNextPlayer() {
        var remotePlayers = client.getRemotePlayers();
        java.util.List<GameClient.RemotePlayer> alivePlayers = new java.util.ArrayList<>();
        for (GameClient.RemotePlayer player : remotePlayers.values()) {
            if (player.isAlive && !player.isSpectator) {
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.isEmpty()) {
            setFreeCameraMode();
            return;
        }
        
        // Find current index
        int currentIndex = -1;
        for (int i = 0; i < alivePlayers.size(); i++) {
            if (alivePlayers.get(i).playerId == currentSpectatedPlayer) {
                currentIndex = i;
                break;
            }
        }
        
        // Move to next player
        int nextIndex = (currentIndex + 1) % alivePlayers.size();
        setSpectatedPlayer(alivePlayers.get(nextIndex).playerId);
    }
    
    private void spectatePreviousPlayer() {
        var remotePlayers = client.getRemotePlayers();
        java.util.List<GameClient.RemotePlayer> alivePlayers = new java.util.ArrayList<>();
        for (GameClient.RemotePlayer player : remotePlayers.values()) {
            if (player.isAlive && !player.isSpectator) {
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.isEmpty()) {
            setFreeCameraMode();
            return;
        }
        
        // Find current index
        int currentIndex = -1;
        for (int i = 0; i < alivePlayers.size(); i++) {
            if (alivePlayers.get(i).playerId == currentSpectatedPlayer) {
                currentIndex = i;
                break;
            }
        }
        
        // Move to previous player
        int prevIndex = (currentIndex - 1 + alivePlayers.size()) % alivePlayers.size();
        setSpectatedPlayer(alivePlayers.get(prevIndex).playerId);
    }
    
    private void setFreeCameraMode() {
        currentSpectatedPlayer = -1;
        
        if (mobileSpectatorControls != null) {
            mobileSpectatorControls.updateSpectatorStatus(-1, null, 
                new java.util.ArrayList<>(client.getRemotePlayers().values()));
        }
    }
    
    private String getCurrentSpectatedPlayerName() {
        if (currentSpectatedPlayer == -1) return null;
        return getPlayerName(currentSpectatedPlayer);
    }
    
    private String getPlayerName(int playerId) {
        GameClient.RemotePlayer player = client.getRemotePlayers().get(playerId);
        return player != null ? player.playerName : "Unknown";
    }
    
    private void returnToMainMenu() {
        game.setScreen(new MenuScreen(game));
        dispose();
    }
    
    private void setupMobileKnifeControls() {
        mobileKnifeControls = new MobileKnifeControls(null, new MobileKnifeControls.KnifeCallback() {
            @Override
            public void onAttack() {
                performKnifeAttack();
            }
            
            @Override
            public void onToggleAttackMode() {
                // Toggle between different attack modes if implemented
            }
        });
        
        mobileKnifeHUD = mobileKnifeControls.createKnifeHUD();
        mobileKnifeHUD.setVisible(false); // Hidden by default
        stage.addActor(mobileKnifeHUD);
    }
    
    private void checkWeaponStatus() {
        if (world.player != null && world.player.activeWeapon != null) {
            boolean wasKnifeEquipped = isKnifeEquipped;
            isKnifeEquipped = world.player.activeWeapon instanceof Knife;
            
            // Show/hide mobile knife controls based on weapon
            if (Main.isMobile()) {
                if (isKnifeEquipped && !wasKnifeEquipped) {
                    // Just equipped knife
                    mobileKnifeHUD.setVisible(true);
                    if (mobileSpectatorHUD != null) {
                        mobileSpectatorHUD.setVisible(false); // Hide spectator controls
                    }
                } else if (!isKnifeEquipped && wasKnifeEquipped) {
                    // Just unequipped knife
                    mobileKnifeHUD.setVisible(false);
                    if (isSpectator) {
                        mobileSpectatorHUD.setVisible(true); // Show spectator controls again
                    }
                }
                
                // Update knife controls if visible
                if (isKnifeEquipped && mobileKnifeControls != null) {
                    mobileKnifeControls.updateKnifeStatus();
                }
            }
        }
    }
    
    private void performKnifeAttack() {
        if (world.player != null && world.player.activeWeapon instanceof Knife) {
            Knife knife = (Knife) world.player.activeWeapon;
            if (knife.isReady()) {
                // Send attack to server if in multiplayer
                if (client != null && client.isConnected()) {
                    client.sendWeaponFired(0, 0, 0, 0); // Weapon type 0 for knife
                }
                
                // Perform attack locally
                knife.forceAttack();
                
                // Show feedback
                if (mobileKnifeControls != null) {
                    mobileKnifeControls.showAttackFeedback("SLICE!");
                }
            }
        }
    }
    
    private void handleGameOver(NetworkMessages.GameOverMessage gameOver) {
        this.gameOver = true;
        
        spectatorLabel.setText("GAME OVER\n" + gameOver.winner + "\nReturning to menu...");
        
        // Return to main menu after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                game.setScreen(new MenuScreen(game));
                dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @Override
    public void render(float delta) {
        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!gameOver) {
                // Show pause menu
                pause();
            }
        }
        
        if (isSpectator && !Main.isMobile() && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            cycleSpectatedPlayer(); // Desktop spectator cycling
        }
        
        // Mobile spectator controls are handled through touch events in mobileSpectatorControls
        
        // Send input to server (if not spectator)
        if (!isSpectator && world.player != null) {
            client.sendInput(
                world.player.movementInput.x,
                world.player.movementInput.y,
                world.player.firing1,
                world.player.firing2,
                false, // jump - would need to detect jump key press
                world.player.shouldReload,
                -1 // weapon change - would need to detect weapon change
            );
        }
        
        // Update world
        world.update(delta);
        
        // Check weapon status (for mobile knife controls)
        checkWeaponStatus();
        
        // Render world
        worldRenderer.render();
        
        // Update UI
        updateStatusLabel();
        
        // Update mobile controls
        if (Main.isMobile()) {
            updateMobileControls();
        }
    }
    
    private void updateMobileControls() {
        // Update mobile spectator controls
        if (isSpectator && mobileSpectatorControls != null) {
            mobileSpectatorControls.updateSpectatorStatus(currentSpectatedPlayer, 
                getCurrentSpectatedPlayerName(), 
                new java.util.ArrayList<>(client.getRemotePlayers().values()));
            mobileSpectatorControls.updateButtonStates(new java.util.ArrayList<>(client.getRemotePlayers().values()));
        }
        
        // Update mobile knife controls
        if (isKnifeEquipped && mobileKnifeControls != null && world.player != null && world.player.activeWeapon instanceof Knife) {
            mobileKnifeControls.updateKnifeStatus();
        }
    }
    
    private void cycleSpectatedPlayer() {
        var remotePlayers = client.getRemotePlayers();
        if (remotePlayers.isEmpty()) {
            currentSpectatedPlayer = -1;
            return;
        }
        
        // Get list of alive players
        java.util.List<GameClient.RemotePlayer> alivePlayers = new java.util.ArrayList<>();
        for (GameClient.RemotePlayer player : remotePlayers.values()) {
            if (player.isAlive && !player.isSpectator) {
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.isEmpty()) {
            currentSpectatedPlayer = -1;
            return;
        }
        
        // Find current spectated player index
        int currentIndex = -1;
        for (int i = 0; i < alivePlayers.size(); i++) {
            if (alivePlayers.get(i).playerId == currentSpectatedPlayer) {
                currentIndex = i;
                break;
            }
        }
        
        // Move to next player
        int nextIndex = (currentIndex + 1) % alivePlayers.size();
        currentSpectatedPlayer = alivePlayers.get(nextIndex).playerId;
        
        spectatorLabel.setText("SPECTATING: " + alivePlayers.get(nextIndex).playerName);
    }
    
    @Override
    public void show() {
        Main.music.fadeOut();
    }
    
    @Override
    public void hide() {
        
    }
    
    @Override
    public void pause() {
        if (!gameOver) {
            // Show pause dialog
            // In real implementation, would show pause menu with options to disconnect, etc.
        }
    }
    
    @Override
    public void resume() {
        
    }
    
    @Override
    public void resize(int width, int height) {
        worldRenderer.screenResize(width, height);
    }
    
    @Override
    public void dispose() {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        worldRenderer.dispose();
    }
    
    // Callback methods for GameScreen compatibility
    public void playerDied() {
        if (client != null && client.isConnected()) {
            client.sendPlayerDeath();
        }
    }
    
    public void playerHurt() {
        // Handle hurt feedback
    }
    
    public void gameOver(boolean win) {
        // Handle single-player game over (not used in multiplayer)
    }
    
    public void mainMenu() {
        game.setScreen(new MenuScreen(game));
        dispose();
    }
    
    public void restart() {
        // Handle restart (not typically used in multiplayer)
    }
    
    public void nextLevel() {
        // Handle level progression (server-managed in multiplayer)
    }
}