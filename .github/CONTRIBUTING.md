# Contributing to HubParkour
We welcome contributions to HubParkour, in fact we actively encourage it. Contributors create a community of Developers that are actively working to make great software for all to enjoy. HubParkour was created to be configurable, customisable, and open. This is why we are open-source, and the reason why we allow outside contributors to contribute to the plugin.

That being said, we do have some guidelines that Developers must follow in order to maintain standards, as well as to keep things consistent and easy to understand. That way any Developer can clone the project and start working!

## Types of Contributions

There are many ways you can contribute to the plugin, and not all of them include writing code. Some of the ways you can contribute include:

 - Pull Requests
 - Bug Reports
 - New Feature Requests
 - Community Support
 - Participating in Discussions

While this is not an exhaustive list, this is the main ways we see contributions. Each of them have specific guidelines, as the type of contribution changes how we need to process them.

## Guidelines

### Code Style

We are not too strict when it comes to code styles, as we understand that developers prefer different styles. In general, we follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) however we do have a few exceptions/additions.

In terms of exceptions, we use +4-space indentation instead of the +2-space that is used in the guide. We also, for the most part, ignore the section in imports, as most IDEs manage those automatically.

A few additions we enforce:
 - All Event classes must end in Event.
 - All Listener classes must end in Listener.
 - All pressure plates must end in Point.
 - All items must end in Item.
 - DB Schema updates must be called after the version number of the schema.
 - Manager classes must end in Manager.
 - Signs must end in Sign.
 - Any database queries must be run asynchronously.
 - Configuration Items must be in Upper Camel Case, and spaces should be replaced with a dash: ``-``
 - All admin messages must use ``ConfigUtil#getStringOrDefault()`` as any admin message cannot be disabled.
 - All non-essential messages must use ``ConfigUtil#getString()`` as these must be able to be disabled.
 - Where possible, prevent cache hits by storing variables locally. E.g. when a player object is used several times, store it in a local variable rather than hitting the cache each time.
 - Any changes to the API must have an accompanying JavaDoc comment.
 - Any changes to entities must also be reflected in their parent interface.
 - Ensure any necessary null-checks are in-place.

### Commits

In terms of the format of commits, we only have a few requirements.

Firstly, commits must be formatted in a specific format. The first line should contain a short description, followed by a blank line then a longer description of your change. For example:
```
Fix NPE when fetching player

Fixed a NullPointerException that occurs
when a player is not in a parkour and
attempts to use an item that is also
a parkour item. 
```
Commits should also only contain basic information, not a bullet-pointed list of changes. It should be a brief overview of the changes, whats changed and what it introduces.

Commits should not contain inappropriate, vulgar, or otherwise unprofessional commit messages. These messages are being posted in a public repository, and as such should be treated as if your employer could look at these messages. 

### Pull Requests

Pull requests must abide-by the following guidelines:

 - The pull request must use the specified PR template.
 - The PR title must match the commit name. 
 - All commits must be squashed unless an exception is granted.
 - The pull request must receive a review from a designated reviewer before it can be merged.
 - When changes are suggested by a reviewer, please amend the commit. Do not add new commits with the changes. If you don't know how to do this, you can find a guide to how to do this from Atlassian [here](https://www.atlassian.com/git/tutorials/rewriting-history).
 - The pull request should not contain any changes to the project configuration files (e.g. the files in .idea) unless there is a specific reason to.

### Issues

Issues can be categorised into 2 types: bug reports and feature suggestions. While they serve different purposes, the guidelines are almost identical. When creating an issue, you must abide-by the following guidelines:

 - You must follow the designated Issue template.
 - You must categorise your issue correctly.
 - All questions from the template must be answered in the issue.

It is vitally important that you **never report a security vulnerability using GitHub issues**. This can result in other users becoming vulnerable. Please follow our security policy as outlined [here](SECURITY.md).

### Community Support and Discussions

We appreciate any support you can give other users about questions or concerns they have about the plugin. We get lots of support requests, so any help we receive makes our workload a lot easier to manage.

Generally speaking, we ask that you follow the [Code of Conduct](CODE_OF_CONDUCT.md) when requesting or responding to support. It is important to understand that some people may be asking a question for the first time, and we want to create a welcoming community here.

There are no stupid questions, however we do kindly ask that you read all of the documentation before requesting support, as many of the questions may already be answered. Any questions that relate to items directly addressed in either the config file or FAQs will result in the discussion being closed, pointing to the relevant source.

## Summary

We hope with these guidelines, we can make the plugin the best it can be! If you have any questions or concerns, feel free to reach out to me at [ethan@block2block.me](mailto:ethan@block2block.me). Furthermore, if you have any suggestions for this doc, feel free to submit a PR!