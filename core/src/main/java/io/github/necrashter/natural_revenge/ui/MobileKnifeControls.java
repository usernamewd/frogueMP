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
import io.github.necrashter.natural_revenge.world.player.Knife;

/**
 * Mobile-optimized controls for the Knife weapon
 */
public class MobileKnifeControls {
    
    public interface KnifeCallback {
        void onAttack();
        void onToggleAttackMode();
    }
    
    private final Knife knife;
    private final KnifeCallback callback;
    
    // UI components
    private TextButton attackButton;
    private Label cooldownLabel;
    private Label statusLabel;
    private Container<TextButton> attackContainer;
    private Container<Label> cooldownContainer;
    
    public MobileKnifeControls(Knife knife, KnifeCallback callback) {
        this.knife = knife;
        this.callback = callback;
    }
    
    /**
     * Create mobile knife weapon HUD
     */
    public Table createKnifeHUD() {
        Table knifeTable = new Table();
        knifeTable.setFillParent(true);
        
        // Attack button (bottom-right)
        attackButton = createAttackButton();
        attackContainer = new Container<>(attackButton);
        attackContainer.setFillParent(true);
        attackContainer.bottom().right().pad(20);
        
        // Cooldown indicator (bottom-right, above attack button)
        cooldownLabel = new Label("", Main.skin);
        cooldownLabel.setFontScale(1.0f);
        cooldownLabel.setAlignment(Align.center);
        cooldownLabel.setColor(1, 0.7f, 0.3f, 1); // Orange for cooldown
        cooldownLabel.setVisible(false);
        
        Container<Label> cooldownContainer = new Container<>(cooldownLabel);
        cooldownContainer.setFillParent(true);
        cooldownContainer.bottom().right().pad(20);
        cooldownContainer.padBottom(80); // Position above attack button
        this.cooldownContainer = cooldownContainer;
        
        // Status indicator (top-right)
        statusLabel = new Label("", Main.skin);
        statusLabel.setFontScale(0.9f);
        statusLabel.setAlignment(Align.right);
        statusLabel.setColor(0.3f, 1f, 0.3f, 1); // Green for ready
        
        Container<Label> statusContainer = new Container<>(statusLabel);
        statusContainer.setFillParent(true);
        statusContainer.top().right().pad(20);
        
        knifeTable.add(attackContainer).expand().fill();
        knifeTable.add(cooldownContainer).expand().fill();
        knifeTable.add(statusContainer).expand().fill();
        
        return knifeTable;
    }
    
