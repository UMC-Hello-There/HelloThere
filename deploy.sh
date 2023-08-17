#!/usr/bin/env bash

REPOSITORY=/home/ubuntu/.ssh/HelloThere
cd $REPOSITORY

APP_NAME=hello_there
JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

CURRENT_PID=$(pgrep -f $APP_NAME)

# 기존 yml 파일 백업 및 복원 함수 정의
backup_and_restore_yml() {
  # 백업 파일 이름 생성 (타임스탬프 활용)
  TIMESTAMP=$(date +%Y%m%d%H%M%S)
  BACKUP_YML_PATH=/src/main/resources/application_$TIMESTAMP.yml

  # 기존 yml 파일 백업
  cp /src/main/resources/application.yml $BACKUP_YML_PATH

  # 서버 재시작 이후에 복원
  echo "> 서버 재시작 이전 기존 yml 파일 백업: $BACKUP_YML_PATH"
  echo "> 기존 yml 파일을 서버 재시작 후에 복원합니다."
  cp $BACKUP_YML_PATH /src/main/resources/application.yml
}

if [ -z $CURRENT_PID ]
then
  echo "> 종료할 애플리케이션이 없습니다."
else
  echo "> kill -9 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

# 기존 yml 파일 백업
backup_and_restore_ymlls


echo "> Deploy - $JAR_PATH "
nohup java -jar $JAR_PATH > /dev/null 2> /dev/null < /dev/null &