%dw 2.0

/**
 * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x Ftp transport.
 */
fun ftpWriteOutputfile(vars: {}, pathDslParams: {}) = do {
    ((((vars.outbound_filename
        default pathDslParams.outputPattern)
        default vars.outbound_outputPattern)
        default pathDslParams.outputPatternConfig)
        default (uuid() ++ '.dat'))
}

