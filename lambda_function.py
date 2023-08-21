import json
import boto3

def send_sms_message(event, context):
    phone = "{phone-number}"
    # SQS 메시지 정보 추출
    records = event.get('Records', [])
    if not records:
        return {
            'statusCode': 400,
            'body': json.dumps('SQS 메시지 정보가 없습니다.')
        }
    
    sqs_message = records[0].get('body')
    if not sqs_message:
        return {
            'statusCode': 400,
            'body': json.dumps('SQS 메시지가 없습니다.')
        }
    # SQS 메시지의 JSON 데이터 추출
    try:
        data = json.loads(sqs_message)
        phoneNumber = data.get('phoneNumber')
        content = data.get('content')
    except Exception as e:
        return {
            'statusCode': 400,
            'body': json.dumps('SQS 메시지 파싱 오류: ' + str(e))
        }
    # 전화번호와 내용 확인 및 처리
    if phoneNumber and content:
        print("전화번호: ", phoneNumber)
        print("내용: ", content)

    sns = boto3.client('sns', region_name='ap-northeast-1', aws_access_key_id='{AccessKey}',
                   aws_secret_access_key='{SecretKey}')
    
    try:
        print(phoneNumber + " / " + content )
        response = sns.publish(
            PhoneNumber=phone,
            Message=phoneNumber + " / " + content 
        )
        print(response)
        return {
            'statusCode': 200,
            'body': json.dumps('메시지가 성공적으로 전송되었습니다.')
        }
    except Exception as e:
        error_message = str(e)
        print(error_message)
        return {
            'statusCode': 500,
            'body': json.dumps('메시지 전송 중 오류가 발생하였습니다. 에러 메시지: ' + error_message)
        }
