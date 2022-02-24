%dw 2.0

/**
 * Emulates the request headers building logic of the Mule 3.x HTTP Connector.
 */
fun httpRequesterHeaders(vars: {}) = do {
    var matcher_regex = /(?i)http\..*|Connection|Host|Transfer-Encoding/
    ---
    vars.compatibility_outboundProperties filterObject
        ((value,key) -> not ((key as String) matches matcher_regex))
        mapObject ((value, key, index) -> {
            (if (upper(key as String) startsWith 'MULE_') upper('X-' ++ key as String) else key) : value
        })
}

/**
 * Emulates the request headers building logic of the Mule 3.x HTTP Transport.
 */
fun httpRequesterTransportHeaders(vars: {}) = do {
    var matcher_regex = /(?i)http\..*|Connection|Host|Transfer-Encoding|Accept-Ranges|Age|Content-Disposition|Set-Cookie|ETag|Location|Proxy-Authenticate|Retry-After|Server|Vary|WWW-Authenticate/
    ---
    vars.compatibility_outboundProperties filterObject
        ((value,key) -> not ((key as String) matches matcher_regex))
        mapObject ((value, key, index) -> {
            (if (upper(key as String) startsWith 'MULE_') upper('X-' ++ key as String) else key) : value
        })
}

/**
 * Emulates the request method logic of the Mule 3.x HTTP Connector.
 */
fun httpRequesterMethod(vars: {}) = do {
    vars.compatibility_outboundProperties['http.method'] default 'POST'
}

