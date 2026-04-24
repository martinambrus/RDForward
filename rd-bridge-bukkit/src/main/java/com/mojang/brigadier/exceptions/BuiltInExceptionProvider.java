package com.mojang.brigadier.exceptions;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BuiltInExceptionProvider {
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType doubleTooLow();
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType doubleTooHigh();
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType floatTooLow();
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType floatTooHigh();
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType integerTooLow();
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType integerTooHigh();
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType longTooLow();
    com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType longTooHigh();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType literalIncorrect();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType readerExpectedStartOfQuote();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType readerExpectedEndOfQuote();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType readerInvalidEscape();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType readerInvalidBool();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType readerInvalidInt();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType readerExpectedInt();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType readerInvalidLong();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType readerExpectedLong();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType readerInvalidDouble();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType readerExpectedDouble();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType readerInvalidFloat();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType readerExpectedFloat();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType readerExpectedBool();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType readerExpectedSymbol();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType dispatcherUnknownCommand();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType dispatcherUnknownArgument();
    com.mojang.brigadier.exceptions.SimpleCommandExceptionType dispatcherExpectedArgumentSeparator();
    com.mojang.brigadier.exceptions.DynamicCommandExceptionType dispatcherParseException();
}
