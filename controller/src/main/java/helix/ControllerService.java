package helix;

import org.apache.helix.HelixManager;
import org.apache.helix.controller.GenericHelixController;
import org.apache.helix.controller.HelixControllerMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class ControllerService {

    private static final Logger log = LoggerFactory.getLogger(ControllerService.class);
    private final transient ControllerConfig config;
    private transient HelixManager helixManager;

    public ControllerService(ControllerConfig config) {
        this.config = config;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() throws Exception {
        log.info("Starting helix controller");
        this.helixManager = HelixControllerMain.startHelixController(
            config.getZkSvr(),
            config.getCluster(),
            config.getName(),
            config.getMode()
        );
         GenericHelixController controller = new GenericHelixController(config.getCluster());
         this.helixManager.addControllerListener(controller);
         this.helixManager.addInstanceConfigChangeListener(controller);
         this.helixManager.addResourceConfigChangeListener(controller);
         this.helixManager.addClusterfigChangeListener(controller);
         this.helixManager.addCustomizedStateConfigChangeListener(controller);
         this.helixManager.addLiveInstanceChangeListener(controller);
         this.helixManager.addIdealStateChangeListener(controller);
    }

    @PreDestroy
    public void onShutdown() throws Exception {
        log.info("Stopping helix controller");
        this.helixManager.disconnect();
    }
}
