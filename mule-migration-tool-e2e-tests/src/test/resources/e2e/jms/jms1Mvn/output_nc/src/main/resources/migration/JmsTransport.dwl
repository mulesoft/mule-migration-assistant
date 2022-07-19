%dw 2.0

/**
 * Emulates the properties building logic of the Mule 3.x JMS Connector.
 * Replicates logic from org.mule.transport.jms.transformers.AbstractJmsTransformer.setJmsProperties(MuleMessage, Message).
 */
fun jmsPublishProperties(vars: {}) = do {
    var jmsProperties = ['JMSCorrelationID', 'JMSDeliveryMode', 'JMSDestination', 'JMSExpiration', 'JMSMessageID', 'JMSPriority', 'JMSRedelivered', 'JMSReplyTo', 'JMSTimestamp', 'JMSType', 'selector', 'MULE_REPLYTO']
    ---
    vars filterObject ($$ startsWith 'outbound_') mapObject {($$ dw::core::Strings::substringAfter '_'): $} default {} filterObject
    ((value,key) -> not contains(jmsProperties, (key as String)))
    mapObject ((value, key, index) -> {
        ((key as String) replace " " with "_") : value
        })
}

/**
 * Adapts the Mule 4 correlationId to the way it was used in 3.x
 */
fun jmsCorrelationId(correlationId, vars: {}) = do {
    vars.outbound_MULE_CORRELATION_ID default correlationId
}

/**
 * Adapts the Mule 4 correlationId to the way it was used in 3.x
 */
fun jmsSendCorrelationId(vars: {}) = do {
    if (vars.outbound_MULE_CORRELATION_ID == null) 'NEVER' else 'ALWAYS'
}

/**
 * Adapts the Mule 4 reply-to to the way it was used in 3.x
 */
fun jmsPublishReplyTo(vars: {}) = do {
    message.attributes.headers.replyTo.destination default
    (if (vars.outbound_MULE_REPLYTO != null)
        (vars.outbound_MULE_REPLYTO splitBy 'jms://')[1]
        else null)
}




