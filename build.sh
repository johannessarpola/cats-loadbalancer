#!/bin/sh
sbt clean # rm old
sbt makeDistribution # build fat jar and move to dist