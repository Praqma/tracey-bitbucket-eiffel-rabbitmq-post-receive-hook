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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

// TODO: tests
// TODO: static analysis
// TODO: documentation
// TODO: add issues parsing
// TODO: resolve username and add it to the message Data.Author.id
// TODO: Move GitService to proper service when I figure how to make ExportAsService annotation work
// TODO: Move ProtocolService to proper service when I figure how to make ExportAsService annotation work
// TODO: Move BrokerService to proper service when I figure how to make ExportAsService annotation work
// TODO: Move BrokerConfigurationService to proper service when I figure how to make ExportAsService annotation work
// TODO: fix configuration - global and per repo
// TODO: add configuration validation
// TODO: read domainId from config
// TODO: read rabbitmq configuration from config
// TODO: add plugin configuration service and use it for branchFilter per repo configuration
// TODO: add support for filtering branches
// TODO: publish plugin

@Scanned
public class EiffelRabbitMQPostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator
{
    private static final Logger log = LoggerFactory.getLogger(EiffelRabbitMQPostReceiveHook.class.getName());
    // TODO: all services below should be declared as actual services using ExportAsService. This is a shortcut because I can't make spring to discover them
    private final BrokerConfigurationService brokerConfigurationService;
    private final ProtocolConfigurationService protocolConfigurationService;
    private final BrokerService brokerService;
    private final ProtocolService protocolService;
    private final GitService gitService;

    public EiffelRabbitMQPostReceiveHook (@ComponentImport final ApplicationPropertiesService applicationPropertiesService,
                                          @ComponentImport final CommitService commitService) {
        // TODO: When exported all serives below should be brought in using @ComponentImport as it is done for the system services
        // TODO: Also, applicationPropertiesService and commitService will be not needed here since they will be consumed by corresponding services
        this.gitService = new GitServiceImpl(commitService);
        this.brokerConfigurationService = new RabbitMQBrokerConfigurationServiceImpl();
        this.protocolConfigurationService = new EiffelProtocolConfigurationServiceImpl();
        this.brokerService = new RabbitMQBrokerServiceImpl(brokerConfigurationService);
        this.protocolService = new EiffelProtocolServiceImpl(applicationPropertiesService, protocolConfigurationService);
    }

    @Override
    public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges)
    {
        final Repository repository = context.getRepository();
        try {
            for (RefChange change:refChanges) {
                for (String sha1:gitService.getCommitsDelta(repository, change)) {
                    String message = protocolService.getMessage(sha1, change.getRefId(), repository);
                    brokerService.send(message, ((RabbitMQBrokerConfigurationServiceImpl) brokerConfigurationService).getExchange());
                }
            }
        } catch (BrokerServiceException|ProtocolServiceException error) {
            log.error("Can't send message notification about new commit for repository " + repository.getName(), error);
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository)
    {
    }
}