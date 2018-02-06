---
maintainer: buep
---
# Post receive hook to send Eiffel messages using RabbitMQ message broker

This is a prof of concept version of the plugin and it was only validated in very simple ways with Bitbucket server 5.4.0.

**Limitations**:

  * not all configuration fields in current version is used
  * the plugin is not released on Atlassian marketplace
  * it not thoroughly tested yet, but seems to do the job
  * it's opinionated in how tthe messages are constructed, but they are valid EiffelSourceChangeCreatedEvent so you can easily start to use it
  * Check the Github issues on all the things still missing


## Install the add-on on a Bitbucket Server

You need admin access to the Bitbucket server.

Download the un-released (not yet on Atlassian Marketplace) file from Github Releases where we push it from our Travis builds:

https://github.com/Praqma/tracey-bitbucket-eiffel-rabbitmq-post-receive-hook/releases

(or point Bitbucket add installation directly to the relevant link of released OBR file)

If not familiar with Bitbucket here is how you install add-ons:

  * Open the admin panel in Bitbucket. You can find this panel by pressing the cogwheel.
  * Look to the left, lower part of the administration page and find `Manage add-ons` and go there.
   * On the Manage add-ons page there is a button on the right side which is named `Upload add-on` and from there you can upload the downloaded released OBR file from the above mentioned Github releases or supply the direct link to the file.

When you finishd the installation, it should show up as an enabled add-on. Each repository on your Bitbucket server can now enable this post-receive hook and configure it **per repository level** (no global default configuration is offered yet).

## Configuring the post-receive add-on

Find the relevant repository to enable the post-receive hook for.

Go to `Repository settings` and `Hooks` under Workflows. You should see the **Eiffel Rabbit MQ Post Receive Hook**.

![Enable the Eiffel Rabbit MQ Post Receive Hook](/images/Eiffel-RabbitMQ-Post-Receive-Hook-enable-it.jpg)

Enable it and the configuration pops up. Configure the first 3 fields, and last 2 fields. The image is a bit blurry, so it could all be seen, but these values are the important ones:

  * `RabbitMQ server address` (required): IP or DNS name of your RabbitMQ server, obviously reachable from the Bitbucket server itself.
  * `RabbitMQ server port` (required): Port number, here we use the default.
  * `RabbitMQ exchange name` (required): An existing or new exchange on your RabbitMQ server. Will be created if not exists. Recommend to use `tracey` as name for now.
  * `RabbitMQ routing key`: _not use - leavy empty_.
  * `RabbitMQ user name` and `RabbitMQ password` (required): user account on RabbitMQ used to deliver messages with.

![Configure the Eiffel Rabbit MQ Post Receive Hook](/images/Eiffel-RabbitMQ-Post-Receive-Hook-configure-it.jpg)


## Why?
To provide a better (comparing to web hooks) way to trigger events on SCM changes and enable better traceability within Continuous Delivery pipeline

