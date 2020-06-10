package sp.yeyu.customeenchants.customenchants.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

import java.util.Objects;

public class DataManager {

    private static final Logger LOGGER = LogManager.getLogger(DataManager.class);

    private DataManager() {
    }

    public enum IntAttributes {
        REFRESH_RATE("refreshTickRate", 5, 1, "Refresh rate too low!"),
        EFFECT_DURATION("effectDurationTick", 60, 1, "Effect duration too low!"),
        DEV_MODE("devMode", 0, null, null),
        EXPERIMENTAL_ANVIL("useExperimentalAnvil", 1, null, null);

        public final String attrName;
        public final int defaultValue;
        public final Integer minValue;
        public final String errorMessage;
        public int value;

        IntAttributes(String attrName, int defaultValue, Integer minValue, String errorMessage) {
            this.attrName = attrName;
            this.defaultValue = defaultValue;
            this.minValue = minValue;
            this.errorMessage = errorMessage;

            final DataStorageInstance data = EnchantPlus.getPluginData().getData(EnchantPlus.DEV_DATA_FILENAME);
            final Integer attrValue = data.getIntegerOrDefault(this.attrName, this.defaultValue);
            if (Objects.nonNull(this.minValue) && attrValue < this.minValue) {
                LOGGER.error(String.format("%s Using default value: %d", this.errorMessage, this.defaultValue));
                data.putAttr(this.attrName, this.defaultValue);
                this.value = this.defaultValue;
            } else {
                this.value = attrValue;
            }
            LOGGER.info(String.format("(%s) Plugin configuration - %s: %d", EnchantPlus.PLUGIN_NAME, this.attrName, this.value));
        }

        public int getValue() {
            return value;
        }
    }
}
