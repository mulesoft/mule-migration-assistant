import java.nio.file.Files
import java.nio.file.Paths;

import static groovy.io.FileType.FILES
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

println "----------------------------"
println "Overwriting e2e tests output"
println "----------------------------"

def actualOutputPath = Paths.get("mule-migration-tool-e2e-tests/target/apps")
def expectedOutputPath = Paths.get("mule-migration-tool-e2e-tests/src/test/resources/e2e")

if (!actualOutputPath.toFile().exists()) {
  println "Run e2e tests before trying to update the tests output files"
  return
}

def sourceFiles = []
actualOutputPath.eachFileRecurse(FILES) {
  sourceFiles.add(it)
}

expectedOutputPath.eachFileRecurse(FILES) {
  if (it.toString().contains("/output/")) {
    def rel = expectedOutputPath.relativize(it)
    def source = findSourceFile(sourceFiles, rel.toString().replace("/output/", "/"))
    if (source != null) {
      println "Copying $source to $it"
      Files.copy(source, it, REPLACE_EXISTING)
    } else {
      println "[WARNING] Source file not found for $it"
    }
  }
}

static def findSourceFile(sources, target) {
  return sources.find { it.endsWith(target) }
}
