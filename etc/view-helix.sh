#!/bin/bash
PARENT_PATH=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
HELIX_BIN=$PARENT_PATH/../helix-core-1.0.1/bin

$HELIX_BIN/helix-admin.sh --zkSvr localhost:2181 --listResourceInfo default-cluster default-resource
