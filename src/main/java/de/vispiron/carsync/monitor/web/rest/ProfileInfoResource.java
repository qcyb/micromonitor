package de.vispiron.carsync.monitor.web.rest;

import de.vispiron.carsync.monitor.library.config.MicroMonitorProperties;
import de.vispiron.carsync.monitor.config.ConfigServerConfig;
import de.vispiron.carsync.monitor.config.DefaultProfileUtil;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Resource to return information about the currently running Spring profiles.
 */
@RestController
@RequestMapping("/api")
public class ProfileInfoResource {

    private final Environment env;

    private final MicroMonitorProperties microMonitorProperties;

    private final ConfigServerConfig configServerConfig;

    public ProfileInfoResource(Environment env, MicroMonitorProperties microMonitorProperties, ConfigServerConfig configServerConfig) {
        this.env = env;
        this.microMonitorProperties = microMonitorProperties;
        this.configServerConfig = configServerConfig;
    }

    @GetMapping("/profile-info")
    public ProfileInfoVM getActiveProfiles() {
        String[] activeProfiles = DefaultProfileUtil.getActiveProfiles(env);

        return new ProfileInfoVM(activeProfiles, getRibbonEnv(activeProfiles), configServerConfig.getComposite());
    }

    private String getRibbonEnv(String[] activeProfiles) {
        String[] displayOnActiveProfiles = {};
        if (displayOnActiveProfiles == null) {
            return null;
        }
        List<String> ribbonProfiles = new ArrayList<>(Arrays.asList(displayOnActiveProfiles));
        List<String> springBootProfiles = Arrays.asList(activeProfiles);
        ribbonProfiles.retainAll(springBootProfiles);
        if (!ribbonProfiles.isEmpty()) {
            return ribbonProfiles.get(0);
        }
        return null;
    }

    class ProfileInfoVM {

        private String[] activeProfiles;

        private String ribbonEnv;

        private List<Map<String, Object>> configurationSources;

        ProfileInfoVM(String[] activeProfiles, String ribbonEnv, List<Map<String, Object>> configurationSources) {
            this.activeProfiles = activeProfiles;
            this.ribbonEnv = ribbonEnv;
            this.configurationSources = configurationSources;
        }

        public String[] getActiveProfiles() {
            return activeProfiles;
        }

        public String getRibbonEnv() {
            return ribbonEnv;
        }

        public List<Map<String, Object>> getConfigurationSources() {
            return configurationSources;
        }
    }
}
