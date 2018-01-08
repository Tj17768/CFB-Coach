package Simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.text.DecimalFormat;


public class Game implements Serializable {

    public League league;

    public Team homeTeam;
    public Team awayTeam;

    public boolean hasPlayed;
    public boolean QT1;
    public boolean QT2;
    public boolean QT3;

    public String gameName;

    public int homeScore;
    public int[] homeQScore;
    public int awayScore;
    public int[] awayQScore;
    public int homeYards;
    public int awayYards;

    public int numOT;
    public int homeTOs;
    public int awayTOs;

    private ArrayList<String> homePassingStats;
    private ArrayList<String> homeRushingStats;
    private ArrayList<String> homeReceivingStats;
    private ArrayList<String> homeDefenseStats;
    private ArrayList<String> homeKickingStats;
    private ArrayList<String> awayPassingStats;
    private ArrayList<String> awayRushingStats;
    private ArrayList<String> awayReceivingStats;
    private ArrayList<String> awayDefenseStats;
    private ArrayList<String> awayKickingStats;
    
    private int homePassYards;
    private int awayPassYards;
    private int homeRushYards;
    private int awayRushYards;
    
    String gameEventLog;
    String tdInfo;

    //private variables used when simming games
    private int gameTime;
    private boolean gamePoss; //1 if home, 0 if away
    private int gameYardLine;
    private int gameDown;
    private int gameYardsNeed;
    private boolean playingOT;
    private boolean bottomOT;

    private int timePerPlay = 22; //affects snaps per game!
    private int intValue = 150;
    private int sackValue = 175;
    private int fatigueDropHigh = 9;
    private int fatigueDropMed = 6;
    private int fatigueDropLow = 3;
    private int fatigueGain = 4;
    private int snapCount = 0;

    Random rand = new Random();

    public Game(Team home, Team away, String name) {
        homeTeam = home;
        awayTeam = away;

        gameName = name;

        homeScore = 0;
        homeQScore = new int[10];
        awayScore = 0;
        awayQScore = new int[10];
        numOT = 0;

        homeTOs = 0;
        awayTOs = 0;

        //playGame();
        hasPlayed = false;

        if (gameName.equals("Conference") && (homeTeam.rivalTeam.equals(awayTeam.abbr) || awayTeam.rivalTeam.equals(homeTeam.abbr))) {
            // Rivalry game!
            gameName = "Rivalry Game";
        }
    }

    public Game(Team home, Team away) {
        homeTeam = home;
        awayTeam = away;
        numOT = 0;
        homeTOs = 0;
        awayTOs = 0;

        gameName = "";

        homeScore = 0;
        homeQScore = new int[10];
        awayScore = 0;
        awayQScore = new int[10];

        //playGame();
        hasPlayed = false;

    }

    private int getHFadv() {
        //home field advantage
        int footIQadv = (homeTeam.getCompositeFootIQ() - awayTeam.getCompositeFootIQ()) / 5;
        if (footIQadv > 3) footIQadv = 3;
        if (footIQadv < -3) footIQadv = -3;
        if (gameName.contains("Bowl") || gameName.contains("NC") || gameName.contains("SF"))  return 0;
        if (gamePoss) {
            return 3 + footIQadv;
        } else {
            return -footIQadv;
        }
    }

    private int coachAdv() {
        int adv = 0;

        if (gamePoss) {
            adv = Math.round((homeTeam.HC.get(0).ratOff - awayTeam.HC.get(0).ratDef) / 4);
        } else {
            adv = Math.round((awayTeam.HC.get(0).ratOff - homeTeam.HC.get(0).ratDef) / 4);
        }
        if (adv > 3) adv = 3;
        if (adv < -3) adv = -3;
        return adv;
    }

    public void playGame() {
        if (!hasPlayed) {
            gameEventLog = "LOG: #" + awayTeam.rankTeamPollScore + " " + awayTeam.abbr + " (" + awayTeam.wins + "-" + awayTeam.losses + ") @ #" +
                    homeTeam.rankTeamPollScore + " " + homeTeam.abbr + " (" + homeTeam.wins + "-" + homeTeam.losses + ")" + "\n" +
                    "---------------------------------------------------------\n\n" +
                    awayTeam.abbr + " Off Strategy: " + awayTeam.teamStratOff.getStratName() + "\n" +
                    awayTeam.abbr + " Def Strategy: " + awayTeam.teamStratDef.getStratName() + "\n" +
                    homeTeam.abbr + " Off Strategy: " + homeTeam.teamStratOff.getStratName() + "\n" +
                    homeTeam.abbr + " Def Strategy: " + homeTeam.teamStratDef.getStratName() + "\n";
            //probably establish some home field advantage before playing
            gameTime = 3600;
            gameDown = 1;
            gamePoss = true;
            kickOff(homeTeam);
            
            //Reset Game Stats & Fatigue
            for(int i = 0; i < homeTeam.getAllPlayers().size(); ++i) {
                Player player = homeTeam.getAllPlayers().get(i);
                player.gameFatigue = 100;
                player.gameSim = 0;
                player.posDepth = 0;
                player.gamePassAtempts = 0;
                player.gamePassComplete = 0;
                player.gamePassYards = 0;
                player.gamePassTDs = 0;
                player.gamePassInts = 0;
                player.gameRushAttempts = 0;
                player.gameRushYards = 0;
                player.gameRushTDs = 0;
                player.gameTargets = 0;
                player.gameReceptions = 0;
                player.gameRecYards = 0;
                player.gameRecTDs = 0;
                player.gameDrops = 0;
                player.gameFumbles = 0;
                player.gameTackles = 0;
                player.gameSacks = 0;
                player.gameInterceptions = 0;
                player.gameDefended = 0;
                player.gameIncomplete = 0;
                player.gameFGAttempts = 0;
                player.gameFGMade = 0;
                player.gameXPAttempts = 0;
                player.gameXPMade = 0;
            }
            for(int i = 0; i < awayTeam.getAllPlayers().size(); ++i) {
                Player player = awayTeam.getAllPlayers().get(i);
                player.gameFatigue = 100;
                player.gameSim = 0;
                player.posDepth = 0;
                player.gamePassAtempts = 0;
                player.gamePassComplete = 0;
                player.gamePassYards = 0;
                player.gamePassTDs = 0;
                player.gamePassInts = 0;
                player.gameRushAttempts = 0;
                player.gameRushYards = 0;
                player.gameRushTDs = 0;
                player.gameTargets = 0;
                player.gameReceptions = 0;
                player.gameRecYards = 0;
                player.gameRecTDs = 0;
                player.gameDrops = 0;
                player.gameFumbles = 0;
                player.gameTackles = 0;
                player.gameSacks = 0;
                player.gameInterceptions = 0;
                player.gameDefended = 0;
                player.gameIncomplete = 0;
                player.gameFGAttempts = 0;
                player.gameFGMade = 0;
                player.gameXPAttempts = 0;
                player.gameXPMade = 0;
            }

            // Regulation
            while (gameTime > 0) {
                //play ball!
                if (gamePoss) runPlay(homeTeam, awayTeam);
                else runPlay(awayTeam, homeTeam);
            }

            // Add last play
            if (homeScore != awayScore) {
                gameEventLog += getEventPrefix() + "Time has expired! The game is over.";
            } else {
                gameEventLog += getEventPrefix() + "OVERTIME!\nTie game at 0:00, overtime begins!";
            }

            //Overtime (if needed)
            if (gameTime <= 0 && homeScore == awayScore) {
                playingOT = true;
                gamePoss = false;
                gameYardLine = 75;
                numOT++;
                gameTime = -1;
                gameDown = 1;
                gameYardsNeed = 10;

                while (playingOT) {
                    if (gamePoss) runPlay(homeTeam, awayTeam);
                    else runPlay(awayTeam, homeTeam);
                }
            }

            //game over, add wins
            if (homeScore > awayScore) {
                homeTeam.wins++;
                homeTeam.totalWins++;
                homeTeam.gameWLSchedule.add("W");
                awayTeam.losses++;
                awayTeam.totalLosses++;
                awayTeam.gameWLSchedule.add("L");
                homeTeam.gameWinsAgainst.add(awayTeam);
                awayTeam.gameLossesAgainst.add(homeTeam);
                homeTeam.winStreak.addWin(homeTeam.league.getYear());
                homeTeam.league.checkLongestWinStreak(homeTeam.winStreak);
                awayTeam.winStreak.resetStreak(awayTeam.league.getYear());
                homeTeam.HC.get(0).wins++;
                awayTeam.HC.get(0).losses++;
            } else {
                homeTeam.losses++;
                homeTeam.totalLosses++;
                homeTeam.gameWLSchedule.add("L");
                awayTeam.wins++;
                awayTeam.totalWins++;
                awayTeam.gameWLSchedule.add("W");
                awayTeam.gameWinsAgainst.add(homeTeam);
                homeTeam.gameLossesAgainst.add(awayTeam);
                awayTeam.winStreak.addWin(awayTeam.league.getYear());
                awayTeam.league.checkLongestWinStreak(awayTeam.winStreak);
                homeTeam.winStreak.resetStreak(homeTeam.league.getYear());
                homeTeam.HC.get(0).losses++;
                awayTeam.HC.get(0).wins++;
            }

            // Add points/opp points
            homeTeam.addGamePlayedPlayers(homeScore > awayScore);
            awayTeam.addGamePlayedPlayers(awayScore > homeScore);

            homeTeam.teamPoints += homeScore;
            awayTeam.teamPoints += awayScore;

            homeTeam.teamOppPoints += awayScore;
            awayTeam.teamOppPoints += homeScore;

            homeYards = getPassYards(false) + getRushYards(false);
            awayYards = getPassYards(true) + getRushYards(true);

            homeTeam.teamYards += homeYards;
            awayTeam.teamYards += awayYards;

            homeTeam.teamOppYards += awayYards;
            awayTeam.teamOppYards += homeYards;

            homeTeam.teamOppPassYards += getPassYards(true);
            awayTeam.teamOppPassYards += getPassYards(false);
            homeTeam.teamOppRushYards += getRushYards(true);
            awayTeam.teamOppRushYards += getRushYards(false);
            homePassYards = getPassYards(false);
            awayPassYards = getPassYards(true);
            homeRushYards = getRushYards(false);
            awayRushYards = getRushYards(true);

            homeTeam.teamTODiff += awayTOs - homeTOs;
            awayTeam.teamTODiff += homeTOs - awayTOs;

            gameStatistics();

            hasPlayed = true;

            addNewsStory();

            if (homeTeam.rivalTeam.equals(awayTeam.abbr) || awayTeam.rivalTeam.equals(homeTeam.abbr)) {
                if (homeScore > awayScore) {
                    homeTeam.wonRivalryGame = true;
                } else {
                    awayTeam.wonRivalryGame = true;
                }
            }

            homeTeam.checkForInjury();
            awayTeam.checkForInjury();

        }
    }

    // PRE-SNAP DECISIONS

    private void runPlay(Team offense, Team defense) {
        quarterCheck();
        recoup(false, 0);
        snapCount++;

        if (gameDown > 4) {
            if (!playingOT) {
                //Log the turnover on downs, reset down and distance, give possession to the defense, exit this runPlay()
                gameEventLog += getEventPrefix() + "TURNOVER ON DOWNS!\n" + offense.abbr + " failed to convert on " + (gameDown - 1) + "th down. " + defense.abbr + " takes over possession on downs.";

                //Turn over on downs, change possession, set to first down and 10 yards to go
                gamePoss = !gamePoss;
                gameDown = 1;
                gameYardsNeed = 10;
                //and flip which direction the ball is moving in
                gameYardLine = 100 - gameYardLine;

            } else {
                //OT is over for the offense, log the turnover on downs, run resetForOT().
                gameEventLog += getEventPrefix() + "TURNOVER ON DOWNS!\n" + offense.abbr + " failed to convert on " + (gameDown - 1) + "th down in OT and their possession is over.";
                resetForOT();

            }
        } else {
            double preferPass = (offense.getPassProf() - defense.getPassDef()) / 100 + Math.random() * offense.teamStratOff.getPassPref();       //STRATEGIES
            double preferRush = (offense.getRushProf() - defense.getRushDef()) / 90 + Math.random() * offense.teamStratOff.getRunPref();

            // If it's 1st and Goal to go, adjust yards needed to reflect distance for a TD so that play selection reflects actual yards to go
            // If we don't do this, gameYardsNeed may be higher than the actually distance for a TD and suboptimal plays may be chosen
            if (gameDown == 1 && gameYardLine >= 91) gameYardsNeed = 100 - gameYardLine;

            //Under 30 seconds to play, check that the team with the ball is trailing or tied, do something based on the score difference
            if (gameTime <= 30 && !playingOT && ((gamePoss && (awayScore >= homeScore)) || (!gamePoss && (homeScore >= awayScore)))) {
                //Down by 3 or less, or tied, and you have the ball
                if (((gamePoss && (awayScore - homeScore) <= 3) || (!gamePoss && (homeScore - awayScore) <= 3)) && gameYardLine > 60) {
                    //last second FGA
                    fieldGoalAtt(offense, defense);
                } else {
                    //hail mary
                    passingPlay(offense, defense);
                }
            } else if (gameDown >= 4) {
                if (((gamePoss && (awayScore - homeScore) > 3) || (!gamePoss && (homeScore - awayScore) > 3)) && gameTime < 300) {
                    //go for it since we need 7 to win -- This also forces going for it if down by a TD in BOT OT
                    if (gameYardsNeed < 3 && preferRush * 3 > preferPass) {
                        rushingPlay(offense, defense);
                    } else {
                        passingPlay(offense, defense);
                    }
                } else {
                    //4th down
                    if (gameYardsNeed < 3) {
                        if (gameYardLine > 65) {
                            //fga
                            fieldGoalAtt(offense, defense);
                        } else if (gameYardLine > 55) {
                            // run play, go for it!
                            rushingPlay(offense, defense);
                        } else {
                            //punt
                            puntPlay(offense);
                        }
                    } else if (gameYardLine > 60) {
                        //fga
                        fieldGoalAtt(offense, defense);
                    } else {
                        //punt
                        puntPlay(offense);
                    }
                }
            } else if ((gameDown == 3 && gameYardsNeed > 4) || ((gameDown == 1 || gameDown == 2) && (preferPass >= preferRush))) {
                // pass play
                passingPlay(offense, defense);
            } else {
                //run play
                rushingPlay(offense, defense);
            }
        }


    }

