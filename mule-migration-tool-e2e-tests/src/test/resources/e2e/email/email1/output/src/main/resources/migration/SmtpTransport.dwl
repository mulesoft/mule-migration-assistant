%dw 2.0

fun smtpToAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.toAddresses[0]
}

fun smtpCcAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.ccAddresses[0]
}

fun smtpBccAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.bccAddresses[0]
}

fun smtpFromAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.fromAddress
}

fun smtpReplyToAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.replyToAddresses[0]
}

fun smtpSubject(vars: {}) = do {
    vars.compatibility_outboundProperties.subject
}

fun smtpCustomHeaders(vars: {}) = do {
    vars.compatibility_outboundProperties.customHeaders
}
