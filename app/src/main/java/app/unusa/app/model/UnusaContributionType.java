package app.unusa.app.model;

public class UnusaContributionType {
    public String contributionId;
    public int startMonth;
    public int contribution_target;
    public int startYear;
    public String dateCreated;
    public int endMonth,
            endYear;
    public boolean isOpen;

    public String title, details;

    public UnusaContributionType() {
        startMonth = 0;
        startYear = 0;
        endMonth = 0;
        contribution_target = 0;
        endYear = 0;
        dateCreated = "";
        isOpen = true;
        title = "";
        details = "";
    }

    public UnusaContributionType(String contributionId, int startMonth, int startYear, String dateCreated, int endMonth, int endYear, boolean isOpen, String title, String details, int contribution_target) {
        this.contributionId = contributionId;
        this.startMonth = startMonth;
        this.startYear = startYear;
        this.dateCreated = dateCreated;
        this.endMonth = endMonth;
        this.endYear = endYear;
        this.isOpen = isOpen;
        this.title = title;
        this.contribution_target = contribution_target;
        this.details = details;
    }
}
