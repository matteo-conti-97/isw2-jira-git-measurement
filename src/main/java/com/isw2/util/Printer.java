package com.isw2.util;

import com.isw2.model.Project;
import com.isw2.model.Release;
import com.isw2.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Printer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Printer.class);

    private Printer() {
    }

    public static void printTicketsDetailed(List<Ticket> tickets) {
        int ticketSize = tickets.size();
        LOGGER.info("\n{} tickets:", ticketSize);
        for (Ticket ticket : tickets) {
            String ticketKey = ticket.getKey();
            String ticketCreationDate = ticket.getCreationDate();
            String ticketResolutionDate = ticket.getResolutionDate();
            String fvName = ticket.getFv().getName();
            String fvNumber = ticket.getFv().getNumberStr();
            String ovName = ticket.getOv().getName();
            String ovNumber = ticket.getOv().getNumberStr();
            LOGGER.info(
                    "{} Creation Date: {} Resolution Date: {} Fix Version: {} Num: {} Creation Date: {} Opening Version: {} Num: {}",
                    ticketKey, ticketCreationDate, ticketResolutionDate, fvName, fvNumber, ticket.getCreationDate(),
                    ovName, ovNumber);
            LOGGER.info("\tAffected Versions: ");
            for (Release av : ticket.getJiraAv()) {
                String avName = av.getName();
                LOGGER.info("\t\tVersion: {}", avName);
            }
        }
    }

    public static void printTicketsBasic(List<Ticket> tickets, boolean interest) {
        int ticketSize = tickets.size();
        if (interest)
            LOGGER.info("\n{} tickets of interest", ticketSize);
        else
            LOGGER.info("\n{} tickets", ticketSize);
        for (Ticket ticket : tickets) {
            String ticketKey = ticket.getKey();
            String ticketCreationDate = ticket.getCreationDate();
            String ticketResolutionDate = ticket.getResolutionDate();
            LOGGER.info("{} Creation Date: {} Resolution Date: {}", ticketKey, ticketCreationDate,
                    ticketResolutionDate);
        }
    }

    public static void printProjectInfo(Project project) {
        String projectName = project.getName();
        String projectCreationDate = project.getCreationDate();
        LOGGER.info("Project: {}", projectName);
        LOGGER.info("Creation date: {}", projectCreationDate);
    }

    public static void printReleasesDetailed(List<Release> releases, boolean interest) {
        if (interest)
            LOGGER.info("Releases of interest: ");
        else
            LOGGER.info("All releases: ");
        for (Release release : releases) {
            String releaseName = release.getName();
            String releaseNumber = release.getNumberStr();
            String releaseStartDate = release.getStartDate();
            String releaseEndDate = release.getEndDate();
            int releaseNumCommits = release.getCommits().size();
            int releaseNumFiles = release.getFileTreeAtReleaseEnd().size();
            LOGGER.info("Release: {} number {} has {} commits and {} non test java files, starts at {} and ends at {}",
                    releaseName, releaseNumber, releaseNumCommits, releaseNumFiles, releaseStartDate, releaseEndDate);
        }
    }

    public static void printReleasesBasic(List<Release> releases) {
        LOGGER.info("Releases of interest: ");
        for (Release release : releases) {
            String releaseName = release.getName();
            String releaseNumber = release.getNumberStr();
            String releaseStartDate = release.getStartDate();
            String releaseEndDate = release.getEndDate();
            LOGGER.info("Release: {} number {} starts at {} and ends at {}", releaseName, releaseNumber,
                    releaseStartDate, releaseEndDate);
        }
    }

    public static void printMeanEval(List<Double> meanEval, ClassifierType classifierType) {
        switch (classifierType) {
            case RANDOM_FOREST:
                LOGGER.info("\n\nMean evaluation with Random Forest: ");
                break;
            case IBK:
                LOGGER.info("\n\nMean evaluation with IBK: ");
                break;
            case NAIVE_BAYES:
                LOGGER.info("\n\nMean evaluation with Naive Bayes: ");
                break;
        }
        LOGGER.info("Kappa: {}", meanEval.get(0));
        LOGGER.info("Precision: {}", meanEval.get(1));
        LOGGER.info("Recall: {}", meanEval.get(2));
        LOGGER.info("AUC: {}", meanEval.get(3));
        LOGGER.info("TP: {} FN: {}: FP: {} TN: {}", meanEval.get(4), meanEval.get(5), meanEval.get(6), meanEval.get(7));
    }
}
