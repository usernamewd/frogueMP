package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.Damageable;
import io.github.necrashter.natural_revenge.world.GameWorld;

import java.util.StringBuilder;

/**
 * Knife weapon - One-hit kill melee weapon
 * NOTE: This is a placeholder implementation that can be activated later
 */
public class Knife extends PlayerWeapon {
    public String name = "Knife";
    
    // Knife-specific properties
    public float damage = 100.0f; // One-hit kill
    public float attackRange = 2.0f; // Melee range
    public float attackCooldown = 0.5f; // Time between attacks
    private float lastAttackTime = -999f;
    
    // Visual properties
    public ModelInstance knifeModel;
    public Vector3 knifePosition = new Vector3(0.5f, -0.3f, -0.4f);
    public Vector3 knifeRotation = new Vector3(0, 0, 45); // 45 degree angle
    
    // Animation properties
    private float attackProgress = 0f;
    private boolean isAttacking = false;
    private static final float ATTACK_ANIMATION_DURATION = 0.3f;
    
    // Knockback properties
    public float knockback = 3.0f;
    
    // Sound effects
    // NOTE: Add appropriate knife sound effects when available
    // public Sound swingSound;
    // public Sound hitSound;
    
    public Knife(Player player) {
        super(player);
        
        // Create a simple knife model using a box (placeholder)
        // In a real implementation, this would load from assets/models/knife.g3db
        try {
            knifeModel = createSimpleKnifeModel();
        } catch (Exception e) {
            System.out.println("Failed to create knife model: " + e.getMessage());
            knifeModel = null;
        }
        
        if (knifeModel != null) {
            viewModel = knifeModel;
        }
    }
    
    /**
     * Create a simple knife model as a placeholder
     * This creates a basic geometric shape to represent a knife
     */
    private ModelInstance createSimpleKnifeModel() {
        // NOTE: This is a simplified placeholder
        // In a real implementation, you would:
        // 1. Load knife.g3db model using Main.assets
        // 2. Set up proper materials and textures
        // 3. Configure proper positioning
        
        // Placeholder: Create a simple box model
        // This would be replaced with actual knife model loading
        return null; // Return null for now - will be replaced with actual model
    }
    
    @Override
    public void update(float delta) {
        if (isAttacking) {
            attackProgress += delta / ATTACK_ANIMATION_DURATION;
            
            if (attackProgress >= 1.0f) {
                // Attack animation complete
                isAttacking = false;
                attackProgress = 0f;
                lastAttackTime = Main.music.getTime(); // Use music time as game time reference
            } else {
                // Update knife position during attack animation
                updateKnifeAnimation();
            }
        }
        
        // Handle attack input
        if (player.firing1 && canAttack()) {
            performAttack();
        }
    }
    
    private boolean canAttack() {
        float currentTime = Main.music.getTime(); // Use music time as game time reference
        return (currentTime - lastAttackTime) >= attackCooldown && !isAttacking;
    }
    
    private void performAttack() {
        isAttacking = true;
        attackProgress = 0f;
        lastAttackTime = Main.music.getTime();
        
        // Play swing sound (when available)
        // if (swingSound != null) swingSound.play(Main.sfxVolume);
        
        // Perform hit detection
        performHitDetection();
        
        // Apply knockback to player
        applyKnockback();
    }
    
    private void performHitDetection() {
        // Cast a ray in the direction the player is looking
        player.castShootRay(0.1f); // Small spread for melee attacks
        
        // Check for hits
        if (player.shootIntersection.object != null) {
            if (player.shootIntersection.object instanceof Damageable) {
                Damageable damageable = (Damageable) player.shootIntersection.object;
                damageable.takeDamage(damage, Damageable.DamageAgent.Player, Damageable.DamageSource.Melee);
                
                // Play hit sound (when available)
                // if (hitSound != null) hitSound.play(Main.sfxVolume * 0.8f);
            }
        } else if (player.shootIntersection.entity != null) {
            player.shootIntersection.entity.takeDamage(damage, Damageable.DamageAgent.Player, Damageable.DamageSource.Melee);
            
            // Play hit sound (when available)
            // if (hitSound != null) hitSound.play(Main.sfxVolume * 0.8f);
        }
        
        // Update statistics
        world.statistics.update(this);
    }
    
    private void applyKnockback() {
        // Apply knockback to player
        player.pitchMod += knockback * 0.1f; // Small pitch kick
        player.hitBox.velocity.add(
            player.camera.direction.x * knockback * 0.1f,
            0, // No vertical knockback
            player.camera.direction.z * knockback * 0.1f
        );
    }
    
    private void updateKnifeAnimation() {
        if (viewModel == null) return;
        
        // Simple attack animation: swing the knife in an arc
        float swingAngle = MathUtils.lerp(-45f, 45f, attackProgress);
        Vector3 rotation = new Vector3(0, 0, swingAngle);
        
        // Apply rotation to knife model
        viewModel.transform.rotate(Vector3.Y, rotation.y);
        viewModel.transform.rotate(Vector3.X, rotation.x);
        viewModel.transform.rotate(Vector3.Z, rotation.z);
    }
    
    @Override
    public void setView(Camera camera) {
        if (viewModel != null) {
            // Position the knife in front of the camera
            viewModel.transform
                    .set(camera.view).inv()
                    .scale(0.2f, 0.2f, 0.2f) // Smaller scale for knife
                    .translate(knifePosition.x, knifePosition.y, knifePosition.z)
            ;
            
            // Apply attack animation if active
            if (isAttacking) {
                updateKnifeAnimation();
            }
        }
    }
    
    @Override
    public void render(GameWorld world) {
        super.render(world);
        
        // Additional rendering for attack effects could go here
        // (particles, trails, etc.)
    }
    
    @Override
    public void buildText(StringBuilder stringBuilder) {
        stringBuilder.append(name).append('\n');
        stringBuilder.append("Type: Melee Weapon\n");
        stringBuilder.append("Damage: ").append((int)damage).append('\n');
        stringBuilder.append("Range: ").append(attackRange).append("m\n");
        stringBuilder.append("Cooldown: ").append(attackCooldown).append("s\n");
        stringBuilder.append("Attack: Left Click\n");
        
        // Attack status
        if (canAttack()) {
            stringBuilder.append("Status: Ready\n");
        } else {
            float timeUntilReady = attackCooldown - (Main.music.getTime() - lastAttackTime);
            stringBuilder.append("Status: ").append(String.format("%.1f", Math.max(0, timeUntilReady))).append("s\n");
        }
    }
    
    @Override
    public void onEquip() {
        // Reset attack state when equipping
        isAttacking = false;
        attackProgress = 0f;
        lastAttackTime = -999f;
        
        // Set weapon-specific properties
        speedMod = 1.1f; // Slightly faster movement with knife
    }
    
    // Utility methods for external access
    public boolean isReady() {
        return canAttack();
    }
    
    public float getAttackCooldown() {
        return attackCooldown;
    }
    
    public float getTimeUntilReady() {
        float currentTime = Main.music.getTime();
        float timeSinceLastAttack = currentTime - lastAttackTime;
        return Math.max(0, attackCooldown - timeSinceLastAttack);
    }
    
    /**
     * Force an attack (useful for AI or testing)
     */
    public void forceAttack() {
        if (canAttack()) {
            performAttack();
        }
    }
    
    /**
     * Get the attack range of the knife
     */
    public float getAttackRange() {
        return attackRange;
    }
}