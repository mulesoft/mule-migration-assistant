%dw 2.0

/**
 * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x Ftp transport.
 */
fun ftpWriteOutputfile(vars: {}, pathDslParams: {}) = do {
    ((((vars.compatibility_outboundProperties.filename
        default pathDslParams.outputPattern)
        default vars.compatibility_outboundProperties.outputPattern)
        default pathDslParams.outputPatternConfig)
        default (uuid() ++ '.dat'))
}

