%dw 2.0

fun smtpToAddress(vars: {}) = do {
    vars.outbound_toAddresses[0]
}

fun smtpCcAddress(vars: {}) = do {
    vars.outbound_ccAddresses[0]
}

fun smtpBccAddress(vars: {}) = do {
    vars.outbound_bccAddresses[0]
}

fun smtpFromAddress(vars: {}) = do {
    vars.outbound_fromAddress
}

fun smtpReplyToAddress(vars: {}) = do {
    vars.outbound_replyToAddresses[0]
}

fun smtpSubject(vars: {}) = do {
    vars.outbound_subject
}

fun smtpCustomHeaders(vars: {}) = do {
    vars.outbound_customHeaders
}
