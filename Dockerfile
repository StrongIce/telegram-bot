FROM ubuntu:20.04

ENV TZ=Asia/Novosibirsk

RUN apt update -y \
    && apt install -y python3 python3-pip python3-dev libldap2-dev libsasl2-dev 

RUN mkdir /opt/jira-bot

WORKDIR /jira-bot

ADD . ./

RUN pip3 install -r requirements.txt

CMD "python3" "./app.py"