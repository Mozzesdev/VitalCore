package me.winflix.vitalcore.citizen.utils;

import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;

public class BukkitCompletable<T> {
    private final Plugin plugin;
    private final UnsafeSupplier<T> runnable;
    private Consumer<Throwable> errorHandler;
    private Consumer<T> callbackHandler;
    private Runnable emptyCallbackHandler;

    private BukkitCompletable(Plugin plugin, UnsafeSupplier<T> runnable) {
        this.plugin = plugin;
        this.runnable = runnable;
    }

    public static <T> BukkitCompletable<T> supplyASync(Plugin plugin, UnsafeSupplier<T> runnable) {
        return new BukkitCompletable<T>(plugin, runnable);
    }

    public BukkitCompletable<T> onFinish(Consumer<T> callbackHandler) {
        this.callbackHandler = callbackHandler;
        return this;
    }

    public BukkitCompletable<T> onEmptyFinish(Runnable emptyCallbackHandler) {
        this.emptyCallbackHandler = emptyCallbackHandler;
        return this;
    }

    public BukkitCompletable<T> onException(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public void getSafe() {
        this.get(true);
    }

    public void getUnsafe() {
        this.get(false);
    }

    public T getSync() throws Exception {
        return runnable.get();
    }

    private void get(boolean safe) {
        async(plugin, () -> {
            try {
                T t = this.runnable.get();
                if (!(this.callbackHandler == null && this.emptyCallbackHandler == null)) {
                    if (safe) {
                        sync(plugin, () -> {
                            if (callbackHandler != null)
                                this.callbackHandler.accept(t);
                            if (emptyCallbackHandler != null)
                                this.emptyCallbackHandler.run();
                        });
                    } else {
                        if (callbackHandler != null)
                            this.callbackHandler.accept(t);
                        if (emptyCallbackHandler != null)
                            this.emptyCallbackHandler.run();
                    }
                }
            } catch (Throwable e) {
                if (this.errorHandler != null) {
                    if (safe)
                        sync(plugin, () -> this.errorHandler.accept(e));
                    else
                        this.errorHandler.accept(e);
                }
            }
        });
    }


    @FunctionalInterface
    public interface UnsafeSupplier<T> {
        T get() throws Exception;
    }

    public void sync(Plugin plugin, Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void async(Plugin plugin, Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

}