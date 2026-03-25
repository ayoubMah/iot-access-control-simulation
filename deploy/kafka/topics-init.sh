#!/bin/bash
# Creates all required Kafka topics.
# Run this once after the Kafka container is healthy.
#
# Usage:
#   docker exec -it badge-kafka bash /topics-init.sh
#   OR mount and run via docker compose exec.

set -e

BOOTSTRAP="kafka:9092"

kafka-topics --bootstrap-server "$BOOTSTRAP" \
  --create --if-not-exists \
  --topic access-events \
  --partitions 1 \
  --replication-factor 1

echo "Kafka topics created successfully."
kafka-topics --bootstrap-server "$BOOTSTRAP" --list
