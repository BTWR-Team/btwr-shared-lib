package org.btwr.shared_library;

import org.btwr.shared_library.compat.LeaveMyBarsAloneCompat;
import org.btwr.shared_library.compat.ToughAsNailsCompat;
import org.btwr.shared_library.config.BTWRSLModConfig;
import net.fabricmc.api.ClientModInitializer;
import org.btwr.shared_library.gui.hud.PenaltyDisplayManager;

public class BTWRSLModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BTWRSLModConfig.register();

        // Default status bar offset conditions for penalty text
        PenaltyDisplayManager.HudYOffsetRegistry.registerDefaults();

        // Compatibility modules
        LeaveMyBarsAloneCompat.init();
        ToughAsNailsCompat.init();
    }
}