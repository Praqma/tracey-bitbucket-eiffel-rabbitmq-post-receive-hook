package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import net.praqma.stash.plugins.tracey.components.api.ProtocolConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiffelProtocolConfigurationServiceImpl implements ProtocolConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(EiffelProtocolConfigurationServiceImpl.class);
    private String domainId = "domainId";

    public EiffelProtocolConfigurationServiceImpl(RepositoryHookContext context) {
        domainId = context.getSettings().getString("domainid", domainId);
        LOG.info("Eiffel information from plugin context");
    }

    public String getDomainId() {
        return domainId;
    }
}