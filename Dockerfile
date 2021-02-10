FROM openjdk:8-jdk-alpine
LABEL maintainer="National Institute of Standards and Technology"

COPY VERSION /

ENV DEBIAN_FRONTEND noninteractive
ARG EXEC_DIR="/opt/executables"
ARG DATA_DIR="/data"

#Create folders
RUN mkdir -p ${EXEC_DIR} \
    && mkdir -p ${DATA_DIR}/inputs \
    && mkdir ${DATA_DIR}/outputs

# Copy wipp-thresholding-plugin JAR
COPY target/wipp-thresholding-plugin*.jar ${EXEC_DIR}/wipp-thresholding-plugin.jar


# Default command. Additional arguments are provided through the command line
ENTRYPOINT ["java", "-jar", "/opt/executables/wipp-thresholding-plugin.jar"]