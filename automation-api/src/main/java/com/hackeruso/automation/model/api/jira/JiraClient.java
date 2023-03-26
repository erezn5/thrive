package com.hackeruso.automation.model.api.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.utils.Waiter;
import io.atlassian.util.concurrent.Promise;
import org.awaitility.Duration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class JiraClient {
    protected static final String HTTPS_SCHEME = "https";
    private String JQL = "reporter=5f95509d44658b00710137ff and status=done";
    private final JiraRestClient client;

    public JiraClient() throws URISyntaxException {
        client = createJiraClient();
    }

    private JiraRestClient createJiraClient() throws URISyntaxException {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        String JIRA_CLOUD_HOST = EnvConf.getProperty("jira.cloud.host");
        URI uri = new URI(String.format("%s://%s", HTTPS_SCHEME, JIRA_CLOUD_HOST));
        String JIRA_TOKEN = EnvConf.getProperty("jira.token");
        String JIRA_AUTOMATION_USER = EnvConf.getProperty("jira.user");
        return factory.createWithBasicHttpAuthentication(uri, JIRA_AUTOMATION_USER, JIRA_TOKEN);
    }

    /**
     * @param issueKey - for example AUT-169
     * @return
     */
    public Issue getIssueByKey(String issueKey) {
        return client.getIssueClient()
                .getIssue(issueKey)
                .claim();
    }

    public boolean jql(Promise<SearchResult> searchJqlPromise) {
        org.awaitility.core.Condition<Boolean> condition = () -> searchJqlPromise.claim().getIssues().spliterator().estimateSize() != 0;
        return Waiter.waitCondition(Duration.TEN_SECONDS, condition, Duration.ONE_SECOND);
    }

    public Promise<SearchResult> getIssuesByJQL(String jql) {
        Promise<SearchResult> searchJqlPromise = client.getSearchClient().searchJql(jql);
        int times = 1;
        while (!jql(searchJqlPromise) && times <= 5) {
            searchJqlPromise = client.getSearchClient().searchJql(jql);
            times++;
        }
        return searchJqlPromise;
    }

    public boolean verifyJqlContainsSubject(String jql, String desiredTxt) {
        return Objects.requireNonNull(returnSuitableJiraIssue(jql, desiredTxt)).getSummary().contains(desiredTxt);
    }

    public Issue returnSuitableJiraIssue(String jql, String desiredTxt) {
        Promise<SearchResult> searchJqlPromise = getIssuesByJQL(jql);
        for (Issue issue : searchJqlPromise.claim().getIssues()) {
            if (issue.getSummary().contains(desiredTxt)) {
                return issue;
            }
        }
        return null;
    }

    public List<IssueField> returnIssueFields(String jql, String desiredTxt) {
        return (List<IssueField>) Objects.requireNonNull(returnSuitableJiraIssue(jql, desiredTxt)).getFields();
    }


    public String getLastIssueKey(String jql, String desiredTxt) {
        Issue issue = returnSuitableJiraIssue(jql, desiredTxt);
        return issue.getKey();
    }
}
