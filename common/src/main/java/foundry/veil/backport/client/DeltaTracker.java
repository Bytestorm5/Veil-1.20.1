package foundry.veil.backport.client;

import net.minecraft.client.Minecraft;

/**
 * Backport of {@code net.minecraft.client.DeltaTracker}.
 * <p>
 * On 1.20.1 the game passes a single partial tick around. {@link #of(float, float)} wraps those values so APIs that
 * expect a delta tracker can be called.
 */
public interface DeltaTracker {

    DeltaTracker ZERO = new DefaultValue(0.0F);
    DeltaTracker ONE = new DefaultValue(1.0F);

    /**
     * @return The number of ticks that passed since the last frame
     */
    float getGameTimeDeltaTicks();

    /**
     * @param runsNormally Whether to get the partial tick of the game when it is running normally, ignoring pausing
     * @return The current partial tick
     */
    float getGameTimeDeltaPartialTick(boolean runsNormally);

    /**
     * @return The number of ticks that passed since the last frame, ignoring pausing
     */
    float getRealtimeDeltaTicks();

    /**
     * Creates a delta tracker for a single frame.
     *
     * @param deltaTicks  The ticks passed since the last frame
     * @param partialTick The partial tick of the frame
     * @return A new delta tracker
     */
    static DeltaTracker of(float deltaTicks, float partialTick) {
        return new Snapshot(deltaTicks, partialTick);
    }

    /**
     * Equivalent of 1.21's {@code Minecraft#getTimer()}. Only call this on the client.
     *
     * @return A delta tracker for the frame currently being rendered
     */
    static DeltaTracker current() {
        Minecraft minecraft = Minecraft.getInstance();
        return new Snapshot(minecraft.getDeltaFrameTime(), minecraft.getFrameTime());
    }

    record DefaultValue(float value) implements DeltaTracker {

        @Override
        public float getGameTimeDeltaTicks() {
            return this.value;
        }

        @Override
        public float getGameTimeDeltaPartialTick(boolean runsNormally) {
            return this.value;
        }

        @Override
        public float getRealtimeDeltaTicks() {
            return this.value;
        }
    }

    record Snapshot(float deltaTicks, float partialTick) implements DeltaTracker {

        @Override
        public float getGameTimeDeltaTicks() {
            return this.deltaTicks;
        }

        @Override
        public float getGameTimeDeltaPartialTick(boolean runsNormally) {
            return this.partialTick;
        }

        @Override
        public float getRealtimeDeltaTicks() {
            return this.deltaTicks;
        }
    }
}
