package net.praqma.stash.plugins.tracey.hook;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.hook.repository.*;
import com.atlassian.stash.repository.*;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.setting.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.jcabi.manifests.Manifests;
import net.praqma.stash.plugins.tracey.components.api.GitService;
import net.praqma.stash.plugins.tracey.components.impl.GitServiceImpl;
import net.praqma.tracey.broker.TraceyBroker;
import net.praqma.tracey.broker.TraceyIOError;
import net.praqma.tracey.broker.TraceyValidatorError;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQSenderImpl;
import net.praqma.tracey.protocol.eiffel.events.EiffelSourceChangeCreatedEventOuterClass.EiffelSourceChangeCreatedEvent;
import net.praqma.tracey.protocol.eiffel.events.EiffelSourceChangeCreatedEventOuterClass.EiffelSourceChangeCreatedEvent.EiffelSourceChangeCreatedEventData;
import net.praqma.tracey.protocol.eiffel.factories.EiffelSourceChangeCreatedEventFactory;
import net.praqma.tracey.protocol.eiffel.models.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

// TODO: Fix gav to read plugin values
// TODO: resolve username and add it to the message Data.Author.id
// TODO: add issues parsing
// TODO: fix configuration - global and per repo
// TODO: read domainId from config
// TODO: read rabbitmq configuration from config
// TODO: tests
// TODO: static analysis
// TODO: documentation
// TODO: get branch - almost done
// TODO: Move GitService to proper service when I figure how to make ExportAsService annotation work

@Scanned
public class EiffelRabbitMQPostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator
{
    private static final Logger log = LoggerFactory.getLogger(EiffelRabbitMQPostReceiveHook.class.getName());
    private final TraceyBroker broker;
    private final ApplicationPropertiesService applicationPropertiesService;
    private final GitService gitService;

    public EiffelRabbitMQPostReceiveHook (@ComponentImport final ApplicationPropertiesService applicationPropertiesService,
                                          @ComponentImport final CommitService commitService) {
        this.applicationPropertiesService = applicationPropertiesService;
        this.gitService = new GitServiceImpl(commitService);
        this.broker = new TraceyRabbitMQBrokerImpl();
        broker.setSender(new TraceyRabbitMQSenderImpl());
    }

    @Override
    public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges)
    {
        final Repository repository = context.getRepository();
        final String repoPath = applicationPropertiesService.getRepositoryDir(repository).getAbsolutePath();

        EiffelSourceChangeCreatedEventFactory factory = new EiffelSourceChangeCreatedEventFactory(
                getHostName(),
                applicationPropertiesService.getDisplayName(),
                applicationPropertiesService.getBaseUrl().toString(),
                "domainId",
                getGAV());
        for (RefChange change:refChanges) {
            for (String sha1:gitService.getCommitsDelta(repository, change)) {
                try {
                    factory.parseFromGit(repoPath, sha1, change.getRefId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EiffelSourceChangeCreatedEvent.Builder event = (EiffelSourceChangeCreatedEvent.Builder) factory.create();
                // Update GitIdentifier
                EiffelSourceChangeCreatedEventData.Builder data = event.getData().toBuilder();
                Models.Data.GitIdentifier.Builder git = data.getGitIdentifier().toBuilder();
                git.setRepoName(repository.getSlug());
                git.setRepoUri(applicationPropertiesService.getBaseUrl().toString() + "/scm/" + repository.getProject().getKey() + "/" + repository.getSlug());
                event.setData(data.setGitIdentifier(git));
                log.debug("Ready to send the following message\n" + event.build().toString());
                try {
                    broker.send(JsonFormat.printer().print(event.build()), "EiffelSourceChangeCreatedEvent");
                } catch (InvalidProtocolBufferException invalidProtocolBufferException) {
                    invalidProtocolBufferException.printStackTrace();
                } catch (TraceyValidatorError traceyValidatorError) {
                    traceyValidatorError.printStackTrace();
                } catch (TraceyIOError traceyIOError) {
                    traceyIOError.printStackTrace();
                }
            }
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository)
    {
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

    private Models.Data.GAV getGAV() {
        final Models.Data.GAV.Builder gav = Models.Data.GAV.newBuilder();
        gav.setGroupId(Manifests.read("Implementation-Vendor-Id"));
        gav.setArtifactId(Manifests.read("Implementation-Title"));
        gav.setVersion(Manifests.read("Implementation-Version"));
        return gav.build();
    }
}