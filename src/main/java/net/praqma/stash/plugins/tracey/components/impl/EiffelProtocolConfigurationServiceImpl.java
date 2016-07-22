package net.praqma.stash.plugins.tracey.components.impl;

import net.praqma.stash.plugins.tracey.components.api.ProtocolConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiffelProtocolConfigurationServiceImpl implements ProtocolConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(EiffelProtocolConfigurationServiceImpl.class);

    public EiffelProtocolConfigurationServiceImpl() {
        LOG.warn("Implement me!");
    }

    public String getDomainId() {
        // TODO: we should read this from repository configuration instead
        return "domainId";
    }
}