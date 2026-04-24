package net.kyori.option;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface OptionSchema$Mutable extends net.kyori.option.OptionSchema {
    net.kyori.option.Option stringOption(java.lang.String arg0, java.lang.String arg1);
    net.kyori.option.Option booleanOption(java.lang.String arg0, boolean arg1);
    net.kyori.option.Option intOption(java.lang.String arg0, int arg1);
    net.kyori.option.Option doubleOption(java.lang.String arg0, double arg1);
    net.kyori.option.Option enumOption(java.lang.String arg0, java.lang.Class arg1, java.lang.Enum arg2);
    net.kyori.option.OptionSchema frozenView();
}
