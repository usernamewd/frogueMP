package io.github.necrashter.natural_revenge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.network.GameClient;

/**
 * Mobile-optimized server browser with touch-friendly interface
 */
public class MobileServerBrowser {
    
    public interface ServerSelectionCallback {
        void onServerSelected(GameClient.ServerInfo server);
        void onRefreshRequested();
        void onCancel();
    }
    
    private final Main game;
    private final ServerSelectionCallback callback;
    
    public MobileServerBrowser(Main game, ServerSelectionCallback callback) {
        this.game = game;
        this.callback = callback;
    }
    
    /**
     * Create a mobile-optimized server list dialog
     */
    public Table createServerListTable(java.util.List<GameClient.ServerInfo> servers) {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        
        // Header
        Table headerTable = new Table();
        Label titleLabel = new Label("Available Servers", Main.skin);
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);
        headerTable.add(titleLabel).expandX().center().row();
        
        mainTable.add(headerTable).expandX().fillX().padBottom(20).row();
        
        // Server list with scroll capability
        Table serverListTable = new Table();
        
        if (servers.isEmpty()) {
            Label noServersLabel = new Label("No servers found\n\nTip: Make sure you're on the same WiFi network", Main.skin);
            noServersLabel.setAlignment(Align.center);
            noServersLabel.setFontScale(1.2f);
            serverListTable.add(noServersLabel).center().expand();
        } else {
            for (GameClient.ServerInfo server : servers) {
                Table serverRow = createServerRow(server);
                serverListTable.add(serverRow).expandX().fillX().padBottom(15).row();
            }
        }
        
        mainTable.add(serverListTable).expand().fill().row();
        
        // Action buttons
        Table buttonTable = new Table();
        
        TextButton refreshButton = new TextButton("Refresh", Main.skin);
        refreshButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null) {
                    callback.onRefreshRequested();
                }
            }
        });
        
        TextButton cancelButton = new TextButton("Cancel", Main.skin);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null) {
                    callback.onCancel();
                }
            }
        });
        
        // Mobile-friendly button layout
        buttonTable.add(refreshButton).expandX().left().padRight(10);
        buttonTable.add(cancelButton).expandX().right().padLeft(10);
        
        mainTable.add(buttonTable).expandX().fillX().padTop(20).row();
        
        return mainTable;
    }
    
    /**
     * Create a single server row optimized for mobile
     */
    private Table createServerRow(GameClient.ServerInfo server) {
        Table serverRow = new Table();
        serverRow.setBackground(Main.skin.newDrawable("white", 0.1f, 0.1f, 0.1f, 0.8f));
        
        // Server info (left side)
        Table infoTable = new Table();
        
        Label serverNameLabel = new Label(server.serverName, Main.skin);
        serverNameLabel.setFontScale(1.3f);
        infoTable.add(serverNameLabel).left().row();
        
        // Player count with color coding
        String playerCountText = server.playerCount + "/" + server.maxPlayers + " players";
        Label playerCountLabel = new Label(playerCountText, Main.skin);
        
        // Color code based on server fullness
        if (server.playerCount >= server.maxPlayers) {
            playerCountLabel.setColor(1, 0.3f, 0.3f, 1); // Red for full
        } else if (server.playerCount >= server.maxPlayers * 0.8f) {
            playerCountLabel.setColor(1, 0.7f, 0.3f, 1); // Orange for nearly full
        } else {
            playerCountLabel.setColor(0.3f, 1, 0.3f, 1); // Green for available
        }
        
        infoTable.add(playerCountLabel).left().row();
        
        // Server details
        Table detailsTable = new Table();
        
        Label levelLabel = new Label("Level: " + server.currentLevel, Main.skin, "small");
        levelLabel.setFontScale(0.9f);
        detailsTable.add(levelLabel).left().padRight(20);
        
        Label friendlyFireLabel = new Label("FF: " + (server.friendlyFire ? "ON" : "OFF"), Main.skin, "small");
        friendlyFireLabel.setFontScale(0.9f);
        detailsTable.add(friendlyFireLabel).left();
        
        infoTable.add(detailsTable).left().padTop(5).row();
        
        // Join button (right side)
        TextButton joinButton = new TextButton("Join", Main.skin);
        joinButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (server.playerCount >= server.maxPlayers) {
                    // Server is full
                    showFullServerDialog();
                } else {
                    if (callback != null) {
                        callback.onServerSelected(server);
                    }
                }
            }
        });
        
        // Disable button if server is full
        if (server.playerCount >= server.maxPlayers) {
            joinButton.setDisabled(true);
            joinButton.setText("Full");
        }
        
        serverRow.add(infoTable).expandX().left().pad(15);
        serverRow.add(joinButton).right().pad(15);
        
        return serverRow;
    }
    
    /**
     * Show dialog when trying to join a full server
     */
    private void showFullServerDialog() {
        com.badlogic.gdx.scenes.scene2d.ui.Dialog dialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("Server Full", Main.skin) {
            @Override
            protected void result(Object object) {
                // Just close the dialog
            }
        };
        
        dialog.getContentTable().add(new Label("This server is full.\nPlease try another server.", Main.skin)).pad(20);
        dialog.button("OK", true);
        dialog.show(Gdx.app.getGraphics().getGL20() != null ? Main.viewport.getStage() : null);
    }
    
    /**
     * Create loading spinner for server discovery
     */
    public Table createLoadingTable() {
        Table loadingTable = new Table();
        loadingTable.setFillParent(true);
        
        Label loadingLabel = new Label("Searching for servers...", Main.skin);
        loadingLabel.setFontScale(1.3f);
        loadingLabel.setAlignment(Align.center);
        
        Label tipLabel = new Label("Make sure you're on the same WiFi network", Main.skin, "small");
        tipLabel.setFontScale(0.9f);
        tipLabel.setAlignment(Align.center);
        tipLabel.setColor(0.7f, 0.7f, 0.7f, 1);
        
        loadingTable.add(loadingLabel).center().padBottom(10).row();
        loadingTable.add(tipLabel).center().padBottom(20).row();
        
        // Simple loading indicator (dots)
        Table dotsTable = new Table();
        for (int i = 0; i < 3; i++) {
            Label dot = new Label("•", Main.skin);
            dot.setFontScale(2f);
            dot.setColor(0.3f, 0.7f, 1f, 1);
            dotsTable.add(dot).pad(5);
        }
        loadingTable.add(dotsTable).center().row();
        
        TextButton cancelButton = new TextButton("Cancel", Main.skin);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null) {
                    callback.onCancel();
                }
            }
        });
        
        loadingTable.add(cancelButton).center().padTop(20).row();
        
        return loadingTable;
    }
    
    /**
     * Create connection status dialog for mobile
     */
    public Table createConnectionStatusTable(String status, boolean showProgress) {
        Table statusTable = new Table();
        statusTable.setFillParent(true);
        
        Label statusLabel = new Label(status, Main.skin);
        statusLabel.setFontScale(1.2f);
        statusLabel.setAlignment(Align.center);
        statusTable.add(statusLabel).center().padBottom(20).row();
        
        if (showProgress) {
            // Simple progress indicator for mobile
            Table progressTable = new Table();
            for (int i = 0; i < 5; i++) {
                Label progressDot = new Label("●", Main.skin);
                progressDot.setFontScale(1.5f);
                progressDot.setColor(0.3f, 0.7f, 1f, 0.3f + i * 0.15f);
                progressTable.add(progressDot).pad(3);
            }
            statusTable.add(progressTable).center().row();
        }
        
        return statusTable;
    }
}