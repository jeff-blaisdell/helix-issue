package helix;

import org.apache.helix.*;
import org.apache.helix.api.listeners.CurrentStateChangeListener;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.model.CurrentState;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.model.Message;
import org.apache.helix.model.OnlineOfflineSMD;
import org.apache.helix.participant.statemachine.StateModel;
import org.apache.helix.participant.statemachine.StateModelFactory;
import org.apache.helix.participant.statemachine.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class ParticipantService {

    private static final Logger log = LoggerFactory.getLogger(ParticipantService.class);

    private final ParticipantConfig config;
    private final HelixAdmin admin;
    private HelixManager participant;

    public ParticipantService(ParticipantConfig config) {
        this.config = config;
        this.admin = new ZKHelixAdmin.Builder()
            .setZkAddress(config.getZkSvr())
            .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() throws Exception {
        participant = HelixManagerFactory.getZKHelixManager(
            config.getCluster(),
            config.getInstanceName(),
            InstanceType.PARTICIPANT,
            config.getZkSvr()
        );
        participant.getStateMachineEngine().registerStateModelFactory(
            OnlineOfflineSMD.name,
            new DefaultStateModelFactory()
        );

        if (!admin.getInstancesInCluster(config.getCluster()).contains(participant.getInstanceName())) {
            admin.addInstance(config.getCluster(), new InstanceConfig(participant.getInstanceName()));
        } else {
            admin.enableInstance(config.getCluster(), participant.getInstanceName(), true);
        }

        log.info("Starting helix participant {}", participant.getInstanceName());
        participant.connect();
    }

    @PreDestroy
    public void onShutdown() throws Exception {
        log.info("Stopping helix controller");
        this.participant.disconnect();

        if (participant != null) {
            log.info("Disabling helix participant {}", participant.getInstanceName());
            CountDownLatch latch = new CountDownLatch(1);
            admin.enableInstance(config.getCluster(), participant.getInstanceName(), false);
            participant.addCurrentStateChangeListener(
                new DefaultStateChangeListener(latch),
                participant.getInstanceName(), participant.getSessionId()
            );
            // wait for all partitions to be handed off before disconnecting and dropping the participant
            latch.await();
            participant.disconnect();
            log.info("Disconnected participant {}", participant.getInstanceName());
            admin.dropInstance(config.getCluster(), new InstanceConfig(participant.getInstanceName()));
            log.info("Dropped participant {} from cluster", participant.getInstanceName());
        }
        admin.close();
    }

    public class DefaultStateChangeListener implements CurrentStateChangeListener {

        private final CountDownLatch latch;

        public DefaultStateChangeListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onStateChange(
            String instanceName,
            List<CurrentState> statesInfo,
            NotificationContext changeContext
        ) {
            Long inUsePartitions = statesInfo.stream()
                .filter(s -> s.getResourceName().equals(config.getResourceName())).findFirst()
                .map(state -> state.getPartitionStateMap().values().stream()
                    .filter(s -> !s.equals("DROPPED")).count())
                .orElse(0L);
            if (inUsePartitions == 0) {
                log.info("all of participant {} partitions has been dropped", participant.getInstanceName());
                latch.countDown();
            } else {
                log.info("participant {} state changed but still has {} non-dropped partitions",
                    participant.getInstanceName(), inUsePartitions);
            }
        }
    }

    public static class DefaultStateModelFactory extends StateModelFactory<DefaultStateModel> {
        @Override
        public DefaultStateModel createNewStateModel(String resourceName, String partitionName) {
            long partition = Long.parseLong(partitionName.replaceFirst(resourceName + "_", ""));
            return new DefaultStateModel(partition);
        }
    }

    public static class DefaultStateModel extends StateModel {
        private static final Logger log = LoggerFactory.getLogger(DefaultStateModel.class);

        private final long partition;

        public DefaultStateModel(long partition) {
            this.partition = partition;
        }

        @Transition(from = "OFFLINE", to = "ONLINE")
        public void onBecomeOnlineFromOffline(Message message, NotificationContext context) {
            log.info("partition {} going online", partition);
        }


        @Transition(from = "ONLINE", to = "OFFLINE")
        public void onBecomeOfflineFromOnline(Message message, NotificationContext context) {
            log.info("partition {} going offline", partition);
        }

        @Transition(from = "OFFLINE", to = "DROPPED")
        public void onBecomeDroppedFromOffline(Message message, NotificationContext context) {
            log.info("partition {} getting dropped", partition);
        }
    }
}
