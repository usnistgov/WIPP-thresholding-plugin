FROM openjdk:8-jdk-alpine
LABEL maintainer="National Institue of Standards and Technology"

ENV DEBIAN_FRONTEND noninteractive
ARG EXEC_DIR="/opt/executables"
ARG DATA_DIR="/data"

#Create folders
RUN mkdir -p ${EXEC_DIR} \
    && mkdir -p ${DATA_DIR}/inputs \
    && mkdir ${DATA_DIR}/outputs

#Copy executable
COPY target/wipp-thresholding-plugin-0.0.1-SNAPSHOT-jar-with-dependencies.jar ${EXEC_DIR}/wipp-thresholding-plugin.jar
#COPY dist/Thresholding/target/threshLaunch.sh ${EXEC_DIR}/.

# Set working directory
WORKDIR ${EXEC_DIR}

# Default command. Additional arguments are provided through the command line
ENTRYPOINT ["/usr/bin/java", "-jar", "wipp-thresholding-plugin.jar"]