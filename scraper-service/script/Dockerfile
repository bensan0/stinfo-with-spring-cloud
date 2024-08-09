FROM ubuntu:22.04

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

RUN dpkg --add-architecture amd64 && \
    echo 'deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports lunar main restricted universe multiverse\n\
deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports lunar-updates main restricted universe multiverse\n\
deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports lunar-security main restricted universe multiverse\n\
deb [arch=amd64] http://archive.ubuntu.com/ubuntu lunar main restricted universe multiverse\n\
deb [arch=amd64] http://archive.ubuntu.com/ubuntu lunar-updates main restricted universe multiverse\n\
deb [arch=amd64] http://archive.ubuntu.com/ubuntu lunar-security main restricted universe multiverse' > /etc/apt/sources.list && \
    apt-get update

RUN apt-get install -y --no-install-recommends \
    wget \
    bzip2 \
    openjdk-21-jdk \
    libc6:amd64 \
    libstdc++6:amd64 \
    libxtst6:amd64 \
    libgtk-3-0:amd64 \
    libpci-dev:amd64 \
    libglib2.0-0:amd64 \
    libx11-6:amd64 \
    libdbus-1-3:amd64 \
    libnspr4:amd64 \
    libnss3:amd64 \
    libasound2-plugins:amd64 \
    # libx11-xcb-dev:amd64 \
    # libdbus-glib-1-2:amd64 \
    # libxt6:amd64 \
    # libatk1.0-0:amd64 \
    # libcairo2:amd64 \
    # libgdk-pixbuf2.0-0:amd64 \ 
    # libpango-1.0-0:amd64 \
    # libpangocairo-1.0-0:amd64 \ 
    # libxcomposite1:amd64 \
    # libxcursor1:amd64 \
    # libxdamage1:amd64 \
    # libxext6:amd64 \
    # libxfixes3:amd64 \
    # libxi6:amd64 \
    # libxrandr2:amd64 \
    # libxrender1:amd64 \
    # libxss1:amd64 \
    # libpulse0:amd64 \
    && rm -rf /var/lib/apt/lists/*

RUN wget -O firefox-latest.tar.bz2 "https://download.mozilla.org/?product=firefox-latest&os=linux64&lang=en-US" && \
    tar xjf firefox-latest.tar.bz2 && \
    mv firefox /opt/firefox-latest && \
    ln -s /opt/firefox-latest/firefox /usr/bin/firefox && \
    rm firefox-latest.tar.bz2

ADD ./scraper-service.jar /app/scraper-service.jar

MAINTAINER phillip

EXPOSE 8083

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/scraper-service.jar $PARAMS"]