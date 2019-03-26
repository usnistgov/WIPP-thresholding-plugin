FROM ubuntu
MAINTAINER Mohamed Ouladi

ENV DEBIAN_FRONTEND noninteractive
ARG EXEC_DIR="/opt/executables"
ARG DATA_DIR="/data"

#Prerequisites
RUN apt-get update -y \
    && apt-get install -y software-properties-common

#Install java 8
RUN add-apt-repository ppa:openjdk-r/ppa -y \
    && apt-get update \
    && apt-get install -y openjdk-8-jdk \
    && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

#Create folders
RUN mkdir -p ${EXEC_DIR} \
    && mkdir -p ${DATA_DIR}/inputs \
    && mkdir ${DATA_DIR}/outputs

#Copy executable
COPY target/wipp-thresholding-plugin-0.0.1-SNAPSHOT-jar-with-dependencies.jar ${EXEC_DIR}/.
#COPY dist/Thresholding/target/threshLaunch.sh ${EXEC_DIR}/.

#RUN chmod u+x ${EXEC_DIR}/threshLaunch.sh
WORKDIR ${EXEC_DIR}

# Default command. Additional arguments are provided through the command line
ENTRYPOINT ["java", "-jar", "wipp-thresholding-plugin-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]