    private void passingPlay(Team offense, Team defense) {
        PlayerQB selQB;
        PlayerRB selRB;
        PlayerWR selWR;
        PlayerTE selTE;
        PlayerDL selDL;
        PlayerLB selLB;
        PlayerLB selLB2;
        PlayerCB selCB;
        PlayerS selS;

        ArrayList<Player> receiver = new ArrayList<>();
        ArrayList<PlayerRB> RunningBack = new ArrayList<>();
        ArrayList<PlayerWR> WideReceiver = new ArrayList<>();
        ArrayList<PlayerTE> TightEnd = new ArrayList<>();
        ArrayList<PlayerDL> DLineman = new ArrayList<>();
        ArrayList<PlayerLB> Linebacker = new ArrayList<>();
        ArrayList<PlayerCB> Cornerback = new ArrayList<>();
        ArrayList<PlayerS> Safety = new ArrayList<>();

        //Receiver Options
        for(int i = 0; i < offense.startersWR; ++i) {
            if (offense.getWR(i).gameFatigue > 0) {
                offense.getWR(i).gameSim = (double)Math.pow(offense.getWR(i).ratOvr, 1) * Math.random();
                offense.getWR(i).posDepth = i;
                offense.getWR(i).gameSnaps++;
                receiver.add(offense.getWR(i));
                WideReceiver.add(offense.getWR(i));
            } else {
                offense.getWR(offense.startersWR).gameSim = (double)Math.pow(offense.getWR(offense.startersWR).ratOvr, 1) * Math.random();
                offense.getWR(offense.startersWR).posDepth = i;
                offense.getWR(offense.startersWR).gameSnaps++;
                receiver.add(offense.getWR(offense.startersWR));
                WideReceiver.add(offense.getWR(offense.startersWR));
            }
        }

        for(int i = 0; i < offense.startersTE; ++i) {
            if (offense.getTE(i).gameFatigue > 0) {
                if (gameYardLine > 90) {
                    offense.getTE(i).gameSim = Math.pow(((offense.getTE(i).ratCatch + offense.getTE(0).ratSpeed) / 2), 1) * Math.random() * 1.25;
                } else {
                    offense.getTE(i).gameSim = Math.pow(((offense.getTE(i).ratCatch + offense.getTE(i).ratSpeed) / 2), 1) * Math.random() * .55;
                }
                offense.getTE(i).gameSnaps++;
                receiver.add(offense.getTE(i));
                TightEnd.add(offense.getTE(i));
            } else {
                if (gameYardLine > 90) {
                    offense.getTE(offense.startersTE).gameSim = Math.pow(((offense.getTE(offense.startersTE).ratCatch + offense.getTE(offense.startersTE).ratSpeed) / 2), 1) * Math.random() * 1.25;
                } else {
                    offense.getTE(offense.startersTE).gameSim = Math.pow(((offense.getTE(offense.startersTE).ratCatch + offense.getTE(offense.startersTE).ratSpeed) / 2), 1) * Math.random() * .55;
                }
                offense.getTE(offense.startersTE).gameSnaps++;
                receiver.add(offense.getTE(offense.startersTE));
                TightEnd.add(offense.getTE(offense.startersTE));
            }
        }
        for(int i = 0; i < offense.startersRB; ++i) {
            if (offense.getRB(i).gameFatigue > 0) {
                offense.getRB(i).gameSim = Math.pow(((offense.getRB(0).ratCatch + offense.getRB(i).ratSpeed) / 2), 1) * Math.random() * .55;
                offense.getRB(i).gameSnaps++;
                receiver.add(offense.getRB(i));
                RunningBack.add(offense.getRB(i));
            } else {
                offense.getRB(offense.startersRB).gameSim = Math.pow(((offense.getRB(offense.startersRB).ratCatch + offense.getRB(offense.startersRB).ratSpeed) / 2), 1) * Math.random() * .55;
                offense.getRB(i).gameSnaps++;
                receiver.add(offense.getRB(offense.startersRB));
                RunningBack.add(offense.getRB(offense.startersRB));
            }
        }

        for(int i = 0; i < defense.startersLB; ++i) {
            if (defense.getLB(i).gameFatigue > 0) {
                defense.getLB(i).gameSim = Math.pow(defense.getLB(i).ratCoverage, 1) * Math.random();
                defense.getLB(i).gameSnaps++;
                Linebacker.add(defense.getLB(i));
            } else {
                defense.getLB(defense.startersLB).gameSim = Math.pow(defense.getLB(defense.startersLB).ratCoverage, 1) * Math.random();
                defense.getLB(defense.startersLB+1).gameSim = Math.pow(defense.getLB(defense.startersLB+1).ratCoverage, 1) * Math.random();
                defense.getLB(defense.startersLB).gameSnaps++;
                defense.getLB(defense.startersLB+1).gameSnaps++;
                Linebacker.add(defense.getLB(defense.startersLB));
                Linebacker.add(defense.getLB(defense.startersLB+1));
            }
        }

        for(int i = 0; i < defense.startersDL; ++i) {
            if (defense.getDL(i).gameFatigue > 0) {
                defense.getDL(i).gameSim = Math.pow(defense.getDL(i).ratPassRush, 1) * Math.random();
                defense.getDL(i).gameSnaps++;
                DLineman.add(defense.getDL(i));
            } else {
                defense.getDL(defense.startersDL).gameSim = Math.pow(defense.getDL(defense.startersDL).ratPassRush, 1) * Math.random();
                defense.getDL(defense.startersDL+1).gameSim = Math.pow(defense.getDL(defense.startersDL+1).ratPassRush, 1) * Math.random();
                defense.getDL(defense.startersDL).gameSnaps++;
                defense.getDL(defense.startersDL).gameSnaps++;
                DLineman.add(defense.getDL(defense.startersDL));
                DLineman.add(defense.getDL(defense.startersDL+1));
            }
        }

        //Rank Players
        Collections.sort(WideReceiver, new CompGamePlayerPicker());
        Collections.sort(TightEnd, new CompGamePlayerPicker());
        Collections.sort(RunningBack, new CompGamePlayerPicker());
        Collections.sort(receiver, new CompGamePlayerPicker());
        Collections.sort(Linebacker, new CompGamePlayerPicker());
        Collections.sort(DLineman, new CompGamePlayerPicker());


        //Choose Action Players
        selQB = offense.getQB(0);
        selRB = RunningBack.get(0);
        selTE = TightEnd.get(0);
        selWR = WideReceiver.get(0);
        selDL = DLineman.get(0);
        selLB = Linebacker.get(0);
        selLB2 = Linebacker.get(1);
        selCB = defense.getCB(WideReceiver.get(0).posDepth);
        selS = defense.getS(0);

        if (selCB.gameFatigue <= 0) selCB = defense.getCB(defense.startersCB);
        if (selS.gameFatigue <= 0) selS = defense.getS(defense.startersS);


        //Fatigue selected Action Players
        selRB.gameFatigue -= Math.round((100-selRB.ratDur)/10);
        selWR.gameFatigue -= fatigueDropHigh + Math.round((100-selWR.ratDur)/10);
        selTE.gameFatigue -= fatigueDropHigh + Math.round((100-selTE.ratDur)/10);
        selDL.gameFatigue -= fatigueDropHigh + Math.round((100-selDL.ratDur)/10);
        selLB.gameFatigue -= fatigueDropLow + Math.round((100-selLB.ratDur)/10);
        selLB2.gameFatigue -= Math.round((100-selLB2.ratDur)/10);
        selCB.gameFatigue -= fatigueDropMed + Math.round((100-selCB.ratDur)/10);
        selS.gameFatigue -= fatigueDropMed + Math.round((100-selS.ratDur)/10);

        //Choose the Catch Target
        if (receiver.get(0).position.equals("TE")) {
            passingPlayTE(offense, defense, selQB, selTE, selDL, selLB, selLB2, selCB, selS);
        } else if (receiver.get(0).position.equals("RB")) {
            passingPlayRB(offense, defense, selQB, selRB, selDL, selLB, selLB2, selCB, selS);
        } else {
            passingPlayWR(offense, defense, selQB, selWR, selTE, selDL, selLB, selLB2, selCB, selS);
        }

    }

    private void rushingPlay(Team offense, Team defense) {
        PlayerQB selQB;
        PlayerRB selRB;
        PlayerTE selTE;
        PlayerDL selDL;
        PlayerLB selLB;
        PlayerCB selCB;
        PlayerS selS;

        ArrayList<Player> rusher = new ArrayList<>();
        ArrayList<PlayerRB> RunningBack = new ArrayList<>();
        ArrayList<PlayerTE> TightEnd = new ArrayList<>();
        ArrayList<PlayerDL> DLineman = new ArrayList<>();
        ArrayList<PlayerLB> Linebacker = new ArrayList<>();
        ArrayList<PlayerCB> Cornerback = new ArrayList<>();

        //Action Players

        for(int i = 0; i < offense.startersRB; ++i) {
            if (offense.getRB(i).gameFatigue > 0) {
                offense.getRB(i).gameSim = Math.pow(offense.getRB(i).ratOvr, 1.5) * Math.random();
                offense.getRB(i).gameSnaps++;
                rusher.add(offense.getRB(i));
                RunningBack.add(offense.getRB(i));
            } else {
                offense.getRB(offense.startersRB).gameSim = Math.pow(offense.getRB(i).ratOvr, 1.5) * Math.random();
                offense.getRB(offense.startersRB).gameSnaps++;
                rusher.add(offense.getRB(offense.startersRB));
                RunningBack.add(offense.getRB(offense.startersRB));
            }
        }

        if (offense.teamStratOffNum == 4) offense.getQB(0).gameSim = Math.pow(offense.getQB(0).ratSpeed, 1.5) * Math.random();
        else offense.getQB(0).gameSim = 0.25 * Math.pow(offense.getQB(0).ratSpeed, 1.5) * Math.random();
        rusher.add(offense.getQB(0));

        for(int i = 0; i < defense.startersDL; ++i) {
            if (defense.getDL(i).gameFatigue > 0) {
                defense.getDL(i).gameSim = Math.pow(defense.getDL(i).ratRunStop, 1) * Math.random();
                defense.getDL(i).gameSnaps++;
                DLineman.add(defense.getDL(i));
            } else {
                defense.getDL(defense.startersDL).gameSim = Math.pow(defense.getDL(defense.startersDL).ratRunStop, 1) * Math.random();
                defense.getDL(defense.startersDL+1).gameSim = Math.pow(defense.getDL(defense.startersDL+1).ratRunStop, 1) * Math.random();
                defense.getDL(defense.startersDL).gameSnaps++;
                defense.getDL(defense.startersDL+1).gameSnaps++;
                DLineman.add(defense.getDL(defense.startersDL));
                DLineman.add(defense.getDL(defense.startersDL+1));
            }
        }

        for(int i = 0; i < defense.startersLB; ++i) {
            if (defense.getLB(i).gameFatigue > 0) {
                defense.getLB(i).gameSim = Math.pow(defense.getLB(i).ratRunStop, 1) * Math.random();
                defense.getLB(i).gameSnaps++;
                Linebacker.add(defense.getLB(i));
            } else {
                defense.getLB(defense.startersLB).gameSim = Math.pow(defense.getLB(defense.startersLB).ratRunStop, 1) * Math.random();
                defense.getLB(defense.startersLB+1).gameSim = Math.pow(defense.getLB(defense.startersLB+1).ratRunStop, 1) * Math.random();
                defense.getLB(defense.startersLB).gameSnaps++;
                defense.getLB(defense.startersLB+1).gameSnaps++;
                Linebacker.add(defense.getLB(defense.startersLB));
                Linebacker.add(defense.getLB(defense.startersLB+1));
            }
        }

        for(int i = 0; i < defense.startersCB; ++i) {
            if (defense.getCB(i).gameFatigue > 0) {
                defense.getCB(i).gameSim = Math.pow(defense.getCB(i).ratTackle, 1) * Math.random();
                defense.getCB(i).gameSnaps++;
                Cornerback.add(defense.getCB(i));
            } else {
                defense.getCB(defense.startersCB).gameSim = Math.pow(defense.getCB(defense.startersCB).ratTackle, 1) * Math.random();
                defense.getCB(defense.startersCB).gameSnaps++;
                Cornerback.add(defense.getCB(defense.startersCB));
            }
        }

        //Rank Players
        Collections.sort(TightEnd, new CompGamePlayerPicker());
        Collections.sort(RunningBack, new CompGamePlayerPicker());
        Collections.sort(rusher, new CompGamePlayerPicker());
        Collections.sort(Linebacker, new CompGamePlayerPicker());
        Collections.sort(DLineman, new CompGamePlayerPicker());

        //Choose Action Players
        selQB = offense.getQB(0);
        selRB = RunningBack.get(0);
        selTE = offense.getTE(0);
        selDL = DLineman.get(0);
        selLB = Linebacker.get(0);
        selCB = Cornerback.get(0);
        selS = defense.getS(0);

        if (selTE.gameFatigue <= 0) selTE = defense.getTE(offense.startersTE);
        if (selS.gameFatigue <= 0) selS = defense.getS(defense.startersS);

        //Fatigue
        selRB.gameFatigue -= fatigueDropMed + Math.round((100-selRB.ratDur)/10);
        selTE.gameFatigue -= fatigueDropMed + Math.round((100-selTE.ratDur)/10);
        selDL.gameFatigue -= fatigueDropHigh + Math.round((100-selDL.ratDur)/10);
        selLB.gameFatigue -= fatigueDropLow + Math.round((100-selLB.ratDur)/10);
        selCB.gameFatigue -= fatigueDropLow + Math.round((100-selCB.ratDur)/10);
        selS.gameFatigue -= fatigueDropMed + Math.round((100-selS.ratDur)/10);

        if (rusher.get(0).position.equals("QB")) {
            RushPlayQB(offense, defense, selQB, selTE, selDL, selLB, selCB, selS);
        } else {
            RushPlayRB(offense, defense, selRB, selTE, selDL, selLB, selCB, selS);
        }

    }

    //PASSING PLAY - POST-SNAP

