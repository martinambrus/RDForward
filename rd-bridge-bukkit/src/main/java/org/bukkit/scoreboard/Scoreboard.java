package org.bukkit.scoreboard;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Scoreboard {
    default org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Scoreboard.registerNewObjective(Ljava/lang/String;Ljava/lang/String;)Lorg/bukkit/scoreboard/Objective;");
        return null;
    }
    default org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, java.lang.String arg1, net.kyori.adventure.text.Component arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Scoreboard.registerNewObjective(Ljava/lang/String;Ljava/lang/String;Lnet/kyori/adventure/text/Component;)Lorg/bukkit/scoreboard/Objective;");
        return null;
    }
    org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, java.lang.String arg1, net.kyori.adventure.text.Component arg2, org.bukkit.scoreboard.RenderType arg3) throws java.lang.IllegalArgumentException;
    default org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, org.bukkit.scoreboard.Criteria arg1, net.kyori.adventure.text.Component arg2) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Scoreboard.registerNewObjective(Ljava/lang/String;Lorg/bukkit/scoreboard/Criteria;Lnet/kyori/adventure/text/Component;)Lorg/bukkit/scoreboard/Objective;");
        return null;
    }
    org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, org.bukkit.scoreboard.Criteria arg1, net.kyori.adventure.text.Component arg2, org.bukkit.scoreboard.RenderType arg3) throws java.lang.IllegalArgumentException;
    default org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Scoreboard.registerNewObjective(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/bukkit/scoreboard/Objective;");
        return null;
    }
    org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, org.bukkit.scoreboard.RenderType arg3);
    default org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, org.bukkit.scoreboard.Criteria arg1, java.lang.String arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Scoreboard.registerNewObjective(Ljava/lang/String;Lorg/bukkit/scoreboard/Criteria;Ljava/lang/String;)Lorg/bukkit/scoreboard/Objective;");
        return null;
    }
    default org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String arg0, org.bukkit.scoreboard.Criteria arg1, java.lang.String arg2, org.bukkit.scoreboard.RenderType arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Scoreboard.registerNewObjective(Ljava/lang/String;Lorg/bukkit/scoreboard/Criteria;Ljava/lang/String;Lorg/bukkit/scoreboard/RenderType;)Lorg/bukkit/scoreboard/Objective;");
        return null;
    }
    org.bukkit.scoreboard.Objective getObjective(java.lang.String arg0);
    java.util.Set getObjectivesByCriteria(java.lang.String arg0);
    java.util.Set getObjectivesByCriteria(org.bukkit.scoreboard.Criteria arg0);
    java.util.Set getObjectives();
    org.bukkit.scoreboard.Objective getObjective(org.bukkit.scoreboard.DisplaySlot arg0);
    java.util.Set getScores(org.bukkit.OfflinePlayer arg0);
    java.util.Set getScores(java.lang.String arg0);
    void resetScores(org.bukkit.OfflinePlayer arg0);
    void resetScores(java.lang.String arg0);
    org.bukkit.scoreboard.Team getPlayerTeam(org.bukkit.OfflinePlayer arg0);
    org.bukkit.scoreboard.Team getEntryTeam(java.lang.String arg0);
    org.bukkit.scoreboard.Team getTeam(java.lang.String arg0);
    java.util.Set getTeams();
    org.bukkit.scoreboard.Team registerNewTeam(java.lang.String arg0);
    java.util.Set getPlayers();
    java.util.Set getEntries();
    void clearSlot(org.bukkit.scoreboard.DisplaySlot arg0);
    java.util.Set getScoresFor(org.bukkit.entity.Entity arg0) throws java.lang.IllegalArgumentException;
    void resetScoresFor(org.bukkit.entity.Entity arg0) throws java.lang.IllegalArgumentException;
    org.bukkit.scoreboard.Team getEntityTeam(org.bukkit.entity.Entity arg0) throws java.lang.IllegalArgumentException;
}
