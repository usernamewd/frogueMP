package io.github.necrashter.natural_revenge.network;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import io.github.necrashter.natural_revenge.network.NetworkMessages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server for handling multiplayer game sessions
 */
public class GameServer {
    private Server server;
    private boolean isRunning = false;
    private boolean friendlyFire = false;
    private int nextPlayerId = 1;
    
    // Player management
    private Map<Integer, ConnectedPlayer> players = new ConcurrentHashMap<>();
    private Map<Connection, Integer> connectionToPlayerId = new ConcurrentHashMap<>();
    
    // Game state
    private int currentLevel = 1;
    private float easiness = 1.0f;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private String gameName = "Frogue Server";
    
    // Server discovery
    private ServerDiscoveryThread discoveryThread;
    
    public static class ConnectedPlayer {
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
        public Connection connection;
        public long lastUpdate = 0;
        
        public ConnectedPlayer(int playerId, String playerName, boolean isSpectator, Connection connection) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.isSpectator = isSpectator;
            this.isAlive = !isSpectator;
            this.connection = connection;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    public GameServer() {
        server = new Server();
        NetworkRegistrator.register(server);
        
        // Connection listener
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("New connection from: " + connection.getRemoteAddressTCP());
            }
            
            @Override
            public void disconnected(Connection connection) {
                handleDisconnect(connection);
            }
            
