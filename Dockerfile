FROM gradle:jdk11 as builder
COPY --chown=gradle:gradle . /home/gradle/fxgame
WORKDIR /home/gradle/fxgame
RUN gradle dist --no-daemon
RUN unzip /home/gradle/fxgame/build/distributions/game2048-*.zip -d /tmp/fxgame

FROM ubuntu:18.04
COPY --from=builder /tmp/fxgame/image/ /fxgame
RUN apt-get update && apt-get install --no-install-recommends -y xorg libgl1-mesa-glx && rm -rf /var/lib/apt/lists/* 
WORKDIR /fxgame
CMD ./bin/fxgame
 
