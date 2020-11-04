# gist-backup

Create backup for your Gist [https://gist.github.com/](https://gist.github.com/)

## Build

You can build the project with two way

### First with Maven

1. Install Maven [https://maven.apache.org/](https://maven.apache.org/)
1. Install Java 8 or higher
1. Execute in project directory ```mvn clean package```
1. Jar file in ```target``` folder ready for use
1. Run as ```java -Dgist.token={you gist token} -Dgist.username={username} -Dgist.bkp.folder={backup folder} -jar {jar file}```


### Second with Docker

1. Install Docker [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
1. Build image ```docker build -t gist-backup:v1 .```
1. Run ```docker run --name=gist-backup -e ARGS=" -Dgist.token={you gist token} -Dgist.username={username} -Dgist.bkp.folder={backup folder}" -it gist-backup:v1```