package net.praqma.stash.plugins.tracey.hook;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.hook.repository.*;
import com.atlassian.stash.repository.*;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.setting.*;
import net.praqma.stash.plugins.tracey.components.api.*;
import net.praqma.stash.plugins.tracey.components.impl.*;
import net.praqma.stash.plugins.tracey.exceptions.BrokerServiceException;
import net.praqma.stash.plugins.tracey.exceptions.ProtocolServiceException;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQRoutingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;

@Scanned
public class EiffelRabbitMQPostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator
{
    private static final Logger LOG = LoggerFactory.getLogger(EiffelRabbitMQPostReceiveHook.class.getName());
    // TODO: all services below should be declared as actual services using ExportAsService. This is a shortcut because I can't make spring to discover them
    private final ProtocolConfigurationService protocolConfigurationService;
    private BrokerService brokerService;
    private final ProtocolService protocolService;
    private final GitService gitService;

    public EiffelRabbitMQPostReceiveHook (@ComponentImport final ApplicationPropertiesService applicationPropertiesService,
                                          @ComponentImport final CommitService commitService) {
        // TODO: When exported all serives below should be brought in using @ComponentImport as it is done for the system services
        // TODO: Also, applicationPropertiesService and commitService will be not needed here since they will be consumed by corresponding services
        this.gitService = new GitServiceImpl(commitService);
        this.protocolConfigurationService = new EiffelProtocolConfigurationServiceImpl();
        this.protocolService = new EiffelProtocolServiceImpl(applicationPropertiesService, protocolConfigurationService);
    }

    @Override
    public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges)
    {
        this.brokerService = new RabbitMQBrokerServiceImpl(context);
        final Repository repository = context.getRepository();
        try {
            for (RefChange change:refChanges) {
                for (String sha1:gitService.getCommitsDelta(repository, change)) {
                    // TODO: read Jira project name from the plugin config when available
                    String message = protocolService.getMessage(sha1, change.getRefId(), "http://jira.com", "myproject", repository);
                    // TODO: read routing info from the plugin config when available
                    RabbitMQRoutingInfo destination = new RabbitMQRoutingInfo();
                    brokerService.send(message, destination);
                }
            }
        } catch (BrokerServiceException|ProtocolServiceException error) {
            LOG.error("Can't send message notification about new commit for repository " + repository.getName(), error);
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository)
    {
    }
}