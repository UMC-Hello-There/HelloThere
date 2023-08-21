import smtplib
from email.mime.text import MIMEText
import boto3
import json
import requests

# 이메일 설정
send_email = "{naverEmail}"
send_pwd = "{naverPassword}"
smtp_name = "smtp.naver.com"
smtp_port = 587

# Boto3 클라이언트 생성
sqs = boto3.client('sqs', region_name='ap-northeast-2', aws_access_key_id='{accessKey}',
                   aws_secret_access_key='{secretKey}')
queue_url = "{SQS_URL}"

# 스프링 API 엔드포인트 설정
# spring_api_url = "http://localhost:8080/complete"
def receive_message():
    while True:
        response = sqs.receive_message(
            QueueUrl=queue_url,
            MaxNumberOfMessages=1,
            WaitTimeSeconds=10
        )  
    
        # 메시지 수신 및 처리
        messages = response.get('Messages', [])
        for message in messages:
            body = message['Body']
            receipt_handle = message['ReceiptHandle']
            # body 변수에 들어있는 JSON 문자열을 파싱하여 value 값만 추출하여 text 변수에 저장
            body_dict = json.loads(body)
            recv_email = body_dict['receiverEmail']
            duration = body_dict['duration']
            prohibition = body_dict['prohibition']
            
            if duration == -1:
                text = f"""
                    귀하께서는 신고 누적으로 인해 계정이 영구 정지되었습니다. 
                    어플리케이션의 정책상 영구 정지된 계정을 복구하는 것은 불가합니다.
                    귀하의 계정으로는 더 이상 서비스를 이용할 수 없는 점 양해 바랍니다.
                """    
            else:
                text = f"""
                    귀하께서는 신고 누적으로 인해 현재 {duration}일 {prohibition} 처분되었습니다.
                    신고가 지속되면 애플리케이션 이용에 불이익이 있을 수 있는 점 양해 바라며, 건전한 용도로 이용해주실 것을 부탁드립니다.
                    항상 저희 어플리케이션을 애용해주셔서 진심으로 감사드립니다.
                """
             
            # 메시지 처리 완료 후 SQS에서 메시지 삭제
            sqs.delete_message(
                QueueUrl=queue_url,
                ReceiptHandle=receipt_handle
            )    
        
            # MIMEText 객체 생성을 위해 text 변수가 초기화된 상태를 확인하여 오류 해결
            msg = MIMEText(text)
            msg["Subject"] = "[Hello There] 신고 누적으로 인해 제재되었습니다."
            msg["From"] = send_email
            msg["To"] = recv_email
            print(msg.as_string())
            # 이메일 발송
            s = smtplib.SMTP(smtp_name, smtp_port)
            s.starttls()
            s.login(send_email, send_pwd)
            s.sendmail(send_email, recv_email, msg.as_string())
            s.quit()
            # 스프링 API 호출
            #response = requests.get(spring_api_url, params={"title": text})
            #if response.status_code == 200:
            #    print(response.json())
            #else:
            #    print("스프링 API 호출에 실패했습니다.")
# 메시지 수신 반복
while True:
    receive_message()
