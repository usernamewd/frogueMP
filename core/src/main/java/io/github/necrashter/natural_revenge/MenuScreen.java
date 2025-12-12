package io.github.necrashter.natural_revenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.GameWorldRenderer;
import io.github.necrashter.natural_revenge.world.LowResWorldRenderer;
import io.github.necrashter.natural_revenge.world.levels.LevelMenuBg;
import io.github.necrashter.natural_revenge.network.MultiplayerManager;

public class MenuScreen implements Screen {
    final Main game;
    private final Stage stage;

    private final GameWorld world;
    private final GameWorldRenderer worldRenderer;

    public MenuScreen(final Main game) {
        this.game = game;

        stage = new Stage(Main.createViewport());
        Gdx.input.setInputProcessor(stage);

        final TextButton start=new TextButton("Start Game", Main.skin);
        start.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startLevel(1);
            }
        });

        final TextButton levelSelect=new TextButton("Level Select", Main.skin);
        levelSelect.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                levelSelectDialog();
            }
        });

        final TextButton optionsButton = new TextButton("Options", Main.skin);
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new OptionsDialog(null).show(stage);
            }
        });

        final TextButton startServerButton = new TextButton("Start Server", Main.skin);
        startServerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showServerConfigDialog();
            }
        });

        final TextButton findServerButton = new TextButton("Find a Server", Main.skin);
        findServerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showServerBrowserDialog();
            }
        });

        TextButton exit=new TextButton("Exit", Main.skin);
        exit.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Table table=new Table();
        table.setFillParent(true);

        // Center the whole table content for a cleaner look
        table.center();

        table.row().padTop(10);
        table.add(start);
        table.row().padTop(10);
        table.add(levelSelect);
        table.row().padTop(10);
        table.add(startServerButton);
        table.row().padTop(10);
        table.add(findServerButton);
        table.row().padTop(10);
        table.add(optionsButton);
        table.row().padTop(10);
        table.add(exit);

        stage.addActor(table);

        // Background world
        world = new LevelMenuBg(game);
        worldRenderer = new LowResWorldRenderer(world);
    }

    public void startLevel(int level) {
        game.setScreen(game.getLevel(level, 1.0f));
        dispose();
    }

    public void levelSelectDialog() {
        Dialog dialog = new Dialog("Select Level", Main.skin) {
            @Override
            protected void result(Object object) {
                int i = (int) object;
                if (i > 0) startLevel(i);
            }
        };
        dialog.button("Go Back", 0);
        dialog.getButtonTable().row();
        dialog.button("Level 1: Swamp", 1);
        dialog.getButtonTable().row();
        dialog.button("Level 2: Flying", 2);
        dialog.getButtonTable().row();
//        dialog.button("Level 3: Zombie", 3);
//        dialog.getButtonTable().row();
        dialog.button("Boss Rush", 3);
//        dialog.getButtonTable().row();
        dialog.padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Pause Menu").height*1.2f);
        dialog.padLeft(16); dialog.padRight(16);
        dialog.show(stage);
    }

    public void showServerConfigDialog() {
        Dialog dialog = new Dialog("Server Configuration", Main.skin) {
            private boolean friendlyFire = false;
            private String serverName = "Frogue Server";
            private int tcpPort = MultiplayerManager.DEFAULT_TCP_PORT;
            private int udpPort = MultiplayerManager.DEFAULT_UDP_PORT;
            
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    // Start server
                    startMultiplayerServer(friendlyFire, serverName, tcpPort, udpPort);
                }
            }
        };
        
        Table contentTable = new Table();
        
        // Server name
        contentTable.add(new Label("Server Name:", Main.skin)).left().row();
        Label nameLabel = new Label("Frogue Server", Main.skin);
        contentTable.add(nameLabel).left().padBottom(10).row();
        
        // Friendly fire
        contentTable.add(new Label("Friendly Fire:", Main.skin)).left().row();
        Label friendlyFireLabel = new Label("Disabled", Main.skin);
        contentTable.add(friendlyFireLabel).left().padBottom(20).row();
        
        // Port information
        contentTable.add(new Label("Port: " + MultiplayerManager.DEFAULT_TCP_PORT, Main.skin)).left().row();
        
        dialog.getContentTable().add(contentTable);
        dialog.padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Server Configuration").height*1.2f);
        dialog.padLeft(16); dialog.padRight(16);
        
        dialog.button("Cancel", false);
        dialog.button("Start Server", true);
        dialog.show(stage);
    }
    
    public void showServerBrowserDialog() {
        // Use mobile-optimized server browser if on mobile
        if (Main.isMobile()) {
            showMobileServerBrowser();
        } else {
            showDesktopServerBrowser();
        }
    }
    
    private void showDesktopServerBrowser() {
        Dialog dialog = new Dialog("Server Browser", Main.skin);
        
        Table contentTable = new Table();
        contentTable.add(new Label("Searching for servers on local network...", Main.skin)).center();
        dialog.getContentTable().add(contentTable);
        
        dialog.padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Server Browser").height*1.2f);
        dialog.padLeft(16); dialog.padRight(16);
        
        dialog.button("Cancel", false);
        dialog.show(stage);
        
        // Discover servers
        MultiplayerManager.getInstance().discoverServers(MultiplayerManager.DEFAULT_UDP_PORT, new MultiplayerManager.DiscoveredServerCallback() {
            @Override
            public void onServersFound(List<GameClient.ServerInfo> servers) {
                dialog.hide();
                showServerListDialog(servers);
            }
        });
    }
    
    private void showMobileServerBrowser() {
        io.github.necrashter.natural_revenge.ui.MobileServerBrowser mobileBrowser = 
            new io.github.necrashter.natural_revenge.ui.MobileServerBrowser(game, new io.github.necrashter.natural_revenge.ui.MobileServerBrowser.ServerSelectionCallback() {
                @Override
                public void onServerSelected(GameClient.ServerInfo server) {
                    showMobileJoinDialog(server);
                }
                
                @Override
                public void onRefreshRequested() {
                    showMobileServerBrowser(); // Refresh the browser
                }
                
                @Override
                public void onCancel() {
                    // Return to main menu
                }
            });
        
        // Show loading screen first
        Table loadingTable = mobileBrowser.createLoadingTable();
        stage.addActor(loadingTable);
        
        // Start server discovery
        MultiplayerManager.getInstance().discoverServers(MultiplayerManager.DEFAULT_UDP_PORT, 
            new MultiplayerManager.DiscoveredServerCallback() {
                @Override
                public void onServersFound(List<GameClient.ServerInfo> servers) {
                    // Remove loading screen and show server list
                    loadingTable.remove();
                    Table serverListTable = mobileBrowser.createServerListTable(servers);
                    stage.addActor(serverListTable);
                }
            });
    }
    
    private void showServerListDialog(List<GameClient.ServerInfo> servers) {
        Dialog dialog = new Dialog("Available Servers", Main.skin);
        
        Table contentTable = new Table();
        
        if (servers.isEmpty()) {
            contentTable.add(new Label("No servers found", Main.skin)).center();
        } else {
            for (GameClient.ServerInfo server : servers) {
                Table serverRow = new Table();
                
                // Server info
                Table serverInfo = new Table();
                serverInfo.add(new Label(server.serverName, Main.skin)).left().row();
                serverInfo.add(new Label(server.playerCount + "/" + server.maxPlayers + " players", Main.skin, "small")).left();
                
                // Join button
                TextButton joinButton = new TextButton("Join", Main.skin);
                joinButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        dialog.hide();
                        showJoinServerDialog(server);
                    }
                });
                
                serverRow.add(serverInfo).expand().left();
                serverRow.add(joinButton).right();
                contentTable.add(serverRow).padBottom(10).row();
            }
        }
        
        dialog.getContentTable().add(contentTable);
        dialog.padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Available Servers").height*1.2f);
        dialog.padLeft(16); dialog.padRight(16);
        
        dialog.button("Cancel", false);
        dialog.show(stage);
    }
    
    private void showJoinServerDialog(GameClient.ServerInfo server) {
        if (Main.isMobile()) {
            showMobileJoinDialog(server);
        } else {
            showDesktopJoinDialog(server);
        }
    }
    
    private void showDesktopJoinDialog(GameClient.ServerInfo server) {
        Dialog dialog = new Dialog("Join Server", Main.skin) {
            private String playerName = "Player" + (int)(Math.random() * 1000);
            private boolean joinAsSpectator = false;
            
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    joinServer(server, playerName, joinAsSpectator);
                }
            }
        };
        
        Table contentTable = new Table();
        
        contentTable.add(new Label("Server: " + server.serverName, Main.skin)).left().row();
        contentTable.add(new Label("Players: " + server.playerCount + "/" + server.maxPlayers, Main.skin)).left().row();
        contentTable.add(new Label("Friendly Fire: " + (server.friendlyFire ? "ON" : "OFF"), Main.skin)).left().row();
        contentTable.add(new Label("Level: " + server.currentLevel, Main.skin)).left().padBottom(20).row();
        
        // Player name
        contentTable.add(new Label("Player Name:", Main.skin)).left().row();
        Label nameLabel = new Label(playerName, Main.skin);
        contentTable.add(nameLabel).left().padBottom(20).row();
        
        dialog.getContentTable().add(contentTable);
        dialog.padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Join Server").height*1.2f);
        dialog.padLeft(16); dialog.padRight(16);
        
        dialog.button("Cancel", false);
        dialog.button("Join Game", true);
        dialog.show(stage);
    }
    
    private void showMobileJoinDialog(GameClient.ServerInfo server) {
        Dialog dialog = new Dialog("Join Server", Main.skin) {
            private String playerName = "Player" + (int)(Math.random() * 1000);
            private boolean joinAsSpectator = false;
            
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    joinServer(server, playerName, joinAsSpectator);
                }
            }
        };
        
        Table contentTable = new Table();
        
        // Server info with mobile-friendly layout
        Label serverNameLabel = new Label(server.serverName, Main.skin);
        serverNameLabel.setFontScale(1.3f);
        serverNameLabel.setColor(1, 1, 0, 1); // Yellow for server name
        contentTable.add(serverNameLabel).center().padBottom(10).row();
        
        // Server details in a compact format
        Table detailsTable = new Table();
        detailsTable.add(new Label("👥 " + server.playerCount + "/" + server.maxPlayers, Main.skin)).padRight(20);
        detailsTable.add(new Label("🔥 " + (server.friendlyFire ? "ON" : "OFF"), Main.skin)).padRight(20);
        detailsTable.add(new Label("📍 Level " + server.currentLevel, Main.skin));
        contentTable.add(detailsTable).center().padBottom(20).row();
        
        // Player name section
        Label nameLabel = new Label("👤 " + playerName, Main.skin);
        nameLabel.setFontScale(1.1f);
        contentTable.add(nameLabel).center().padBottom(20).row();
        
        // Spectator option
        Label spectatorLabel = new Label(joinAsSpectator ? "👁️ Joining as SPECTATOR" : "🎮 Joining as PLAYER", Main.skin);
        spectatorLabel.setFontScale(1.0f);
        contentTable.add(spectatorLabel).center().padBottom(10).row();
        
        dialog.getContentTable().add(contentTable);
        dialog.padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Join Server").height*1.2f);
        dialog.padLeft(16); dialog.padRight(16);
        
        dialog.button("Cancel", false);
        dialog.button(joinAsSpectator ? "Join as Spectator" : "Join Game", true);
        dialog.show(stage);
        
        // Add touch interaction for toggling spectator mode
        spectatorLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                joinAsSpectator = !joinAsSpectator;
                spectatorLabel.setText(joinAsSpectator ? "👁️ Joining as SPECTATOR" : "🎮 Joining as PLAYER");
            }
        });
    }
    
    private void startMultiplayerServer(boolean friendlyFire, String serverName, int tcpPort, int udpPort) {
        MultiplayerManager.getInstance().initialize(game, new MultiplayerManager.MultiplayerCallback() {
            @Override
            public void onServerStarted(String serverName, boolean friendlyFire) {
                Dialog infoDialog = new Dialog("Server Started", Main.skin) {
                    @Override
                    protected void result(Object object) {
                        // Server is running, stay in menu to wait for players
                    }
                };
                infoDialog.getContentTable().add(new Label("Server is running!\nPort: " + tcpPort + "\nWaiting for players...", Main.skin));
                infoDialog.button("OK", true);
                infoDialog.show(stage);
            }
            
            @Override
            public void onServerStopped() {
                Dialog infoDialog = new Dialog("Server Stopped", Main.skin) {
                    @Override
                    protected void result(Object object) {
                        // Server stopped
                    }
                };
                infoDialog.getContentTable().add(new Label("Server has been stopped", Main.skin));
                infoDialog.button("OK", true);
                infoDialog.show(stage);
            }
            
            @Override
            public void onConnectedToServer(String serverName) {
                // Not used for server
            }
            
            @Override
            public void onDisconnectedFromServer(String reason) {
                // Not used for server
            }
            
            @Override
            public void onServerListReceived(List<GameClient.ServerInfo> servers) {
                // Not used for server
            }
            
            @Override
            public void onGameError(String error) {
                Dialog errorDialog = new Dialog("Error", Main.skin) {
                    @Override
                    protected void result(Object object) {
                        // Handle error
                    }
                };
                errorDialog.getContentTable().add(new Label("Error: " + error, Main.skin));
                errorDialog.button("OK", true);
                errorDialog.show(stage);
            }
        });
        
        MultiplayerManager.getInstance().startServer(friendlyFire, serverName, tcpPort, udpPort);
    }
    
    private void joinServer(GameClient.ServerInfo server, String playerName, boolean asSpectator) {
        // Extract host from address
        String host = server.address.split(":")[0]; // Remove port
        
        MultiplayerManager.getInstance().initialize(game, new MultiplayerManager.MultiplayerCallback() {
            @Override
            public void onServerStarted(String serverName, boolean friendlyFire) {
                // Not used for client
            }
            
            @Override
            public void onServerStopped() {
                // Not used for client
            }
            
            @Override
            public void onConnectedToServer(String serverName) {
                // Connection successful, game screen will be shown by MultiplayerManager
            }
            
            @Override
            public void onDisconnectedFromServer(String reason) {
                Dialog errorDialog = new Dialog("Disconnected", Main.skin) {
                    @Override
                    protected void result(Object object) {
                        // Return to menu
                    }
                };
                errorDialog.getContentTable().add(new Label("Disconnected from server:\n" + reason, Main.skin));
                errorDialog.button("OK", true);
                errorDialog.show(stage);
            }
            
            @Override
            public void onServerListReceived(List<GameClient.ServerInfo> servers) {
                // Not used for client
            }
            
            @Override
            public void onGameError(String error) {
                Dialog errorDialog = new Dialog("Error", Main.skin) {
                    @Override
                    protected void result(Object object) {
                        // Handle error
                    }
                };
                errorDialog.getContentTable().add(new Label("Error: " + error, Main.skin));
                errorDialog.button("OK", true);
                errorDialog.show(stage);
            }
        });
        
        MultiplayerManager.getInstance().connectToServer(
            host, 
            MultiplayerManager.DEFAULT_TCP_PORT, 
            MultiplayerManager.DEFAULT_UDP_PORT, 
            playerName, 
            asSpectator
        );
    }

    @Override
    public void show() {
        Main.music.fadeOut();
    }

    @Override
    public void render(float delta) {
        world.update(delta);
        stage.act(delta);

        double s = (double) TimeUtils.millis() / 100.0;
        double y = 100 * Math.sin(s) + 100;
        double x = 100 * Math.cos(s) + 100;
        double b = y > 100 ? (y - 100) * 0.01 : 0;
//        ScreenUtils.clear((float)b, (float)b, (float)b, 1);
        ScreenUtils.clear(0, 0, 0, 1);
        worldRenderer.render();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        worldRenderer.screenResize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        // World renderer is supposed to dispose world as well.
        worldRenderer.dispose();
        stage.dispose();
    }
}
