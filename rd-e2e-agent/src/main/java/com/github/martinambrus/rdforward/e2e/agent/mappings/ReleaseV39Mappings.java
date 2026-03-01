package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.3.1 (protocol v39).
 * Verified by CFR decompilation of the 1.3.1 client JAR.
 *
 * Major restructuring from 1.2.x: integrated server, field letters shifted significantly.
 * Entity: nk->jm, pos o/p/q->t/u/v, yaw u->z, pitch v->A, onGround z->E.
 * Minecraft: player h->g, world f->e, displayWidth d->c, displayHeight e->d,
 *   gameSettings A->y, currentScreen s->r, ingameGui w->v, tickMethod k->l,
 *   serverHost af->ae, serverPort ag->af, mouseGrabbed R->G, clickCooldown aa->W.
 * GuiNewChat introduced: chatLines on aoh.c instead of GuiIngame.e.
 * sendChatMessage a->d, movementInput a->b, inventoryField ap->by.
 */
public class ReleaseV39Mappings implements FieldMappings {

    @Override
    public String minecraftClassName() {
        return "net.minecraft.client.Minecraft";
    }

    @Override
    public String runMethodName() {
        return "run";
    }

    // l() = runTick
    @Override
    public String tickMethodName() {
        return "l";
    }

    // g = thePlayer (type jv)
    @Override
    public String playerFieldName() {
        return "g";
    }

    // e = theWorld (type atc)
    @Override
    public String worldFieldName() {
        return "e";
    }

    // ae = serverName (String)
    @Override
    public String serverHostFieldName() {
        return "ae";
    }

    // af = serverPort (int)
    @Override
    public String serverPortFieldName() {
        return "af";
    }

    // c = displayWidth (int)
    @Override
    public String displayWidthFieldName() {
        return "c";
    }

    // d = displayHeight (int)
    @Override
    public String displayHeightFieldName() {
        return "d";
    }

    // Entity (jm) position fields
    @Override
    public String posXFieldName() {
        return "t";
    }

    @Override
    public String posYFieldName() {
        return "u";
    }

    @Override
    public String posZFieldName() {
        return "v";
    }

    // y = GameSettings (type any)
    @Override
    public String gameSettingsFieldName() {
        return "y";
    }

    // b = movementInput (type aun) on aup (AbstractClientPlayer)
    @Override
    public String movementInputFieldName() {
        return "b";
    }

    @Override
    public String pressedKeysFieldName() {
        return null;
    }

    // z = yaw on jm (Entity)
    @Override
    public String yawFieldName() {
        return "z";
    }

    // A = pitch on jm (Entity)
    @Override
    public String pitchFieldName() {
        return "A";
    }

    // E = onGround on jm (Entity)
    @Override
    public String onGroundFieldName() {
        return "E";
    }

    // by = inventory (type oe = InventoryPlayer) on of (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "by";
    }

    // a = mainInventory (ri[36]) on oe (InventoryPlayer)
    @Override
    public String mainInventoryFieldName() {
        return "a";
    }

    // c = currentItem (int) on oe (InventoryPlayer)
    @Override
    public String currentItemFieldName() {
        return "c";
    }

    // c = itemID (int) on ri (ItemStack)
    @Override
    public String itemIdFieldName() {
        return "c";
    }

    // a = stackSize (int) on ri (ItemStack)
    @Override
    public String stackSizeFieldName() {
        return "a";
    }

    // a = getBlockId(int,int,int) on uo (World)
    @Override
    public String getBlockIdMethodName() {
        return "a";
    }

    // c = click handler on Minecraft
    @Override
    public String clickMethodName() {
        return "c";
    }

    // G = mouseGrabbed (boolean) on Minecraft
    @Override
    public String mouseGrabbedFieldName() {
        return "G";
    }

    // d = sendChatMessage(String) on atf (EntityClientPlayerMP)
    @Override
    public String sendChatMessageMethodName() {
        return "d";
    }

    // bB = dropOneItem on of (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "bB";
    }

    // bB = dropOneItem() on of/atf
    @Override
    public String dropOneItemMethodName() {
        return "bB";
    }

    // a = displayGuiScreen(apm) on Minecraft
    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    // r = currentScreen (type apm = GuiScreen)
    @Override
    public String currentScreenFieldName() {
        return "r";
    }

    // v = ingameGUI (type aov = GuiIngame)
    @Override
    public String ingameGuiFieldName() {
        return "v";
    }

    // c = chatLines (List) on aoh (GuiNewChat)
    @Override
    public String chatLinesFieldName() {
        return "c";
    }

    // g = cursorItem (ri, private) on oe (InventoryPlayer)
    @Override
    public String cursorItemFieldName() {
        return "g";
    }

    @Override
    public String craftingInventoryFieldName() {
        return null;
    }

    // apm = GuiScreen
    @Override
    public String guiScreenClassName() {
        return "apm";
    }

    // j = session (type aof) on Minecraft
    @Override
    public String sessionFieldName() {
        return "j";
    }

    // W = clickCooldown (int) on Minecraft
    @Override
    public String clickCooldownFieldName() {
        return "W";
    }

    // b = playerController (type atb) on Minecraft
    @Override
    public String playerControllerFieldName() {
        return "b";
    }

    // b = clickBlock on atb (PlayerController)
    @Override
    public String digMethodName() {
        return "b";
    }

    // b = text (String) on and (ChatLine)
    @Override
    public String chatLineTextFieldName() {
        return "b";
    }

    // f = width (int) on apm (GuiScreen)
    @Override
    public String guiScreenWidthFieldName() {
        return "f";
    }

    // g = height (int) on apm (GuiScreen)
    @Override
    public String guiScreenHeightFieldName() {
        return "g";
    }

    // e = pressed (boolean) on ane (KeyBinding)
    @Override
    public String keyBindingPressedFieldName() {
        return "e";
    }

    // GameSettings (any) key binding fields
    @Override
    public String forwardKeyFieldName() {
        return "w";
    }

    @Override
    public String leftKeyFieldName() {
        return "x";
    }

    @Override
    public String backKeyFieldName() {
        return "y";
    }

    @Override
    public String rightKeyFieldName() {
        return "z";
    }

    @Override
    public String jumpKeyFieldName() {
        return "A";
    }

    @Override
    public String sneakKeyFieldName() {
        return "E";
    }

    // aqm = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "aqm";
    }

    // aqt = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "aqt";
    }

    // j = closeContainer() on atf (EntityClientPlayerMP)
    @Override
    public String closeContainerMethodName() {
        return "j";
    }
}
