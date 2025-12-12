package io.github.necrashter.natural_revenge.network;

import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import io.github.necrashter.natural_revenge.network.NetworkMessages.*;
import io.github.necrashter.natural_revenge.world.player.Player;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for connecting to multiplayer game servers
 */
public class GameClient {
    private Client client;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    
    // Player management
    private int playerId = -1;
    private String playerName = "Player";
    private boolean isSpectator = false;
    private Map<Integer, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
    
    // Game state
    private int currentLevel = 1;
    private float easiness = 1.0f;
    private boolean gameStarted = false;
    private boolean friendlyFire = false;
    private boolean gameOver = false;
    
    // Connection callback
    private MultiplayerCallback callback;
    
    public interface MultiplayerCallback {
        void onConnected(int playerId, String playerName);
        void onDisconnected(String reason);
        void onPlayerJoined(int playerId, String playerName);
        void onPlayerLeft(int playerId);
        void onGameStateChanged(int level, float easiness, boolean friendlyFire);
        void onPlayerStateChanged(PlayerStateMessage state);
        void onDamageReceived(DamageMessage damage);
        void onPlayerDied(PlayerDeathMessage death);
        void onGameOver(GameOverMessage gameOver);
        void onServerDiscovered(ServerInfo server);
    }
    
    public static class RemotePlayer {
        public int playerId;
        public String playerName;
        public boolean isSpectator;
        public boolean isAlive;
        public Vector3 position = new Vector3();
        public float yaw = 0f;
        public float pitch = 0f;
        public float health = 100f;
        public int activeWeapon = 0;
        public String weaponInfo = "";
        public long lastUpdate = 0;
    }
    
    public static class ServerInfo {
        public String address;
        public String serverName;
        public int playerCount;
        public int maxPlayers;
        public int currentLevel;
        public boolean friendlyFire;
    }
    
