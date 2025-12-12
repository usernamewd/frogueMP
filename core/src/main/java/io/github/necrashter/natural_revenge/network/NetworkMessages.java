package io.github.necrashter.natural_revenge.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Network message classes for multiplayer communication
 */
public class NetworkMessages {
    
    // Base message interface
    public interface Message {
        int getType();
    }
    
    // Connection Messages
    public static class ConnectMessage implements Message {
        public String playerName;
        public boolean isSpectator;
        
        public ConnectMessage() {}
        public ConnectMessage(String playerName, boolean isSpectator) {
            this.playerName = playerName;
            this.isSpectator = isSpectator;
        }
        
        @Override
        public int getType() {
            return 1;
        }
    }
    
    public static class ConnectedMessage implements Message {
        public int playerId;
        public String playerName;
        public boolean isSpectator;
        
        @Override
        public int getType() {
            return 2;
        }
    }
    
    public static class PlayerDisconnectMessage implements Message {
        public int playerId;
        
        @Override
        public int getType() {
            return 3;
        }
    }
    
    // Game State Messages
    public static class GameStateMessage implements Message {
        public int level;
        public float easiness;
        public boolean gameStarted;
        public boolean friendlyFire;
        public List<PlayerState> players;
        
        public GameStateMessage() {
            this.players = new ArrayList<>();
        }
        
        @Override
        public int getType() {
            return 10;
        }
    }
    
    // Player State Messages
    public static class PlayerStateMessage implements Message {
        public int playerId;
        public float x, y, z;
        public float yaw, pitch;
        public float health;
        public boolean isAlive;
        public boolean isSpectating;
        public int activeWeapon;
        public String weaponInfo;
        
        @Override
        public int getType() {
            return 11;
        }
    }
    
    public static class PlayerState {
        public int playerId;
        public String playerName;
        public boolean isSpectator;
        public boolean isAlive;
    }
    
    // Input Messages
    public static class PlayerInputMessage implements Message {
        public float movementX, movementY;
        public boolean firing1, firing2;
        public boolean jump;
        public boolean reload;
        public int weaponChange;
        
        @Override
        public int getType() {
            return 20;
        }
    }
    
    // Weapon and Damage Messages
    public static class WeaponFiredMessage implements Message {
        public int playerId;
        public int weaponType;
        public float targetX, targetY, targetZ;
        
        @Override
        public int getType() {
            return 30;
        }
    }
    
    public static class DamageMessage implements Message {
        public int targetPlayerId;
        public int sourcePlayerId;
        public float damage;
        public int damageSource;
        
        @Override
        public int getType() {
            return 31;
        }
    }
    
    public static class PlayerDeathMessage implements Message {
        public int playerId;
        public int killerId;
        
        @Override
        public int getType() {
            return 32;
        }
    }
    
    // Level Progression Messages
    public static class LevelCompleteMessage implements Message {
        public int completedLevel;
        
        @Override
        public int getType() {
            return 40;
        }
    }
    
    public static class GameOverMessage implements Message {
        public boolean gameWon;
        public String winner;
        
        @Override
        public int getType() {
            return 41;
        }
    }
    
    // Spectator Messages
    public static class SpectatePlayerMessage implements Message {
        public int targetPlayerId;
        
        @Override
        public int getType() {
            return 50;
        }
    }
    
    // Server Discovery Messages
    public static class ServerDiscoveryMessage implements Message {
        public String serverName;
        public int playerCount;
        public int maxPlayers;
        public int currentLevel;
        public boolean friendlyFire;
        
        @Override
        public int getType() {
            return 60;
        }
    }
    
    public static class ServerInfoRequest implements Message {
        @Override
        public int getType() {
            return 61;
        }
    }
    
    // Utility class to register all messages with KryoNet
    public static class NetworkRegistrator {
        public static void register(EndPoint endPoint) {
            Kryo kryo = endPoint.getKryo();
            
            // Register connection messages
            kryo.register(ConnectMessage.class);
            kryo.register(ConnectedMessage.class);
            kryo.register(PlayerDisconnectMessage.class);
            
            // Register game state messages
            kryo.register(GameStateMessage.class);
            kryo.register(PlayerStateMessage.class);
            kryo.register(PlayerState.class);
            
            // Register input messages
            kryo.register(PlayerInputMessage.class);
            
            // Register weapon and damage messages
            kryo.register(WeaponFiredMessage.class);
            kryo.register(DamageMessage.class);
            kryo.register(PlayerDeathMessage.class);
            
            // Register level progression messages
            kryo.register(LevelCompleteMessage.class);
            kryo.register(GameOverMessage.class);
            
            // Register spectator messages
            kryo.register(SpectatePlayerMessage.class);
            
            // Register server discovery messages
            kryo.register(ServerDiscoveryMessage.class);
            kryo.register(ServerInfoRequest.class);
        }
    }
}