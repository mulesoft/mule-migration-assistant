%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.extension.http.api.HttpResponseAttributes')
{
    'http.status': message.attributes.statusCode,
    'http.reason': message.attributes.reasonPhrase,
    'http.headers': message.attributes.headers
}
 ++ message.attributes.headers mapObject ((value, key, index) -> { (if(upper(key as String) startsWith 'X-MULE_') upper((key as String) [2 to -1]) else key) : value })
else
{}
