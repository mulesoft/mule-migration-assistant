%dw 2.0

/**
 * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x File transport.
 */
fun fileWriteOutputfile(vars: {}, pathDslParams: {}) = do {
    ((vars.compatibility_outboundProperties['writeToDirectoryName']
        default pathDslParams.writeToDirectory)
        default pathDslParams.address)
    ++ '/' ++
    ((((pathDslParams.outputPattern
        default vars.compatibility_outboundProperties.outputPattern)
        default pathDslParams.outputPatternConfig)
        default vars.compatibility_inboundProperties.filename)
        default (uuid() ++ '.dat'))
}

