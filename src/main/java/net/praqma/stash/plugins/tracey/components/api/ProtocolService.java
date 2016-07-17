package net.praqma.stash.plugins.tracey.components.api;

import com.atlassian.stash.repository.Repository;

public interface ProtocolService {

    public String getMessage(final String commmitId, final String branch, final Repository repository);

}