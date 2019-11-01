#!/bin/bash
version=$(<VERSION)
docker build . -t wipp/wipp-thresh-plugin:${version}
