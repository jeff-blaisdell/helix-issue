package helix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("participant")
public class ParticipantConfig {

    private String zkSvr = "localhost:2181";
    private String cluster;
    private String resourceName;
    private String instanceName;

    public String getZkSvr() {
        return zkSvr;
    }

    public void setZkSvr(String zkSvr) {
        this.zkSvr = zkSvr;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
