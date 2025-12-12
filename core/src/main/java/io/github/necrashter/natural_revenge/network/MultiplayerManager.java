package io.github.necrashter.natural_revenge.network;

import com.badlogic.gdx.Game;
import io.github.necrashter.natural_revenge.MenuScreen;
import io.github.necrashter.natural_revenge.MultiplayerGameScreen;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.levels.Level1Swamp;
import io.github.necrashter.natural_revenge.world.levels.Level2Flying;
import io.github.necrashter.natural_revenge.world.levels.LevelBossRush;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manager class for coordinating multiplayer game sessions
 */
public class MultiplayerManager {
    private static MultiplayerManager instance;
    
    private GameServer server;
    private GameClient client;
    private Main game;
    private boolean isServerMode = false;
    private boolean isClientMode = false;
    
    // Server management
    private ScheduledExecutorService serverExecutor;
    private int serverTcpPort = 27960;
    private int serverUdpPort = 27960;
    
    // Client management
    private String serverHost;
    private int serverTcpPortClient = 27960;
    private int serverUdpPortClient = 27960;
    private String playerName = "Player";
    private boolean joinAsSpectator = false;
    
    // Game state
    private MultiplayerCallback callback;
    private ConcurrentHashMap<String, Object> gameState = new ConcurrentHashMap<>();
    
    public interface MultiplayerCallback {
        void onServerStarted(String serverName, boolean friendlyFire);
        void onServerStopped();
        void onConnectedToServer(String serverName);
        void onDisconnectedFromServer(String reason);
        void onServerListReceived(List<GameClient.ServerInfo> servers);
        void onGameError(String error);
    }
    
    public interface DiscoveredServerCallback {
        void onServersFound(List<GameClient.ServerInfo> servers);
    }
    
    private MultiplayerManager() {
        // Singleton pattern
    }
    
    public static MultiplayerManager getInstance() {
        if (instance == null) {
            instance = new MultiplayerManager();
        }
        return instance;
    }
    
    public void initialize(Main game, MultiplayerCallback callback) {
        this.game = game;
        this.callback = callback;
    }
    
