package com.fable.fablesiegeplugin.config;

import de.leonhard.storage.Json;
import lombok.Getter;

public class DataManager {

    private @Getter Json config = new Json("config", "plugins/FableSiegePlugin");
}
