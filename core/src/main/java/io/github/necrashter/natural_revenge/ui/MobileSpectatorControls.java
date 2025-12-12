package io.github.necrashter.natural_revenge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.network.GameClient;

import java.util.List;

/**
 * Mobile-optimized spectator controls and UI
 */
public class MobileSpectatorControls {
    
    public interface SpectatorCallback {
        void onSpectatePlayer(int playerId);
        void onSpectateNext();
        void onSpectatePrevious();
        void onSpectateFree();
        void onExitSpectator();
    }
    
    private final Main game;
    private final SpectatorCallback callback;
    
    // UI components
    private Label spectatorStatusLabel;
    private Label playerListLabel;
    private TextButton nextPlayerButton;
    private TextButton previousPlayerButton;
    private TextButton freeCameraButton;
    private TextButton exitSpectatorButton;
    private Container<Label> statusContainer;
    private Container<Label> playerListContainer;
    
    public MobileSpectatorControls(Main game, SpectatorCallback callback) {
        this.game = game;
        this.callback = callback;
    }
    
    /**
     * Create mobile spectator HUD layout
     */
    public Table createSpectatorHUD() {
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        
        // Top status bar
        Table statusBar = new Table();
        statusBar.setBackground(Main.skin.newDrawable("white", 0, 0, 0, 0.7f));
        
        spectatorStatusLabel = new Label("SPECTATOR MODE", Main.skin);
        spectatorStatusLabel.setFontScale(1.2f);
        spectatorStatusLabel.setAlignment(Align.center);
        spectatorStatusLabel.setColor(1, 1, 0, 1); // Yellow text
        
        statusBar.add(spectatorStatusLabel).expand().center();
        
        Container<Label> statusContainer = new Container<>(statusBar);
        statusContainer.setFillParent(true);
        statusContainer.top().pad(10);
        this.statusContainer = statusContainer;
        
        hudTable.add(statusContainer).expandX().fillX().row();
        
        // Bottom controls panel
        Table controlsPanel = new Table();
        controlsPanel.setBackground(Main.skin.newDrawable("white", 0, 0, 0, 0.7f));
        
        // Player selection buttons
        Table buttonRow = new Table();
        
        previousPlayerButton = createMobileButton("◀", "Previous Player");
        previousPlayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null) {
                    callback.onSpectatePrevious();
                }
            }
        });
        
        freeCameraButton = createMobileButton("🎥", "Free Camera");
        freeCameraButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null) {
                    callback.onSpectateFree();
                }
            }
        });
        
        nextPlayerButton = createMobileButton("▶", "Next Player");
        nextPlayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null) {
                    callback.onSpectateNext();
                }
            }
        });
        
        buttonRow.add(previousPlayerButton).expandX().left().pad(10);
        buttonRow.add(freeCameraButton).center().pad(10);
        buttonRow.add(nextPlayerButton).expandX().right().pad(10);
        
        controlsPanel.add(buttonRow).expandX().fillX().row();
        
        // Player list display
        playerListLabel = new Label("", Main.skin);
        playerListLabel.setFontScale(0.8f);
        playerListLabel.setAlignment(Align.left);
        playerListLabel.setColor(0.9f, 0.9f, 0.9f, 1);
        
        Container<Label> playerListContainer = new Container<>(playerListLabel);
        playerListContainer.setFillParent(true);
        playerListContainer.center().pad(20);
        this.playerListContainer = playerListContainer;
        
        // Exit button (top-right corner)
        exitSpectatorButton = new TextButton("Exit", Main.skin);
        exitSpectatorButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null) {
                    callback.onExitSpectator();
                }
            }
        });
        
        Container<TextButton> exitContainer = new Container<>(exitSpectatorButton);
        exitContainer.setFillParent(true);
        exitContainer.top().right().pad(10);
        
        // Add all components
        hudTable.add(controlsPanel).expandX().fillX().bottom().row();
        hudTable.add(playerListContainer).expand().center();
        hudTable.add(exitContainer).expandX().fillX().row();
        
        return hudTable;
    }
    
    /**
     * Create a mobile-friendly button
     */
    private TextButton createMobileButton(String text, String tooltip) {
        TextButton button = new TextButton(text, Main.skin);
        
        // Make buttons larger for mobile touch
        button.getLabel().setFontScale(1.5f);
        
        // Add touch feedback
        button.addListener(new ClickListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                button.setColor(0.7f, 0.7f, 0.7f, 1);
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                button.setColor(1, 1, 1, 1);
            }
        });
        
        return button;
    }
    
    /**
     * Update spectator status display
     */
    public void updateSpectatorStatus(int currentSpectatedPlayerId, String currentSpectatedPlayerName, 
                                    List<GameClient.RemotePlayer> allPlayers) {
        if (currentSpectatedPlayerId == -1) {
            spectatorStatusLabel.setText("SPECTATOR MODE - FREE CAMERA");
            spectatorStatusLabel.setColor(0.5f, 1f, 0.5f, 1); // Green for free camera
        } else if (currentSpectatedPlayerName != null) {
            spectatorStatusLabel.setText("SPECTATING: " + currentSpectatedPlayerName);
            spectatorStatusLabel.setColor(1, 1, 0, 1); // Yellow for spectating player
        } else {
            spectatorStatusLabel.setText("SPECTATOR MODE");
            spectatorStatusLabel.setColor(1, 1, 0, 1); // Yellow default
        }
        
        updatePlayerList(allPlayers, currentSpectatedPlayerId);
    }
    
    /**
     * Update the player list display
     */
    private void updatePlayerList(List<GameClient.RemotePlayer> players, int currentSpectatedPlayerId) {
        StringBuilder playerList = new StringBuilder();
        playerList.append("Players:\n\n");
        
        for (GameClient.RemotePlayer player : players) {
            String prefix = "";
            if (player.playerId == currentSpectatedPlayerId) {
                prefix = "▶ "; // Arrow for currently spectated player
            } else if (!player.isAlive) {
                prefix = "☠ "; // Skull for dead players
            } else if (player.isSpectator) {
                prefix = "~ "; // Tilde for spectators
            } else {
                prefix = "• "; // Dot for alive players
            }
            
            playerList.append(prefix).append(player.playerName);
            
            if (!player.isAlive) {
                playerList.append(" (Dead)");
            } else if (player.isSpectator) {
                playerList.append(" (Spectating)");
            }
            
            playerList.append("\n");
        }
        
        if (players.isEmpty()) {
            playerList.append("No other players");
        }
        
        playerListLabel.setText(playerList.toString());
    }
    
    /**
     * Update button states based on available players
     */
    public void updateButtonStates(List<GameClient.RemotePlayer> players) {
        boolean hasPlayers = !players.isEmpty();
        boolean hasAlivePlayers = players.stream().anyMatch(p -> p.isAlive && !p.isSpectator);
        
        // Enable/disable buttons based on available players
        nextPlayerButton.setDisabled(!hasAlivePlayers);
        previousPlayerButton.setDisabled(!hasAlivePlayers);
        
        // Visual feedback for disabled buttons
        float alpha = hasAlivePlayers ? 1.0f : 0.5f;
        nextPlayerButton.getColor().a = alpha;
        previousPlayerButton.getColor().a = alpha;
    }
    
    /**
     * Show mobile-specific spectator help dialog
     */
    public void showSpectatorHelp() {
        com.badlogic.gdx.scenes.scene2d.ui.Dialog helpDialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("Spectator Controls", Main.skin) {
            @Override
            protected void result(Object object) {
                // Just close the dialog
            }
        };
        
        Table contentTable = new Table();
        
        String helpText = "📱 MOBILE SPECTATOR CONTROLS:\n\n" +
                         "▶ NEXT PLAYER: Switch to next alive player\n" +
                         "◀ PREVIOUS PLAYER: Switch to previous alive player\n" +
                         "🎥 FREE CAMERA: Move camera freely\n" +
                         "EXIT: Return to main menu\n\n" +
                         "💡 TIP: Tap the player list to quickly\n" +
                         "    select a specific player to watch";
        
        Label helpLabel = new Label(helpText, Main.skin);
        helpLabel.setFontScale(1.0f);
        helpLabel.setAlignment(Align.left);
        
        contentTable.add(helpLabel).pad(20);
        helpDialog.getContentTable().add(contentTable);
        
        helpDialog.button("Got it!", true);
        helpDialog.show(Gdx.app.getGraphics().getGL20() != null ? Main.viewport.getStage() : null);
    }
    
    /**
     * Create quick spectate player selection grid (for tablets/large screens)
     */
    public Table createPlayerSelectionGrid(List<GameClient.RemotePlayer> players, int currentSpectatedPlayerId) {
        Table gridTable = new Table();
        
        // Only show on larger screens or when requested
        float screenWidth = Gdx.graphics.getWidth();
        if (screenWidth < 800) {
            return new Table(); // Return empty table for small screens
        }
        
        int columns = Math.min(3, (int) Math.sqrt(players.size()));
        columns = Math.max(1, columns);
        
        for (int i = 0; i < players.size(); i++) {
            GameClient.RemotePlayer player = players.get(i);
            
            TextButton playerButton = new TextButton(player.playerName, Main.skin);
            
            // Visual state for player button
            if (player.playerId == currentSpectatedPlayerId) {
                playerButton.setColor(1, 1, 0, 1); // Yellow for current
            } else if (!player.isAlive) {
                playerButton.setColor(0.5f, 0.5f, 0.5f, 1); // Gray for dead
                playerButton.setText(player.playerName + " (Dead)");
            } else if (player.isSpectator) {
                playerButton.setColor(0.7f, 0.7f, 1f, 1); // Light blue for spectating
                playerButton.setText(player.playerName + " (Spectating)");
            } else {
                playerButton.setColor(0.3f, 1f, 0.3f, 1); // Green for alive
            }
            
            playerButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (callback != null && player.isAlive && !player.isSpectator) {
                        callback.onSpectatePlayer(player.playerId);
                    }
                }
            });
            
            gridTable.add(playerButton).expand().fill().pad(5);
            
            if ((i + 1) % columns == 0) {
                gridTable.row();
            }
        }
        
        return gridTable;
    }
    
    /**
     * Create mobile-friendly connection status display
     */
    public Table createConnectionStatusTable(String status, boolean isConnected) {
        Table statusTable = new Table();
        statusTable.setFillParent(true);
        
        Label statusLabel = new Label(status, Main.skin);
        statusLabel.setFontScale(1.2f);
        statusLabel.setAlignment(Align.center);
        
        if (isConnected) {
            statusLabel.setColor(0.3f, 1f, 0.3f, 1); // Green for connected
        } else {
            statusLabel.setColor(1f, 0.3f, 0.3f, 1); // Red for disconnected
        }
        
        statusTable.add(statusLabel).center().expand();
        
        return statusTable;
    }
}