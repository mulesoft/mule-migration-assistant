%dw 2.0

/**
 * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x File transport.
 */
fun fileWriteOutputfile(vars: {}, pathDslParams: {}) = do {
    ((vars.outbound_writeToDirectoryName
        default pathDslParams.writeToDirectory)
        default pathDslParams.address)
    ++ '/' ++
    ((((pathDslParams.outputPattern
        default vars.outbound_outputPattern)
        default pathDslParams.outputPatternConfig)
        default message.attributes.fileName)
        default (uuid() ++ '.dat'))
}

