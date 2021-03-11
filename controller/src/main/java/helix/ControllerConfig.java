package helix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("controller")
public class ControllerConfig {
    public static final String STANDALONE = "STANDALONE";

    private String zkSvr = "localhost:2181";
    private String cluster = "ticker-scheduler";
    private String mode = STANDALONE;
    private String name = null;

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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
