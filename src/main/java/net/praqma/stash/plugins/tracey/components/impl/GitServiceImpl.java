package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.commit.Commit;
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.commit.CommitsBetweenRequest;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.praqma.stash.plugins.tracey.components.api.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class GitServiceImpl implements GitService {
    private static final Logger log = LoggerFactory.getLogger(GitServiceImpl.class);
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
        log.debug("Get list of new commits for the following change");
        log.debug("type: " + refChange.getType());
        log.debug("refId: " + refChange.getRefId());
        log.debug("from hash: " + refChange.getFromHash());
        log.debug("to hash: " + refChange.getToHash());
        if (refChange.getType() != RefChangeType.DELETE && refChange.getRefId().startsWith(REFS_HEADS)) {
            final CommitsBetweenRequest request = new CommitsBetweenRequest.Builder(repository)
                    .exclude(refChange.getFromHash())
                    .include(refChange.getToHash())
                    .build();
            final Page<Commit> commits = commitService.getCommitsBetween(request, PageUtils.newRequest(0, 999));
            log.debug("CommitsBetweenRequest returned the following number of commits: " + commits.getSize());
            // We reverse order of the commits because first one will be the latest
            for (Commit commit : Lists.reverse(Lists.newArrayList(commits.getValues()))) {
                log.debug("New commit found " + commit.getDisplayId());
                newCommits.add(commit.getDisplayId());
            }
        } else {
            log.debug("Ref is not a branch or type DELETE, no commits to retrieve");
        }
        return newCommits;
    }
}