package name.modid;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DusaruysClient implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("dusaruysclient");

    @Override
    public void onInitialize() {
        LOGGER.info("DusaruysClient loaded!");
    }
}
