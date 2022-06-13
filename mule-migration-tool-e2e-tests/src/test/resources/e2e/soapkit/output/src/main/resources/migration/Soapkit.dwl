%dw 2.0

/**
 * Write the body as xml string
 */
fun soapBody(body: Any) = do { 
    if (typeOf(body) as String == "String") body 
    else body write "application/xml"
}

/**
 * Get Soap headers from vars by filtering properties starting with 'soap.'
 */
fun soapHeaders(vars: {}) = do {
    var matcher_regex = /(?i)soap\..*/
    ---
    vars default {} 
        filterObject($$ matches matcher_regex)
        mapObject {
            (($$ as String)[5 to -1]): $ write "application/xml"
        }
}

/**
 * Get attachments from vars by filtering properties starting with 'att_'
 */
fun soapAttachments(vars: {}) = do {
    var matcher_regex = /(?i)att_.*/
    ---
    vars default {} 
        filterObject($$ matches matcher_regex)
        mapObject {
            (($$ as String)[4 to -1]): {
                content: $,
                contentType: $.^mimeType
            }
        }
}