Read more about traceability using Eiffel messages [here](https://github.com/Ericsson/eiffel) and check out examples
[here](https://github.com/Ericsson/eiffel/blob/master/usage-examples/delivery-interface.md)

## What?
RabbitMQ message containing EiffelSourceChangeCreatedEvent (see example below) will be sent every time new commit is pushed.

```
{
  "meta": {
    "id": "6ce06a7b-bd94-4b84-858c-6082eaa3b7b1",
    "type": "EiffelSourceChangeCreatedEvent",
    "time": "1469216448583",
    "source": {
      "domainId": "domainId",
      "host": "Andreys-MacBook-Pro.local",
      "name": "Stash",
      "uri": "http://Andreys-MacBook-Pro.local:7990/stash",
      "serializer": {
        "groupId": "net.praqma.stash.plugins.tracey.eiffel-rabbitmq-post-receive-hook",
        "artifactId": "eiffel-rabbitmq-post-receive-hook",
        "version": "1.0.0.SNAPSHOT"
      }
    }
  },
  "data": {
    "author": {
      "name": "Andrey Devyatkin",
      "email": "andrey.a.devyatkin@gmail.com"
    },
    "issues": [{
      "id": "JSP-1",
      "transition": "PARTIAL",
      "uri": "http://jira.com/projects/myproject/issues/JSP-1",
      "trackerType": "Jira"
    }],
    "change": {
      "insertions": 1,
      "files": ["ADD README.md"]
    },
    "gitIdentifier": {
      "commitId": "bc8dd2f8904a74a8e890e555f42e425238874c64",
      "branch": "refs/heads/master",
      "repoName": "rep_1",
      "repoUri": "http://Andreys-MacBook-Pro.local:7990/stash/scm/PROJECT_1/rep_1"
    }
  }
}
```

## How?
We are using [Tracey project](https://github.com/praqma/tracey) and [its implementation of Eiffel protocol](https://github.com/Praqma/tracey-protocol-eiffel)
Hook will parse new commit message using factory from Tracey Eiffel protocol library and [commit-message-parser](https://github.com/Praqma/commit-message-parser) library
to generate the message and then will send it using [Tracey Broker library](https://github.com/praqma/tracey-broker)

You can use [Tracey RabbitMQ trigger plugin for Jenkins](https://github.com/Praqma/tracey-jenkins-trigger-plugin) to trigger Jenkins job based on Eiffel events. And you can use
[command line client](https://github.com/Praqma/tracey-cli-rabbitmq) to send events or subscribe to them for debugging and experiments as well.


## Usage/Demo

**These steps are DEPRECATED (it works without Atlas run and with non-local Bitbucket/RabbitMQ server). The images still shows some important internals of how it works**.
  * New demo setup will be supplied at some point to allow you to try it out using docker images and few demo scripts.


Install Atlassian SDK using [this instruction](https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project)

Clone this repo, step in to the repository and start Bitbucket in development mode

```
git clone https://github.com/Praqma/tracey-bitbucket-eiffel-rabbitmq-post-receive-hook.git
cd tracey-bitbucket-eiffel-rabbitmq-post-receive-hook
atlas-run
```

When Bitbucket is up go to localhost:7990 and login using admin/admin username and password.
By default, development instance has one project - PROJECT_1 with one repository called rep_1.
Go there and check repository settings, hooks configuration. You should see something like this

![Hook setup](/images/hook_setup1.png)

Click Enable Eiffel RabbitMQ Post Receive Hook. Then use supposed to see a pop up where you can configure the plugin.
However configuration form is not implemented yet so what you are going to see is this. It is fine just click Enable

![Hook setup](/images/hook_setup2.png)

Now let create few commits and push them to Bitbucket

```
git clone http://admin@localhost:7990/stash/scm/project_1/rep_1.git
cd rep_1

# Create commit that referes to Jira issue
emacs README.md && git add README.md && git commit -s
[master 769735f] JSP-1
 1 file changed, 1 insertion(+), 1 deletion(-)

# Create commit that closes Jira issue
emacs README.md && git add README.md && git commit -s
[master a17099d] JSP-2 #close
 1 file changed, 1 insertion(+), 1 deletion(-)

# Create commit that reverts Jira issue
emacs README.md && git add README.md && git commit -s && git push origin master
[master 70f65f2] JSP-1 #revert
 1 file changed, 1 insertion(+), 1 deletion(-)
Counting objects: 3, done.
Delta compression using up to 8 threads.
Compressing objects: 100% (2/2), done.
Writing objects: 100% (3/3), 310 bytes | 0 bytes/s, done.
Total 3 (delta 1), reused 0 (delta 0)
To http://admin@localhost:7990/stash/scm/project_1/rep_1.git
   a17099d..70f65f2  master -> master
```

So now we can see the messages in the BitBucket log

![Example output](/images/example_output1.png)

As well as in RabbitMQ

![Example output](/images/example_output2.png)

![Example output](/images/example_output3.png)

## Contributions

Follow the GitHub issues

Definition of Done
- Buildable
- All tests are passing
- Documentation updated accordingly



### Releasing
To release a new version of this CLI on Github release you need to tag the commit to release. This will be picked up by Travis CI.

Github auth for Travis release

Release is done a ReleasePraqma user and was securely created using travis setup releases

```
$ travis setup releases
```

and then adjusting the inputs afterwards to only released on tagging, as well as using file-pattern to find the file as the version number are included in the filename.
