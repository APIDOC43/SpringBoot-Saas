package com.hocs.server.saas_v2.out.git.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RepositoryResponse(
	@JsonProperty("id")
	long id,
	@JsonProperty("node_id")
	String nodeId,
	@JsonProperty("name")
	String name,
	@JsonProperty("full_name")
	String fullName,
	@JsonProperty("owner")
	Owner owner,
	@JsonProperty("private")
	boolean isPrivate,
	@JsonProperty("html_url")
	String htmlUrl,
	@JsonProperty("description")
	String description,
	@JsonProperty("fork")
	boolean fork,
	@JsonProperty("url")
	String url,
	@JsonProperty("archive_url")
	String archiveUrl,
	@JsonProperty("assignees_url")
	String assigneesUrl,
	@JsonProperty("blobs_url")
	String blobsUrl,
	@JsonProperty("branches_url")
	String branchesUrl,
	@JsonProperty("collaborators_url")
	String collaboratorsUrl,
	@JsonProperty("comments_url")
	String commentsUrl,
	@JsonProperty("commits_url")
	String commitsUrl,
	@JsonProperty("compare_url")
	String compareUrl,
	@JsonProperty("contents_url")
	String contentsUrl,
	@JsonProperty("contributors_url")
	String contributorsUrl,
	@JsonProperty("deployments_url")
	String deploymentsUrl,
	@JsonProperty("downloads_url")
	String downloadsUrl,
	@JsonProperty("events_url")
	String eventsUrl,
	@JsonProperty("forks_url")
	String forksUrl,
	@JsonProperty("git_commits_url")
	String gitCommitsUrl,
	@JsonProperty("git_refs_url")
	String gitRefsUrl,
	@JsonProperty("git_tags_url")
	String gitTagsUrl,
	@JsonProperty("git_url")
	String gitUrl,
	@JsonProperty("issue_comment_url")
	String issueCommentUrl,
	@JsonProperty("issue_events_url")
	String issueEventsUrl,
	@JsonProperty("issues_url")
	String issuesUrl,
	@JsonProperty("keys_url")
	String keysUrl,
	@JsonProperty("labels_url")
	String labelsUrl,
	@JsonProperty("languages_url")
	String languagesUrl,
	@JsonProperty("merges_url")
	String mergesUrl,
	@JsonProperty("milestones_url")
	String milestonesUrl,
	@JsonProperty("notifications_url")
	String notificationsUrl,
	@JsonProperty("pulls_url")
	String pullsUrl,
	@JsonProperty("releases_url")
	String releasesUrl,
	@JsonProperty("ssh_url")
	String sshUrl,
	@JsonProperty("stargazers_url")
	String stargazersUrl,
	@JsonProperty("statuses_url")
	String statusesUrl,
	@JsonProperty("subscribers_url")
	String subscribersUrl,
	@JsonProperty("subscription_url")
	String subscriptionUrl,
	@JsonProperty("tags_url")
	String tagsUrl,
	@JsonProperty("teams_url")
	String teamsUrl,
	@JsonProperty("trees_url")
	String treesUrl,
	@JsonProperty("clone_url")
	String cloneUrl,
	@JsonProperty("mirror_url")
	String mirrorUrl,
	@JsonProperty("hooks_url")
	String hooksUrl,
	@JsonProperty("svn_url")
	String svnUrl,
	@JsonProperty("homepage")
	String homepage,
	@JsonProperty("language")
	String language,
	@JsonProperty("forks_count")
	int forksCount,
	@JsonProperty("stargazers_count")
	int stargazersCount,
	@JsonProperty("watchers_count")
	int watchersCount,
	@JsonProperty("size")
	int size,
	@JsonProperty("default_branch")
	String defaultBranch,
	@JsonProperty("open_issues_count")
	int openIssuesCount,
	@JsonProperty("is_template")
	boolean isTemplate,
	@JsonProperty("topics")
	List<String> topics,
	@JsonProperty("has_issues")
	boolean hasIssues,
	@JsonProperty("has_projects")
	boolean hasProjects,
	@JsonProperty("has_wiki")
	boolean hasWiki,
	@JsonProperty("has_pages")
	boolean hasPages,
	@JsonProperty("has_downloads")
	boolean hasDownloads,
	@JsonProperty("has_discussions")
	boolean hasDiscussions,
	@JsonProperty("archived")
	boolean archived,
	@JsonProperty("disabled")
	boolean disabled,
	@JsonProperty("visibility")
	String visibility,
	@JsonProperty("pushed_at")
	String pushedAt,
	@JsonProperty("created_at")
	String createdAt,
	@JsonProperty("updated_at")
	String updatedAt,
	@JsonProperty("permissions")
	Permissions permissions,
	@JsonProperty("security_and_analysis")
	SecurityAndAnalysis securityAndAnalysis
){

public record Owner(
	@JsonProperty("login") String login,
	@JsonProperty("id") long id,
	@JsonProperty("node_id") String nodeId,
	@JsonProperty("avatar_url") String avatarUrl,
	@JsonProperty("gravatar_id") String gravatarId,
	@JsonProperty("url") String url,
	@JsonProperty("html_url") String htmlUrl,
	@JsonProperty("followers_url") String followersUrl,
	@JsonProperty("following_url") String followingUrl,
	@JsonProperty("gists_url") String gistsUrl,
	@JsonProperty("starred_url") String starredUrl,
	@JsonProperty("subscriptions_url") String subscriptionsUrl,
	@JsonProperty("organizations_url") String organizationsUrl,
	@JsonProperty("repos_url") String reposUrl,
	@JsonProperty("events_url") String eventsUrl,
	@JsonProperty("received_events_url") String receivedEventsUrl,
	@JsonProperty("type") String type,
	@JsonProperty("site_admin") boolean siteAdmin
) {

}

public record Permissions(
	@JsonProperty("admin") boolean admin,
	@JsonProperty("push") boolean push,
	@JsonProperty("pull") boolean pull
) {

}

public record SecurityAndAnalysis(
	@JsonProperty("advanced_security") AdvancedSecurity advancedSecurity,
	@JsonProperty("secret_scanning") SecretScanning secretScanning,
	@JsonProperty("secret_scanning_push_protection") SecretScanningPushProtection secretScanningPushProtection,
	@JsonProperty("secret_scanning_non_provider_patterns") SecretScanningNonProviderPatterns secretScanningNonProviderPatterns
) {

	public record AdvancedSecurity(@JsonProperty("status") String status) {

	}

	public record SecretScanning(@JsonProperty("status") String status) {

	}

	public record SecretScanningPushProtection(@JsonProperty("status") String status) {

	}

	public record SecretScanningNonProviderPatterns(@JsonProperty("status") String status) {

	}
}
}