    /**
     * Create mobile-friendly attack button
     */
    private TextButton createAttackButton() {
        TextButton button = new TextButton("KNIFE", Main.skin);
        
        // Make button large for easy tapping
        button.getLabel().setFontScale(1.3f);
        button.getLabel().setColor(1, 0.3f, 0.3f, 1); // Red color for attack
        
        // Set minimum size for touch-friendly interface
        button.setSize(120, 60);
        
        // Touch feedback
        button.addListener(new ClickListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                button.setColor(0.8f, 0.2f, 0.2f, 1); // Darker red when pressed
                
                // Trigger attack immediately on touch
                if (callback != null && knife.isReady()) {
                    callback.onAttack();
                }
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                button.setColor(1, 0.3f, 0.3f, 1); // Return to normal color
            }
        });
        
        // Also allow click/tap for desktop compatibility
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (callback != null && knife.isReady()) {
                    callback.onAttack();
                }
            }
        });
        
        return button;
    }
    
    /**
     * Update knife weapon status display
     */
    public void updateKnifeStatus() {
        if (knife.isReady()) {
            // Weapon is ready to use
            attackButton.setDisabled(false);
            attackButton.setText("KNIFE");
            cooldownLabel.setVisible(false);
            statusLabel.setText("KNIFE READY");
            statusLabel.setColor(0.3f, 1f, 0.3f, 1); // Green
        } else {
            // Weapon is on cooldown
            attackButton.setDisabled(true);
            attackButton.setText("COOLDOWN");
            
            float timeUntilReady = knife.getTimeUntilReady();
            cooldownLabel.setText(String.format("%.1fs", timeUntilReady));
            cooldownLabel.setVisible(true);
            
            statusLabel.setText("COOLDOWN");
            statusLabel.setColor(1, 0.7f, 0.3f, 1); // Orange
        }
    }
    
    /**
     * Show knife weapon information dialog
     */
    public void showKnifeInfo() {
        com.badlogic.gdx.scenes.scene2d.ui.Dialog infoDialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("Knife Weapon", Main.skin) {
            @Override
            protected void result(Object object) {
                // Just close the dialog
            }
        };
        
        Table contentTable = new Table();
        
        String knifeInfo = "🔪 KNIFE WEAPON\n\n" +
                          "💥 DAMAGE: One-hit kill (100 HP)\n" +
                          "📏 RANGE: 2 meters\n" +
                          "⏱️ COOLDOWN: 0.5 seconds\n" +
                          "💥 KNOCKBACK: High\n\n" +
                          "📱 MOBILE CONTROLS:\n" +
                          "• Tap KNIFE button to attack\n" +
                          "• Attack in range for instant kill\n" +
                          "• Perfect for close combat\n\n" +
                          "💡 TIPS:\n" +
                          "• Approach enemies silently\n" +
                          "• Use for instant elimination\n" +
                          "• Faster movement when equipped";
        
        Label infoLabel = new Label(knifeInfo, Main.skin);
        infoLabel.setFontScale(1.0f);
        infoLabel.setAlignment(Align.left);
        
        contentTable.add(infoLabel).pad(20);
        infoDialog.getContentTable().add(contentTable);
        
        infoDialog.button("Close", true);
        infoDialog.show(Gdx.app.getGraphics().getGL20() != null ? Main.viewport.getStage() : null);
    }
    
    /**
     * Create knife tutorial dialog for first-time users
     */
    public void showKnifeTutorial() {
        com.badlogic.gdx.scenes.scene2d.ui.Dialog tutorialDialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("Knife Tutorial", Main.skin) {
            @Override
            protected void result(Object object) {
                // Tutorial completed
            }
        };
        
        Table contentTable = new Table();
        
        String tutorialText = "🎮 KNIFE BASICS\n\n" +
                             "The Knife is a powerful melee weapon\n" +
                             "that can eliminate enemies instantly!\n\n" +
                             "📱 HOW TO USE ON MOBILE:\n\n" +
                             "1. 🔴 Tap the KNIFE button to attack\n" +
                             "2. 📏 Get within 2 meters of target\n" +
                             "3. 💥 Instant kill on successful hit\n" +
                             "4. ⏱️ Wait 0.5s cooldown between attacks\n\n" +
                             "🎯 STRATEGY:\n" +
                             "• Sneak up on enemies\n" +
                             "• Use when ammunition is low\n" +
                             "• Fast movement when equipped\n\n" +
                             "Ready to start slicing?";
        
        Label tutorialLabel = new Label(tutorialText, Main.skin);
        tutorialLabel.setFontScale(1.0f);
        tutorialLabel.setAlignment(Align.left);
        
        contentTable.add(tutorialLabel).pad(20);
        tutorialDialog.getContentTable().add(contentTable);
        
        tutorialDialog.button("Let's Go!", true);
        tutorialDialog.show(Gdx.app.getGraphics().getGL20() != null ? Main.viewport.getStage() : null);
    }
    
    /**
     * Create enhanced attack feedback for mobile
     */
    public void showAttackFeedback(String feedback) {
        // Create temporary feedback label
        Label feedbackLabel = new Label(feedback, Main.skin);
        feedbackLabel.setFontScale(1.5f);
        feedbackLabel.setAlignment(Align.center);
        
        // Color based on feedback type
        if (feedback.contains("HIT") || feedback.contains("KILL")) {
            feedbackLabel.setColor(1, 0.3f, 0.3f, 1); // Red for hits
        } else if (feedback.contains("MISS")) {
            feedbackLabel.setColor(1, 0.7f, 0.3f, 1); // Orange for misses
        } else {
            feedbackLabel.setColor(0.3f, 1f, 0.3f, 1); // Green for ready
        }
        
        Container<Label> feedbackContainer = new Container<>(feedbackLabel);
        feedbackContainer.setFillParent(true);
        feedbackContainer.center();
        
        // Add to stage temporarily
        if (Main.viewport != null && Main.viewport.getStage() != null) {
            Main.viewport.getStage().addActor(feedbackContainer);
            
            // Remove after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Gdx.app.postRunnable(() -> {
                        feedbackContainer.remove();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Update attack button based on current state
     */
    public void updateAttackButton() {
        if (knife.isReady()) {
            attackButton.setDisabled(false);
            attackButton.getColor().a = 1.0f; // Full opacity
            attackButton.setText("KNIFE");
        } else {
            attackButton.setDisabled(true);
            attackButton.getColor().a = 0.6f; // Semi-transparent when on cooldown
            attackButton.setText("WAIT");
        }
    }
    
    /**
     * Create compact knife HUD for small screens
     */
    public Table createCompactKnifeHUD() {
        Table compactTable = new Table();
        compactTable.setFillParent(true);
        
        // Compact attack button (smaller size)
        TextButton compactAttackButton = new TextButton("🔪", Main.skin);
        compactAttackButton.getLabel().setFontScale(2.0f);
        compactAttackButton.setSize(60, 60);
        
        compactAttackButton.addListener(new ClickListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (callback != null && knife.isReady()) {
                    callback.onAttack();
                }
            }
        });
        
        Container<TextButton> compactButtonContainer = new Container<>(compactAttackButton);
        compactButtonContainer.setFillParent(true);
        compactButtonContainer.bottom().right().pad(10);
        
        compactTable.add(compactButtonContainer).expand().fill();
        
        return compactTable;
    }
    
    /**
     * Check if we should use compact mode (small screens)
     */
    public boolean shouldUseCompactMode() {
        float screenWidth = Gdx.graphics.getWidth();
        return screenWidth < 480; // Use compact mode on very small screens
    }
}