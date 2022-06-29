%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.extension.email.api.attributes.POP3EmailAttributes')
{
    'toAddresses': message.attributes.toAddresses joinBy ', ',
    'ccAddresses': message.attributes.ccAddresses joinBy ', ',
    'bccAddresses': message.attributes.bccAddresses joinBy ', ',
    'replyToAddresses': message.attributes.replyToAddresses joinBy ', ',
    'fromAddresses': message.attributes.fromAddresses joinBy ', ',
    'subject': if (message.attributes.subject != '') message.attributes.subject else '(no subject)',
    'contentType': payload.^mimeType,
    'sentDate': message.attributes.sentDate
}
 ++ message.attributes.headers mapObject ((value, key, index) -> { key : value })
else
{}