    public GameClient() {
        client = new Client();
        NetworkRegistrator.register(client);
        
        // Mobile-specific client configuration
        if (isMobileDevice()) {
            configureForMobile();
        }
        
        client.addListener(new Listener() {
            @Override
            public void connected(com.esotericsoftware.kryonet.Connection connection) {
                isConnected = true;
                isConnecting = false;
                System.out.println("Connected to server: " + connection.getRemoteAddressTCP());
                
                // Mobile-specific connection handling
                if (isMobileDevice()) {
                    handleMobileConnection();
                }
            }
            
            @Override
            public void disconnected(com.esotericsoftware.kryonet.Connection connection) {
                isConnected = false;
                isConnecting = false;
                System.out.println("Disconnected from server");
                if (callback != null) {
                    callback.onDisconnected("Connection lost");
                }
                
                // Mobile-specific disconnection handling
                if (isMobileDevice()) {
                    handleMobileDisconnection();
                }
            }
            
            @Override
            public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                handleMessage(object);
            }
        });
    }
    
    /**
     * Check if running on mobile device
     */
    private boolean isMobileDevice() {
        return com.badlogic.gdx.Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android ||
               com.badlogic.gdx.Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.iOS;
    }
    
    /**
     * Configure client for mobile networking
     */
    private void configureForMobile() {
        // Reduce network timeout for mobile networks
        client.setTimeout(3000); // 3 seconds instead of default 5
        
        // Enable TCP no-delay for better responsiveness on mobile
        client.setTcpNoDelay(true);
        
        // Set up connection retry for unstable mobile networks
        setupConnectionRetry();
    }
    
    /**
     * Handle mobile-specific connection events
     */
    private void handleMobileConnection() {
        // Reduce update frequency to save battery on mobile
        client.setUpdateThreadFrequency(30); // 30 FPS instead of 60
        
        // Enable connection heartbeat for mobile networks
        startConnectionHeartbeat();
    }
    
    /**
     * Handle mobile-specific disconnection events
     */
    private void handleMobileDisconnection() {
        // Stop battery-draining operations
        stopConnectionHeartbeat();
        
        // Attempt reconnection for mobile users
        if (shouldAutoReconnect()) {
            scheduleReconnection();
        }
    }
    
    /**
     * Set up connection retry mechanism for mobile
     */
    private void setupConnectionRetry() {
        maxRetries = 3;
        retryDelay = 2000; // 2 seconds between retries
        retryCount = 0;
    }
    
    /**
     * Start connection heartbeat for mobile
     */
    private void startConnectionHeartbeat() {
        // In a real implementation, this would send periodic ping messages
        // to keep the connection alive on mobile networks
    }
    
    /**
     * Stop connection heartbeat
     */
    private void stopConnectionHeartbeat() {
        // Stop any heartbeat-related operations
    }
    
    /**
     * Determine if auto-reconnection should be attempted
     */
    private boolean shouldAutoReconnect() {
        return isMobileDevice() && retryCount < maxRetries;
    }
    
    /**
     * Schedule reconnection attempt
     */
    private void scheduleReconnection() {
        new Thread(() -> {
            try {
                Thread.sleep(retryDelay);
                retryCount++;
                System.out.println("Mobile reconnection attempt " + retryCount + "/" + maxRetries);
                
                if (serverHost != null && callback != null) {
                    connect(serverHost, serverTcpPortClient, serverUdpPortClient, playerName, isSpectator);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    // Mobile-specific connection management
    private int maxRetries = 3;
    private int retryDelay = 2000;
    private int retryCount = 0;
    private String serverHost;
    private int serverTcpPortClient;
    private int serverUdpPortClient;
    
    public void setCallback(MultiplayerCallback callback) {
        this.callback = callback;
    }
    
    public void connect(String host, int tcpPort, int udpPort, String playerName, boolean isSpectator) {
        if (isConnected || isConnecting) return;
        
        // Store connection details for mobile reconnection
        this.serverHost = host;
        this.serverTcpPortClient = tcpPort;
        this.serverUdpPortClient = udpPort;
        this.playerName = playerName;
        this.isSpectator = isSpectator;
        isConnecting = true;
        
        new Thread(() -> {
            try {
                client.start();
                client.connect(5000, host, tcpPort, udpPort);
                
                // Send connection message
                ConnectMessage connectMsg = new ConnectMessage(playerName, isSpectator);
                client.sendTCP(connectMsg);
                
            } catch (IOException e) {
                isConnecting = false;
                System.err.println("Failed to connect: " + e.getMessage());
                if (callback != null) {
                    callback.onDisconnected("Failed to connect: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public void disconnect() {
        if (isConnected) {
            client.stop();
            isConnected = false;
            remotePlayers.clear();
        }
    }
    
    private void handleMessage(Object object) {
        if (object instanceof ConnectedMessage) {
            handleConnectedMessage((ConnectedMessage) object);
        } else if (object instanceof GameStateMessage) {
            handleGameStateMessage((GameStateMessage) object);
        } else if (object instanceof PlayerStateMessage) {
            handlePlayerStateMessage((PlayerStateMessage) object);
        } else if (object instanceof PlayerDisconnectMessage) {
            handlePlayerDisconnectMessage((PlayerDisconnectMessage) object);
        } else if (object instanceof DamageMessage) {
            handleDamageMessage((DamageMessage) object);
        } else if (object instanceof PlayerDeathMessage) {
            handlePlayerDeathMessage((PlayerDeathMessage) object);
        } else if (object instanceof GameOverMessage) {
            handleGameOverMessage((GameOverMessage) object);
        } else if (object instanceof WeaponFiredMessage) {
            handleWeaponFiredMessage((WeaponFiredMessage) object);
        }
    }
    
    private void handleConnectedMessage(ConnectedMessage message) {
        this.playerId = message.playerId;
        
        if (callback != null) {
            callback.onConnected(playerId, playerName);
        }
    }
    
    private void handleGameStateMessage(GameStateMessage message) {
        this.currentLevel = message.level;
        this.easiness = message.easiness;
        this.friendlyFire = message.friendlyFire;
        this.gameStarted = message.gameStarted;
        
        // Clear existing remote players
        remotePlayers.clear();
        
        // Add known players
        for (PlayerState state : message.players) {
            if (state.playerId != playerId) {
                RemotePlayer remotePlayer = new RemotePlayer();
                remotePlayer.playerId = state.playerId;
                remotePlayer.playerName = state.playerName;
                remotePlayer.isSpectator = state.isSpectator;
                remotePlayer.isAlive = state.isAlive;
                remotePlayers.put(state.playerId, remotePlayer);
                
                if (callback != null) {
                    callback.onPlayerJoined(state.playerId, state.playerName);
                }
            }
        }
        
        if (callback != null) {
            callback.onGameStateChanged(currentLevel, easiness, friendlyFire);
        }
    }
    
    private void handlePlayerStateMessage(PlayerStateMessage message) {
        RemotePlayer player = remotePlayers.get(message.playerId);
        if (player == null) {
            player = new RemotePlayer();
            player.playerId = message.playerId;
            remotePlayers.put(message.playerId, player);
        }
        
        player.position.set(message.x, message.y, message.z);
        player.yaw = message.yaw;
        player.pitch = message.pitch;
        player.health = message.health;
        player.isAlive = message.isAlive;
        player.isSpectator = message.isSpectating;
        player.activeWeapon = message.activeWeapon;
        player.weaponInfo = message.weaponInfo;
        player.lastUpdate = System.currentTimeMillis();
        
        if (callback != null) {
            callback.onPlayerStateChanged(message);
        }
    }
    
    private void handlePlayerDisconnectMessage(PlayerDisconnectMessage message) {
        remotePlayers.remove(message.playerId);
        
        if (callback != null) {
            callback.onPlayerLeft(message.playerId);
        }
    }
    
    private void handleDamageMessage(DamageMessage message) {
        if (message.targetPlayerId == playerId && callback != null) {
            callback.onDamageReceived(message);
        }
    }
    
    private void handlePlayerDeathMessage(PlayerDeathMessage message) {
        RemotePlayer player = remotePlayers.get(message.playerId);
        if (player != null) {
            player.isAlive = false;
            player.isSpectator = true;
        }
        
        if (message.playerId == playerId && callback != null) {
            callback.onPlayerDied(message);
        }
    }
    
    private void handleGameOverMessage(GameOverMessage message) {
        this.gameOver = true;
        
        if (callback != null) {
            callback.onGameOver(message);
        }
    }
    
    private void handleWeaponFiredMessage(WeaponFiredMessage message) {
        // Handle weapon firing from other players
        // This would trigger visual effects, sounds, etc.
    }
    
    // Send player input to server
    public void sendInput(float movementX, float movementY, boolean firing1, boolean firing2, 
                         boolean jump, boolean reload, int weaponChange) {
        if (!isConnected) return;
        
        PlayerInputMessage inputMsg = new PlayerInputMessage();
        inputMsg.movementX = movementX;
        inputMsg.movementY = movementY;
        inputMsg.firing1 = firing1;
        inputMsg.firing2 = firing2;
        inputMsg.jump = jump;
        inputMsg.reload = reload;
        inputMsg.weaponChange = weaponChange;
        
        client.sendUDP(inputMsg);
    }
    
    // Send weapon fired event to server
    public void sendWeaponFired(int weaponType, float targetX, float targetY, float targetZ) {
        if (!isConnected) return;
        
        WeaponFiredMessage weaponMsg = new WeaponFiredMessage();
        weaponMsg.playerId = playerId;
        weaponMsg.weaponType = weaponType;
        weaponMsg.targetX = targetX;
        weaponMsg.targetY = targetY;
        weaponMsg.targetZ = targetZ;
        
        client.sendUDP(weaponMsg);
    }
    
    // Send damage event to server
    public void sendDamage(int targetPlayerId, float damage, int damageSource) {
        if (!isConnected) return;
        
        DamageMessage damageMsg = new DamageMessage();
        damageMsg.targetPlayerId = targetPlayerId;
        damageMsg.sourcePlayerId = playerId;
        damageMsg.damage = damage;
        damageMsg.damageSource = damageSource;
        
        client.sendTCP(damageMsg);
    }
    
    // Send player death to server (if needed)
    public void sendPlayerDeath() {
        if (!isConnected) return;
        
        PlayerDeathMessage deathMsg = new PlayerDeathMessage();
        deathMsg.playerId = playerId;
        deathMsg.killerId = -1; // Self-kill or environmental
        
        client.sendTCP(deathMsg);
    }
    
    // Spectate another player
    public void spectatePlayer(int targetPlayerId) {
        if (!isConnected) return;
        
        SpectatePlayerMessage spectateMsg = new SpectatePlayerMessage();
        spectateMsg.targetPlayerId = targetPlayerId;
        
        client.sendUDP(spectateMsg);
    }
    
    // Network discovery for finding servers
    public List<ServerInfo> discoverServers(int udpPort) {
        List<ServerInfo> servers = new ArrayList<>();
        
        try {
            Client discoveryClient = new Client();
            NetworkRegistrator.register(discoveryClient);
            
            discoveryClient.start();
            
            // Listen for server responses
            discoveryClient.addListener(new Listener() {
                @Override
                public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                    if (object instanceof ServerDiscoveryMessage) {
                        ServerDiscoveryMessage msg = (ServerDiscoveryMessage) object;
                        ServerInfo info = new ServerInfo();
                        info.address = connection.getRemoteAddressTCP().toString();
                        info.serverName = msg.serverName;
                        info.playerCount = msg.playerCount;
                        info.maxPlayers = msg.maxPlayers;
                        info.currentLevel = msg.currentLevel;
                        info.friendlyFire = msg.friendlyFire;
                        servers.add(info);
                    }
                }
            });
            
            // Send discovery request to local network
            ServerInfoRequest request = new ServerInfoRequest();
            discoveryClient.sendUDP(request);
            
            // Wait for responses
            Thread.sleep(2000);
            
            discoveryClient.stop();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return servers;
    }
    
    // Getters
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isConnecting() {
        return isConnecting;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean isSpectator() {
        return isSpectator;
    }
    
    public Map<Integer, RemotePlayer> getRemotePlayers() {
        return new HashMap<>(remotePlayers);
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public float getEasiness() {
        return easiness;
    }
    
    public boolean isFriendlyFire() {
        return friendlyFire;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
}