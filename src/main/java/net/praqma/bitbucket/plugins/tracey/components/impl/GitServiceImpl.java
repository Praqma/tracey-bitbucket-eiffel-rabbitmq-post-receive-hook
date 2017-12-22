package net.praqma.bitbucket.plugins.tracey.components.impl;

import com.atlassian.bitbucket.commit.Commit;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.commit.CommitsBetweenRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.praqma.bitbucket.plugins.tracey.components.api.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class GitServiceImpl implements GitService {
    private static final Logger LOG = LoggerFactory.getLogger(GitServiceImpl.class);
    private static final String REFS_HEADS = "refs/heads/";
    private final CommitService commitService;

    public GitServiceImpl(final CommitService commitService) {
        this.commitService = commitService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getCommitsDelta(Repository repository, RefChange refChange) {
        // Important! We are using LinkedHashSet to preserve order
        final Set<String> newCommits = Sets.newLinkedHashSet();

        // We only care about non-deleted branches
        LOG.debug("Get list of new commits for the following change");
        LOG.debug("type: " + refChange.getType());
        LOG.debug("refId: " + refChange.getRef().getId());
        LOG.debug("from hash: " + refChange.getFromHash());
        LOG.debug("to hash: " + refChange.getToHash());
        if (refChange.getType() != RefChangeType.DELETE && refChange.getRef().getId().startsWith(REFS_HEADS)) {
            final CommitsBetweenRequest request = new CommitsBetweenRequest.Builder(repository)
                    .exclude(refChange.getFromHash())
                    .include(refChange.getToHash())
                    .build();
            final Page<Commit> commits = commitService.getCommitsBetween(request, PageUtils.newRequest(0, 999));
            LOG.debug("CommitsBetweenRequest returned the following number of commits: " + commits.getSize());
            // We reverse order of the commits because first one will be the latest
            for (Commit commit : Lists.reverse(Lists.newArrayList(commits.getValues()))) {
                LOG.debug("New commit found " + commit.getDisplayId());
                newCommits.add(commit.getDisplayId());
            }
        } else {
            LOG.debug("Ref is not a branch or type DELETE, no commits to retrieve");
        }
        return newCommits;
    }
}