%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.extension.http.api.HttpRequestAttributes')
{
    'http.listener.path': message.attributes.listenerPath,
    'http.context.path': if (endsWith(message.attributes.listenerPath, '/*')) message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath,
    'http.relative.path': message.attributes.requestPath[1 + sizeOf(if (endsWith(message.attributes.listenerPath, '/*')) message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath) to -1],
    'http.version': message.attributes.version,
    'http.scheme': message.attributes.scheme,
    'http.method': message.attributes.method,
    'http.request.uri': message.attributes.requestUri,
    'http.query.string': message.attributes.queryString,
    'http.remote.address': message.attributes.remoteAddress,
    'http.client.cert': message.attributes.clientCertificate,
    'LOCAL_CERTIFICATES': [message.attributes.clientCertificate],
    'PEER_CERTIFICATES': [message.attributes.clientCertificate],
    'http.query.params': message.attributes.queryParams,
    'http.uri.params': message.attributes.uriParams,
    'http.request': message.attributes.requestPath,
    'http.request.path': message.attributes.requestPath,
    'http.headers': message.attributes.headers
}
 ++ message.attributes.headers mapObject ((value, key, index) -> { (if(upper(key as String) startsWith 'X-MULE_') upper((key as String) [2 to -1]) else key) : value })
 ++ message.attributes.queryParams
else
{}
