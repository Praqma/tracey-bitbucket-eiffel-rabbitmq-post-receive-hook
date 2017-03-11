package net.praqma.stash.plugins.tracey.hook;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import net.praqma.stash.plugins.tracey.components.api.GitService;
import net.praqma.stash.plugins.tracey.components.impl.*;
import net.praqma.stash.plugins.tracey.exceptions.BrokerServiceException;
import net.praqma.stash.plugins.tracey.exceptions.ProtocolServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Scanned
public class EiffelRabbitMQPostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    private static final Logger LOG = LoggerFactory.getLogger(EiffelRabbitMQPostReceiveHook.class.getName());
    private final ApplicationPropertiesService applicationPropertiesService;
    private final GitService gitService;

    public EiffelRabbitMQPostReceiveHook(@ComponentImport final ApplicationPropertiesService applicationPropertiesService,
                                         @ComponentImport final CommitService commitService) {
        this.applicationPropertiesService = applicationPropertiesService;
        this.gitService = new GitServiceImpl(commitService);
    }

    @Override
    public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges) {

        final EiffelProtocolConfigurationServiceImpl protocolConfigurationService = new EiffelProtocolConfigurationServiceImpl(context); // ???
        final Repository repository = context.getRepository();

        final RabbitMQBrokerConfigurationServiceImpl brokerConfigurationService = new RabbitMQBrokerConfigurationServiceImpl(context);
        final RabbitMQRoutingInfoConfigService routingInfoConfigService = new RabbitMQRoutingInfoConfigService(context);
        final RabbitMQBrokerServiceImpl brokerService = new RabbitMQBrokerServiceImpl(brokerConfigurationService);
        System.out.print("try to message");
        try {
            for (RefChange change : refChanges) {
                for (String sha1 : gitService.getCommitsDelta(repository, change)) {
                    String message = EiffelProtocolMessage.builder()
                            .withCommitId(sha1)
                            .withBranch(change.getRefId())
                            .withJiraProjectName(protocolConfigurationService.getJiraProjectName())
                            .withJiraUrl(protocolConfigurationService.getJiraUrl())
                            .withRepoPath(applicationPropertiesService.getRepositoryDir(repository))
                            .withRepository(repository)
                            .withDisplayName(applicationPropertiesService.getDisplayName())
                            .withBaseUrl(applicationPropertiesService.getBaseUrl())
                            .withDomainId(protocolConfigurationService.getDomainId())
                            .build();
                    LOG.debug("Message to send : " + message);
                    brokerService.send(message, routingInfoConfigService.destination());
                    LOG.debug("Message sent : " + message);
                }
            }
        } catch (BrokerServiceException | ProtocolServiceException error) {
            System.out.print("Message was not sent");
            LOG.error("Can't send message notification about new commit for repository " + repository.getName(), error);
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository) {
    }
}