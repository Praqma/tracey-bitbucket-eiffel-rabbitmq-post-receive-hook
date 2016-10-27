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
        try {
            for (RefChange change : refChanges) {
                for (String sha1 : gitService.getCommitsDelta(repository, change)) {
                    String message = EiffelProtocolMessage.builder()
                            .withCommitId(sha1)
                            .withBranch(change.getRefId())
                            .withJiraProjectName("myproject")
                            .withJiraUrl("http://jira.com") // TODO: read Jira project name from the plugin config when available
                            .withRepoPath(applicationPropertiesService.getRepositoryDir(repository))
                            .withRepository(repository)
                            .withDisplayName(applicationPropertiesService.getDisplayName())
                            .withBaseUrl(applicationPropertiesService.getBaseUrl())
                            .withDomainId(protocolConfigurationService.getDomainId())
                            .build();
                    brokerService.send(message, routingInfoConfigService.destination());
                }
            }
        } catch (BrokerServiceException | ProtocolServiceException error) {
            LOG.error("Can't send message notification about new commit for repository " + repository.getName(), error);
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository) {
    }
}