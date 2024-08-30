package de.hbt.support.controller

import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import java.io.FileNotFoundException
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.jar.JarFile

@Controller
@RequestMapping(value = ["/timetable"])
class VersionHtmlController {
    @GetMapping(path = ["/version"])
    fun version(): String {
        return "version"
    }

    @ModelAttribute("version")
    private fun getApplicationVersion(): String {
        return "${getTitle()} ${getVersion()}"
    }

    @ModelAttribute("footerString")
    private fun getApplicationVersion(@RequestHeader("host") hostName: String): String {
        return "${getTitle()} ${getVersion()} - $hostName"
    }

    private fun getTitle(): String {
        val title = VersionHtmlController::class.java.`package`.implementationTitle
                ?: return "application"
        return title.ifBlank {
            "application"
        }
    }

    private fun getVersion(): String {
        val version = VersionHtmlController::class.java.`package`.implementationVersion
                ?: return "DEVELOPER"
        return version.ifBlank {
            "DEVELOPER"
        }
    }

    @ModelAttribute("hostname")
    private fun getHostname(): String {
        try {
            return InetAddress.getLocalHost().hostName
        } catch (e: UnknownHostException) {
            log.warn(e.toString(), e)
        }
        return ""
    }

    @ModelAttribute("manifest")
    private fun getManifest(): Collection<String> {
        try {
            val location = javaClass.getProtectionDomain().codeSource.location
            val jarFileName = Paths.get(location.toURI()).toString()
            JarFile(jarFileName).use { jarFile ->
                val entry = jarFile.getEntry(JarFile.MANIFEST_NAME)
                jarFile.getInputStream(entry).use { `in` ->
                    return String(`in`.readAllBytes(), StandardCharsets.UTF_8).lines().toList()
                }
            }
        } catch (ignored: FileNotFoundException) {
            // do nothing if manifest file is not available
        } catch (e: Exception) {
            log.info(e.toString(), e)
        }
        return listOf("${getTitle()} ${getVersion()}")
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}