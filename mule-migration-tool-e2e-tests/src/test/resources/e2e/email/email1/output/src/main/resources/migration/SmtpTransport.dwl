%dw 2.0

fun smptToAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.toAddresses[0]
}

fun smptCcAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.ccAddresses[0]
}

fun smptBccAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.bccAddresses[0]
}

fun smptFromAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.fromAddress
}

fun smptReplyToAddress(vars: {}) = do {
    vars.compatibility_outboundProperties.replyToAddresses[0]
}

fun smptSubject(vars: {}) = do {
    vars.compatibility_outboundProperties.subject
}


