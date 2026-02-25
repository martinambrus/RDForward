package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.2.3_04 (protocol v5).
 * Verified by CFR decompilation of the official Alpha 1.2.3_04 client JAR.
 *
 * Almost identical to AlphaV6Mappings — only the guiInventoryClassName
 * differs (nc in v5 vs ne in v6). All field/method names are the same.
 */
public class AlphaV5Mappings extends AlphaV6Mappings {

    // nc = survival inventory GUI (GuiInventory) in v5
    @Override
    public String guiInventoryClassName() {
        return "nc";
    }
}
