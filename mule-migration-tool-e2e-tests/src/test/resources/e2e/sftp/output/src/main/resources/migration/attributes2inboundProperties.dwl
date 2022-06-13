%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.extension.sftp.api.SftpFileAttributes')
{
    'originalFilename': message.attributes.name,
    'filename': message.attributes.name
}
else
{}
