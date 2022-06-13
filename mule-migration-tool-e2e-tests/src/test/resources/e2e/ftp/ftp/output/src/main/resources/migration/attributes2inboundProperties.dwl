%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.extension.ftp.api.ftp.FtpFileAttributes')
{
    'originalFilename': message.attributes.name,
    'fileSize': message.attributes.size,
    'timestamp': message.attributes.timestamp
}
else
{}
