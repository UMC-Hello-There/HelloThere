#!/usr/bin/env bash
BUILD_JAR=$(ls /home/ubuntu/.ssh/HelloThere/build/libs/*.jar | tail -n 1) # /home/ubuntu/.ssh/HelloThere/build/libs/hello_there-0.0.1-SNAPSHOT.jar
REPOSITORY=/home/ubuntu/.ssh/HelloThere

cd $REPOSITORY

APP_NAME=hello_there-0.0.1-SNAPSHOT.jar
JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1) # hello_there-0.0.1-SNAPSHOT.jar
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME # /home/ubuntu/.ssh/HelloThere/build/libs/hello_there-0.0.1-SNAPSHOT.jar

DEPLOY_PATH=/home/ubuntu/.ssh/HelloThere/
cp $BUILD_JAR $DEPLOY_PATH # cp /home/ubuntu/.ssh/HelloThere/build/libs/hello_there-0.0.1-SNAPSHOT.jar /home/ubuntu/.ssh/HelloThere/

CURRENT_PID=$(pgrep -f $APP_NAME) # 기존 서버 프로세스의 pid

if [ -z $CURRENT_PID ] # current pid의 길이가 zero이면(= 기존 프로세스가 없으면)
then
  echo "> 종료할 애플리케이션이 없습니다."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME # /home/ubuntu/.ssh/HelloThere/hello_there-0.0.1-SNAPSHOT.jar
chmod +x $DEPLOY_JAR
echo "> Deploy - $JAR_PATH "
nohup java -jar $DEPLOY_JAR > /dev/null 2> /dev/null < /dev/null &