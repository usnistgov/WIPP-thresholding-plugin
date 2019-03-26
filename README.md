# Thresholding plugin

## Building

To build from source code, run ./mvn-packager.sh

The pre-built jar file can be found in the target folder.

To build the Docker image for the thresholding plugin,
run `docker build -t wipp-thresh-plugin` or `./build-docker.sh`.

## Thresholdtype options

The options are documented at https://imagej.net/Auto_Threshold.
We are currently supporting:
```
Manual,
IJDefault,
Huang,
Huang2,
Intermodes,
IsoData,
Li,
MaxEntropy,
Mean,
MinErrorI,
Minimum,
Moments,
Otsu,
Percentile,
RenyiEntropy,
Shanbhag,
Triangle,
Yen
```


## Run the plugin

### Manually

```bash
docker run -v /path/to/data:/data wipp-thresh-plugin \
  --input /data/input \
  --thresholdtype Manual \
  --thresholdvalue 150 \
  --output /data/output
```

Note: threshvalue is needed only if thresholdtype is set to Manual.


### Using Argo

```bash
argo submit ./workflow-sample.yaml
```

