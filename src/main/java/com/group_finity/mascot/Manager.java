package com.group_finity.mascot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import java.awt.Point;

/**
 *
 * Maintains a list of mascot, the object to time.
 * <p>
 * Original Author: Yuki Yamada of Group Finity
 * (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */
public class Manager {

	private static final Logger log = Logger.getLogger(Manager.class.getName());

	/**
	 * Interval timer is running.
	 */
	public static final int TICK_INTERVAL = 40;

	/**
	 * A list of mascot.
	 */
	private final List<Mascot> mascots = new ArrayList<>();

	/**
	 * The mascot will be added later.
	 * (@Link ConcurrentModificationException) to prevent the addition of the mascot
	 * (@link # tick ()) are each simultaneously reflecting.
	 */
	private final Set<Mascot> added = new LinkedHashSet<>();

	/**
	 * The mascot will be added later.
	 * (@Link ConcurrentModificationException) to prevent the deletion of the mascot
	 * (@link # tick ()) are each simultaneously reflecting.
	 */
	private final Set<Mascot> removed = new LinkedHashSet<>();

	private boolean exitOnLastRemoved = true;

	private Thread thread;

	public void setExitOnLastRemoved(boolean exitOnLastRemoved) {
		this.exitOnLastRemoved = exitOnLastRemoved;
	}

	public boolean isExitOnLastRemoved() {
		return exitOnLastRemoved;
	}

