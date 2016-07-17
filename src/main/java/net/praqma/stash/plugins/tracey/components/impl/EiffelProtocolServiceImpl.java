package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.jcabi.manifests.Manifests;
import net.praqma.stash.plugins.tracey.components.api.ProtocolService;
import net.praqma.tracey.protocol.eiffel.events.EiffelSourceChangeCreatedEventOuterClass.EiffelSourceChangeCreatedEvent;
import net.praqma.tracey.protocol.eiffel.factories.EiffelSourceChangeCreatedEventFactory;
import net.praqma.tracey.protocol.eiffel.models.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class EiffelProtocolServiceImpl implements ProtocolService {
    private static final Logger log = LoggerFactory.getLogger(EiffelProtocolServiceImpl.class);
    private final ApplicationPropertiesService applicationPropertiesService;

    public EiffelProtocolServiceImpl(final ApplicationPropertiesService applicationPropertiesService) {
        this.applicationPropertiesService = applicationPropertiesService;
    }

    @Override
    public String getMessage(final String commmitId, final String branch, final Repository repository) {
        final String repoPath = applicationPropertiesService.getRepositoryDir(repository).getAbsolutePath();
        final EiffelSourceChangeCreatedEventFactory factory = new EiffelSourceChangeCreatedEventFactory(
                getHostName(),
                this.applicationPropertiesService.getDisplayName(),
                this.applicationPropertiesService.getBaseUrl().toString(),
                "domainId",
                getGAV());
        String result;
        try {
            factory.parseFromGit(repoPath, commmitId, branch);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private Models.Data.GAV getGAV() {
        final Models.Data.GAV.Builder gav = Models.Data.GAV.newBuilder();
        gav.setGroupId(Manifests.read("Implementation-Vendor-Id"));
        gav.setArtifactId(Manifests.read("Implementation-Title"));
        gav.setVersion(Manifests.read("Implementation-Version"));
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