    private void passingPlayWR(Team offense, Team defense, PlayerQB selQB, PlayerWR selWR, PlayerTE selTE, PlayerDL selDL, PlayerLB selLB, PlayerLB selLB2, PlayerCB selCB, PlayerS selS) {
        int yardsGain = 0;
        boolean gotTD = false;
        boolean gotFumble = false;

        //get how much pressure there is on qb, check if sack
        int pressureOnQB = defense.getCompositeDLPass() * 2 - offense.getCompositeOLPass() - getHFadv() - (int) (Math.random() * coachAdv()) + (defense.teamStratDef.getRunProtection() * 2 - offense.teamStratDef.getRunProtection());  //STRATEGIES
        if (Math.random() * sackValue < pressureOnQB / 8) {

            if (Math.random() * 100 < pressureOnQB / 8 && selQB.ratSpeed > selDL.ratPassRush) {
                RushPlayQB(offense, defense, selQB, selTE, selDL, selLB, selCB, selS);
            } else {

                //sacked!
                String defender;
                double DL = selDL.ratTackle * Math.random() * 100;
                double LB = selLB2.ratTackle * Math.random() * 60;
                double S = selS.ratTackle * Math.random() * 25;

                if (DL >= LB && DL >= S) {
                    selDL.gameTackles++;
                    selDL.gameSacks++;
                    selDL.statsTackles++;
                    selDL.statsSacks++;
                    defender = ("DL " + selDL.name);
                } else if (LB > DL && LB >= S) {
                    selLB.gameTackles++;
                    selLB.gameSacks++;
                    selLB2.statsTackles++;
                    selLB2.statsSacks++;
                    defender = ("LB " + selLB2.name);
                } else {
                    selS.gameTackles++;
                    selS.gameSacks++;
                    selS.statsTackles++;
                    selS.statsSacks++;
                    defender = ("S " + selS.name);
                }

                qbSack(offense, defense, defender, selQB);

                return;
            }
        }

        //check for int
        double intChance = (pressureOnQB + defense.getS(0).ratOvr - (offense.getQB(0).ratPassAcc + offense.getQB(0).ratFootIQ + 100) / 3) / 18    //STRATEGIES
                - offense.teamStratOff.getPassProtection() + defense.teamStratDef.getPassProtection();
        if (intChance < 0.015) intChance = 0.015;
        if (intValue * Math.random() < intChance) {
            //Interception
            double cb = selCB.ratCoverage * Math.random() * 100;
            double s = selS.ratCoverage * Math.random() * 60;
            double lb = selLB.ratCoverage * Math.random() * 35;
            String defender;

            if (cb >= s) {
                selCB.gameInterceptions++;
                selCB.statsInts++;
                defender = ("CB " + selCB.name);
            } else if (s > cb && s >= lb) {
                selS.gameInterceptions++;
                selS.statsInts++;
                defender = ("S " + selS.name);
            } else {
                selLB.gameInterceptions++;
                selLB.statsInts++;
                defender = ("LB " + selLB.name);
            }

            qbInterception(offense, defender, selQB);

            return;
        }

        //throw ball, check for completion
        double completion = (getHFadv() + (int) (Math.random() * coachAdv()) + normalize(offense.getQB(0).ratPassAcc) + normalize(selWR.ratCatch)
                - normalize(selCB.ratCoverage)) / 2 + 18.25 - pressureOnQB / 16.8 + offense.teamStratOff.getPassProtection() - defense.teamStratDef.getPassProtection();   //STRATEGIES
        if (100 * Math.random() < completion) {
            if (100 * Math.random() < (100 - selWR.ratCatch) / 3) {
                //drop
                gameDown++;
                selWR.gameDrops++;
                selWR.statsDrops++;
                passAttempt(selQB, selWR);
                //Drop ball = inc pass, so run time for the play, stop clock until next play, move on (aka return;)
                gameTime -= timePerPlay * Math.random();
                return;

            } else {
                //no drop
                yardsGain = (int) ((normalize(offense.getQB(0).ratPassPow) + normalize(selWR.ratSpeed) - normalize(selCB.ratSpeed)) * Math.random() / 4.8 //STRATEGIES
                        + offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential());
                //see if receiver can get yards after catch
                double escapeChance = (normalize(selWR.ratEvasion) * 3 - selCB.ratTackle - selS.ratTackle) * Math.random()   //STRATEGIES
                        + offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential();
                if (escapeChance > 92 || Math.random() > 0.95) {
                    yardsGain += 3 + (selWR.ratSpeed * Math.random() / 4);
                }
                if (escapeChance > 80 && Math.random() < (0.1 + (offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential()) / 200)) {  //STRATEGIES
                    //wr escapes for TD
                    yardsGain += 100;
                }

                //add yardage
                gameYardLine += yardsGain;
                if (gameYardLine >= 100) { //TD!
                    yardsGain -= gameYardLine - 100;
                    gameYardLine = 100 - yardsGain;
                    addPointsQuarter(6);
                    passingTD(offense, selQB, selWR, yardsGain);
                    gotTD = true;
                } else {
                    //check for fumble
                    double fumChance = (selS.ratTackle + selCB.ratTackle) / 2;
                    if (100 * Math.random() < fumChance / 50) {
                        //Fumble!
                        gotFumble = true;
                    }
                }

                if (!gotTD && !gotFumble) {
                    //check downs if there wasnt fumble or TD
                    gameYardsNeed -= yardsGain;

                    if (escapeChance <= 70) {
                        selCB.gameTackles++;
                        selCB.statsTackles++;
                    } else if (escapeChance > 70 && escapeChance < 85) {
                        selS.gameTackles++;
                        selS.statsTackles++;
                    } else {
                        selLB.gameTackles++;
                        selLB.statsTackles++;
                    }

                    if (gameYardsNeed <= 0) {
                        // Only set new down and distance if there wasn't a TD
                        gameDown = 1;
                        gameYardsNeed = 10;
                    } else gameDown++;
                }

                //stats management
                selCB.statsTargets++;
                selCB.gameTargets++;
                passCompletion(offense, selQB, selWR, yardsGain);
            }

        } else {
            //no completion, advance downs
            passAttempt(selQB, selWR);
            gameDown++;
            //Incomplete pass stops the clock, so just run time for how long the play took, then move on
            gameTime -= timePerPlay * Math.random();

            if ((selCB.ratJump * Math.random() + selCB.ratCoverage * Math.random()) > (selWR.ratJump * Math.random() + selWR.ratCatch * Math.random()) * 2) {
                selCB.statsTargets++;
                selCB.gameTargets++;
                selCB.statsDefended++;  //pass was tipped
                selCB.gameDefended++;
                selCB.statsIncomplete++;
                selCB.gameIncomplete++;
            } else {
                selCB.statsTargets++;
                selCB.gameTargets++;
                selCB.statsIncomplete++;
                selCB.gameIncomplete++;
            }
            return;
        }

        passAttempt(selQB, selWR);


        if (gotFumble) {
            String defender;
            if (selS.ratTackle * Math.random() > selCB.ratTackle * Math.random()) {
                selS.gameTackles++;
                selS.gameFumbles++;
                selS.statsTackles++;
                selS.statsFumbles++;
                defender = ("S " + selS.name);
            } else {
                selCB.gameTackles++;
                selCB.gameFumbles++;
                selCB.statsTackles++;
                selCB.statsFumbles++;
                defender = ("CB " + selCB.name);
            }

            selWR.gameFumbles++;
            selWR.statsFumbles++;

            gameEventLog += getEventPrefix() + "FUMBLE!\n" + offense.abbr + " receiver " + selWR.name + " fumbled the ball after a catch. It was recovered by " + defender + ".";


            if (gamePoss) { // home possession
                homeTOs++;
            } else {
                awayTOs++;
            }
            if (!playingOT) {
                gameDown = 1;
                gameYardsNeed = 10;
                gamePoss = !gamePoss;
                gameYardLine = 100 - gameYardLine;
                gameTime -= timePerPlay * Math.random();
                return;
            } else {
                resetForOT();
                return;
            }
        }

        if (gotTD) {
            gameTime -= timePerPlay * Math.random();
            kickXP(offense, defense);
            if (!playingOT) kickOff(offense);
            else resetForOT();
            return;
        }

        gameTime -= timePerPlay + timePerPlay * Math.random();
    }

    private void passingTD(Team offense, PlayerQB selQB, PlayerWR selWR, int yardsGain) {
        if (gamePoss) { // home possession
            homeScore += 6;
        } else {
            awayScore += 6;
        }
        selQB.gamePassTDs++;
        selWR.gameRecTDs++;
        selQB.statsPassTD++;
        selWR.statsTD++;
        tdInfo = offense.abbr + " QB " + selQB.name + " threw a " + yardsGain + " yard TD to WR " + selWR.name + ".";
    }

    private void passCompletion(Team offense, PlayerQB selQB, PlayerWR selWR, int yardsGain) {
        selQB.statsPassComp++;
        selQB.statsPassYards += yardsGain;
        selQB.gamePassComplete++;
        selQB.gamePassYards += yardsGain;
        selWR.statsReceptions++;
        selWR.statsRecYards += yardsGain;
        selWR.gameReceptions++;
        selWR.gameRecYards += yardsGain;
        offense.teamPassYards += yardsGain;

    }

    private void passAttempt(PlayerQB selQB, PlayerWR selWR) {
        selQB.statsPassAtt++;
        selWR.statsTargets++;
        selQB.gamePassAtempts++;
        selWR.gameTargets++;
    }


    private void passingPlayTE(Team offense, Team defense, PlayerQB selQB, PlayerTE selTE, PlayerDL selDL, PlayerLB selLB, PlayerLB selLB2, PlayerCB selCB, PlayerS selS) {
        int yardsGain = 0;
        boolean gotTD = false;
        boolean gotFumble = false;

        //get how much pressure there is on qb, check if sack
        int pressureOnQB = defense.getCompositeDLPass() * 2 - offense.getCompositeOLPass() - getHFadv() - (int) (Math.random() * coachAdv()) + (defense.teamStratDef.getRunProtection() * 2 - offense.teamStratDef.getRunProtection());  //STRATEGIES
        if (Math.random() * sackValue < pressureOnQB / 8) {
            if (Math.random() * 100 < pressureOnQB / 8 && selQB.ratSpeed > selDL.ratPassRush) {
                RushPlayQB(offense, defense, selQB, selTE, selDL, selLB, selCB, selS);
            } else {
                //sacked!
                String defender;
                double DL = selDL.ratTackle * Math.random() * 100;
                double LB = selLB2.ratTackle * Math.random() * 65;
                double S = selS.ratTackle * Math.random() * 50;
                double CB = selCB.ratTackle * Math.random() * 25;
                if (DL >= S && DL >= CB && DL >= LB) {
                    selDL.gameTackles++;
                    selDL.gameSacks++;
                    selDL.statsTackles++;
                    selDL.statsSacks++;
                    defender = ("DL " + selDL.name);
                } else if (LB > DL && LB >= S && LB >= CB) {
                    selLB2.gameTackles++;
                    selLB2.gameSacks++;
                    selLB2.statsTackles++;
                    selLB2.statsSacks++;
                    defender = ("LB " + selLB2.name);
                } else if (S > DL && S > LB && S >= CB) {
                    selS.gameTackles++;
                    selS.gameSacks++;
                    selS.statsTackles++;
                    selS.statsSacks++;
                    defender = ("S " + selDL.name);
                } else {
                    selCB.gameTackles++;
                    selCB.gameSacks++;
                    selCB.statsTackles++;
                    selCB.statsSacks++;
                    defender = ("CB " + selCB.name);
                }

                qbSack(offense, defense, defender, selQB);
                return;
            }
        }

        //check for int
        double intChance = (pressureOnQB + defense.getS(0).ratOvr - (offense.getQB(0).ratPassAcc + offense.getQB(0).ratFootIQ + 100) / 3) / 18
                - offense.teamStratOff.getPassProtection() + defense.teamStratDef.getPassProtection();   //STRATEGIES
        if (intChance < 0.015) intChance = 0.015;
        if (intValue * Math.random() < intChance) {
            //Interception
            double lb = selLB.ratCoverage * Math.random() * 100;
            double s = selS.ratCoverage * Math.random() * 70;
            double cb = selCB.ratCoverage * Math.random() * 25;
            String defender;

            if (lb >= s & lb >= cb) {
                selLB.gameInterceptions++;
                selLB.statsInts++;
                defender = ("LB " + selLB.name);
            } else if (s > lb && s >= cb) {
                selS.gameInterceptions++;
                selS.statsInts++;
                defender = ("S " + selS.name);
            } else {
                selCB.gameInterceptions++;
                selCB.statsInts++;
                defender = ("CB " + selCB.name);
            }

            qbInterception(offense, defender, selQB);
            return;
        }

        //throw ball, check for completion
        double completion = (getHFadv() + (int) (Math.random() * coachAdv()) + normalize(offense.getQB(0).ratPassAcc) + normalize(selTE.ratCatch)
                - normalize(selLB.ratCoverage)) / 2 + 18.25 - pressureOnQB / 16.8 + offense.teamStratOff.getPassProtection() - defense.teamStratDef.getPassProtection();  //STRATEGIES
        if (100 * Math.random() < completion) {
            if (100 * Math.random() < (100 - selTE.ratCatch) / 3) {
                //drop
                gameDown++;
                selTE.gameDrops++;
                selTE.statsDrops++;
                passAttemptTE(selQB, selTE);
                //Drop ball = inc pass, so run time for the play, stop clock until next play, move on (aka return;)
                gameTime -= timePerPlay * Math.random();
                return;

            } else {
                //no drop
                yardsGain = (int) ((normalize(offense.getQB(0).ratPassPow) + normalize(selTE.ratSpeed) - normalize(selLB.ratSpeed)) * Math.random() / 4.8 //STRATEGIES
                        + offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential());
                //see if receiver can get yards after catch
                double escapeChance = (normalize(selTE.ratEvasion) * 3 - selLB.ratTackle - defense.getS(0).ratOvr) * Math.random()  //STRATEGIES
                        + offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential();
                if (escapeChance > 92 || Math.random() > 0.95) {
                    yardsGain += 3 + (selTE.ratSpeed * Math.random() / 4);
                }
                if (escapeChance > 75 && Math.random() < (0.1 + (offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential()) / 200)) {  //STRATEGIES
                    //wr escapes for TD
                    yardsGain += 100;
                }

                //add yardage
                gameYardLine += yardsGain;
                if (gameYardLine >= 100) { //TD!
                    yardsGain -= gameYardLine - 100;
                    gameYardLine = 100 - yardsGain;
                    addPointsQuarter(6);
                    passingTDTE(offense, selQB, selTE, yardsGain);
                    gotTD = true;
                } else {
                    //check for fumble
                    double fumChance = (defense.getS(0).ratTackle + selLB.ratTackle) / 2;
                    if (100 * Math.random() < fumChance / 50) {
                        //Fumble!
                        gotFumble = true;
                    }
                }

                if (!gotTD && !gotFumble) {
                    //check downs if there wasnt fumble or TD
                    gameYardsNeed -= yardsGain;

                    if (escapeChance <= 80) {
                        selLB.gameTackles++;
                        selLB.statsTackles++;
                    } else if (escapeChance > 80 && escapeChance < 95) {
                        selS.gameTackles++;
                        selS.statsTackles++;
                    } else {
                        selCB.gameTackles++;
                        selCB.statsTackles++;
                    }

                    if (gameYardsNeed <= 0) {
                        // Only set new down and distance if there wasn't a TD
                        gameDown = 1;
                        gameYardsNeed = 10;
                    } else gameDown++;
                }

                //stats management
                passCompletionTE(offense, selQB, selTE, yardsGain);
            }

        } else {
            //no completion, advance downs
            passAttemptTE(selQB, selTE);
            gameDown++;
            //Incomplete pass stops the clock, so just run time for how long the play took, then move on
            gameTime -= timePerPlay * Math.random();
            return;
        }

        passAttemptTE(selQB, selTE);


        if (gotFumble) {

            selTE.gameFumbles++;
            selTE.statsFumbles++;
            String defender;
            if (selS.ratTackle * Math.random() > selLB.ratTackle * Math.random()) {
                selS.gameTackles++;
                selS.gameFumbles++;
                selS.statsTackles++;
                selS.statsFumbles++;
                defender = ("S " + selS.name);
            } else if (selLB.ratTackle * Math.random() > (selCB.ratTackle * Math.random()) * 0.5) {
                selLB.gameTackles++;
                selLB.gameFumbles++;
                selLB.statsTackles++;
                selLB.statsFumbles++;
                defender = ("LB " + selLB.name);
            } else {
                selCB.gameTackles++;
                selCB.gameFumbles++;
                selCB.statsTackles++;
                selCB.statsFumbles++;
                defender = ("CB " + selCB.name);
            }

            gameEventLog += getEventPrefix() + "FUMBLE!\n" + offense.abbr + " TE " + selTE.name + " fumbled the ball after a catch. It was recovered by " + defender + ".";


            if (gamePoss) { // home possession
                homeTOs++;
            } else {
                awayTOs++;
            }
            if (!playingOT) {
                gameDown = 1;
                gameYardsNeed = 10;
                gamePoss = !gamePoss;
                gameYardLine = 100 - gameYardLine;
                gameTime -= timePerPlay * Math.random();
                return;
            } else {
                resetForOT();
                return;
            }
        }

        if (gotTD) {
            gameTime -= timePerPlay * Math.random();
            kickXP(offense, defense);
            if (!playingOT) kickOff(offense);
            else resetForOT();
            return;
        }

        gameTime -= 17 + timePerPlay * Math.random();
    }

