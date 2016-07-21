package net.praqma.stash.plugins.tracey.components.api;

import com.atlassian.stash.repository.Repository;
import net.praqma.stash.plugins.tracey.exceptions.ProtocolServiceException;

public interface ProtocolService {
    String getMessage(final String commmitId, final String branch, final String jiraProjectName, final String jiraUrl, final Repository repository) throws ProtocolServiceException;
}