            @Override
            public void received(Connection connection, Object object) {
                handleMessage(connection, object);
            }
        });
    }
    
    public void start(int tcpPort, int udpPort, boolean friendlyFire, String serverName) throws IOException {
        this.friendlyFire = friendlyFire;
        this.gameName = serverName;
        
        server.start();
        server.bind(tcpPort, udpPort);
        isRunning = true;
        
        // Start server discovery
        discoveryThread = new ServerDiscoveryThread(udpPort, gameName);
        discoveryThread.start();
        
        System.out.println("Server started on TCP:" + tcpPort + " UDP:" + udpPort);
        System.out.println("Friendly Fire: " + friendlyFire);
    }
    
    public void stop() {
        if (isRunning) {
            isRunning = false;
            server.stop();
            if (discoveryThread != null) {
                discoveryThread.stopDiscovery();
            }
            System.out.println("Server stopped");
        }
    }
    
    private void handleDisconnect(Connection connection) {
        Integer playerId = connectionToPlayerId.remove(connection);
        if (playerId != null) {
            ConnectedPlayer player = players.remove(playerId);
            if (player != null) {
                // Notify all players about disconnect
                PlayerDisconnectMessage disconnectMsg = new PlayerDisconnectMessage();
                disconnectMsg.playerId = playerId;
                server.sendToAllExceptTCP(connection.getID(), disconnectMsg);
                
                System.out.println("Player " + player.playerName + " disconnected");
                checkGameEnd();
            }
        }
    }
    
    private void handleMessage(Connection connection, Object object) {
        if (object instanceof ConnectMessage) {
            handleConnect(connection, (ConnectMessage) object);
        } else if (object instanceof PlayerInputMessage) {
            handlePlayerInput(connection, (PlayerInputMessage) object);
        } else if (object instanceof WeaponFiredMessage) {
            handleWeaponFired(connection, (WeaponFiredMessage) object);
        } else if (object instanceof DamageMessage) {
            handleDamage(connection, (DamageMessage) object);
        } else if (object instanceof SpectatePlayerMessage) {
            handleSpectatePlayer(connection, (SpectatePlayerMessage) object);
        } else if (object instanceof ServerInfoRequest) {
            sendServerInfo(connection);
        }
    }
    
    private void handleConnect(Connection connection, ConnectMessage message) {
        if (players.size() >= 16) { // Max 16 players
            connection.close();
            return;
        }
        
        int playerId = nextPlayerId++;
        ConnectedPlayer player = new ConnectedPlayer(playerId, message.playerName, message.isSpectator, connection);
        
        // Store player
        players.put(playerId, player);
        connectionToPlayerId.put(connection, playerId);
        
        // Send connection confirmation to new player
        ConnectedMessage connectedMsg = new ConnectedMessage();
        connectedMsg.playerId = playerId;
        connectedMsg.playerName = message.playerName;
        connectedMsg.isSpectator = message.isSpectator;
        server.sendToTCP(connection.getID(), connectedMsg);
        
        // Send current game state to new player
        sendGameStateToPlayer(connection);
        
        // Notify all players about new player
        server.sendToAllExceptTCP(connection.getID(), connectedMsg);
        
        System.out.println("Player " + message.playerName + " connected with ID: " + playerId);
        
        // Check if game should start
        checkGameStart();
    }
    
    private void sendGameStateToPlayer(Connection connection) {
        GameStateMessage gameState = new GameStateMessage();
        gameState.level = currentLevel;
        gameState.easiness = easiness;
        gameState.friendlyFire = friendlyFire;
        gameState.gameStarted = gameStarted;
        
        // Add existing players
        for (ConnectedPlayer player : players.values()) {
            PlayerState state = new PlayerState();
            state.playerId = player.playerId;
            state.playerName = player.playerName;
            state.isSpectator = player.isSpectator;
            state.isAlive = player.isAlive;
            gameState.players.add(state);
        }
        
        server.sendToTCP(connection.getID(), gameState);
    }
    
    private void handlePlayerInput(Connection connection, PlayerInputMessage input) {
        Integer playerId = connectionToPlayerId.get(connection);
        if (playerId == null) return;
        
        ConnectedPlayer player = players.get(playerId);
        if (player == null || player.isSpectator) return;
        
        player.lastUpdate = System.currentTimeMillis();
        
        // Broadcast input to all other players
        PlayerStateMessage stateMsg = new PlayerStateMessage();
        stateMsg.playerId = playerId;
        stateMsg.x = player.position.x;
        stateMsg.y = player.position.y;
        stateMsg.z = player.position.z;
        stateMsg.yaw = player.yaw;
        stateMsg.pitch = player.pitch;
        stateMsg.health = player.health;
        stateMsg.isAlive = player.isAlive;
        stateMsg.isSpectating = false;
        stateMsg.activeWeapon = player.activeWeapon;
        stateMsg.weaponInfo = player.weaponInfo;
        
        server.sendToAllExceptTCP(connection.getID(), stateMsg);
    }
    
    private void handleWeaponFired(Connection connection, WeaponFiredMessage message) {
        server.sendToAllExceptTCP(connection.getID(), message);
    }
    
    private void handleDamage(Connection connection, DamageMessage message) {
        if (!friendlyFire && message.sourcePlayerId != message.targetPlayerId) {
            return; // Friendly fire disabled
        }
        
        ConnectedPlayer target = players.get(message.targetPlayerId);
        if (target == null) return;
        
        target.health -= message.damage;
        if (target.health <= 0 && target.isAlive) {
            target.isAlive = false;
            target.isSpectator = true;
            
            // Notify all players about death
            PlayerDeathMessage deathMsg = new PlayerDeathMessage();
            deathMsg.playerId = message.targetPlayerId;
            deathMsg.killerId = message.sourcePlayerId;
            server.sendToAllTCP(deathMsg);
            
            checkGameEnd();
        }
        
        // Broadcast damage update
        server.sendToAllTCP(message);
    }
    
    private void handleSpectatePlayer(Connection connection, SpectatePlayerMessage message) {
        Integer playerId = connectionToPlayerId.get(connection);
        if (playerId == null) return;
        
        // In a real implementation, this would change the spectator's camera
        // For now, just acknowledge the request
    }
    
    private void sendServerInfo(Connection connection) {
        ServerDiscoveryMessage info = new ServerDiscoveryMessage();
        info.serverName = gameName;
        info.playerCount = players.size();
        info.maxPlayers = 16;
        info.currentLevel = currentLevel;
        info.friendlyFire = friendlyFire;
        
        server.sendToUDP(connection.getID(), info);
    }
    
    private void checkGameStart() {
        if (!gameStarted && players.size() >= 2) {
            gameStarted = true;
            System.out.println("Game started with " + players.size() + " players");
        }
    }
    
    private void checkGameEnd() {
        // Check if all non-spectator players are dead
        boolean allDead = true;
        for (ConnectedPlayer player : players.values()) {
            if (!player.isSpectator && player.isAlive) {
                allDead = false;
                break;
            }
        }
        
        if (allDead && gameStarted && !gameOver) {
            gameOver = true;
            GameOverMessage gameOverMsg = new GameOverMessage();
            gameOverMsg.gameWon = false;
            gameOverMsg.winner = "No one survived!";
            server.sendToAllTCP(gameOverMsg);
            
            // Reset game after 5 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    resetGame();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    private void resetGame() {
        gameStarted = false;
        gameOver = false;
        currentLevel = 1;
        
        // Respawn all players
        for (ConnectedPlayer player : players.values()) {
            if (!player.isSpectator) {
                player.isAlive = true;
                player.health = 100f;
                // Reset position - in real implementation would use actual spawn points
                player.position.set(0, 2, 0);
            }
        }
        
        System.out.println("Game reset - Level 1");
    }
    
    // Update server state
    public void update(float delta) {
        if (!isRunning) return;
        
        // Update player positions based on inputs
        // This would be more sophisticated in a real implementation
        for (ConnectedPlayer player : players.values()) {
            if (!player.isSpectator && player.isAlive) {
                // Apply movement physics, etc.
            }
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public String getGameName() {
        return gameName;
    }
    
    // Server discovery thread for LAN broadcasting
    private static class ServerDiscoveryThread extends Thread {
        private final int port;
        private final String serverName;
        private volatile boolean running = true;
        private DatagramSocket socket;
        
        public ServerDiscoveryThread(int port, String serverName) {
            this.port = port;
            this.serverName = serverName;
        }
        
        @Override
        public void run() {
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                
                byte[] buffer = ("FROGUE_SERVER|" + serverName).getBytes();
                
                while (running) {
                    try {
                        // Broadcast to local network
                        InetAddress broadcast = InetAddress.getByName("255.255.255.255");
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcast, port);
                        socket.send(packet);
                        
                        Thread.sleep(2000); // Broadcast every 2 seconds
                    } catch (Exception e) {
                        if (running) e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }
        
        public void stopDiscovery() {
            running = false;
            if (socket != null) {
                socket.close();
            }
        }
    }
}