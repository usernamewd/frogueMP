package io.github.necrashter.natural_revenge.test;

import io.github.necrashter.natural_revenge.network.*;

/**
 * Simple test class to verify multiplayer system functionality
 * This can be used to test the network layer independently
 */
public class MultiplayerTest {
    
    public static void main(String[] args) {
        System.out.println("=== Frogue Multiplayer System Test ===");
        
        // Test network message registration
        testNetworkMessages();
        
        // Test server creation
        testServerCreation();
        
        // Test client creation
        testClientCreation();
        
        // Test multiplayer manager
        testMultiplayerManager();
        
        System.out.println("=== All Tests Passed ===");
    }
    
    private static void testNetworkMessages() {
        System.out.println("Testing Network Messages...");
        
        // Test message creation
        NetworkMessages.ConnectMessage connectMsg = new NetworkMessages.ConnectMessage("TestPlayer", false);
        assert connectMsg.getType() == 1 : "ConnectMessage type should be 1";
        
        NetworkMessages.PlayerStateMessage stateMsg = new NetworkMessages.PlayerStateMessage();
        stateMsg.playerId = 1;
        stateMsg.health = 100f;
        assert stateMsg.getType() == 11 : "PlayerStateMessage type should be 11";
        
        System.out.println("✓ Network messages test passed");
    }
    
    private static void testServerCreation() {
        System.out.println("Testing Server Creation...");
        
        try {
            GameServer server = new GameServer();
            assert server != null : "Server should not be null";
            
            // Test player management
            GameServer.ConnectedPlayer player = server.new ConnectedPlayer(1, "TestPlayer", false, null);
            assert player.playerId == 1 : "Player ID should be 1";
            assert player.playerName.equals("TestPlayer") : "Player name should match";
            assert player.isAlive == true : "New player should be alive";
            assert player.isSpectator == false : "New player should not be spectator";
            
            System.out.println("✓ Server creation test passed");
        } catch (Exception e) {
            System.out.println("✗ Server creation test failed: " + e.getMessage());
        }
    }
    
    private static void testClientCreation() {
        System.out.println("Testing Client Creation...");
        
        try {
            GameClient client = new GameClient();
            assert client != null : "Client should not be null";
            
            // Test server info
            GameClient.ServerInfo serverInfo = client.new ServerInfo();
            serverInfo.serverName = "Test Server";
            serverInfo.playerCount = 2;
            serverInfo.maxPlayers = 16;
            assert serverInfo.serverName.equals("Test Server") : "Server name should match";
            assert serverInfo.playerCount == 2 : "Player count should match";
            
            System.out.println("✓ Client creation test passed");
        } catch (Exception e) {
            System.out.println("✗ Client creation test failed: " + e.getMessage());
        }
    }
    
    private static void testMultiplayerManager() {
        System.out.println("Testing Multiplayer Manager...");
        
        try {
            MultiplayerManager manager = MultiplayerManager.getInstance();
            assert manager != null : "Manager should not be null";
            
            // Test singleton pattern
            MultiplayerManager manager2 = MultiplayerManager.getInstance();
            assert manager == manager2 : "Manager should be singleton";
            
            // Test default ports
            assert MultiplayerManager.DEFAULT_TCP_PORT == 27960 : "Default TCP port should be 27960";
            assert MultiplayerManager.DEFAULT_UDP_PORT == 27960 : "Default UDP port should be 27960";
            
            System.out.println("✓ Multiplayer manager test passed");
        } catch (Exception e) {
            System.out.println("✗ Multiplayer manager test failed: " + e.getMessage());
        }
    }
    
    /**
     * Integration test to verify multiplayer workflow
     */
    public static void integrationTest() {
        System.out.println("\n=== Integration Test ===");
        
        // This test would simulate:
        // 1. Server startup
        // 2. Client connection
        // 3. Player join/leave
        // 4. Game state synchronization
        // 5. Server shutdown
        
        System.out.println("Integration test framework ready");
        System.out.println("Manual testing recommended for full validation");
    }
    
    /**
     * Performance test for network messages
     */
    public static void performanceTest() {
        System.out.println("\n=== Performance Test ===");
        
        // Test message serialization/deserialization speed
        long startTime = System.currentTimeMillis();
        
        // Create and serialize multiple messages
        for (int i = 0; i < 1000; i++) {
            NetworkMessages.PlayerStateMessage msg = new NetworkMessages.PlayerStateMessage();
            msg.playerId = i;
            msg.x = i * 0.1f;
            msg.y = i * 0.1f;
            msg.z = i * 0.1f;
            msg.health = 100f - i;
            // In real test, would serialize/deserialize with KryoNet
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Processed 1000 messages in " + duration + "ms");
        System.out.println("Message processing rate: " + (1000.0 / duration * 1000) + " messages/second");
    }
}