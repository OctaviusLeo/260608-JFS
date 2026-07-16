#!/bin/sh
# Docker volumes are owned by root when they first mount, so the spring user
# can't write to /opt/app/data until we fix that. We do it here at startup,
# then hand off to the spring user for the actual app launch.
mkdir -p /opt/app/data
chown spring:spring /opt/app/data
exec gosu spring java org.springframework.boot.loader.launch.JarLauncher "$@"
