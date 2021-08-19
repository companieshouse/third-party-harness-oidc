FROM adoptopenjdk/openjdk11 AS builder

RUN mkdir /opt/app
WORKDIR /opt/app

RUN apt-get update && apt-get install -y make maven

COPY . .

RUN make

FROM adoptopenjdk/openjdk11

RUN mkdir /opt/app
WORKDIR /opt/app

COPY --from=builder /opt/app/third-party-test-harness.jar .

CMD [ "java", "-jar", "-Dserver.port=8090", "third-party-test-harness.jar" ]