package com.github.martinambrus.rdforward.codegen;

/**
 * Classification of a method's body shape in an emitted stub file.
 * Drives what the generator writes as the method body.
 *
 * <p>Rules are documented in {@code PLAN-FULL-STUBS.md} §2 (Hybrid
 * Semantics Contract). Keep this enum aligned with the contract table
 * so the generator and the plan stay in sync.
 */
public enum StubSemantics {

    /**
     * {@code <init>}. Generator emits a constructor that stores args
     * into matching fields if present, otherwise an empty body.
     */
    CONSTRUCTOR,

    /**
     * {@code <clinit>} or any other synthetic static initializer the
     * generator does not emit. The enum value marks "ignore entirely".
     */
    STATIC_INITIALIZER,

    /**
     * Plugin-override entry points (e.g. {@code onEnable}, {@code onLoad},
     * {@code onDisable}, {@code onInitialize}). Generator emits an empty
     * body with no warn log. Subclasses override to run their code.
     */
    LIFECYCLE,

    /**
     * Zero-arg non-void instance method. Generator emits a return of
     * the default value for the return type (primitive zero, empty
     * sentinel, or null). No warn log.
     */
    GETTER_DEFAULT,

    /**
     * Void method, or non-void with args that does not return the
     * declaring class. Generator emits a one-line
     * {@code StubCallLog.logOnce(plugin, signature)} call plus a
     * default return for non-void variants.
     */
    SETTER_WARN_NOOP,

    /**
     * Non-void method whose return type equals the declaring class.
     * Treated as a fluent builder mutation. Generator emits
     * {@code StubCallLog.logOnce(...)} plus {@code return this}.
     */
    BUILDER_CHAIN,

    /**
     * Static non-void method. Generator emits a return of the default
     * value for the return type with no warn log (static calls have
     * no plugin-identity ThreadLocal to key the dedup map on).
     */
    STATIC_FACTORY
}
