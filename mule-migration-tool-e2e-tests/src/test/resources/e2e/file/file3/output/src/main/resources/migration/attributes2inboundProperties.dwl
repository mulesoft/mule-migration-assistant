%dw 2.0
output application/java
 ---
if (message.attributes.^class == 'org.mule.extension.file.api.LocalFileAttributes')
{
    'originalFilename': message.attributes.fileName,
    'originalDirectory': (message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))],
    'sourceFileName': message.attributes.fileName,
    'sourceDirectory': (message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))],
    'filename': message.attributes.fileName,
    'directory': (message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))],
    'fileSize': message.attributes.size,
    'timestamp': message.attributes.lastModifiedTime,
    'MULE.FORCE_SYNC': false
}
else
{}
