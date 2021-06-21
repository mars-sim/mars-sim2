/**
 * Mars Simulation Project
 * NoopMemoryMeterListener.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.memory;

/**
 * Listener that does nothing.
 */
public final class NoopMemoryMeterListener implements MemoryMeterListener {

    /**
     * Singleton instance.
     */
    private static final MemoryMeterListener INSTANCE = new NoopMemoryMeterListener();

    public static final Factory FACTORY = new Factory() {

        @Override
        public MemoryMeterListener newInstance() {
            return INSTANCE;
        }
    };

    @Override
    public void objectMeasured(Object current, long size) {
    }

    @Override
    public void fieldAdded(Object obj, String fieldName, Object fieldValue) {
    }

    @Override
    public void done(long size) {
    }

    @Override
    public void started(Object obj) {
    }

    @Override
    public void objectCounted(Object current) {
    }

    private NoopMemoryMeterListener() {
    }
}