    private void passingTDTE(Team offense, PlayerQB selQB, PlayerTE selTE, int yardsGain) {
        if (gamePoss) { // home possession
            homeScore += 6;
        } else {
            awayScore += 6;
        }

        selQB.gamePassTDs++;
        selTE.gamePassTDs++;
        selQB.statsPassTD++;
        selTE.statsRecTD++;

        tdInfo = offense.abbr + " QB " + offense.getQB(0).name + " threw a " + yardsGain + " yard TD to TE " + selTE.name + ".";
    }

    private void passCompletionTE(Team offense, PlayerQB selQB, PlayerTE selTE, int yardsGain) {
        selQB.statsPassComp++;
        selQB.statsPassYards += yardsGain;
        selQB.gamePassComplete++;
        selQB.gamePassYards += yardsGain;

        selTE.statsReceptions++;
        selTE.statsRecYards += yardsGain;
        selTE.gameReceptions++;
        selTE.gameRecYards += yardsGain;

        offense.teamPassYards += yardsGain;
    }

    private void passAttemptTE(PlayerQB selQB, PlayerTE selTE) {
        selQB.statsPassAtt++;
        selQB.gamePassAtempts++;
        selTE.statsTargets++;
        selTE.gameTargets++;
    }


    private void passingPlayRB(Team offense, Team defense, PlayerQB selQB, PlayerRB selRB, PlayerDL selDL, PlayerLB selLB, PlayerLB selLB2, PlayerCB selCB, PlayerS selS) {
        int yardsGain = 0;
        boolean gotTD = false;
        boolean gotFumble = false;

        //get how much pressure there is on qb, check if sack
        int pressureOnQB = defense.getCompositeDLPass() * 2 - offense.getCompositeOLPass() - getHFadv() - (int) (Math.random() * coachAdv()) + (defense.teamStratDef.getPassProtection() - offense.teamStratDef.getPassProtection());   //STRATEGIES
        if (Math.random() * 2*sackValue < pressureOnQB / 8) {
            //sacked!
            String defender;
            double DL = selDL.ratPassRush * Math.random() * 100;
            double LB = selLB2.ratOvr * Math.random() * 65;
            double S = selS.ratOvr * Math.random() * 50;
            double CB = selCB.ratOvr * Math.random() * 25;
            if (DL >= S && DL >= CB && DL >= LB) {
                selDL.gameTackles++;
                selDL.gameSacks++;
                selDL.statsTackles++;
                selDL.statsSacks++;
                defender = ("DL " + selDL.name);
            } else if (LB > DL && LB >= S && LB >= CB) {
                selLB2.gameTackles++;
                selLB2.gameSacks++;
                selLB2.statsTackles++;
                selLB2.statsSacks++;
                defender = ("LB " + selLB2.name);
            } else if (S > DL && S > LB && S >= CB) {
                selS.gameTackles++;
                selS.gameSacks++;
                selS.statsTackles++;
                selS.statsSacks++;
                defender = ("S " + selDL.name);
            } else {
                selCB.gameTackles++;
                selCB.gameSacks++;
                selCB.statsTackles++;
                selCB.statsSacks++;
                defender = ("CB " + selCB.name);
            }

            qbSack(offense, defense, defender, selQB);
            return;
        }

        //check for int
        double intChance = (pressureOnQB + defense.getS(0).ratOvr - (offense.getQB(0).ratPassAcc + offense.getQB(0).ratFootIQ + 100) / 3) / 25
                - offense.teamStratOff.getPassProtection() + defense.teamStratDef.getPassProtection();   //STRATEGIES
        if (intChance < 0.015) intChance = 0.015;
        if (intValue * Math.random() < intChance) {
            //Interception
            double lb = selLB.ratCoverage * Math.random() * 80;
            double s = selS.ratCoverage * Math.random() * 50;
            double dl = selDL.ratPassRush * Math.random() * 100;
            String defender;

            if (lb >= s & lb >= dl) {
                selLB.gameInterceptions++;
                selLB.statsInts++;
                defender = ("LB " + selLB.name);
            } else if (s > lb && s >= dl) {
                selS.gameInterceptions++;
                selS.statsInts++;
                defender = ("S " + selS.name);
            } else {
                selCB.gameInterceptions++;
                selCB.statsInts++;
                defender = ("CB " + selCB.name);
            }

            qbInterception(offense, defender, selQB);
            return;
        }

        //throw ball, check for completion
        double completion = (getHFadv() + (int) (Math.random() * coachAdv()) + normalize(offense.getQB(0).ratPassAcc) + normalize(selRB.ratCatch)
                - normalize(selLB.ratCoverage)) / 2 + 18.25 - pressureOnQB / 16.8 + offense.teamStratOff.getPassProtection() - defense.teamStratDef.getPassProtection();  //STRATEGIES
        if (100 * Math.random() < completion) {
            if (100 * Math.random() < (100 - selRB.ratCatch) / 4) {
                //drop
                gameDown++;
                selRB.gameDrops++;
                passAttemptRB(selQB, selRB);
                //Drop ball = inc pass, so run time for the play, stop clock until next play, move on (aka return;)
                gameTime -= timePerPlay * Math.random();
                return;

            } else {
                //no drop
                yardsGain = (int) ((normalize(offense.getQB(0).ratPassPow) + normalize(selRB.ratSpeed) - normalize(selLB.ratSpeed)) * Math.random() / 4.8 //STRATEGIES
                        + offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential()) - 2;  //subtract 2 for screen pass behind line of scrimmage
                //see if receiver can get yards after catch
                double escapeChance = (normalize(selRB.ratEvasion) * 3 - selLB.ratTackle - defense.getS(0).ratOvr) * Math.random()  //STRATEGIES
                        + offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential();
                if (escapeChance > 91 || Math.random() > 0.94) {
                    yardsGain += 4 + (selRB.ratSpeed * Math.random() / 4);
                }
                if (escapeChance > 75 && Math.random() < (0.1 + (offense.teamStratOff.getPassPotential() - defense.teamStratDef.getPassPotential()) / 200)) {  //STRATEGIES
                    //wr escapes for TD
                    yardsGain += 100;
                }

                //add yardage
                gameYardLine += yardsGain;
                if (gameYardLine >= 100) { //TD!
                    yardsGain -= gameYardLine - 100;
                    gameYardLine = 100 - yardsGain;
                    addPointsQuarter(6);
                    passingTDRB(offense, selQB, selRB, yardsGain);
                    gotTD = true;
                } else {
                    //check for fumble
                    double fumChance = (defense.getS(0).ratTackle + selLB.ratTackle) / 2;
                    if (100 * Math.random() < fumChance / 50) {
                        //Fumble!
                        gotFumble = true;
                    }
                }

                if (!gotTD && !gotFumble) {
                    //check downs if there wasnt fumble or TD
                    gameYardsNeed -= yardsGain;

                    if (escapeChance <= 80) {
                        selLB.gameTackles++;
                        selLB.statsTackles++;
                    } else if (escapeChance > 80 && escapeChance < 95) {
                        selS.gameTackles++;
                        selS.statsTackles++;
                    } else {
                        selCB.gameTackles++;
                        selCB.statsTackles++;
                    }

                    if (gameYardsNeed <= 0) {
                        // Only set new down and distance if there wasn't a TD
                        gameDown = 1;
                        gameYardsNeed = 10;
                    } else gameDown++;
                }

                //stats management
                passCompletionRB(offense, selQB, selRB, yardsGain);
            }

        } else {
            //no completion, advance downs
            passAttemptRB(selQB, selRB);
            gameDown++;
            //Incomplete pass stops the clock, so just run time for how long the play took, then move on
            gameTime -= timePerPlay * Math.random();
            return;
        }

        passAttemptRB(selQB, selRB);


        if (gotFumble) {

            selRB.gameFumbles++;
            selRB.statsFumbles++;
            String defender;
            if (selS.ratTackle * Math.random() > selLB.ratTackle * Math.random()) {
                selS.gameTackles++;
                selS.gameFumbles++;
                selS.statsTackles++;
                selS.statsFumbles++;
                defender = ("S " + selS.name);
            } else if (selLB.ratTackle * Math.random() > (selCB.ratTackle * Math.random()) * 0.5) {
                selLB.gameTackles++;
                selLB.gameFumbles++;
                selLB.statsTackles++;
                selLB.statsFumbles++;
                defender = ("LB " + selLB.name);
            } else {
                selCB.gameTackles++;
                selCB.gameFumbles++;
                selCB.statsTackles++;
                selCB.statsFumbles++;
                defender = ("CB " + selCB.name);
            }

            gameEventLog += getEventPrefix() + "FUMBLE!\n" + offense.abbr + " RB " + selRB.name + " fumbled the ball after a catch. It was recovered by " + defender + ".";


            if (gamePoss) { // home possession
                homeTOs++;
            } else {
                awayTOs++;
            }
            if (!playingOT) {
                gameDown = 1;
                gameYardsNeed = 10;
                gamePoss = !gamePoss;
                gameYardLine = 100 - gameYardLine;
                gameTime -= timePerPlay * Math.random();
                return;
            } else {
                resetForOT();
                return;
            }
        }

        if (gotTD) {
            gameTime -= timePerPlay * Math.random();
            kickXP(offense, defense);
            if (!playingOT) kickOff(offense);
            else resetForOT();
            return;
        }

        gameTime -= 17 + timePerPlay * Math.random();
    }

    private void passingTDRB(Team offense, PlayerQB selQB, PlayerRB selRB, int yardsGain) {
        if (gamePoss) { // home possession
            homeScore += 6;
        } else {
            awayScore += 6;
        }
        selQB.statsPassTD++;
        selQB.gamePassTDs++;
        selRB.statsRecTD++;
        selRB.gameRecTDs++;

        tdInfo = offense.abbr + " QB " + selQB.name + " threw a " + yardsGain + " yard TD to RB " + selRB.name + ".";

    }

    private void passCompletionRB(Team offense, PlayerQB selQB, PlayerRB selRB, int yardsGain) {
        selQB.statsPassComp++;
        selQB.statsPassYards += yardsGain;
        selQB.gamePassComplete++;
        selQB.gamePassYards += yardsGain;

        selRB.statsReceptions++;
        selRB.statsRecYards += yardsGain;
        selRB.gameReceptions++;
        selRB.gameRecYards += yardsGain;

        offense.teamPassYards += yardsGain;
    }

    private void passAttemptRB(PlayerQB selQB, PlayerRB selRB) {
        selQB.statsPassAtt++;
        selQB.gamePassAtempts++;
        selRB.gameTargets++;
    }

    private void qbInterception(Team offense, String defender, PlayerQB selQB) {
        if (gamePoss) { // home possession
            homeTOs++;
        } else {
            awayTOs++;
        }
        selQB.statsInt++;
        selQB.gamePassInts++;

        //Log the event before decreasing the time, in keeping with the standard of other logged plays (TD, Fumble, etc.)
        gameEventLog += getEventPrefix() + "INTERCEPTED!\n" + offense.abbr + " QB " + offense.getQB(0).name + " was intercepted by " + defender + ".";
        //Clock stops after a pick, so just run time off the clock for the play that occurred
        //NOTE: If the ability to run an interception back is ever added, this should be changed to be more time
        gameTime -= timePerPlay * Math.random();
        if (!playingOT) {
            gameDown = 1;
            gameYardsNeed = 10;
            gamePoss = !gamePoss;
            gameYardLine = 100 - gameYardLine;
        } else resetForOT();
    }

