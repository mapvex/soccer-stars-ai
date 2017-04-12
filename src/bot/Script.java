package bot;

import bot.misc.Helper;
import bot.simulation.*;
import bot.simulation.objects.Movable;

import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class Script {
    public Bot bot;
    public SimulationRunner guiSimulationRunner;

    public Script(Bot bot) {
        this.bot = bot;
    }

    public void turn() {
        bot.overlay.setStatusText("Calculating...");

        SimulationRunner runner = new SimulationRunner(null, bot.xCoordinates, bot.yCoordinates, Math.toRadians(0.0), Math.toRadians(360.0), Math.toRadians(1.0));
        runner.run();

        System.out.println("Results: " + runner.results.size());

        Vector<SimulationResult> scoringMoves = new Vector<>();
        Vector<SimulationResult> defensiveMoves = new Vector<>();
        Vector<SimulationResult> ballInBadPositionForEnemyMoves = new Vector<>();
        Vector<SimulationResult> ballNotNearOurGoalMoves = new Vector<>();
        int currentAmountOfDefensivePucks = getCurrentAmountOfDefensivePucks();

        for (SimulationResult result : runner.results) {
            // Found a move that could potentially score
            if (result.scored) {
                scoringMoves.add(result);
            }

            // Found a move that would increase our defense?
            if (result.defendingPucks > currentAmountOfDefensivePucks) {
                defensiveMoves.add(result);
            }

            // Found a move that puts the ball in a bad location for the enemy?
            if (result.ballNearEnemyWall) {
                ballInBadPositionForEnemyMoves.add(result);
            }

            // Found a move where the ball is not near our goal
            if (result.ballX > -Constants.FIELD_WIDTH / 2.0 + 50.0) {
                ballNotNearOurGoalMoves.add(result);
            }
        }

        SimulationResult chosenResult = null;
        if (!bot.isFirstShot() && scoringMoves.size() > 0) {
            // there is a shot which we could potentially score with
            chosenResult = getMostLikelyScoringMoveFromList(scoringMoves);
            bot.overlay.setStatusText("Scoring...");
        } else if (!bot.isFirstShot() && !Helper.areCoordinatesNearBadPosition(bot.xCoordinates[0], bot.yCoordinates[0]) && ballInBadPositionForEnemyMoves.size() > 0) {
            // can put ball in a bad position for the enemy at least
            chosenResult = getRandomMoveFromList(ballInBadPositionForEnemyMoves);
            bot.overlay.setStatusText("Putting ball in a bad location...");
        } else if (isBallNearOurGoal()) {
            // need to get rid of that ball
            chosenResult = getBestBallNotNearOurGoalMove(ballNotNearOurGoalMoves);
            bot.overlay.setStatusText("Moving ball away from goal...");
        } else if (defensiveMoves.size() > 0) {
            // there is a move which can fortify our defenses
            chosenResult = getBestDefensiveMoveFromList(defensiveMoves);
            bot.overlay.setStatusText("Defending...");
        }

        if (chosenResult == null) {
            bot.overlay.setStatusText("Skipping move...");
            return;
        }
        bot.overlay.repaint();

        System.out.println("Shot taken while packet " + bot.netWatcher.packetSequenceNr + " is with puck " + chosenResult.puckId +
                " and angle: " + Math.toDegrees(chosenResult.angle) + "(" + chosenResult.angle + ").");
        takeShotWithPuck(chosenResult.puckId, chosenResult.power, chosenResult.angle);
    }

    public SimulationResult getBestBallNotNearOurGoalMove(Vector<SimulationResult> pool) {
        SimulationResult bestResult = null;
        double bestScore = 0.0;
        for (SimulationResult currentResult : pool) {
            double currentScore = currentResult.ballX;
            if (currentScore > bestScore) {
                bestResult = currentResult;
                bestScore = currentScore;
            }
        }

        return bestResult;
    }

    public SimulationResult getBestDefensiveMoveFromList(Vector<SimulationResult> pool) {
        SimulationResult bestResult = null;
        double bestScore = 0.0;
        for (SimulationResult currentResult : pool) {
            double currentScore = 1.0 * currentResult.defendingPucks;
            if (currentScore > bestScore) {
                bestResult = currentResult;
                bestScore = currentScore;
            }
        }

        return bestResult;
    }

    public SimulationResult getMostLikelyScoringMoveFromList(Vector<SimulationResult> pool) {
        SimulationResult bestResult = null;
        double bestScore = 0.0;
        for (SimulationResult currentResult : pool) {
            double currentScore = 1.0 / currentResult.collisions;
            if (currentScore > bestScore) {
                bestResult = currentResult;
                bestScore = currentScore;
            }
        }

        return bestResult;
    }

    public SimulationResult getRandomMoveFromList(Vector<SimulationResult> pool) {
        int index = ThreadLocalRandom.current().nextInt(0, pool.size());
        return pool.get(index);
    }

    public boolean isBallNearOurGoal() {
        return bot.xCoordinates[0] < -Constants.FIELD_WIDTH + 70.0;
    }

    public void takeShotWithPuck(int puckId, double power, double angle) {
        int screenX = bot.helper.toScreenX(bot.xCoordinates[puckId]);
        int screenY = bot.helper.toScreenY(bot.yCoordinates[puckId]);
        bot.helper.takeShot(screenX, screenY, power * 5.0, angle);
    }

    public int getCurrentAmountOfDefensivePucks() {
        int movablesInGoalBox = 0;

        for (int i = 1; i < bot.xCoordinates.length; i++) {
            if (Helper.areCoordinatesInOwnGoalBox(bot.xCoordinates[i], bot.yCoordinates[i])) {
                movablesInGoalBox++;
            }
        }

        return movablesInGoalBox;
    }
}
