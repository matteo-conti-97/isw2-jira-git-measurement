package com.isw2.model;

import java.util.ArrayList;
import java.util.List;

public class Release {
    private String name;
    private int number;
    private String startDate;
    private String endDate;
    private List<Commit> commits; // The first element is the last commit of the release and the last element is the first commit of the release
    private List<Ticket> tickets;
    private List<JavaFile> fileTreeAtReleaseEnd;

    public Release(String name) {
        this.name = name;
    }

    public Release(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public Release(String name, int number, String startDate) {
        this.name = name;
        this.number = number;
        this.startDate = startDate;
        this.commits = new ArrayList<>();
        this.tickets = new ArrayList<>();
    }

    public Release(String name, int number, String startDate, String endDate) {
        this.name = name;
        this.number = number;
        this.startDate = startDate;
        this.endDate = endDate;
        this.commits = new ArrayList<>();
        this.tickets = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumberStr() {
        return Integer.toString(number);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addCommit(Commit commit) {
        this.commits.add(commit);
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
    }

    public List<JavaFile> getFileTreeAtReleaseEnd() {
        return fileTreeAtReleaseEnd;
    }

    public void setFileTreeAtReleaseEnd(List<JavaFile> fileTreeAtReleaseEnd) {
        this.fileTreeAtReleaseEnd = fileTreeAtReleaseEnd;
    }
}
