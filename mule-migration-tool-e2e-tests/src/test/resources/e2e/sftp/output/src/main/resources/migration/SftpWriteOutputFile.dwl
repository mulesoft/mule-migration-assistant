%dw 2.0

/**
 * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x Sftp transport.
 */
fun sftpWriteOutputfile(vars: {}, pathDslParams: {}) = do {
    (((((pathDslParams.outputPattern
         default vars.compatibility_outboundProperties.outputPattern)
         default pathDslParams.outputPatternConfig)
         default vars.compatibility_outboundProperties.filename)
         default vars.filename)
         default vars.compatibility_inboundProperties.filename)
}