    private void qbSack(Team offense, Team defense, String defender, PlayerQB selQB) {
        int sackloss = (3 + (int) (Math.random() * (normalize(defense.getCompositeDLPass()) - normalize(offense.getCompositeOLPass())) / 2));
        if (sackloss < 2) sackloss = 2;
        gameEventLog += getEventPrefix() + "SACK!\n" + " QB " + offense.getQB(0).name +
                " was sacked for a loss of " + sackloss + " by " + defender + ".";
        selQB.statsSacked++;
        selQB.gameSacks++;
        selQB.statsRushYards -= sackloss;
        selQB.gameRushYards -= sackloss;
        gameYardsNeed += sackloss;
        gameYardLine -= sackloss;

        if (gameYardLine < 0) {
            // Safety!
            // Eat some time up for the play that was run, stop it once play is over
            gameTime -= 10 * Math.random();
            safety(defender);
            return; // Run safety then get out of qbSack (safety() will take care of free kick)
        }

        gameDown++; // Advance gameDown after checking for Safety, otherwise game log reports Safety occurring one down later than it did

        //Similar amount of time as rushing, minus some in-play time -- sacks are faster (usually)
        gameTime -= timePerPlay + timePerPlay * Math.random();
    }

    private void safety(String defender) {
        if (gamePoss) {
            awayScore += 2;
            gameEventLog += getEventPrefix() + "SAFETY!\n" + homeTeam.abbr + " QB " + homeTeam.getQB(0).name +
                    " was tackled in the endzone by " + defender + "! Result is a safety and " + awayTeam.abbr + " will get possession.";
            freeKick(homeTeam);
        } else {
            homeScore += 2;
            gameEventLog += getEventPrefix() + "SAFETY!\n" + awayTeam.abbr + " QB " + awayTeam.getQB(0).name +
                    " was tackled in the endzone by " + defender + "! Result is a safety and " + homeTeam.abbr + " will get possession.";
            freeKick(awayTeam);
        }
    }

    //RUSHING PLAYS POST-SNAP **

    private void RushPlayRB(Team offense, Team defense, PlayerRB selRB, PlayerTE selTE, PlayerDL selDL, PlayerLB selLB, PlayerCB selCB, PlayerS selS) {
        boolean gotTD;
        gotTD = false;

        int blockAdv = offense.getCompositeOLRush() - defense.getCompositeDLRush() + (offense.teamStratDef.getRunProtection() - defense.teamStratDef.getRunProtection());  //STRATEGIES
        int blockAdvOutside = selTE.ratRunBlock * 2 - selLB.ratRunStop - selS.ratRunStop + (offense.teamStratDef.getRunUsage() - defense.teamStratDef.getRunUsage());
        int yardsGain = (int) ((selRB.ratSpeed + blockAdv + blockAdvOutside + getHFadv() + (int) (Math.random() * coachAdv())) * Math.random() / 10 + (double) offense.teamStratOff.getRunPotential() / 2 - (double) defense.teamStratDef.getRunPotential() / 2);  //STRATEGIES

        if (yardsGain < 2) {
            yardsGain += selRB.ratRushPower / 20 - 3 - (double) defense.teamStratDef.getRunPotential() / 2;  //STRATEGIES
        } else {
            //break free from tackles
            if (Math.random() < (0.28 + (offense.teamStratOff.getRunPotential() - (double) defense.teamStratDef.getRunPotential() / 2) / 50)) {  //STRATEGIES
                yardsGain += (selRB.ratEvasion - blockAdvOutside) / 5 * Math.random();
            }
        }

        //add yardage
        gameYardLine += yardsGain;

        if (gameYardLine >= 100) { //TD!
            addPointsQuarter(6);
            yardsGain -= gameYardLine - 100;
            gameYardLine = 100 - yardsGain;

            selRB.gameRushTDs++;
            selRB.statsRushTD++;

            if (gamePoss) { // home possession
                homeScore += 6;
            } else {
                awayScore += 6;
            }
            tdInfo = offense.abbr + " RB " + selRB.name + " rushed " + yardsGain + " yards for a TD.";
            gotTD = true;
        }

        //check downs if there wasn't TD
        if (!gotTD) {
            //check downs
            gameYardsNeed -= yardsGain;
            if (gameYardsNeed <= 0) {
                // Only set new down and distance if there wasn't a TD
                gameDown = 1;
                gameYardsNeed = 10;
            } else gameDown++;
        }

        if (yardsGain < 2) {
            selDL.gameTackles++;
            selDL.statsTackles++;
        } else if (yardsGain >= 2 && yardsGain < 9) {
            selLB.gameTackles++;
            selLB.statsTackles++;
        } else {
            if (selCB.ratTackle * Math.random() * 50 >= selS.ratTackle * Math.random() * 100) {
                selCB.gameTackles++;
                selCB.statsTackles++;
            } else {
                selS.gameTackles++;
                selS.statsTackles++;
            }
        }
        //stats management
        rushAttempt(selRB, yardsGain);

        if (gotTD) {
            gameTime -= 5 + timePerPlay * Math.random(); // Clock stops for the TD, just burn time for the play
            kickXP(offense, defense);
            if (!playingOT) kickOff(offense);
            else resetForOT();
        } else {
            gameTime -= timePerPlay + timePerPlay * Math.random();
            //check for fumble
            double fumChance = ((defense.getS(0).ratTackle + selLB.ratTackle) / 2 + defense.getCompositeDLRush() - getHFadv()) / 2 + offense.teamStratOff.getRunProtection();  //STRATEGIES
            if (100 * Math.random() < fumChance / 50) {
                //Fumble!

                selRB.gameFumbles++;
                selRB.statsFumbles++;
                if (gamePoss) {
                    homeTOs++;
                } else {
                    awayTOs++;
                }

                String defender;
                double cb = selCB.ratTackle * Math.random() * 20;
                double s = selS.ratTackle * Math.random() * 70;
                double lb = selLB.ratTackle * Math.random() * 100;

                if (cb > s && cb > lb) {
                    selCB.gameTackles++;
                    selCB.statsTackles++;
                    selCB.gameFumbles++;
                    selCB.statsFumbles++;
                    defender = ("CB " + selCB.name);
                } else if (s >= cb && s > lb) {
                    selS.gameTackles++;
                    selS.statsTackles++;
                    selS.gameFumbles++;
                    selS.statsFumbles++;
                    defender = ("S " + selS.name);
                } else {
                    selLB.gameTackles++;
                    selLB.statsTackles++;
                    selLB.gameFumbles++;
                    selLB.statsFumbles++;
                    defender = ("LB " + selLB.name);
                }

                gameEventLog += getEventPrefix() + "FUMBLE!\n" + offense.abbr + " RB " + selRB.name + " fumbled the ball while rushing and recovered by " + defender + ".";

                if (!playingOT) {
                    gameDown = 1;
                    gameYardsNeed = 10;
                    gamePoss = !gamePoss;
                    gameYardLine = 100 - gameYardLine;
                } else resetForOT();
            }
        }
    }

    private void RushPlayQB(Team offense, Team defense, PlayerQB selQB, PlayerTE selTE, PlayerDL selDL, PlayerLB selLB, PlayerCB selCB, PlayerS selS) {
        boolean gotTD;
        gotTD = false;

        int blockAdv = offense.getCompositeOLRush() - defense.getCompositeDLRush() + (offense.teamStratDef.getRunProtection() - defense.teamStratDef.getRunProtection());  //STRATEGIES
        int blockAdvOutside = selTE.ratRunBlock * 2 - selLB.ratRunStop - selS.ratRunStop + (offense.teamStratDef.getRunUsage() - defense.teamStratDef.getRunUsage());
        int yardsGain = (int) ((selQB.ratSpeed + blockAdv + blockAdvOutside + getHFadv() + (int) (Math.random() * coachAdv())) * Math.random() / 10 + (double) offense.teamStratOff.getRunPotential() / 2 - (double) defense.teamStratDef.getRunPotential() / 2);  //STRATEGIES

        if (yardsGain < 2) {
            yardsGain += selQB.ratEvasion / 20 - 3 - (double) defense.teamStratDef.getRunPotential() / 2;  //STRATEGIES
        } else {
            //break free from tackles
            if (Math.random() < (0.20 + (offense.teamStratOff.getRunPotential() - (double) defense.teamStratDef.getRunPotential() / 2) / 50)) {  //STRATEGIES
                yardsGain += (selQB.ratEvasion - blockAdvOutside) / 5 * Math.random();
            }
        }

        //add yardage
        gameYardLine += yardsGain;

        if (gameYardLine >= 100) { //TD!
            addPointsQuarter(6);
            yardsGain -= gameYardLine - 100;
            gameYardLine = 100 - yardsGain;

            selQB.gameRushTDs++;
            selQB.statsRushTD++;

            if (gamePoss) { // home possession
                homeScore += 6;
            } else {
                awayScore += 6;
            }

            tdInfo = offense.abbr + " QB " + selQB.name + " rushed " + yardsGain + " yards for a TD.";
            gotTD = true;
        }

        //check downs if there wasn't TD
        if (!gotTD) {
            //check downs
            gameYardsNeed -= yardsGain;
            if (gameYardsNeed <= 0) {
                // Only set new down and distance if there wasn't a TD
                gameDown = 1;
                gameYardsNeed = 10;
            } else gameDown++;
        }

        if (yardsGain < 2) {
            selDL.gameTackles++;
            selDL.statsTackles++;
        } else if (yardsGain >= 2 && yardsGain < 9) {
            selLB.gameTackles++;
            selLB.statsTackles++;
        } else {
            if (selCB.ratTackle * Math.random() * 50 >= selS.ratTackle * Math.random() * 100) {
                selCB.gameTackles++;
                selCB.statsTackles++;
            } else {
                selS.gameTackles++;
                selS.statsTackles++;
            }
        }
        //stats management
        rushAttemptQB(selQB, yardsGain);

        if (gotTD) {
            gameTime -= 5 + timePerPlay * Math.random(); // Clock stops for the TD, just burn time for the play
            kickXP(offense, defense);
            if (!playingOT) kickOff(offense);
            else resetForOT();
        } else {
            gameTime -= timePerPlay + timePerPlay * Math.random();
            //check for fumble
            double fumChance = ((defense.getS(0).ratTackle + selLB.ratTackle) / 2 + defense.getCompositeDLRush() - getHFadv()) / 2 + offense.teamStratOff.getRunProtection();  //STRATEGIES
            if (100 * Math.random() < fumChance / 50) {
                //Fumble!

                selQB.gameFumbles++;
                selQB.statsFumbles++;

                if (gamePoss) {
                    homeTOs++;
                } else {
                    awayTOs++;
                }

                String defender;
                double cb = selCB.ratTackle * Math.random() * 20;
                double s = selS.ratTackle * Math.random() * 70;
                double lb = selLB.ratTackle * Math.random() * 100;

                if (cb > s && cb > lb) {
                    selCB.gameTackles++;
                    selCB.statsTackles++;
                    selCB.gameFumbles++;
                    selCB.statsFumbles++;
                    defender = ("CB " + selCB.name);
                } else if (s >= cb && s > lb) {
                    selS.gameTackles++;
                    selS.statsTackles++;
                    selS.gameFumbles++;
                    selS.statsFumbles++;
                    defender = ("S " + selS.name);
                } else {
                    selLB.gameTackles++;
                    selLB.statsTackles++;
                    selLB.gameFumbles++;
                    selLB.statsFumbles++;
                    defender = ("LB " + selLB.name);
                }


                gameEventLog += getEventPrefix() + "FUMBLE!\n" + offense.abbr + " QB " + selQB.name + " fumbled the ball while rushing and recovered by " + defender + ".";

                if (!playingOT) {
                    gameDown = 1;
                    gameYardsNeed = 10;
                    gamePoss = !gamePoss;
                    gameYardLine = 100 - gameYardLine;
                } else resetForOT();
            }
        }
    }

    private void rushAttempt(PlayerRB selRB, int yardsGain) {
        selRB.statsRushAtt++;
        selRB.statsRushYards += yardsGain;
        selRB.gameRushAttempts++;
        selRB.gameRushYards += yardsGain;

        if (gamePoss) { // home possession
            homeTeam.teamRushYards += yardsGain;
        } else {
            awayTeam.teamRushYards += yardsGain;
        }
    }

    private void rushAttemptQB(PlayerQB selQB, int yardsGain) {
        selQB.statsRushAtt++;
        selQB.statsRushYards += yardsGain;
        selQB.gameRushAttempts++;
        selQB.statsRushYards += yardsGain;

        if (gamePoss) { // home possession
            homeTeam.teamRushYards += yardsGain;
        } else {
            awayTeam.teamRushYards += yardsGain;
        }
    }

    private void fieldGoalAtt(Team offense, Team defense) {
        PlayerK selK = offense.getK(0);
        gameYardLine -= 7;


        double fgDistRatio = Math.pow((110 - gameYardLine) / 50, 2);
        double fgAccRatio = Math.pow((110 - gameYardLine) / 50, 1.25);
        double fgDistChance = (getHFadv() + offense.getK(0).ratKickPow - fgDistRatio * 80);
        double fgAccChance = (getHFadv() + offense.getK(0).ratKickAcc - fgAccRatio * 80);

        if (gameTime > 120 || playingOT == false) {
            if (fgDistChance > 20 && fgAccChance * Math.random() > 15) {
                // made the fg
                if (gamePoss) { // home possession
                    homeScore += 3;
                } else {
                    awayScore += 3;
                }
                gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " made the " + (110 - gameYardLine) + " yard FG.";
                addPointsQuarter(3);

                selK.statsFGMade++;
                selK.statsFGAtt++;
                selK.gameFGMade++;
                selK.gameFGAttempts++;

                if (!playingOT) kickOff(offense);
                else resetForOT();

            } else {
                //miss
                gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " missed the " + (110 - gameYardLine) + " yard FG.";
                selK.statsFGAtt++;
                selK.gameFGAttempts++;
                if (!playingOT) {
                    gameYardLine = Math.max(100 - gameYardLine, 20); //Misses inside the 20 = defense takes over on the 20
                    gameDown = 1;
                    gameYardsNeed = 10;
                    if (gamePoss) { // home possession
                    } else {
                    }
                    gamePoss = !gamePoss;
                } else resetForOT();
            }
        } else {
            if (fgDistChance > 20 && fgAccChance * Math.random() > 15 && offense.getK(0).ratPressure > Math.random() * 95) {
                // made the fg
                if (gamePoss) { // home possession
                    homeScore += 3;
                } else {
                    awayScore += 3;
                }
                gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " made the " + (110 - gameYardLine) + " yard FG.";
                addPointsQuarter(3);
                selK.statsFGMade++;
                selK.statsFGAtt++;
                selK.gameFGMade++;
                selK.gameFGAttempts++;

                if (!playingOT) kickOff(offense);
                else resetForOT();

            } else {
                //miss
                gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " missed the " + (110 - gameYardLine) + " yard FG.";
                offense.getK(0).statsFGAtt++;
                if (!playingOT) {
                    gameYardLine = Math.max(100 - gameYardLine, 20); //Misses inside the 20 = defense takes over on the 20
                    gameDown = 1;
                    gameYardsNeed = 10;
                    selK.gameFGAttempts++;
                    gamePoss = !gamePoss;
                } else resetForOT();
            }
        }

        gameTime -= 20;

    }

