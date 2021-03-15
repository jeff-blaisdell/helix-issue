# Helix Issue

https://github.com/apache/helix/issues/1670

### Setup
* Helix 1.0.1
* Java 8+
* Cluster Definition
  ```
    clusterName: default-cluster
    resources:
      - name: default-resource
        rebalancer:
          mode: FULL_AUTO
        partitions:
          count: 3
          replicas: 1
        stateModel:
          name: OnlineOffline
  ```

### Problem
Helix controller emits a seemingly incorrect "DifferenceWithIdealStateGauge" metric.

### Observed Behavior
On a rebalance operation triggered by a participant entering / leaving a cluster, the standalone controller
never appears to write the IdealState to Zookeeper.  However, the external state is updated correctly.

We recently upgraded from 0.6.9 to 1.0.1.  On the 0.6.9 version we see similar behavior to 1.0.1 in
that neither appear to write out the ideal state.  However, 1.0.1 did change how the `DifferenceWithIdealStateGauge`
was calculated.  In `0.6.9`, if no ideal state was found, `DifferenceWithIdealStateGauge` would be 0. In `1.0.1`,
it correctly emits the difference.

[(1.0.1) ResourceMonitor.java](https://github.com/apache/helix/blob/helix-1.0.1/helix-core/src/main/java/org/apache/helix/monitoring/mbeans/ResourceMonitor.java#L243)

[(0.6.9) ResourceMonitor.java](https://github.com/apache/helix/blob/helix-0.6.9/helix-core/src/main/java/org/apache/helix/monitoring/mbeans/ResourceMonitor.java#L98)

#### Example --listResourceInfo Output
In below example, "DifferenceWithIdealStateGauge" will equal 3.
```
IdealState for default-resource:
{
  "id" : "default-resource",
  "mapFields" : {
    "default-resource_0" : {
    },
    "default-resource_1" : {
    },
    "default-resource_2" : {
    }
  },
  "listFields" : {
    "default-resource_0" : [ ],
    "default-resource_1" : [ ],
    "default-resource_2" : [ ]
  },
  "simpleFields" : {
    "IDEAL_STATE_MODE" : "AUTO_REBALANCE",
    "NUM_PARTITIONS" : "3",
    "REBALANCE_MODE" : "FULL_AUTO",
    "REBALANCE_STRATEGY" : "DEFAULT",
    "REPLICAS" : "1",
    "STATE_MODEL_DEF_REF" : "OnlineOffline",
    "STATE_MODEL_FACTORY_NAME" : "DEFAULT"
  }
}

ExternalView for default-resource:
{
  "id" : "default-resource",
  "mapFields" : {
    "default-resource_0" : {
      "localhost_2181" : "ONLINE"
    },
    "default-resource_1" : {
      "localhost_2181" : "ONLINE"
    },
    "default-resource_2" : {
      "localhost_2181" : "ONLINE"
    }
  },
  "listFields" : {
  },
  "simpleFields" : {
    "BUCKET_SIZE" : "0",
    "IDEAL_STATE_MODE" : "AUTO_REBALANCE",
    "NUM_PARTITIONS" : "3",
    "REBALANCE_MODE" : "FULL_AUTO",
    "REBALANCE_STRATEGY" : "DEFAULT",
    "REPLICAS" : "1",
    "STATE_MODEL_DEF_REF" : "OnlineOffline",
    "STATE_MODEL_FACTORY_NAME" : "DEFAULT"
  }
}
```

### Steps to Reproduce
1. Start Zookeeper `docker-compose up -d`
1. Initialize Helix cluster `./etc/setup-helix.sh`
1. Start Helix controller `./gradlew :controller:bootRun`
1. Start Helix participant `./gradlew :participant:bootRun`
1. Wait for rebalance to complete.
1. Review helix cluster state `./etc/view-helix.sh`
1. Connect to Helix Controller JMX and view `ClusterStatus -> default-cluster -> default-resource`

### Solution
Setting a seemingly undocumented option can "fix" the issue of ideal state being persisted.  Once the ideal state
is persisted, the `DifferenceWithIdealStateGauge` metric will be correct.  The following command can be issued 
against Helix to enable the option:

```
helix-admin.sh --zkSvr localhost:2181 --setConfig CLUSTER default-cluster PERSIST_BEST_POSSIBLE_ASSIGNMENT=true
```
