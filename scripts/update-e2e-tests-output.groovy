import java.nio.file.Files
import java.nio.file.Paths;

import static groovy.io.FileType.FILES
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

println "----------------------------"
println "Overwriting e2e tests output"
println "----------------------------"

def actualOutputPath = Paths.get("mule-migration-tool-e2e-tests/target/apps")
def expectedOutputPath = Paths.get("mule-migration-tool-e2e-tests/src/test/resources/e2e")
def testReportPath = Paths.get("mule-migration-tool-e2e-tests/target/surefire-reports/com.mulesoft.tools.migration.e2e.AllEndToEndTestCase.txt")

if (!testReportPath.toFile().exists() || !actualOutputPath.toFile().exists()) {
  println "Run e2e tests before trying to update the tests output files"
  return
}

def failedTests = []
testReportPath.toFile().eachLine {
  line -> {
    def matcher = line =~ /test\[(.*)\]\(.*FAILURE.*/
    if (matcher) {
      def parts = matcher[0][1].split('-no-compat=')
      failedTests.add(parts[0] + (parts[1] == 'true' ? '_nc' : ''))
    }
  }
}

def sourceFiles = []
actualOutputPath.eachFileRecurse(FILES) {
  sourceFiles.add(it)
}

println "--- compatibility failed tests updated ----"
expectedOutputPath.eachFileRecurse(FILES) { file ->
  {
    if (file.toString().contains("/output/") && failedTests.any{ file.toString().contains(it + "/")}) {
      def rel = expectedOutputPath.relativize(file)
      def source = findSourceFile(sourceFiles, rel.toString().replace("/output/", "/"))
      if (source != null) {
        println "Copying $source to $file"
        Files.copy(source, file, REPLACE_EXISTING)
      } else {
        println "[WARNING] Source file not found for $file"
      }
    }
  }
}

println "--- no compatibility failed tests updated ----"
expectedOutputPath.eachFileRecurse(FILES) { file ->
  {
    if (file.toString().contains("/output_nc/") && failedTests.any{ file.toString().contains(it.substring(0, it.length() - 3) + "/")}) {
      def rel = expectedOutputPath.relativize(file)
      def source = findSourceFile(sourceFiles, rel.toString().replace("/output_nc/", "_nc/"))
      if (source != null) {
        println "Copying $source to $file"
        Files.copy(source, file, REPLACE_EXISTING)
      } else {
        println "[WARNING] Source file not found for $file"
      }
    }
  }
}

static def findSourceFile(sources, target) {
  return sources.find { it.endsWith(target) }
}
