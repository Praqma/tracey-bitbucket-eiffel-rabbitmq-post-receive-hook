package net.praqma.bitbucket.plugins.tracey.components.api;

import com.atlassian.bitbucket.repository.Repository;
import net.praqma.bitbucket.plugins.tracey.exceptions.ProtocolServiceException;

public interface ProtocolService {
    String getMessage(final String commmitId, final String branch, final String jiraProjectName, final String jiraUrl, final Repository repository) throws ProtocolServiceException;
}