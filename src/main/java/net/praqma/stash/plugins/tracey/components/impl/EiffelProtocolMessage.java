package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.repository.Repository;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import net.praqma.stash.plugins.tracey.exceptions.ProtocolServiceException;
import net.praqma.tracey.protocol.eiffel.events.EiffelSourceChangeCreatedEventOuterClass;
import net.praqma.tracey.protocol.eiffel.factories.EiffelSourceChangeCreatedEventFactory;
import net.praqma.tracey.protocol.eiffel.models.Models;
import net.praqma.utils.parsers.cmg.api.CommitMessageParser;
import net.praqma.utils.parsers.cmg.impl.Jira;
import org.osgi.framework.FrameworkUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Dictionary;

import static org.eclipse.jetty.io.nio.SelectorManager.LOG;

public class EiffelProtocolMessage {

    private String commitId;
    private String branch;
    private String project;
    private String jiraUrl;
    private File repoPath;
    private Repository repository;
    private String displayName;
    private URI baseUrl;
    private String domainId;

    public static EiffelProtocolMessage builder() {
        return new EiffelProtocolMessage();
    }

    private EiffelProtocolMessage() {
    }

    public EiffelProtocolMessage withCommitId(String commitId) {
        this.commitId = commitId;
        return this;
    }

    public EiffelProtocolMessage withBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public EiffelProtocolMessage withJiraProjectName(String project) {
        this.project = project;
        return this;
    }

    public EiffelProtocolMessage withJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
        return this;
    }

    public EiffelProtocolMessage withRepoPath(File repoPath) {
        this.repoPath = repoPath;
        return this;
    }

    public EiffelProtocolMessage withRepository(Repository repository) {
        this.repository = repository;
        return this;
    }
    public EiffelProtocolMessage withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public EiffelProtocolMessage withBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public EiffelProtocolMessage withDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String build() throws ProtocolServiceException {
        // final String repoPath = applicationPropertiesService.getRepositoryDir(repository).getAbsolutePath();
        final EiffelSourceChangeCreatedEventFactory factory = new EiffelSourceChangeCreatedEventFactory(
                displayName,
                baseUrl.toString(),
                domainId,
                getGAV());
        String result;
        CommitMessageParser parser;
        try {
            parser = new Jira(new URL(jiraUrl), project);
        } catch (MalformedURLException error) {
            throw new ProtocolServiceException("Can't parse commit " + commitId + ". Can't parse URL " + jiraUrl, error);
        }
        try {
            factory.parseFromGit(repoPath.getAbsolutePath(), commitId, branch, parser);
        } catch (IOException error) {
            throw new ProtocolServiceException("Can't parse commit " + commitId + " info from repository " + repoPath, error);
        }
        EiffelSourceChangeCreatedEventOuterClass.EiffelSourceChangeCreatedEvent.Builder event = (EiffelSourceChangeCreatedEventOuterClass.EiffelSourceChangeCreatedEvent.Builder) factory.create();
        // Update GitIdentifier
        LOG.debug("Generated message before GitIdentifier update: " + event.toString());
        EiffelSourceChangeCreatedEventOuterClass.EiffelSourceChangeCreatedEvent.EiffelSourceChangeCreatedEventData.Builder data = event.getData().toBuilder();
        Models.Data.GitIdentifier.Builder git = data.getGitIdentifier().toBuilder();
        git.setRepoName(repository.getSlug());
        git.setRepoUri(baseUrl + "/scm/" + repository.getProject().getKey() + "/" + repository.getSlug());
        LOG.debug("Set GitIdentifier to " + git.toString());
        event.setData(data.setGitIdentifier(git));
        try {
            result = JsonFormat.printer().print(event.build());
        } catch (InvalidProtocolBufferException error) {
            throw new ProtocolServiceException("Can't format message to JSON\n" + event.build().toString(), error);
        }
        return result;
    }

    private Models.Data.GAV getGAV() {
        final Models.Data.GAV.Builder gav = Models.Data.GAV.newBuilder();
        final Dictionary headers = FrameworkUtil.getBundle(this.getClass()).getHeaders();
        gav.setGroupId(headers.get("Bundle-SymbolicName").toString());
        gav.setArtifactId(headers.get("Bundle-Name").toString());
        gav.setVersion(headers.get("Bundle-Version").toString());
        return gav.build();
    }
}
