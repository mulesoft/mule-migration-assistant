%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.extensions.jms.api.message.JmsAttributes')
{
    'JMSCorrelationID': message.attributes.headers.correlationId,
    'MULE_CORRELATION_ID': message.attributes.headers.correlationId,
    'JMSDeliveryMode': message.attributes.headers.deliveryMode,
    'JMSDestination': message.attributes.headers.destination,
    'JMSExpiration': message.attributes.headers.expiration,
    'JMSMessageID': message.attributes.headers.messageId,
    'JMSPriority': message.attributes.headers.priority,
    'JMSRedelivered': message.attributes.headers.redelivered,
    'JMSReplyTo': message.attributes.headers.replyTo.destination,
    'JMSTimestamp': message.attributes.headers.timestamp,
    'JMSType': message.attributes.headers['type']
}
 ++ message.attributes.properties.userProperties
else
{}
