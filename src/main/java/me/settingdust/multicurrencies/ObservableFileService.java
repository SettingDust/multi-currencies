package me.settingdust.multicurrencies;

import com.google.inject.ImplementedBy;

/**
 * @author The EpicBanItem Team
 */
@ImplementedBy(ObservableFileServiceImpl.class)
public interface ObservableFileService {
    void register(ObservableFile observableFile);
}
