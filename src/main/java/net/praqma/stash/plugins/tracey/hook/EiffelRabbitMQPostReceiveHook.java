package net.praqma.stash.plugins.tracey.hook;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.hook.repository.*;
import com.atlassian.stash.repository.*;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.setting.*;
import net.praqma.stash.plugins.tracey.components.api.GitService;
import net.praqma.stash.plugins.tracey.components.api.ProtocolService;
import net.praqma.stash.plugins.tracey.components.impl.EiffelProtocolServiceImpl;
import net.praqma.stash.plugins.tracey.components.impl.GitServiceImpl;
import net.praqma.tracey.broker.TraceyBroker;
import net.praqma.tracey.broker.TraceyIOError;
import net.praqma.tracey.broker.TraceyValidatorError;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
// TODO: Move ProtocolService to proper service when I figure how to make ExportAsService annotation work
// TODO: better error handling - figure out where to break and where to not

@Scanned
public class EiffelRabbitMQPostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator
{
    private static final Logger log = LoggerFactory.getLogger(EiffelRabbitMQPostReceiveHook.class.getName());
    private final TraceyBroker broker;
    private final ProtocolService protocolService;
    private final GitService gitService;

    public EiffelRabbitMQPostReceiveHook (@ComponentImport final ApplicationPropertiesService applicationPropertiesService,
                                          @ComponentImport final CommitService commitService) {
        this.protocolService = new EiffelProtocolServiceImpl(applicationPropertiesService);
        this.gitService = new GitServiceImpl(commitService);
        this.broker = new TraceyRabbitMQBrokerImpl();
        broker.setSender(new TraceyRabbitMQSenderImpl());
    }

    @Override
    public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges)
    {
        final Repository repository = context.getRepository();
        for (RefChange change:refChanges) {
            for (String sha1:gitService.getCommitsDelta(repository, change)) {
                String message = protocolService.getMessage(sha1, change.getRefId(), repository);
                // Or shall we just break here?
                if (message == null) {
                    continue;
                }
                log.debug("Ready to send the following message\n" + message);
                try {
                    broker.send(message, "EiffelSourceChangeCreatedEvent");
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
}