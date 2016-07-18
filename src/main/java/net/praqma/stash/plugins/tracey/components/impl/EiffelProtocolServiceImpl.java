package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import net.praqma.stash.plugins.tracey.components.api.ProtocolConfigurationService;
import net.praqma.stash.plugins.tracey.components.api.ProtocolService;
import net.praqma.stash.plugins.tracey.exceptions.ProtocolServiceException;
import net.praqma.tracey.protocol.eiffel.events.EiffelSourceChangeCreatedEventOuterClass.EiffelSourceChangeCreatedEvent;
import net.praqma.tracey.protocol.eiffel.factories.EiffelSourceChangeCreatedEventFactory;
import net.praqma.tracey.protocol.eiffel.models.Models;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;

public class EiffelProtocolServiceImpl implements ProtocolService {
    private static final Logger log = LoggerFactory.getLogger(EiffelProtocolServiceImpl.class);
    private final ApplicationPropertiesService applicationPropertiesService;
    private final ProtocolConfigurationService protocolConfigurationService;

    public EiffelProtocolServiceImpl(final ApplicationPropertiesService applicationPropertiesService, final ProtocolConfigurationService protocolConfigurationService) {
        this.applicationPropertiesService = applicationPropertiesService;
        this.protocolConfigurationService = protocolConfigurationService;
    }

    @Override
    public String getMessage(final String commmitId, final String branch, final Repository repository) throws ProtocolServiceException {
        final String repoPath = applicationPropertiesService.getRepositoryDir(repository).getAbsolutePath();
        final EiffelSourceChangeCreatedEventFactory factory = new EiffelSourceChangeCreatedEventFactory(
                getHostName(),
                this.applicationPropertiesService.getDisplayName(),
                this.applicationPropertiesService.getBaseUrl().toString(),
                ((EiffelProtocolConfigurationServiceImpl) this.protocolConfigurationService).getDomainId(),
                getGAV());
        String result;
        try {
            factory.parseFromGit(repoPath, commmitId, branch);
        } catch (IOException error) {
            throw new ProtocolServiceException("Can't parse commit " + commmitId + " info from repository " + repoPath, error);
        }
        EiffelSourceChangeCreatedEvent.Builder event = (EiffelSourceChangeCreatedEvent.Builder) factory.create();
        // Update GitIdentifier
        EiffelSourceChangeCreatedEvent.EiffelSourceChangeCreatedEventData.Builder data = event.getData().toBuilder();
        Models.Data.GitIdentifier.Builder git = data.getGitIdentifier().toBuilder();
        git.setRepoName(repository.getSlug());
        git.setRepoUri(applicationPropertiesService.getBaseUrl().toString() + "/scm/" + repository.getProject().getKey() + "/" + repository.getSlug());
        event.setData(data.setGitIdentifier(git));
        try {
            result = JsonFormat.printer().print(event.build());
        } catch (InvalidProtocolBufferException error) {
            throw new ProtocolServiceException("Can't format message to JSON\n" + event.build().toString(), error);
        }
        return result;
    }

    private Models.Data.GAV getGAV(){
        final Models.Data.GAV.Builder gav = Models.Data.GAV.newBuilder();
        final Dictionary headers = FrameworkUtil.getBundle(this.getClass()).getHeaders();
        gav.setGroupId(headers.get("Bundle-SymbolicName").toString());
        gav.setArtifactId(headers.get("Bundle-Name").toString());
        gav.setVersion(headers.get("Bundle-Version").toString());
        return gav.build();
    }

    private String getHostName() {
        String hostname = "Unknown";
        try
        {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            log.debug("Hostname can not be resolved due to the following. Use " + hostname + " as a hostname\n" + e.getMessage());
        }
        log.debug("Retunr hostname: " + hostname);
        return hostname;
    }
}