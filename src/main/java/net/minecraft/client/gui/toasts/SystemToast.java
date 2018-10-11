package net.minecraft.client.gui.toasts;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class SystemToast implements IToast
{
    private final Type type;
    private String title;
    private String subtitle;
    private long firstDrawTime;
    private boolean newDisplay;

    public SystemToast(Type typeIn, ITextComponent titleComponent, @Nullable ITextComponent subtitleComponent)
    {
        this.type = typeIn;
        this.title = titleComponent.getUnformattedText();
        this.subtitle = subtitleComponent == null ? null : subtitleComponent.getUnformattedText();
    }

    public Visibility draw(GuiToast toastGui, long delta)
    {
        if (this.newDisplay)
        {
            this.firstDrawTime = delta;
            this.newDisplay = false;
        }

        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 64, 160, 32);

        if (this.subtitle == null)
        {
            toastGui.getMinecraft().fontRenderer.drawString(this.title, 18, 12, -256);
        }
        else
        {
            toastGui.getMinecraft().fontRenderer.drawString(this.title, 18, 7, -256);
            toastGui.getMinecraft().fontRenderer.drawString(this.subtitle, 18, 18, -1);
        }

        return delta - this.firstDrawTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }

    public void setDisplayedText(ITextComponent titleComponent, @Nullable ITextComponent subtitleComponent)
    {
        this.title = titleComponent.getUnformattedText();
        this.subtitle = subtitleComponent == null ? null : subtitleComponent.getUnformattedText();
        this.newDisplay = true;
    }

    public Type getType()
    {
        return this.type;
    }

    public static void addOrUpdate(GuiToast p_193657_0_, Type p_193657_1_, ITextComponent p_193657_2_, @Nullable ITextComponent p_193657_3_)
    {
        SystemToast systemtoast = (SystemToast)p_193657_0_.getToast(SystemToast.class, p_193657_1_);

        if (systemtoast == null)
        {
            p_193657_0_.add(new SystemToast(p_193657_1_, p_193657_2_, p_193657_3_));
        }
        else
        {
            systemtoast.setDisplayedText(p_193657_2_, p_193657_3_);
        }
    }

    @SideOnly(Side.CLIENT)
    public static enum Type
    {
        TUTORIAL_HINT,
        NARRATOR_TOGGLE;
    }
}