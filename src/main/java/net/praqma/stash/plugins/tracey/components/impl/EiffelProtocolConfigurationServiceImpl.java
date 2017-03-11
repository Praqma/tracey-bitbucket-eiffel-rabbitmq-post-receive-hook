package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import net.praqma.stash.plugins.tracey.components.api.ProtocolConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiffelProtocolConfigurationServiceImpl implements ProtocolConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(EiffelProtocolConfigurationServiceImpl.class);
    private String domainId = "domainId";
    private String jiraUrl = "http://jira.com";
    private String jiraProjectName = "myproject";

    public EiffelProtocolConfigurationServiceImpl(RepositoryHookContext context) {
        domainId = context.getSettings().getString("domainid");
        jiraUrl = context.getSettings().getString("jira.url");
        jiraProjectName = context.getSettings().getString("jira.project");
        LOG.info("Got domain ID: " + domainId);
    }

    public String getDomainId() { return domainId; }

    public String getJiraUrl() { return jiraUrl; }

    public String getJiraProjectName() { return jiraProjectName; }

}