%dw 2.0

/**
 * Emulates the response headers building logic of the Mule 3.x HTTP Connector.
 */
fun httpListenerResponseHeaders(vars: {}) = do {
    var matcher_regex = /(?i)http\..*|Connection|Transfer-Encoding/
    ---
    vars.compatibility_outboundProperties default {} filterObject
        ((value,key) -> not ((key as String) matches matcher_regex))
        mapObject ((value, key, index) -> {
            (if (upper(key as String) startsWith 'MULE_') upper('X-' ++ key as String) else key) : value
        })
}

/**
 * Emulates the success status code logic of the Mule 3.x HTTP Connector.
 */
fun httpListenerResponseSuccessStatusCode(vars: {}) = do {
    vars.compatibility_outboundProperties['http.status'] default 200
}

/**
 * Emulates the error status code logic of the Mule 3.x HTTP Connector.
 */
fun httpListenerResponseErrorStatusCode(vars: {}) = do {
    vars.compatibility_outboundProperties['http.status']
}

