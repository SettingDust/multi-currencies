package me.settingdust.multicurrencies;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

/**
 * @author The EpicBanItem Team
 */
@Singleton
public class ObservableFileServiceImpl implements ObservableFileService, Closeable {
    private final Map<String, ObservableFileRegistry> observableDirectories;
    private final Task task;

    @Inject
    public ObservableFileServiceImpl(PluginContainer pluginContainer, EventManager eventManager) {
        this.observableDirectories = Maps.newHashMap();

        this.task =
            Task
                .builder()
                .execute(task -> observableDirectories.values().forEach(observableFileRegistry -> observableFileRegistry.tick(task)))
                .intervalTicks(1)
                .submit(pluginContainer);

        eventManager.registerListener(pluginContainer, GameStoppingEvent.class, this::onStopping);
    }

    @Override
    public void register(ObservableFile observableFile) {
        Path filePath = observableFile.getPath().toAbsolutePath();
        Path dirPath = Files.isDirectory(filePath) ? filePath : filePath.getParent();
        String dirPathString = dirPath.toString();
        try {
            if (!observableDirectories.containsKey(dirPathString)) {
                observableDirectories.put(dirPathString, new ObservableFileRegistry(dirPath));
            }
            observableDirectories.get(dirPathString).register(observableFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        this.task.cancel();
        for (ObservableFileRegistry registry : this.observableDirectories.values()) {
            registry.close();
        }
    }

    private void onStopping(GameStoppingEvent event) throws IOException {
        this.close();
    }
}
