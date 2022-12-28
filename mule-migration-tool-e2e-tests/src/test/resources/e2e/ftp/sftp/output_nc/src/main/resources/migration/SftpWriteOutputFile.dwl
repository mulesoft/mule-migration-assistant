%dw 2.0

/**
 * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x Sftp transport.
 */
fun sftpWriteOutputfile(vars: {}, pathDslParams: {}) = do {
    (((((pathDslParams.outputPattern
         default vars.outbound_outputPattern)
         default pathDslParams.outputPatternConfig)
         default vars.outbound_filename)
         default vars.filename)
         default message.attributes.name)
}