    // Server methods
    public void startServer(boolean friendlyFire, String serverName, int tcpPort, int udpPort) {
        if (isServerMode || isClientMode) {
            if (callback != null) {
                callback.onGameError("Already in a multiplayer session");
            }
            return;
        }
        
        this.serverTcpPort = tcpPort;
        this.serverUdpPort = udpPort;
        
        new Thread(() -> {
            try {
                server = new GameServer();
                server.start(tcpPort, udpPort, friendlyFire, serverName);
                isServerMode = true;
                
                // Start server update thread
                serverExecutor = Executors.newSingleThreadScheduledExecutor();
                serverExecutor.scheduleAtFixedRate(() -> {
                    if (server != null && server.isRunning()) {
                        server.update(0.016f); // 60 FPS update
                    }
                }, 0, 16, TimeUnit.MILLISECONDS);
                
                if (callback != null) {
                    callback.onServerStarted(serverName, friendlyFire);
                }
                
                // Automatically start single-player for server host
                game.setScreen(new MenuScreen(game));
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onGameError("Failed to start server: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        
        if (serverExecutor != null) {
            serverExecutor.shutdown();
            serverExecutor = null;
        }
        
        isServerMode = false;
        
        if (callback != null) {
            callback.onServerStopped();
        }
    }
    
    // Client methods
    public void connectToServer(String host, int tcpPort, int udpPort, String playerName, boolean asSpectator) {
        if (isServerMode || isClientMode) {
            if (callback != null) {
                callback.onGameError("Already in a multiplayer session");
            }
            return;
        }
        
        this.serverHost = host;
        this.serverTcpPortClient = tcpPort;
        this.serverUdpPortClient = udpPort;
        this.playerName = playerName;
        this.joinAsSpectator = asSpectator;
        
        client = new GameClient();
        client.setCallback(new GameClient.MultiplayerCallback() {
            @Override
            public void onConnected(int playerId, String playerName) {
                System.out.println("Connected to server as player " + playerId);
                
                // Start multiplayer game screen
                game.setScreen(new MultiplayerGameScreen(game, client, 1, 1.0f));
                
                if (callback != null) {
                    callback.onConnectedToServer("Connected");
                }
            }
            
            @Override
            public void onDisconnected(String reason) {
                System.out.println("Disconnected: " + reason);
                
                // Return to menu
                game.setScreen(new MenuScreen(game));
                
                if (callback != null) {
                    callback.onDisconnectedFromServer(reason);
                }
            }
            
            @Override
            public void onPlayerJoined(int playerId, String playerName) {
                System.out.println("Player joined: " + playerName);
            }
            
            @Override
            public void onPlayerLeft(int playerId) {
                System.out.println("Player left: " + playerId);
            }
            
            @Override
            public void onGameStateChanged(int level, float easiness, boolean friendlyFire) {
                System.out.println("Game state changed: Level " + level + ", Friendly Fire: " + friendlyFire);
            }
            
            @Override
            public void onPlayerStateChanged(NetworkMessages.PlayerStateMessage state) {
                // Handle player state updates
            }
            
            @Override
            public void onDamageReceived(NetworkMessages.DamageMessage damage) {
                // Handle damage received
            }
            
            @Override
            public void onPlayerDied(NetworkMessages.PlayerDeathMessage death) {
                // Handle player death
            }
            
            @Override
            public void onGameOver(NetworkMessages.GameOverMessage gameOver) {
                // Handle game over
            }
            
            @Override
            public void onServerDiscovered(GameClient.ServerInfo server) {
                // Handle server discovery (for server browser)
            }
        });
        
        client.connect(host, tcpPort, udpPort, playerName, asSpectator);
        isClientMode = true;
    }
    
    public void disconnectFromServer() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        
        isClientMode = false;
        
        if (callback != null) {
            callback.onDisconnectedFromServer("Disconnected by user");
        }
    }
    
    // Server discovery
    public void discoverServers(int udpPort, DiscoveredServerCallback callback) {
        new Thread(() -> {
            try {
                GameClient tempClient = new GameClient();
                List<GameClient.ServerInfo> servers = tempClient.discoverServers(udpPort);
                
                if (callback != null) {
                    callback.onServersFound(servers);
                }
                
            } catch (Exception e) {
                if (this.callback != null) {
                    this.callback.onGameError("Failed to discover servers: " + e.getMessage());
                }
            }
        }).start();
    }
    
    // Utility methods
    public boolean isServerRunning() {
        return server != null && server.isRunning();
    }
    
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
    
    public boolean isServerMode() {
        return isServerMode;
    }
    
    public boolean isClientMode() {
        return isClientMode;
    }
    
    public GameServer getServer() {
        return server;
    }
    
    public GameClient getClient() {
        return client;
    }
    
    public int getPlayerCount() {
        if (isServerRunning()) {
            return server.getPlayerCount();
        } else if (isConnected()) {
            return client.getRemotePlayers().size() + 1; // +1 for local player
        }
        return 0;
    }
    
    public String getServerName() {
        if (isServerRunning()) {
            return server.getGameName();
        }
        return "";
    }
    
    // Cleanup
    public void cleanup() {
        if (isServerMode) {
            stopServer();
        }
        
        if (isClientMode) {
            disconnectFromServer();
        }
    }
    
    // Game state management
    public void setGameState(String key, Object value) {
        gameState.put(key, value);
    }
    
    public Object getGameState(String key) {
        return gameState.get(key);
    }
    
    public void clearGameState() {
        gameState.clear();
    }
    
    // Default ports
    public static final int DEFAULT_TCP_PORT = 27960;
    public static final int DEFAULT_UDP_PORT = 27960;
}