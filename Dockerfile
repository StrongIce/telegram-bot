FROM ubuntu:20.04
ENV TZ=Asia/Novosibirsk
RUN apt-get update -y
RUN apt install python3 -y
RUN apt install python3-pip -y
RUN apt-get install python3-dev -y
RUN apt-get install libldap2-dev -y
RUN apt-get install libsasl2-dev -y
RUN mkdir /opt/jira-bot
WORKDIR /jira-bot
ADD . ./
RUN pip3 install -r requirements.txt
CMD "python3" "./app.py"
