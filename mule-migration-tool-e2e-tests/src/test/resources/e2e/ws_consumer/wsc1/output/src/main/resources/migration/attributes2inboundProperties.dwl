%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.runtime.extension.api.soap.SoapAttributes')
{
    'http.headers': message.attributes.protocolHeaders
}
 ++ message.attributes.protocolHeaders
else
{}