	public Manager() {

		new Thread() {
			{
				this.setDaemon(true);
				this.start();
			}

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(Integer.MAX_VALUE);
					} catch (final InterruptedException ignored) {
					}
				}
			}
		};
	}

	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}

		thread = new Thread(() -> {

            long prev = System.nanoTime() / 1000000;
            try {
                for (;;) {
                    for (;;) {
                        final long cur = System.nanoTime() / 1000000;
                        if (cur - prev >= TICK_INTERVAL) {
                            if (cur > prev + TICK_INTERVAL * 2) {
                                prev = cur;
                            } else {
                                prev += TICK_INTERVAL;
                            }
                            break;
                        }
                        Thread.sleep(1, 0);
                    }

                    tick();
                }
            } catch (final InterruptedException ignored) {
            }
        });
		thread.setDaemon(false);

		thread.start();
	}

	public void stop() {
		if (thread == null || !thread.isAlive()) {
			return;
		}
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException ignored) {
		}
	}

	private void tick() {
		// Update the first environmental information
		NativeFactory.getInstance().getEnvironment().tick();

		synchronized (this.getMascots()) {

			// Add the mascot if it should be added
			for (final Mascot mascot : this.getAdded()) {
				this.getMascots().add(mascot);
			}
			this.getAdded().clear();

			// Remove the mascot if it should be removed
			for (final Mascot mascot : this.getRemoved()) {
				this.getMascots().remove(mascot);
			}
			this.getRemoved().clear();

			// Advance mascot's time
			for (final Mascot mascot : this.getMascots()) {
				mascot.tick();
			}

			// Advance mascot's time
			for (final Mascot mascot : this.getMascots()) {
				mascot.apply();
			}
		}

		if (isExitOnLastRemoved()) {
			if (this.getMascots().isEmpty()) {
				Main.getInstance().exit();
			}
		}
	}

	public void add(final Mascot mascot) {
		synchronized (this.getAdded()) {
			this.getAdded().add(mascot);
			this.getRemoved().remove(mascot);
		}
		mascot.setManager(this);
	}

	public void remove(final Mascot mascot) {
		synchronized (this.getAdded()) {
			this.getAdded().remove(mascot);
			this.getRemoved().add(mascot);
		}
		mascot.setManager(null);
	}

	public void setBehaviorAll(final String name) {
		synchronized (this.getMascots()) {
			for (final Mascot mascot : this.getMascots()) {
				try {
					Configuration configuration = Main.getInstance().getConfiguration(mascot.getImageSet());
					mascot.setBehavior(configuration.buildBehavior(configuration.getSchema().getString(name), mascot));
				} catch (final BehaviorInstantiationException e) {
					log.log(Level.SEVERE, "Failed to initialize the following actions", e);
					Main.showError(Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage")
							+ "\n" + e.getMessage() + "\n"
							+ Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
					mascot.dispose();
				} catch (final CantBeAliveException e) {
					log.log(Level.SEVERE, "Fatal Error", e);
					Main.showError(Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage")
							+ "\n" + e.getMessage() + "\n"
							+ Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
					mascot.dispose();
				}
			}
		}
	}

	public void setBehaviorAll(final Configuration configuration, final String name, String imageSet) {
		synchronized (this.getMascots()) {
			for (final Mascot mascot : this.getMascots()) {
				try {
					if (mascot.getImageSet().equals(imageSet)) {
						mascot.setBehavior(
								configuration.buildBehavior(configuration.getSchema().getString(name), mascot));
					}
				} catch (final BehaviorInstantiationException e) {
					log.log(Level.SEVERE, "Failed to initialize the following actions", e);
					Main.showError(Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage")
							+ "\n" + e.getMessage() + "\n"
							+ Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
					mascot.dispose();
				} catch (final CantBeAliveException e) {
					log.log(Level.SEVERE, "Fatal Error", e);
					Main.showError(Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage")
							+ "\n" + e.getMessage() + "\n"
							+ Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
					mascot.dispose();
				}
			}
		}
	}

	public void remainOne() {
		synchronized (this.getMascots()) {
			// Keep first mascot, dispose the rest
			getMascots().stream()
					.skip(1)
					.forEach(Mascot::dispose);
		}
	}

	public void remainOne(Mascot mascot) {
		synchronized (this.getMascots()) {
			// Dispose all mascots except the specified one
			getMascots().stream()
					.filter(m -> !m.equals(mascot))
					.forEach(Mascot::dispose);
		}
	}

	public void remainOne(String imageSet) {
		synchronized (this.getMascots()) {
			// Find all mascots with matching imageSet, keep first one and dispose rest
			List<Mascot> matching = getMascots().stream()
					.filter(m -> m.getImageSet().equals(imageSet))
					.toList();

			// Dispose all but the first matching mascot
			matching.stream()
					.skip(1)
					.forEach(Mascot::dispose);
		}
	}

	public void remainNone(String imageSet) {
		synchronized (this.getMascots()) {
			// Use removeIf for more efficient removal with Java 8
			this.getMascots().removeIf(mascot -> {
				if (mascot.getImageSet().equals(imageSet)) {
					mascot.dispose();
					return true;
				}
				return false;
			});
		}
	}

	public void togglePauseAll() {
		boolean isPaused = true;
		
		synchronized (this.getMascots()) {
			for (final Mascot mascot : this.getMascots()) {
				if (!mascot.isPaused()) {
					isPaused = false;
					break;
				}
			}
			
			for (final Mascot mascot : this.getMascots()) {
				mascot.setPaused(!isPaused);
			}
		}
	}

	public boolean isPaused() {
		boolean isPaused = true;
		
		synchronized (this.getMascots()) {
			for (final Mascot mascot : this.getMascots()) {
				if (!mascot.isPaused()) {
					isPaused = false;
					break;
				}
			}
		}
		
		return isPaused;
	}

	public int getCount() {
		return getCount(null);
	}

	public int getCount(String imageSet) {
		synchronized (getMascots()) {
			if (imageSet == null) {
				return getMascots().size();
			} else {
				// Use Stream API for counting
				return (int) getMascots().stream()
						.filter(mascot -> mascot.getImageSet().equals(imageSet))
						.count();
			}
		}
	}

	private List<Mascot> getMascots() {
		return this.mascots;
	}

	private Set<Mascot> getAdded() {
		return this.added;
	}

	private Set<Mascot> getRemoved() {
		return this.removed;
	}

	/**
	 * Returns a Mascot with the given affordance.
	 * 
	 * @return A WeakReference to a mascot with the required affordance, or null
	 */
	public WeakReference<Mascot> getMascotWithAffordance(String affordance) {
		synchronized (this.getMascots()) {
			// Use Stream API to find first matching mascot
			Optional<Mascot> found = getMascots().stream()
					.filter(mascot -> mascot.getAffordances().contains(affordance))
					.findFirst();

			return found.map(WeakReference::new).orElse(null);
		}
	}

	public boolean hasOverlappingMascotsAtPoint(Point anchor) {
		synchronized (this.getMascots()) {
			// Use Stream API to count mascots at the given anchor point
			long count = getMascots().stream()
					.filter(mascot -> mascot.getAnchor().equals(anchor))
					.count();

			return count > 1;
		}
	}

	public void disposeAll() {
		synchronized (this.getMascots()) {
			// Use forEach for cleaner disposal
			getMascots().forEach(Mascot::dispose);
			getMascots().clear();
		}
	}
}