    private void kickXP(Team offense, Team defense) {
        PlayerK selK = offense.getK(0);

        // No XP/2pt try if the TD puts the bottom OT offense ahead (aka wins the game)
        if (playingOT && bottomOT && (((numOT % 2 == 0) && awayScore > homeScore) || ((numOT % 2 != 0) && homeScore > awayScore))) {
            gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + "\n" + offense.abbr + " wins on a walk-off touchdown!";
        }
        // If a TD is scored as time expires, there is no XP/2pt if the score difference is greater than 2
        else if (!playingOT && gameTime <= 0 && ((homeScore - awayScore > 2) || (awayScore - homeScore > 2))) {
            //Walkoff TD!
            if ((Math.abs(homeScore - awayScore) < 7) && ((gamePoss && homeScore > awayScore) || (!gamePoss && awayScore > homeScore)))
                gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + "\n" + offense.abbr + " wins on a walk-off touchdown!";
                //Just rubbing in the win or saving some pride
            else gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo;
        } else {
            if ((numOT >= 3) || (((gamePoss && (awayScore - homeScore) == 2) || (!gamePoss && (homeScore - awayScore) == 2)) && gameTime < 300)) {
                //go for 2
                boolean successConversion = false;
                if (Math.random() <= 0.50) {
                    //rushing
                    int blockAdv = offense.getCompositeOLRush() - defense.getCompositeDLRush();
                    int yardsGain = (int) ((offense.getRB(0).ratSpeed + blockAdv) * Math.random() / 6);
                    if (yardsGain > 5) {
                        successConversion = true;
                        if (gamePoss) { // home possession
                            homeScore += 2;
                        } else {
                            awayScore += 2;
                        }
                        addPointsQuarter(2);
                        gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + " " + offense.getRB(0).name + " rushed for the 2pt conversion.";
                    } else {
                        gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + " " + offense.getRB(0).name + " stopped at the line of scrimmage, failed the 2pt conversion.";
                    }
                } else {
                    int pressureOnQB = defense.getCompositeDLPass() * 2 - offense.getCompositeOLPass();
                    double completion = (normalize(offense.getQB(0).ratPassAcc) + offense.getWR(0).ratCatch - defense.getCB(0).ratCoverage) / 2 + 25 - pressureOnQB / 20;
                    if (100 * Math.random() < completion) {
                        successConversion = true;
                        if (gamePoss) { // home possession
                            homeScore += 2;
                        } else {
                            awayScore += 2;
                        }
                        addPointsQuarter(2);
                        gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + " " + offense.getQB(0).name + " completed the pass to " + offense.getWR(0).name + " for the 2pt conversion.";
                    } else {
                        gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + " " + offense.getQB(0).name + "'s pass incomplete to " + offense.getWR(0).name + " for the failed 2pt conversion.";
                    }
                }

            } else {
                //kick XP
                if (Math.random() * 100 < 23 + offense.getK(0).ratKickAcc && Math.random() > 0.01) {
                    //made XP
                    if (gamePoss) { // home possession
                        homeScore += 1;
                    } else {
                        awayScore += 1;
                    }
                    gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + " " + offense.getK(0).name + " made the XP.";
                    addPointsQuarter(1);
                    selK.statsXPMade++;
                    selK.gameXPMade++;
                } else {
                    gameEventLog += getEventPrefix() + "TOUCHDOWN!\n" + tdInfo + " " + offense.getK(0).name + " missed the XP.";
                    // missed XP
                }
                selK.statsXPAtt++;
                selK.gameXPAttempts++;
            }
        }
    }

    private void kickOff(Team offense) {
        if (gameTime <= 0) return;
        else {
            //Decide whether to onside kick. Only if losing but within 8 points with < 3 min to go
            if (gameTime < 180 && ((gamePoss && (awayScore - homeScore) <= 8 && (awayScore - homeScore) > 0)
                    || (!gamePoss && (homeScore - awayScore) <= 8 && (homeScore - awayScore) > 0))) {
                // Yes, do onside
                if (offense.getK(0).ratKickFum * Math.random() > 60 || Math.random() < 0.1) {
                    //Success!
                    gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " successfully executes onside kick! " + offense.abbr + " has possession!";
                } else {
                    // Fail
                    gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " failed the onside kick and lost possession.";
                    gamePoss = !gamePoss;
                }
                gameYardLine = 50;
                gameDown = 1;
                gameYardsNeed = 10;

                gameTime -= 4 + 5 * Math.random(); //Onside kicks are very fast, unless there's a weird fight for the ball. Chance to burn a lot of time, odds are you'll burn a little time.
            } else {
                // Just regular kick off
                gameYardLine = (int) (100 - (offense.getK(0).ratKickPow + 20 - 40 * Math.random()));    //FUTURE IMPLEMENTATION: WR speed affect Kick return
                if (gameYardLine <= 0) gameYardLine = 25;
                gameDown = 1;
                gameYardsNeed = 10;
                gamePoss = !gamePoss;
            }

            gameTime -= timePerPlay * Math.random();
        }
    }

    private void freeKick(Team offense) {
        if (gameTime <= 0) return;
        else {
            //Decide whether to onside kick. Only if losing but within 8 points with < 3 min to go
            if (gameTime < 180 && ((gamePoss && (awayScore - homeScore) <= 8 && (awayScore - homeScore) > 0)
                    || (!gamePoss && (homeScore - awayScore) <= 8 && (homeScore - awayScore) > 0))) {
                // Yes, do onside
                if (offense.getK(0).ratKickFum * Math.random() > 60 || Math.random() < 0.1) {
                    //Success!
                    gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " successfully executes onside kick! " + offense.abbr + " has possession!";
                    gameYardLine = 35;
                    gameDown = 1;
                    gameYardsNeed = 10;
                } else {
                    // Fail
                    gameEventLog += getEventPrefix() + offense.abbr + " K " + offense.getK(0).name + " failed the onside kick and lost possession.";
                    gamePoss = !gamePoss;
                    gameYardLine = 65;
                    gameDown = 1;
                    gameYardsNeed = 10;
                }

                gameTime -= 4 + 4 * Math.random(); //Onside kicks are very fast, unless there's a weird fight for the ball. Chance to burn a lot of time, odds are you'll burn a little time.
            } else {
                // Just regular kick off
                gameYardLine = (int) (115 - (offense.getK(0).ratKickPow + 20 - 40 * Math.random()));
                if (gameYardLine <= 0) gameYardLine = 25;
                gameDown = 1;
                gameYardsNeed = 10;
                gamePoss = !gamePoss;
                gameTime -= timePerPlay * Math.random();

            }
        }
    }

    private void puntPlay(Team offense) {
        gameYardLine = (int) (100 - (gameYardLine + offense.getK(0).ratKickPow / 3 + 20 - 10 * Math.random()));
        if (gameYardLine < 0) {
            gameYardLine = 20;
        }
        gameDown = 1;
        gameYardsNeed = 10;
        gamePoss = !gamePoss;

        gameTime -= timePerPlay + timePerPlay * Math.random();
    }


    //CLOCK AND HEALTH MANAGEMENT

    private void addPointsQuarter(int points) {
        if (gamePoss) {
            //home poss
            if (gameTime > 2700) {
                homeQScore[0] += points;
            } else if (gameTime > 1800) {
                homeQScore[1] += points;
            } else if (gameTime > 900) {
                homeQScore[2] += points;
            } else if (numOT == 0) {
                homeQScore[3] += points;
            } else {
                if (3 + numOT < 10) homeQScore[3 + numOT] += points;
                else homeQScore[9] += points;
            }
        } else {
            //away
            if (gameTime > 2700) {
                awayQScore[0] += points;
            } else if (gameTime > 1800) {
                awayQScore[1] += points;
            } else if (gameTime > 900) {
                awayQScore[2] += points;
            } else if (numOT == 0) {
                awayQScore[3] += points;
            } else {
                if (3 + numOT < 10) awayQScore[3 + numOT] += points;
                else awayQScore[9] += points;
            }
        }
    }

    private void resetForOT() {
        if (bottomOT && homeScore == awayScore) {
            //Add some gameFatigue points
            for(int i = 0; i < homeTeam.getAllPlayers().size(); ++i) {
                homeTeam.getAllPlayers().get(i).gameFatigue += 50;
                if (homeTeam.getAllPlayers().get(i).gameFatigue > 100) homeTeam.getAllPlayers().get(i).gameFatigue = 100;
            }
            for(int i = 0; i < awayTeam.getAllPlayers().size(); ++i) {
                awayTeam.getAllPlayers().get(i).gameFatigue += 50;
                if (awayTeam.getAllPlayers().get(i).gameFatigue > 100) awayTeam.getAllPlayers().get(i).gameFatigue = 100;
            }

            gameYardLine = 75;
            gameYardsNeed = 10;
            gameDown = 1;
            numOT++;
            gamePoss = (numOT % 2) == 0;
            gameTime = -1;
            bottomOT = false;
            //runPlay( awayTeam, homeTeam );
        } else if (!bottomOT) {
            //Add some gameFatigue points
            for(int i = 0; i < homeTeam.getAllPlayers().size(); ++i) {
                homeTeam.getAllPlayers().get(i).gameFatigue += 50;
                if (homeTeam.getAllPlayers().get(i).gameFatigue > 100) homeTeam.getAllPlayers().get(i).gameFatigue = 100;
            }
            for(int i = 0; i < awayTeam.getAllPlayers().size(); ++i) {
                awayTeam.getAllPlayers().get(i).gameFatigue += 50;
                if (awayTeam.getAllPlayers().get(i).gameFatigue > 100) awayTeam.getAllPlayers().get(i).gameFatigue = 100;
            }

            gamePoss = !gamePoss;
            gameYardLine = 75;
            gameYardsNeed = 10;
            gameDown = 1;
            gameTime = -1;
            bottomOT = true;
            //runPlay( homeTeam, awayTeam );
        } else {
            // game is not tied after both teams had their chance
            playingOT = false;
        }
    }

    private String convGameTime() {
        if (!playingOT) {
            int qNum = (3600 - gameTime) / 900 + 1;
            int minTime;
            int secTime;
            String secStr;
            if (gameTime <= 0 && numOT <= 0) { // Prevent Q5 1X:XX from displaying in the game log
                return "0:00 Q4";
            } else {
                minTime = (gameTime - 900 * (4 - qNum)) / 60;
                secTime = (gameTime - 900 * (4 - qNum)) - 60 * minTime;
                if (secTime < 10) secStr = "0" + secTime;
                else secStr = "" + secTime;
                return minTime + ":" + secStr + " Q" + qNum;
            }
        } else {
            if (!bottomOT) {
                return "TOP OT" + numOT;
            } else {
                return "BOT OT" + numOT;
            }
        }
    }

    public void quarterCheck() {
        if (gameTime < 2700 && !QT1) {
            QT1 = true;
            //Set Player Fatigue +50
            recoup(true, 1);
            gameTime = 2700;
            gameEventLog += getEventPrefix() + "END OF 1ST QUARTER!";

        } else if (gameTime < 1800 && !QT2) {
            QT2 = true;
            //Set Player Fatigue to 100
            recoup(true, 2);
            gameTime = 1800;
            gameEventLog += getEventPrefix() + "HALFTIME!";
            gamePoss = false;
            kickOff(awayTeam);

        } else if (gameTime < 900 && !QT3) {
            QT3 = true;
            //Set Player Fatigue +50
            recoup(true, 3);
            gameTime = 900;
            gameEventLog += getEventPrefix() + "END OF 3RD QUARTER!";
        }

    }

    public void recoup(boolean endQT, int qt) {
        int gain = fatigueGain;
        if (endQT && qt != 2) gain = (int)Math.random()*35 + 35;
        if (endQT && qt == 2) gain = 100;
        //recoup v2.0
        for(int i=0; i < homeTeam.startersRB; ++i) {
            homeTeam.getRB(i).gameFatigue += gain;
            if (homeTeam.getRB(i).gameFatigue > 100) homeTeam.getRB(i).gameFatigue = 100;
        }
        for(int i=0; i < homeTeam.startersWR; ++i) {
            homeTeam.getWR(i).gameFatigue += gain;
            if (homeTeam.getWR(i).gameFatigue > 100) homeTeam.getWR(i).gameFatigue = 100;
        }
        for(int i=0; i < homeTeam.startersTE; ++i) {
            homeTeam.getTE(i).gameFatigue += gain;
            if (homeTeam.getTE(i).gameFatigue > 100) homeTeam.getTE(i).gameFatigue = 100;
        }
        for(int i=0; i < homeTeam.startersDL; ++i) {
            homeTeam.getDL(i).gameFatigue += gain;
            if (homeTeam.getDL(i).gameFatigue > 100) homeTeam.getDL(i).gameFatigue = 100;
        }
        for(int i=0; i < homeTeam.startersLB; ++i) {
            homeTeam.getLB(i).gameFatigue += gain;
            if (homeTeam.getLB(i).gameFatigue > 100) homeTeam.getLB(i).gameFatigue = 100;
        }
        for(int i=0; i < homeTeam.startersS; ++i) {
            homeTeam.getS(i).gameFatigue += gain;
            if (homeTeam.getS(i).gameFatigue > 100) homeTeam.getS(i).gameFatigue = 100;
        }

    }


    //PREVIEW & BOXSCORE

    public void gameStatistics() {
        Player player;
        homePassingStats = new ArrayList<>();
        awayPassingStats = new ArrayList<>();
        homeRushingStats = new ArrayList<>();
        awayRushingStats = new ArrayList<>();
        homeReceivingStats = new ArrayList<>();
        awayReceivingStats = new ArrayList<>();
        homeKickingStats = new ArrayList<>();
        awayKickingStats = new ArrayList<>();
        homeDefenseStats = new ArrayList<>();
        awayDefenseStats = new ArrayList<>();

        player = homeTeam.getQB(0);
        homePassingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gamePassYards + "," + player.gamePassComplete + "," + player.gamePassAtempts + "," + player.gamePassTDs + "," + player.gamePassInts + "," + player.gameSacks);
        player = awayTeam.getQB(0);
        awayPassingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gamePassYards + "," + player.gamePassComplete + "," + player.gamePassAtempts + "," + player.gamePassTDs + "," + player.gamePassInts + "," + player.gameSacks);


        for (int i = 0; i < homeTeam.getAllPlayers().size(); ++i) {
            if (homeTeam.getAllPlayers().get(i).gameRushAttempts > 0) {
                player = homeTeam.getAllPlayers().get(i);
                homeRushingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameRushYards + "," + player.gameRushAttempts + "," + player.gameRushTDs + "," + player.gameFumbles);
            }
        }

        for (int i = 0; i < awayTeam.getAllPlayers().size(); ++i) {
            if (awayTeam.getAllPlayers().get(i).gameRushAttempts > 0) {
                player = awayTeam.getAllPlayers().get(i);
                awayRushingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameRushYards + "," + player.gameRushAttempts + "," + player.gameRushTDs + "," + player.gameFumbles);
            }
        }

        for (int i = 0; i < homeTeam.getAllPlayers().size(); ++i) {
            if (homeTeam.getAllPlayers().get(i).gameReceptions > 0) {
                player = homeTeam.getAllPlayers().get(i);
                homeReceivingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameRecYards + "," + player.gameReceptions + "," + player.gameTargets + "," + player.gameRecTDs + "," + player.gameDrops);
            }
        }

        for (int i = 0; i < awayTeam.getAllPlayers().size(); ++i) {
            if (awayTeam.getAllPlayers().get(i).gameReceptions > 0) {
                player = awayTeam.getAllPlayers().get(i);
                awayReceivingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameRecYards + "," + player.gameReceptions + "," + player.gameTargets + "," + player.gameRecTDs + "," + player.gameDrops);
            }
        }

        player = homeTeam.getK(0);
        homeKickingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameFGMade + "," + player.gameFGAttempts + "," + player.gameXPMade + "," + player.gameXPAttempts);
        player = awayTeam.getK(0);
        awayKickingStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameFGMade + "," + player.gameFGAttempts + "," + player.gameXPMade + "," + player.gameXPAttempts);

        for (int i = 0; i < homeTeam.getAllPlayers().size(); ++i) {
            if (homeTeam.getAllPlayers().get(i).gameTackles > 0) {
                player = homeTeam.getAllPlayers().get(i);
                homeDefenseStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameTackles + "," + player.gameSacks + "," + player.gameFumbles + "," + player.gameInterceptions + "," + player.gameTargets + "," + player.gameDefended);
            }

        }

        for (int i = 0; i < awayTeam.getAllPlayers().size(); ++i) {
            if (awayTeam.getAllPlayers().get(i).gameTackles > 0) {
                player = awayTeam.getAllPlayers().get(i);
                awayDefenseStats.add(player.getInitialName() + "," + player.team.name + "," + player.position + "," + player.gameTackles + "," + player.gameSacks + "," + player.gameFumbles + "," + player.gameInterceptions + "," + player.gameTargets + "," + player.gameDefended);
            }
        }

    }

    public String[] getGameSummaryStr() {
        DecimalFormat df2 = new DecimalFormat(".##");

        String[] gameSum = new String[19];
        StringBuilder gameL = new StringBuilder();
        StringBuilder gameC = new StringBuilder();
        StringBuilder gameR = new StringBuilder();

        gameL.append("\nPoints\nYards\nPass Yards\nRush Yards\nTOs\n");
        gameC.append("#" + awayTeam.rankTeamPollScore + " " + awayTeam.abbr + "\n" + awayScore + "\n" + awayYards + " yds\n" +
                awayPassYards + " pyds\n" + awayRushYards + " ryds\n" + awayTOs + " TOs\n");
        gameR.append("#" + homeTeam.rankTeamPollScore + " " + homeTeam.abbr + "\n" + homeScore + "\n" + homeYards + " yds\n" +
                homePassYards + " pyds\n" + homeRushYards + " ryds\n" + homeTOs + " TOs\n");

        StringBuilder gamePL = new StringBuilder();
        StringBuilder gamePC = new StringBuilder();
        StringBuilder gamePR = new StringBuilder();

        gamePL.append("\n");
        gamePC.append("[PASSING]\n");
        gamePR.append("\n");
        gamePL.append("\n");
        gamePC.append("\n");
        gamePR.append("\n");

        if(homePassingStats.size() >= awayPassingStats.size()) {
            for(int i = 0; i < homePassingStats.size(); ++i) {
                gamePL.append("QB:" + "\nYards:" + "\nComp/Att:" +  "\nPass TD:" + "\nInterceptions:" + "\nSacks:" + "\nRating:" + "\n\n" );
            }
        } else {
            for(int i = 0; i < awayPassingStats.size(); ++i) {
                gamePL.append("QB:" + "\nYards:" + "\nComp/Att:" +  "\nPass TD:" + "\nInterceptions:" + "\nSacks:" + "\nRating:" + "\n\n" );
            }
        }

        for(int i = 0; i < homePassingStats.size(); ++i) {
            String[] stats = homePassingStats.get(i).split(",");
            gamePC.append(stats[0] + "\n" +  stats[3] + "\n" + stats[4] + "/" + stats[5] + "\n" + stats[6] + "\n" + stats[7] +"\n" + stats[8] + "\n" + getPasserRating( Integer.parseInt(stats[3]), Integer.parseInt(stats[6]), Integer.parseInt(stats[4]), Integer.parseInt(stats[5]), Integer.parseInt(stats[7]) ) + "\n\n" );
        }
        for(int i = 0; i < awayPassingStats.size(); ++i) {
            String[] stats = awayPassingStats.get(i).split(",");
            gamePR.append(stats[0] + "\n" +  stats[3] + "\n" + stats[4] + "/" + stats[5] + "\n" + stats[6] + "\n" + stats[7] +"\n" + stats[8] + "\n" + getPasserRating( Integer.parseInt(stats[3]), Integer.parseInt(stats[6]), Integer.parseInt(stats[4]), Integer.parseInt(stats[5]), Integer.parseInt(stats[7]) ) + "\n\n" );
        }


        StringBuilder gameRL = new StringBuilder();
        StringBuilder gameRC = new StringBuilder();
        StringBuilder gameRR = new StringBuilder();

        gameRL.append("\n");
        gameRC.append("[RUSHING]\n");
        gameRR.append("\n");
        gameRL.append("\n");
        gameRC.append("\n");
        gameRR.append("\n");
        
        if(homeRushingStats.size() >= awayRushingStats.size()) {
            for(int i = 0; i < homeRushingStats.size(); ++i) {
                gameRL.append("Name:" + "\nPosition:" + "\nYards:" + "\nCarries:" +  "\nYards/Carry:" + "\nTouchdowns:" + "\nFumbles:" + "\n\n" );
            }
        } else {
            for(int i = 0; i < awayRushingStats.size(); ++i) {
                gameRL.append("Name:" + "\nPosition:" + "\nYards:" + "\nCarries:" +  "\nYards/Carry:" + "\nTouchdowns:" + "\nFumbles:" + "\n\n" );
            }
        }

        for(int i = 0; i < homeRushingStats.size(); ++i) {
            String[] stats = homeRushingStats.get(i).split(",");
            gameRC.append(stats[0] + "\n" +  stats[2] + "\n" + stats[3] + "\n" + stats[4] + "\n" + df2.format((Double.parseDouble(stats[3])/Double.parseDouble(stats[4]))) + "\n" + stats[5] +"\n" + stats[6] + "\n\n" );
        }
        for(int i = 0; i < awayRushingStats.size(); ++i) {
            String[] stats = awayRushingStats.get(i).split(",");
            gameRR.append(stats[0] + "\n" +  stats[2] + "\n" + stats[3] + "\n" + stats[4] + "\n" + df2.format((Double.parseDouble(stats[3])/Double.parseDouble(stats[4]))) + "\n" + stats[5] +"\n" + stats[6] + "\n\n" );
        }


        StringBuilder gameWL = new StringBuilder();
        StringBuilder gameWC = new StringBuilder();
        StringBuilder gameWR = new StringBuilder();

        gameWL.append("\n");
        gameWC.append("[RECEIVING]\n");
        gameWR.append("\n");
        gameWL.append("\n");
        gameWC.append("\n");
        gameWR.append("\n");

        if(homeReceivingStats.size() >= awayReceivingStats.size()) {
            for(int i = 0; i < homeReceivingStats.size(); ++i) {
                gameWL.append("Name:" + "\nPosition:" + "\nYards:" + "\nReceptions:" +  "\nYards/Rec:" + "\nRec/Targets:" + "\nTouchdowns:"  + "\nDrops:" + "\n\n" );
            }
        } else {
            for(int i = 0; i < awayReceivingStats.size(); ++i) {
                gameWL.append("Name:" + "\nPosition:" + "\nYards:" + "\nReceptions:" +  "\nYards/Rec:" + "\nRec/Targets:" + "\nTouchdowns:"  + "\nDrops:" + "\n\n" );
            }
        }

        for(int i = 0; i < homeReceivingStats.size(); ++i) {
            String[] stats = homeReceivingStats.get(i).split(",");
            gameWC.append(stats[0] + "\n" +  stats[2] + "\n" + stats[3] + "\n" + stats[4] + "\n" + df2.format(getRecYardsperCatch(Double.parseDouble(stats[3]), Double.parseDouble(stats[4]))) + "\n" + stats[4] + "/" + stats[5] + "\n" + stats[6] +"\n" + stats[7] + "\n\n" );
        }
        for(int i = 0; i < awayReceivingStats.size(); ++i) {
            String[] stats = awayReceivingStats.get(i).split(",");
            gameWR.append(stats[0] + "\n" +  stats[2] + "\n" + stats[3] + "\n" + stats[4] + "\n" + df2.format(getRecYardsperCatch(Double.parseDouble(stats[3]), Double.parseDouble(stats[4]))) + "\n" + stats[4] + "/" + stats[5] + "\n" + stats[6] +"\n" + stats[7] + "\n\n" );
        }

        StringBuilder gameDL = new StringBuilder();
        StringBuilder gameDC = new StringBuilder();
        StringBuilder gameDR = new StringBuilder();

        gameDL.append("\n");
        gameDC.append("[DEFENDING]\n");
        gameDR.append("\n");
        gameDL.append("\n");
        gameDC.append("\n");
        gameDR.append("\n");

        if(homeDefenseStats.size() >= awayDefenseStats.size()) {
            for(int i = 0; i < homeDefenseStats.size(); ++i) {
                gameDL.append("Name:" + "\nPosition:" + "\nTackles:" + "\nSacks:" +  "\nFumbles:" + "\nInterceptions:" + "\nDefended:" + "\n\n" );
            }
        } else {
            for(int i = 0; i < awayDefenseStats.size(); ++i) {
                gameDL.append("Name:" + "\nPosition:" + "\nTackles:" + "\nSacks:" +  "\nFumbles:" + "\nInterceptions:" + "\nDefended:" + "\n\n" );
            }
        }

        for(int i = 0; i < homeDefenseStats.size(); ++i) {
            String[] stats = homeDefenseStats.get(i).split(",");
            gameDC.append(stats[0] + "\n" +  stats[2] + "\n" + stats[3] + "\n" + stats[4] + "\n" + stats[5] + "\n" + stats[6] + "\n" + stats[8] + "\n\n" );
        }
        for(int i = 0; i < awayDefenseStats.size(); ++i) {
            String[] stats = awayDefenseStats.get(i).split(",");
            gameDR.append(stats[0] + "\n" +  stats[2] + "\n" + stats[3] + "\n" + stats[4] + "\n" + stats[5] + "\n" + stats[6] + "\n" + stats[8] + "\n\n" );
        }
        
        StringBuilder gameKL = new StringBuilder();
        StringBuilder gameKC = new StringBuilder();
        StringBuilder gameKR = new StringBuilder();
        
        gameKL.append("\n");
        gameKC.append("[KICKING]\n");
        gameKR.append("\n");
        gameKL.append("\n");
        gameKC.append("\n");
        gameKR.append("\n");


        if(homeKickingStats.size() >= awayKickingStats.size()) {
            for(int i = 0; i < homeKickingStats.size(); ++i) {
                gameKL.append("Name:" + "\nFG Made:" + "\nFG Attempted:" +  "\nXP Made:" + "\nXP Attempted:" + "\n\n" );
            }
        } else {
            for(int i = 0; i < awayKickingStats.size(); ++i) {
                gameKL.append("Name:" + "\nFG Made:" + "\nFG Attempted:" +  "\nXP Made:" + "\nXP Attempted:" + "\n\n" );
            }
        }

        for(int i = 0; i < homeKickingStats.size(); ++i) {
            String[] stats = homeKickingStats.get(i).split(",");
            gameKC.append(stats[0] + "\n" +  stats[3] + "\n" + stats[4] + "\n" + stats[5] + "\n" + stats[6] + "\n\n" );
        }
        for(int i = 0; i < awayKickingStats.size(); ++i) {
            String[] stats = awayKickingStats.get(i).split(",");
            gameKR.append(stats[0] + "\n" +  stats[3] + "\n" + stats[4] + "\n" + stats[5] + "\n" + stats[6] + "\n\n" );
        }

        //Send data

        gameSum[0] = gameL.toString();
        gameSum[1] = gameC.toString();
        gameSum[2] = gameR.toString();

        gameSum[3] = gamePL.toString();
        gameSum[4] = gamePC.toString();
        gameSum[5] = gamePR.toString();

        gameSum[6] = gameRL.toString();
        gameSum[7] = gameRC.toString();
        gameSum[8] = gameRR.toString();

        gameSum[9] = gameWL.toString();
        gameSum[10] = gameWC.toString();
        gameSum[11] = gameWR.toString();

        gameSum[12] = gameDL.toString();
        gameSum[13] = gameDC.toString();
        gameSum[14] = gameDR.toString();

        gameSum[15] = gameKL.toString();
        gameSum[16] = gameKC.toString();
        gameSum[17] = gameKR.toString();

        gameSum[18] = getPlaybyPlay();



        return gameSum;

    }

    public String getPlaybyPlay(){
        return "GAME PLAY-BY-PLAY LOG\n" + gameEventLog;
    }

    public String[] getGameScoutStr() {
        /*
          [0] is left side
          [1] is center
          [2] is right
          [3] is bottom (will be empty for scouting)
         */
        String[] gameSum = new String[4];
        StringBuilder gameL = new StringBuilder();
        StringBuilder gameC = new StringBuilder();
        StringBuilder gameR = new StringBuilder();

        gameL.append("Ranking\nRecord\nPPG\nOpp PPG\nYPG\nOpp YPG\n" +
                "\nPass YPG\nRush YPG\nOpp PYPG\nOpp RYPG\n\nOff Talent\nDef Talent\nPrestige\nOffense\nDefense");
        int g = awayTeam.numGames();
        Team t = awayTeam;
        gameC.append("#" + t.rankTeamPollScore + " " + t.abbr + "\n" + t.wins + "-" + t.losses + "\n" +
                t.teamPoints / g + " (" + t.rankTeamPoints + ")\n" + t.teamOppPoints / g + " (" + t.rankTeamOppPoints + ")\n" +
                t.teamYards / g + " (" + t.rankTeamYards + ")\n" + t.teamOppYards / g + " (" + t.rankTeamOppYards + ")\n\n" +
                t.teamPassYards / g + " (" + t.rankTeamPassYards + ")\n" + t.teamRushYards / g + " (" + t.rankTeamRushYards + ")\n" +
                t.teamOppPassYards / g + " (" + t.rankTeamOppPassYards + ")\n" + t.teamOppRushYards / g + " (" + t.rankTeamOppRushYards + ")\n\n" +
                t.teamOffTalent + " (" + t.rankTeamOffTalent + ")\n" + t.teamDefTalent + " (" + t.rankTeamDefTalent + ")\n" +
                t.teamPrestige + " (" + t.rankTeamPrestige + ")\n" + t.teamStratOff.getStratName() + "\n" + t.teamStratDef.getStratName() + "\n");
        g = homeTeam.numGames();
        t = homeTeam;
        gameR.append("#" + t.rankTeamPollScore + " " + t.abbr + "\n" + t.wins + "-" + t.losses + "\n" +
                t.teamPoints / g + " (" + t.rankTeamPoints + ")\n" + t.teamOppPoints / g + " (" + t.rankTeamOppPoints + ")\n" +
                t.teamYards / g + " (" + t.rankTeamYards + ")\n" + t.teamOppYards / g + " (" + t.rankTeamOppYards + ")\n\n" +
                t.teamPassYards / g + " (" + t.rankTeamPassYards + ")\n" + t.teamRushYards / g + " (" + t.rankTeamRushYards + ")\n" +
                t.teamOppPassYards / g + " (" + t.rankTeamOppPassYards + ")\n" + t.teamOppRushYards / g + " (" + t.rankTeamOppRushYards + ")\n\n" +
                t.teamOffTalent + " (" + t.rankTeamOffTalent + ")\n" + t.teamDefTalent + " (" + t.rankTeamDefTalent + ")\n" +
                t.teamPrestige + " (" + t.rankTeamPrestige + ")\n" + t.teamStratOff.getStratName() + "\n" + t.teamStratDef.getStratName() + "\n");

        gameSum[0] = gameL.toString();
        gameSum[1] = gameC.toString();
        gameSum[2] = gameR.toString();

        StringBuilder gameScout = new StringBuilder();
        if (awayTeam.playersInjuredAll != null && !awayTeam.playersInjuredAll.isEmpty()) {
            Collections.sort(awayTeam.playersInjuredAll, new CompPlayerPosition());
            gameScout.append("\n" + awayTeam.abbr + " Injury Report:\n");
            for (Player p : awayTeam.playersInjuredAll) {
                gameScout.append(p.getPosNameYrOvrPot_OneLine() + "\n");
            }
        }
        if (homeTeam.playersInjuredAll != null && !homeTeam.playersInjuredAll.isEmpty()) {
            Collections.sort(homeTeam.playersInjuredAll, new CompPlayerPosition());
            gameScout.append("\n" + homeTeam.abbr + " Injury Report:\n");
            for (Player p : homeTeam.playersInjuredAll) {
                gameScout.append(p.getPosNameYrOvrPot_OneLine() + "\n");
            }
        }

        gameSum[3] = gameScout.toString();

        return gameSum;
    }

    private String getEventPrefix() {
        String possStr;
        if (gamePoss) possStr = homeTeam.abbr;
        else possStr = awayTeam.abbr;
        String yardsNeedAdj = "" + gameYardsNeed;
        if (gameYardLine + gameYardsNeed >= 100) yardsNeedAdj = "Goal";
        int gameDownAdj;
        if (gameDown > 4) {
            gameDownAdj = 4;
        } else {
            gameDownAdj = gameDown;
        }
        return "\n\n" + homeTeam.abbr + " " + homeScore + " - " + awayScore + " " + awayTeam.abbr + ",   Time: " + convGameTime() +
                "\n\t" + possStr + " " + gameDownAdj + " and " + yardsNeedAdj + " at " + gameYardLine + " yard line." + "\n";
    }

    public int getPassYards(boolean ha) {
        //ha = home/away, false for home, true for away
        int yards = 0;
        if (!ha) {
            for (int i = 0; i < homeTeam.teamQBs.size(); ++i) {
                yards += homeTeam.getQB(i).gamePassYards;
            }
            return yards;
        } else {
            for (int i = 0; i < awayTeam.teamQBs.size(); ++i) {
                yards += awayTeam.getQB(i).gamePassYards;
            }
            return yards;
        }
    }

    public int getRushYards(boolean ha) {
        //ha = home/away, false for home, true for away
        int yards = 0;
        if (!ha) {
            for (int i = 0; i < homeTeam.teamRBs.size(); ++i) {
                yards += homeTeam.getRB(i).gameRushYards;
            }
            for (int i = 0; i < homeTeam.teamQBs.size(); ++i) {
                yards += homeTeam.getQB(i).gameRushYards;
            }
            return yards;
        } else {
            for (int i = 0; i < awayTeam.teamRBs.size(); ++i) {
                yards += awayTeam.getRB(i).gameRushYards;
            }
            for (int i = 0; i < awayTeam.teamQBs.size(); ++i) {
                yards += awayTeam.getQB(i).gameRushYards;
            }
            return yards;
        }
    }

    public int getPasserRating(int yards, int td, int comp, int att, int ints) {
        int rating;
        if (att < 1) {
            return 0;
        } else {
            rating = (int) (((8.4 * yards+ (300 * td) + (100 * comp) - (200 * ints)) / att));
            return rating;
        }
    }

    public double getRecYardsperCatch(double yards, double rec) {
        double rating;
        if (rec < 1) {
            return 0;
        } else {
            rating = yards / rec;
            return rating;
        }
    }


    public void addNewsStory() {

        //Weekly Scoreboard Update
        homeTeam.league.weeklyScores.get(homeTeam.league.currentWeek + 1).add(gameName + ">#" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " " + awayScore + "\n" + "#" + homeTeam.rankTeamPollScore + " " + homeTeam.name + " " + homeScore);


        if (numOT >= 3) {
            // Thriller in OT
            Team winner, loser;
            int winScore, loseScore;
            if (awayScore > homeScore) {
                winner = awayTeam;
                loser = homeTeam;
                winScore = awayScore;
                loseScore = homeScore;
            } else {
                winner = homeTeam;
                loser = awayTeam;
                winScore = homeScore;
                loseScore = awayScore;
            }

            homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add(
                    numOT + "OT Thriller!>" + winner.getStrAbbrWL() + " and " + loser.getStrAbbrWL() + " played an absolutely thrilling game " +
                            "that went to " + numOT + " overtimes, with " + winner.name + " finally emerging victorious " + winScore + " to " + loseScore + ".");
        } else if (homeScore > awayScore && awayTeam.losses == 1 && awayTeam.league.currentWeek > 5) {
            // 5-0 or better team given first loss
            awayTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add(
                    "Undefeated no more! " + awayTeam.name + " suffers first loss!" +
                            ">" + homeTeam.getStrAbbrWL() + " hands " + awayTeam.getStrAbbrWL() +
                            " their first loss of the season, winning " + homeScore + " to " + awayScore + ".");
        } else if (awayScore > homeScore && homeTeam.losses == 1 && homeTeam.league.currentWeek > 5) {
            // 5-0 or better team given first loss
            homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add(
                    "Undefeated no more! " + homeTeam.name + " suffers first loss!" +
                            ">" + awayTeam.getStrAbbrWL() + " hands " + homeTeam.getStrAbbrWL() +
                            " their first loss of the season, winning " + awayScore + " to " + homeScore + ".");
        } else if (awayScore > homeScore && homeTeam.rankTeamPollScore < 20 &&
                (awayTeam.rankTeamPollScore - homeTeam.rankTeamPollScore) > 20) {
            // Upset!
            awayTeam.league.newsStories.get(awayTeam.league.currentWeek + 1).add(
                    "Upset! " + awayTeam.getStrAbbrWL() + " beats " + homeTeam.getStrAbbrWL() +
                            ">#" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " was able to pull off the upset on the road against #" +
                            homeTeam.rankTeamPollScore + " " + homeTeam.name + ", winning " + awayScore + " to " + homeScore + ".");
        } else if (homeScore > awayScore && awayTeam.rankTeamPollScore < 20 &&
                (homeTeam.rankTeamPollScore - awayTeam.rankTeamPollScore) > 20) {
            // Upset!
            homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add(
                    "Upset! " + homeTeam.getStrAbbrWL() + " beats " + awayTeam.getStrAbbrWL() +
                            ">#" + homeTeam.rankTeamPollScore + " " + homeTeam.name + " was able to pull off the upset at home against #" +
                            awayTeam.rankTeamPollScore + " " + awayTeam.name + ", winning " + homeScore + " to " + awayScore + ".");
        }
        if (homeTeam.league.currentWeek < 12) {
            if (awayTeam.rankTeamPollScore < 11 && homeTeam.rankTeamPollScore < 11) {
                if (awayScore > homeScore) {
                    homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add("#" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " defeats #" +
                            homeTeam.rankTeamPollScore + " " + homeTeam.name + ">" + awayTeam.getStrAbbrWL() + " went on the road and beat " + homeTeam.getStrAbbrWL() + " today, " + awayScore + " - " + homeScore + ", in the Game of the Week.");
                } else {
                    homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add("#" + homeTeam.rankTeamPollScore + " " + homeTeam.name + " defeats #" +
                            awayTeam.rankTeamPollScore + " " + awayTeam.name + ">" + homeTeam.getStrAbbrWL() + " defeated " + awayTeam.getStrAbbrWL() + " at home today, " + homeScore + " - " + awayScore + ", in an important game of the season between two Top 10 schools.");
                }
            } else if (awayTeam.rankTeamPollScore < 26 && homeTeam.rankTeamPollScore < 26) {
                if (awayScore > homeScore) {
                    homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add("#" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " defeats #" +
                            homeTeam.rankTeamPollScore + " " + homeTeam.name + ">" + awayTeam.getStrAbbrWL() + " defeated " + homeTeam.getStrAbbrWL() + " today, " + awayScore + " - " + homeScore + ", in a battle of two Top 25 ranked teams.");
                } else {
                    homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add("#" + homeTeam.rankTeamPollScore + " " + homeTeam.name + " defeats #" +
                            awayTeam.rankTeamPollScore + " " + awayTeam.name + ">" + homeTeam.getStrAbbrWL() + " defeated " + awayTeam.getStrAbbrWL() + " at home today, " + homeScore + " - " + awayScore + ", in one of the big match-ups of the week.");
                }
            }
        }
    }

    public void addUpcomingGames(Team name) {
        if (name == awayTeam) {
            if (awayTeam.rankTeamPollScore < 11 && homeTeam.rankTeamPollScore < 11) {
                homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add("Upcoming Game: #" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " vs #" +
                        homeTeam.rankTeamPollScore + " " + homeTeam.name + ">The premier game of the week has " + awayTeam.getStrAbbrWL() + " visiting " + homeTeam.getStrAbbrWL() + ", as these two Top Ten teams fight for a crucial playoff spot. " +
                        awayTeam.name + " plays a " + awayTeam.teamStratOff.getStratName() + " offense, which is averaging " + (awayTeam.teamYards / awayTeam.numGames()) + " yards per game. " + homeTeam.name + " plays a " +
                        homeTeam.teamStratOff.getStratName() + " offense, averaging " + (homeTeam.teamYards / homeTeam.numGames()) + " yards per game.");
            } else if (awayTeam.rankTeamPollScore < 26 && homeTeam.rankTeamPollScore < 26) {
                homeTeam.league.newsStories.get(homeTeam.league.currentWeek + 1).add("Upcoming Game: #" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " vs #" +
                        homeTeam.rankTeamPollScore + " " + homeTeam.name + ">Next week, " + awayTeam.getStrAbbrWL() + " visits " + homeTeam.getStrAbbrWL() + " in a battle of two ranked schools. " +
                        awayTeam.name + " plays a " + awayTeam.teamStratOff.getStratName() + " offense, which is averaging " + (awayTeam.teamYards / awayTeam.numGames()) + " yards per game. " + homeTeam.name + " plays a " +
                        homeTeam.teamStratOff.getStratName() + " offense, averaging " + (homeTeam.teamYards / homeTeam.numGames()) + " yards per game.");
            }
        }
    }

    public void addNewSeasonGames(Team name) {
        if (name == awayTeam) {
            if (awayTeam.rankTeamPollScore < 11 && homeTeam.rankTeamPollScore < 11) {
                homeTeam.league.newsStories.get(0).add("Kick-Off: #" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " vs #" +
                        homeTeam.rankTeamPollScore + " " + homeTeam.name + ">The season kicks off with an exciting game between vistors " + awayTeam.name + " and home team, " + homeTeam.name + ". " +
                        "These two teams are in the pre-season Top Ten, and both are expected to have big seasons this year. " + awayTeam.name + " plays a " + awayTeam.teamStratOff.getStratName() + " offense, while " + homeTeam.name + " plays a " +
                        homeTeam.teamStratOff.getStratName() + " offense.");

            } else if (awayTeam.rankTeamPollScore < 26 && homeTeam.rankTeamPollScore < 26) {
                homeTeam.league.newsStories.get(0).add("Kick-Off: #" + awayTeam.rankTeamPollScore + " " + awayTeam.name + " vs #" +
                        homeTeam.rankTeamPollScore + " " + homeTeam.name + ">The " + homeTeam.league.getYear() + " season starts off with " + awayTeam.name + " visiting " + homeTeam.name
                        + " in one of the interesting early season games pitting two ranked teams. " + awayTeam.name + " plays a " + awayTeam.teamStratOff.getStratName() + " offense, while " + homeTeam.name + " plays a " +
                        homeTeam.teamStratOff.getStratName() + " offense.");
            }
        }
    }

    private int normalize(int rating) {
        return (100 + rating) / 2;
    }


}
